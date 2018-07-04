package com.hannesdorfmann.fragmentargs.processor;

import com.hannesdorfmann.fragmentargs.annotation.Arg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Simple data holder class for fragment args of a certain fragment
 *
 * @author Hannes Dorfmann
 */
public class AnnotatedFragment {

  private Set<ArgumentAnnotatedField> requiredFields = new TreeSet<ArgumentAnnotatedField>();
  private Set<ArgumentAnnotatedField> optional = new TreeSet<ArgumentAnnotatedField>();
  private Map<String, ArgumentAnnotatedField> bundleKeyMap =
      new HashMap<String, ArgumentAnnotatedField>();
  private TypeElement classElement;

  // qualified Bundler class is KEY, Varibale / Field name = VALUE
  private Map<String, String> bundlerVariableMap = new HashMap<String, String>();
  private int bundlerCounter = 0;

  // Setter methods will be used
  private Map<String, ExecutableElement> setterMethods = new HashMap<String, ExecutableElement>();

  public AnnotatedFragment(TypeElement classElement) {
    this.classElement = classElement;
  }

  public TypeElement getClassElement() {
    return classElement;
  }

  /**
   * Checks if a field (with the given name) is already in this class
   */
  public boolean containsField(ArgumentAnnotatedField field) {
    return requiredFields.contains(field) || optional.contains(field);
  }

  /**
   * Checks if a key for a bundle has already been used
   */
  public ArgumentAnnotatedField containsBundleKey(ArgumentAnnotatedField field) {
    return bundleKeyMap.get(field.getKey());
  }

  private void checkAndSetCustomBundler(ArgumentAnnotatedField field) {

    if (field.hasCustomBundler()) {
      String bundlerClass = field.getBundlerClass();
      String varName = bundlerVariableMap.get(bundlerClass);
      if (varName == null) {
        varName = "bundler" + (++bundlerCounter);
        bundlerVariableMap.put(bundlerClass, varName);
      }

      field.setBundlerFieldName(varName);
    }
  }

  /**
   * Adds an field as required
   */
  public void addRequired(ArgumentAnnotatedField field) {
    bundleKeyMap.put(field.getKey(), field);
    requiredFields.add(field);
    checkAndSetCustomBundler(field);
  }

  /**
   * Adds an field as optional
   */
  public void addOptional(ArgumentAnnotatedField field) {
    bundleKeyMap.put(field.getKey(), field);
    optional.add(field);

    checkAndSetCustomBundler(field);
  }

  public Set<ArgumentAnnotatedField> getRequiredFields() {
    return requiredFields;
  }

  public Set<ArgumentAnnotatedField> getOptionalFields() {
    return optional;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AnnotatedFragment)) return false;

    AnnotatedFragment fragment = (AnnotatedFragment) o;

    if (!getQualifiedName().equals(fragment.getQualifiedName())) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return getQualifiedName().hashCode();
  }

  public String getQualifiedName() {
    return classElement.getQualifiedName().toString();
  }

  public String getSimpleName() {
    return classElement.getSimpleName().toString();
  }

  /*
   * Returns the Builder name
   * e.g. LoginFragmentBuilder or LoginActivity$$LoginFragment
   */
  public String getBuilderName() {
    String builderName = getSimpleName() + "Builder";

    if(isInnerClass()) {
      return classElement.getEnclosingElement().getSimpleName() + "$$" + builderName;

    } else {
      return builderName;
    }
  }

  /**
   * Returns the qualified Builder name
   * e.g. com.hannesdorfman.package.LoginFragmentBuilder or com.hannesdorfman.package.LoginActivity$$LoginFragment
   */
  public String getQualifiedBuilderName() {
    String qualifiedBuilderName = getQualifiedName() + "Builder";

    if(isInnerClass()) {
      return qualifiedBuilderName
              .replace("." + getSimpleName() + "Builder", "$$" + getSimpleName() + "Builder");

    } else {
      return qualifiedBuilderName;
    }
  }

  public boolean isInnerClass() {
    return classElement.getEnclosingElement().getKind() == ElementKind.CLASS;
  }

  /**
   * Builds a type parameter string
   * e.g. {@literal <V extends Serializable & Parcelable, H extends Object>}
   */
  public String getTypeParametersString() {
    StringBuilder parametersBuilder = new StringBuilder();
    Iterator<? extends TypeParameterElement> typeParametersIterator = classElement.getTypeParameters().iterator();

    if(typeParametersIterator.hasNext()) {
      parametersBuilder.append("<");

      while(typeParametersIterator.hasNext()) {
        TypeParameterElement parameterElement = typeParametersIterator.next();

        // e.g. "V"
        parametersBuilder.append(parameterElement.getSimpleName());

        Iterator<? extends TypeMirror> boundsIterator = parameterElement.getBounds().iterator();

        if(boundsIterator.hasNext()) {
          parametersBuilder.append(" extends ");

          while (boundsIterator.hasNext()) {
            TypeMirror typeMirror = boundsIterator.next();
            parametersBuilder.append(typeMirror.toString());

            if(boundsIterator.hasNext()){
              parametersBuilder.append(" & ");
            }
          }
        }

        if(typeParametersIterator.hasNext()) {
          parametersBuilder.append(", ");
        }
      }

      parametersBuilder.append(">");
    }

    return parametersBuilder.toString();
  }

  public Set<ArgumentAnnotatedField> getAll() {
    Set<ArgumentAnnotatedField> all = new HashSet<ArgumentAnnotatedField>(getRequiredFields());
    all.addAll(getOptionalFields());
    return all;
  }

  public Map<String, String> getBundlerVariableMap() {
    return bundlerVariableMap;
  }

  /**
   * Checks if the given element is a valid setter method and add it to the internal setter
   *
   * @param classMember Could be everything except an field
   */
  public void checkAndAddSetterMethod(Element classMember) {

    if (classMember.getKind() == ElementKind.METHOD) {
      ExecutableElement methodElement = (ExecutableElement) classMember;
      String methodName = methodElement.getSimpleName().toString();
      if (methodName.startsWith("set")) {
        ExecutableElement existingSetter = setterMethods.get(methodName);
        if (existingSetter != null) {
          // Check for better visibility
          if (ModifierUtils.compareModifierVisibility(methodElement, existingSetter) == -1) {
            // this method has better visibility so use this one
            setterMethods.put(methodName, methodElement);
          }
        } else {
          setterMethods.put(methodName, methodElement);
        }
      }
    }

  }

  /**
   * Searches for a setter and returns the setter method
   *
   * @param field the {@link ArgumentAnnotatedField}
   * @return the setter method
   * @throws ProcessingException If no setter method has been found
   */
  public ExecutableElement findSetterForField(ArgumentAnnotatedField field) throws ProcessingException {

    String fieldName = field.getVariableName();
    StringBuilder builder = new StringBuilder("set");
    if (fieldName.length() == 1) {
      builder.append(fieldName.toUpperCase());
    } else {
      builder.append(Character.toUpperCase(fieldName.charAt(0)));
      builder.append(fieldName.substring(1));
    }

    String methodName = builder.toString();
    ExecutableElement setterMethod = setterMethods.get(methodName);
    if (setterMethod != null && isSetterApplicable(field, setterMethod)) {
      return setterMethod; // setter method found
    }

    // Search for setter method with hungarian notion check
    if (field.getName().length() > 1 && field.getName().matches("m[A-Z].*")) {
      // m not in lower case
      String hungarianMethodName = "set" + field.getName();
      setterMethod = setterMethods.get(hungarianMethodName);
      if (setterMethod != null && isSetterApplicable(field, setterMethod)) {
        return setterMethod; // setter method found
      }

      // M in upper case
      hungarianMethodName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
      setterMethod = setterMethods.get(hungarianMethodName);
      if (setterMethod != null && isSetterApplicable(field, setterMethod)) {
        return setterMethod; // setter method found
      }
    }

    // Kotlin special boolean character treatment
    // Fields prefixed with "is" are not accessible through "setIsFoo" but with "setFoo"
    if (field.getName().length() > 1 && field.getName().matches("is[A-Z].*")) {
      String setterName = "set" + field.getName().substring(2);
      setterMethod = setterMethods.get(setterName);
      if (setterMethod != null && isSetterApplicable(field, setterMethod)) {
        return setterMethod; // setter method found
      }
    }

    throw new ProcessingException(field.getElement(), "The @%s annotated field '%s' in class %s has " +
        "private visibility. Hence a corresponding non-private setter method must be provided " +
        "called '%s(%s)'. Unfortunately this is not the case. Please add a setter method for " +
        "this field!", Arg.class.getSimpleName(), field.getName(), getSimpleName(), methodName,
        field.getType());

  }

  private boolean isSetterApplicable(ArgumentAnnotatedField field, ExecutableElement setterMethod) {

    List<? extends VariableElement> parameters = setterMethod.getParameters();
    if (parameters == null || parameters.size() != 1) {
      return false;
    }

    VariableElement parameter = parameters.get(0);
    return parameter.asType().equals(field.getElement().asType());

  }

}

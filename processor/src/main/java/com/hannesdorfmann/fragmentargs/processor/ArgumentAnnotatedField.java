package com.hannesdorfmann.fragmentargs.processor;

import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.bundler.ArgsBundler;
import com.hannesdorfmann.fragmentargs.bundler.NoneArgsBundler;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class ArgumentAnnotatedField implements Comparable<ArgumentAnnotatedField> {

  private final String name;
  private final String key;
  private final String type;
  private final Element element;
  private final boolean required;
  private final TypeElement classElement;

  private String bundlerClass;
  private String bundlerFieldName;

  public ArgumentAnnotatedField(Element element, TypeElement classElement, Arg annotation)
      throws ProcessingException {

    this.name = element.getSimpleName().toString();
    this.key = getKey(element, annotation);
    this.type = element.asType().toString();
    this.element = element;
    this.required = annotation.required();
    this.classElement = classElement;

    try {
      Class<? extends ArgsBundler> clazz = annotation.bundler();
      bundlerClass = getFullQualifiedNameByClass(clazz);
    } catch (MirroredTypeException mte) {
      TypeMirror baggerClass = mte.getTypeMirror();
      bundlerClass = getFullQualifiedNameByTypeMirror(baggerClass);
    }
  }

  public String getBundlerFieldName() {
    return bundlerFieldName;
  }

  public void setBundlerFieldName(String bundlerFieldName) {
    this.bundlerFieldName = bundlerFieldName;
  }

  private static String getKey(Element element, Arg annotation) {
    String field = element.getSimpleName().toString();
    if (!"".equals(annotation.key())) {
      return annotation.key();
    }
    return getVariableName(field);
  }

  private static boolean isRequired(Element element) {
    Arg annotation = element.getAnnotation(Arg.class);
    return annotation.required();
  }

  private String getFullQualifiedNameByTypeMirror(TypeMirror baggerClass)
      throws ProcessingException {
    if (baggerClass == null) {
      throw new ProcessingException(element, "Could not get the ArgsBundler class");
    }

    // If its the NoneArgsBundler, no future checks are required
    if (baggerClass.toString().equals(NoneArgsBundler.class.getCanonicalName())) {
      return NoneArgsBundler.class.getCanonicalName();
    }

    if (baggerClass.getKind() != TypeKind.DECLARED) {
      throw new ProcessingException(element, "@ %s  is not a class in %s ",
          ArgsBundler.class.getSimpleName(), element.getSimpleName());
    }

    if (!isPublicClass((DeclaredType) baggerClass)) {
      throw new ProcessingException(element,
          "The %s must be a public class to be a valid ArgsBundler", baggerClass.toString());
    }

    // Check if the bagger class has a default constructor
    if (!hasPublicEmptyConstructor((DeclaredType) baggerClass)) {
      throw new ProcessingException(element,
          "The %s must provide a public empty default constructor to be a valid ArgsBundler",
          baggerClass.toString());
    }

    return baggerClass.toString();
  }

  private String getFullQualifiedNameByClass(Class<? extends ArgsBundler> clazz)
      throws ProcessingException {

    // It's the none Args bundler, hence no future checks are needed
    if (clazz.equals(NoneArgsBundler.class)) {
      return NoneArgsBundler.class.getCanonicalName();
    }

    // Check public
    if (!Modifier.isPublic(clazz.getModifiers())) {

      throw new ProcessingException(element,
          "The %s must be a public class to be a valid ArgsBundler", clazz.getCanonicalName());
    }

    // Check constructors
    Constructor<?>[] constructors = clazz.getConstructors();

    boolean foundDefaultConstructor = false;
    for (Constructor c : constructors) {
      boolean isPublicConstructor = Modifier.isPublic(c.getModifiers());
      Class<?>[] pType = c.getParameterTypes();

      if (pType.length == 0 && isPublicConstructor) {
        foundDefaultConstructor = true;
        break;
      }
    }

    if (!foundDefaultConstructor) {
      throw new ProcessingException(element,
          "The %s must provide a public empty default constructor", clazz.getCanonicalName());
    }

    return clazz.getCanonicalName();
  }

  /**
   * Checks if a class is public
   */
  private boolean isPublicClass(DeclaredType type) {
    Element element = type.asElement();

    return element.getModifiers().contains(javax.lang.model.element.Modifier.PUBLIC);
  }

  /**
   * Checks if an public empty constructor is available
   */
  private boolean hasPublicEmptyConstructor(DeclaredType type) {
    Element element = type.asElement();

    List<? extends Element> containing = element.getEnclosedElements();

    for (Element e : containing) {
      if (e.getKind() == ElementKind.CONSTRUCTOR) {
        ExecutableElement c = (ExecutableElement) e;

        if ((c.getParameters() == null || c.getParameters().isEmpty()) && c.getModifiers()
            .contains(javax.lang.model.element.Modifier.PUBLIC)) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Get the full qualified name of the ArgsBundler class.
   *
   * @return null if no custom bundler has been set. Otherwise the fully qualified name of the
   * custom bundler class.
   */
  public String getBundlerClass() {
    return bundlerClass.equals(NoneArgsBundler.class.getCanonicalName()) ? null : bundlerClass;
  }

  public boolean hasCustomBundler() {
    return getBundlerClass() != null;
  }

  public TypeElement getClassElement() {
    return classElement;
  }

  public String getVariableName() {
    return getVariableName(name);
  }

  public static String getVariableName(String name) {
    if (name.matches("^m[A-Z]{1}") || name.matches("^_[A-Za-z]{1}")) {
      return name.substring(1, 2).toLowerCase();
    } else if (name.matches("m[A-Z]{1}.*") || name.matches("_[A-Za-z]{1}.*")) {
      return name.substring(1, 2).toLowerCase() + name.substring(2);
    }
    return name;
  }

  public String getKey() {
    return this.key;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Element getElement() {
    return element;
  }

  public boolean isRequired() {
    return required;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArgumentAnnotatedField)) return false;

    ArgumentAnnotatedField that = (ArgumentAnnotatedField) o;

    if (!name.equals(that.name)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return key + "/" + type;
  }

  public String getRawType() {
    if (isArray()) {
      return type.substring(0, type.length() - 2);
    }
    return type;
  }

  public boolean isArray() {
    return type.endsWith("[]");
  }

  @Override
  public int compareTo(ArgumentAnnotatedField o) {
    return getVariableName().compareTo(o.getVariableName());
  }

  public boolean isPrimitive() {
    return element.asType().getKind().isPrimitive();
  }
}

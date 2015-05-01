package com.hannesdorfmann.fragmentargs.processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.TypeElement;

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

  public AnnotatedFragment(TypeElement classElement) {
    this.classElement = classElement;
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

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AnnotatedFragment)) return false;

    AnnotatedFragment fragment = (AnnotatedFragment) o;

    if (!getQualifiedName().equals(fragment.getQualifiedName())) return false;

    return true;
  }

  @Override public int hashCode() {
    return getQualifiedName().hashCode();
  }

  public String getQualifiedName() {
    return classElement.getQualifiedName().toString();
  }

  public String getSimpleName() {
    return classElement.getSimpleName().toString();
  }

  public Set<ArgumentAnnotatedField> getAll() {
    Set<ArgumentAnnotatedField> all = new HashSet<ArgumentAnnotatedField>(getRequiredFields());
    all.addAll(getOptionalFields());
    return all;
  }

  public Map<String, String> getBundlerVariableMap() {
    return bundlerVariableMap;
  }
}

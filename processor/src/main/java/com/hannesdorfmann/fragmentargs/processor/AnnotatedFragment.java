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

  private Set<AnnotatedField> requiredFields = new TreeSet<AnnotatedField>();
  private Set<AnnotatedField> optional = new TreeSet<AnnotatedField>();
  private Map<String, AnnotatedField> bundleKeyMap = new HashMap<String, AnnotatedField>();
  private TypeElement classElement;

  public AnnotatedFragment(TypeElement classElement) {
    this.classElement = classElement;
  }

  /**
   * Checks if a field (with the given name) is already in this class
   */
  public boolean containsField(AnnotatedField field) {
    return requiredFields.contains(field) || optional.contains(field);
  }

  /**
   * Checks if a key for a bundle has already been used
   */
  public AnnotatedField containsBundleKey(AnnotatedField field) {
    return bundleKeyMap.get(field.getKey());
  }

  /**
   * Adds an field as required
   */
  public void addRequired(AnnotatedField field) {
    bundleKeyMap.put(field.getKey(), field);
    requiredFields.add(field);
  }

  /**
   * Adds an field as optional
   */
  public void addOptional(AnnotatedField field) {
    bundleKeyMap.put(field.getKey(), field);
    optional.add(field);
  }

  public Set<AnnotatedField> getRequiredFields() {
    return requiredFields;
  }

  public Set<AnnotatedField> getOptionalFields() {
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

  public Set<AnnotatedField> getAll() {
    Set<AnnotatedField> all = new HashSet<AnnotatedField>(getRequiredFields());
    all.addAll(getOptionalFields());
    return all;
  }
}

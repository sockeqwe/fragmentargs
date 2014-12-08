package com.hannesdorfmann.fragmentargs.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

abstract class AnnotatedField implements Comparable<AnnotatedField> {
  private final String name;
  private final String key;
  private final String type;
  private final Element element;
  private final boolean required;
  private final TypeElement classElement;

  public AnnotatedField(Element element, TypeElement classElement, boolean required, String key) {
    this.name = element.getSimpleName().toString();
    this.key = key;
    this.type = element.asType().toString();
    this.element = element;
    this.required = required;
    this.classElement = classElement;
  }

  public TypeElement getClassElement() {
    return classElement;
  }

  public String getVariableName() {
    return getVariableName(name);
  }

  public static String getVariableName(String name) {
    if (name.matches("^m[A-Z]{1}")) {
      return name.substring(1, 2).toLowerCase();
    } else if (name.matches("m[A-Z]{1}.*")) {
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
    if (!(o instanceof AnnotatedField)) return false;

    AnnotatedField that = (AnnotatedField) o;

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
  public int compareTo(AnnotatedField o) {
    return getVariableName().compareTo(o.getVariableName());
  }
}
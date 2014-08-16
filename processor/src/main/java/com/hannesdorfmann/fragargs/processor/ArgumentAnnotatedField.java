package com.hannesdorfmann.fragargs.processor;

import com.hannesdorfmann.fragargs.annotation.Arg;
import javax.lang.model.element.Element;

public class ArgumentAnnotatedField extends AnnotatedField {

  public ArgumentAnnotatedField(Element element) {
    super(element, isRequired(element), getKey(element));
  }

  private static String getKey(Element element) {
    Arg annotation = element.getAnnotation(Arg.class);
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
}

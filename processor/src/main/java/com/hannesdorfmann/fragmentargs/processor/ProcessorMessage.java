package com.hannesdorfmann.fragmentargs.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * This is a simple static helper class to print error, waring or note messages during annotation
 * processing.
 * <p>
 * You <b>must</b> initialize this class by calling {@link #init(ProcessingEnvironment)}
 * before you can use the messaging methods
 * </p>
 *
 * @author Hannes Dorfmann
 */
public class ProcessorMessage {

  private static ProcessingEnvironment processingEnvironment;

  private ProcessorMessage(ProcessingEnvironment processingEnvironment) {
    this.processingEnvironment = processingEnvironment;
  }

  public static void init(ProcessingEnvironment processingEnv) {
    processingEnvironment = processingEnv;
  }

  public static void error(Element element, String message, Object... args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
  }

  public static void warn(Element element, String message, Object... args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING, message, element);
  }

  public static void note(Element element, String message, Object... args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, message, element);
  }
}

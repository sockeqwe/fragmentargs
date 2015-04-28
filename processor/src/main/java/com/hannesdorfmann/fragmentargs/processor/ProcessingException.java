package com.hannesdorfmann.fragmentargs.processor;

import javax.lang.model.element.Element;

/**
 * A simple exception that will be thrown if something went wrong (error message will be printed
 * before throwing exception)
 *
 * @author Hannes Dorfmann
 */
public class ProcessingException extends Exception {

  private Element element;
  private String message;
  private Object[] messageArgs;

  public ProcessingException(Element element, String message, Object... messageArgs) {
    this.element = element;
    this.message = message;
    this.messageArgs = messageArgs;
  }

  public Element getElement() {
    return element;
  }

  @Override public String getMessage() {
    return message;
  }

  public Object[] getMessageArgs() {
    return messageArgs;
  }
}

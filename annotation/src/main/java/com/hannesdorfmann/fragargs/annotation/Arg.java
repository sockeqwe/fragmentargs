package com.hannesdorfmann.fragargs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated every class that can be parsed from json or can be written as json
 * with this annotation. You can also specify config for this
 *
 * @author Hannes Dorfmann
 */
@Target(ElementType.FIELD) @Retention(RetentionPolicy.CLASS) @Documented
public @interface Arg {

  /**
   * Specifies if the argument is required (default) or not
   * @return true if required, false otherwise
   */
  boolean required() default true;
  /**
   * Key in the arguments bundle, by default uses the field name, minus the "m" prefix.
   */
  String key() default "";

}

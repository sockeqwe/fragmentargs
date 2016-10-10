package com.hannesdorfmann.fragmentargs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use this annotation to mark a Fragments that contains {@link Arg} annotation. This annotation is
 * required to run Annotation processing.
 *
 * @author Hannes Dorfmann
 * @since 3.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface FragmentWithArgs {

  /**
   * Is inheritance hierarchy scanning enabled? default value = true. Specifies if all @{@link Arg}
   * annotations of all super classes (checks the complete inheritance hierarchy) should be included
   * in the fragment. The default value is true and you don't have to specify that for each
   * fragment
   */
  boolean inherited() default true;
}

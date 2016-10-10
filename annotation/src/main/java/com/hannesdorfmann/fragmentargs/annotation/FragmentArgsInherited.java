package com.hannesdorfmann.fragmentargs.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies if all @{@link Arg} annotations of all super classes (checks the complete inheritance
 * hierarchy) should be included in the fragment. The default value is true and you don't
 * have to specify that for each fragment
 *
 * @author Hannes Dorfmann
 * @deprecated Use {@link FragmentWithArgs} instead. This annotation can be removed in any further major
 * release.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Documented
@Deprecated
public @interface FragmentArgsInherited {

  /**
   * Is inheritance hierarchy scanning enabled? default value = true
   */
  boolean value() default true;
}

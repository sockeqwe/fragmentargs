package com.hannesdorfmann.fragmentargs.annotation;

import com.hannesdorfmann.fragmentargs.bundler.ArgsBundler;
import com.hannesdorfmann.fragmentargs.bundler.NoneArgsBundler;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotated every field that should be a fragment argument with this annotation
 *
 * @author Hannes Dorfmann
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
@Documented
public @interface Arg {

  /**
   * Specifies if the argument is required (default) or not
   *
   * @return true if required, false otherwise
   */
  boolean required() default true;

  /**
   * Key in the arguments bundle, by default uses the field name, minus the "m" prefix.
   */
  String key() default "";

  /**
   * You can specify the {@link ArgsBundler} class that should be used to put the annotated Arg into
   * the bundle and read from it.
   *
   * @return The Args Bundler class
   * @since 2.1
   */
  Class<? extends ArgsBundler> bundler() default NoneArgsBundler.class;
}

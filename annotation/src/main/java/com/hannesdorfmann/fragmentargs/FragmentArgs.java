package com.hannesdorfmann.fragmentargs;

/**
 * The root class to inject arguments to a fragment
 *
 * @author Hannes Dorfmann
 */
public class FragmentArgs {

  public static final String AUTO_MAPPING_CLASS_NAME = "AutoFragmentArgInjector";
  public static final String AUTO_MAPPING_PACKAGE = "com.hannesdorfmann.fragmentargs";
  public static final String AUTO_MAPPING_QUALIFIED_CLASS =
      AUTO_MAPPING_PACKAGE + "." + AUTO_MAPPING_CLASS_NAME;

  private static FragmentArgsInjector autoMappingInjector;

  public static void inject(Object fragment) {
    injectFromBundle(fragment);
  }

  static void injectFromBundle(Object target) {

    if (autoMappingInjector == null) {
      // Load the automapping class
      try {
        Class<?> c = Class.forName(AUTO_MAPPING_QUALIFIED_CLASS);
        autoMappingInjector = (FragmentArgsInjector) c.newInstance();
      } catch (Exception e) {
        // Since 2.0.0 we don't throw an exception because of android library support.
        // Instead we print this exception as warning message

        /*
        Exception wrapped = new Exception("Could not load the generated automapping class. "
            + "However, that may be ok, if you use FragmentArgs in library projects", e);
        wrapped.printStackTrace();
        */
      }
    }

    if (autoMappingInjector != null) {
      autoMappingInjector.inject(target);
    }
  }
}

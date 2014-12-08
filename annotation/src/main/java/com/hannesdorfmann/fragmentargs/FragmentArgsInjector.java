package com.hannesdorfmann.fragmentargs;

/**
 * Simple interface for the injector. This a class implementing this interface will be generated in
 * the processor
 *
 * @author Hannes Dorfmann
 */
public interface FragmentArgsInjector {

  public void inject(Object target);
}

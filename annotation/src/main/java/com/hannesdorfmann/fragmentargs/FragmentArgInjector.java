package com.hannesdorfmann.fragmentargs;

import android.os.Bundle;

/**
 * @author Hannes Dorfmann
 */
public interface FragmentArgInjector {

  public void inject(String targetClass, Bundle bundle);
}

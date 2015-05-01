package com.hannesdorfmann.fragmentargs.bundler;

import android.os.Bundle;
import com.hannesdorfmann.fragmentargs.annotation.Arg;


/**
 * This class is just representing that <b>no {@link ArgsBundler} should be used.</b>
 * The {@link Arg#bundler()} annotation uses this class to specify that no ArgsBundler should be
 * used and only the build in types should be used.
 *
 * @author Hannes Dorfmann
 * @since 2.1
 */
public final class NoneArgsBundler implements ArgsBundler<Object> {

  private NoneArgsBundler() {
  }

  @Override public void put(String key, Object value, Bundle bundle) {
  }

  @Override public Object get(String key, Bundle bundle) {
    return null;
  }
}

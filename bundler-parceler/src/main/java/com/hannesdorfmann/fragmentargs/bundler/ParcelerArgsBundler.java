package com.hannesdorfmann.fragmentargs.bundler;

import android.os.Bundle;
import org.parceler.Parcels;

/**
 * A {@link ArgsBundler} implementation for Parceler
 *
 * @author Hannes Dorfmann
 * @since 2.1
 */
public class ParcelerArgsBundler implements ArgsBundler<Object> {

  @Override public void put(String key, Object value, Bundle bundle) {
    bundle.putParcelable(key, Parcels.wrap(value));
  }

  @Override public <V> V get(String key, Bundle bundle) {
    return Parcels.unwrap(bundle.getParcelable(key));
  }
}

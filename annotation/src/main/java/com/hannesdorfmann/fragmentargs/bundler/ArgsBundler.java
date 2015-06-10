package com.hannesdorfmann.fragmentargs.bundler;

import android.os.Bundle;

/**
 * With this class you can provide your own serialization and deserialization implementation to put
 * something into the bundle that is used by
 *
 * @author Hannes Dorfmann
 * @since 2.1
 */
public interface ArgsBundler<T> {

  /**
   * Put (save) a value into the bundle.
   *
   * @param key The key you have to use as the key for the bundle to save the value
   * @param value The value you have to save into the bundle (for the given key)
   * @param bundle The Bundle to save key / value. It's not null.
   */
  public void put(String key, T value, Bundle bundle);

  /**
   * Get a value from the bundle
   *
   * @param key The key for the value
   * @param bundle The Bundle where the value is saved in
   * @return The value retrieved from the Bundle with the given key
   */
  public <V extends T> V get(String key, Bundle bundle);




}

package com.hannesdorfmann.fragmentargs.bundler;

import android.os.Bundle;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

/**
 * This {@link ArgsBundler} takes a java.util.List and casts it to an ArrayList. So it assumes that
 * the List is instance of ArrayList.
 * <p>
 * With this ArgsBundler you can annotate fields of type java.util.List like that
 * {@code @Arg(bundler = CastedArrayListArgsBundler.class) List<Foo> foos>}
 * </p>
 *
 * @author Hannes Dorfmann
 * @since 2.1
 */
public class CastedArrayListArgsBundler implements ArgsBundler<List<? extends Parcelable>> {

  @Override public void put(String key, List<? extends Parcelable> value, Bundle bundle) {
    if (!(value instanceof ArrayList)) {
      throw new ClassCastException(
          "CastedArrayListArgsBundler assumes that the List is instance of ArrayList, but it's instance of "
              + value.getClass().getCanonicalName());
    }

    bundle.putParcelableArrayList(key, (ArrayList<? extends Parcelable>) value);
  }

  @Override public <T extends List<? extends Parcelable>> T get(String key, Bundle bundle) {
    return (T) bundle.getParcelableArrayList(key);
  }
}

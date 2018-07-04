package com.hannesdorfmann.fragmentargstest.test;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

@com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
public class ClassWithGenerics<PS extends Parcelable & Serializable, P extends Parcelable, S extends Serializable> extends android.app.Fragment {

    @com.hannesdorfmann.fragmentargs.annotation.Arg
    ArrayList<PS> genericList;

    @com.hannesdorfmann.fragmentargs.annotation.Arg
    ArrayList<P> parcelableList;

    @com.hannesdorfmann.fragmentargs.annotation.Arg
    ArrayList<S> serializableList;

    @com.hannesdorfmann.fragmentargs.annotation.Arg
    PS generic;

    @com.hannesdorfmann.fragmentargs.annotation.Arg
    P parcelable;

    @com.hannesdorfmann.fragmentargs.annotation.Arg(required = false)
    P optionalParcelable;

    @com.hannesdorfmann.fragmentargs.annotation.Arg
    S serializable;

    @com.hannesdorfmann.fragmentargs.annotation.Arg(required = false)
    S optionalSerializable;

    @com.hannesdorfmann.fragmentargs.annotation.Arg
    private S privateSerializableGeneric;

    @com.hannesdorfmann.fragmentargs.annotation.Arg
    private P privateParcelableGeneric;

    public void setPrivateSerializableGeneric(S privateSerializableGeneric) {
        this.privateSerializableGeneric = privateSerializableGeneric;
    }

    public void setPrivateParcelableGeneric(P privateParcelableGeneric) {
        this.privateParcelableGeneric = privateParcelableGeneric;
    }

}
package com.hannesdorfmann.fragmentargstest.test;

import android.os.Bundle;

public final class ClassWithGenericsBuilder<PS extends android.os.Parcelable & java.io.Serializable, P extends android.os.Parcelable, S extends java.io.Serializable> {

    private final Bundle mArguments = new Bundle();

    public ClassWithGenericsBuilder(PS generic, java.util.ArrayList<PS> genericList, P parcelable, java.util.ArrayList<P> parcelableList, P privateParcelableGeneric, S privateSerializableGeneric, S serializable, java.util.ArrayList<S> serializableList) {

        mArguments.putParcelable("generic", generic);

        mArguments.putParcelableArrayList("genericList", genericList);

        mArguments.putParcelable("parcelable", parcelable);

        mArguments.putParcelableArrayList("parcelableList", parcelableList);

        mArguments.putParcelable("privateParcelableGeneric", privateParcelableGeneric);

        mArguments.putSerializable("privateSerializableGeneric", privateSerializableGeneric);

        mArguments.putSerializable("serializable", serializable);

        mArguments.putSerializable("serializableList", serializableList);
    }

    public static <PS extends android.os.Parcelable & java.io.Serializable, P extends android.os.Parcelable, S extends java.io.Serializable>ClassWithGenerics newClassWithGenerics(PS generic, java.util.ArrayList<PS> genericList, P parcelable, java.util.ArrayList<P> parcelableList, P privateParcelableGeneric, S privateSerializableGeneric, S serializable, java.util.ArrayList<S> serializableList) {
        return new ClassWithGenericsBuilder(generic, genericList, parcelable, parcelableList, privateParcelableGeneric, privateSerializableGeneric, serializable, serializableList).build();
    }

    public ClassWithGenericsBuilder optionalParcelable(P optionalParcelable) {

        if (optionalParcelable != null) {
            mArguments.putParcelable("optionalParcelable", optionalParcelable);
        }
        return this;
    }

    public ClassWithGenericsBuilder optionalSerializable(S optionalSerializable) {

        if (optionalSerializable != null) {
            mArguments.putSerializable("optionalSerializable", optionalSerializable);
        }
        return this;
    }

    public Bundle buildBundle() {
        return new Bundle(mArguments);
    }

    public static final <PS extends android.os.Parcelable & java.io.Serializable, P extends android.os.Parcelable, S extends java.io.Serializable>void injectArguments(ClassWithGenerics fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            throw new IllegalStateException("No arguments set. Have you set up this Fragment with the corresponding FragmentArgs Builder? ");
        }

        if (!args.containsKey("privateSerializableGeneric")) {
            throw new IllegalStateException("required argument privateSerializableGeneric is not set");
        }
        S value0 = (S) args.getSerializable("privateSerializableGeneric");
        fragment.setPrivateSerializableGeneric(value0);

        if (!args.containsKey("genericList")) {
            throw new IllegalStateException("required argument genericList is not set");
        }
        fragment.genericList = args.getParcelableArrayList("genericList");

        if (!args.containsKey("parcelable")) {
            throw new IllegalStateException("required argument parcelable is not set");
        }
        fragment.parcelable = args.getParcelable("parcelable");

        if (!args.containsKey("parcelableList")) {
            throw new IllegalStateException("required argument parcelableList is not set");
        }
        fragment.parcelableList = args.getParcelableArrayList("parcelableList");

        if (args != null && args.containsKey("optionalParcelable")) {
            fragment.optionalParcelable = args.getParcelable("optionalParcelable");
        }

        if (!args.containsKey("privateParcelableGeneric")) {
            throw new IllegalStateException("required argument privateParcelableGeneric is not set");
        }
        P value1 = args.getParcelable("privateParcelableGeneric");
        fragment.setPrivateParcelableGeneric(value1);

        if (!args.containsKey("serializable")) {
            throw new IllegalStateException("required argument serializable is not set");
        }
        fragment.serializable = (S) args.getSerializable("serializable");

        if (!args.containsKey("generic")) {
            throw new IllegalStateException("required argument generic is not set");
        }
        fragment.generic = args.getParcelable("generic");

        if (!args.containsKey("serializableList")) {
            throw new IllegalStateException("required argument serializableList is not set");
        }
        fragment.serializableList = (java.util.ArrayList<S>) args.getSerializable("serializableList");

        if (args != null && args.containsKey("optionalSerializable")) {
            fragment.optionalSerializable = (S) args.getSerializable("optionalSerializable");
        }
    }

    public ClassWithGenerics build() {
        ClassWithGenerics fragment = new ClassWithGenerics();
        fragment.setArguments(mArguments);
        return fragment;
    }
}
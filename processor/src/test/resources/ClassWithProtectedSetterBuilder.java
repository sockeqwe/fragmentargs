package com.hannesdorfmann.fragmentargs.processor.test;

import android.os.Bundle;

public final class ClassWithProtectedSetterBuilder {

    private final Bundle mArguments = new Bundle();

    public ClassWithProtectedSetterBuilder(String privateArg) {

        mArguments.putString("privateArg", privateArg);
    }

    public static ClassWithProtectedSetter newClassWithProtectedSetter(String privateArg) {
        return new ClassWithProtectedSetterBuilder(privateArg).build();
    }

    public Bundle buildBundle() {
        return new Bundle(mArguments);
    }

    public static final void injectArguments(ClassWithProtectedSetter fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            throw new IllegalStateException("No arguments set. Have you set up this Fragment with the corresponding FragmentArgs Builder? ");
        }

        if (!args.containsKey("privateArg")) {
            throw new IllegalStateException("required argument privateArg is not set");
        }
        java.lang.String value0 = args.getString("privateArg");
        fragment.setPrivateArg(value0);
    }

    public ClassWithProtectedSetter build() {
        ClassWithProtectedSetter fragment = new ClassWithProtectedSetter();
        fragment.setArguments(mArguments);
        return fragment;
    }
}
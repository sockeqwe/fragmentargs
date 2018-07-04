package com.hannesdorfmann.fragmentargs.processor.test;

import android.os.Bundle;

public final class ClassWithProtectedFieldBuilder {

    private final Bundle mArguments = new Bundle();

    public ClassWithProtectedFieldBuilder(String protectedArg) {

        mArguments.putString("protectedArg", protectedArg);
    }

    public static ClassWithProtectedField newClassWithProtectedField(String protectedArg) {
        return new ClassWithProtectedFieldBuilder(protectedArg).build();
    }

    public Bundle buildBundle() {
        return new Bundle(mArguments);
    }

    public static final void injectArguments(ClassWithProtectedField fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            throw new IllegalStateException("No arguments set. Have you set up this Fragment with the corresponding FragmentArgs Builder? ");
        }

        if (!args.containsKey("protectedArg")) {
            throw new IllegalStateException("required argument protectedArg is not set");
        }
        fragment.protectedArg = args.getString("protectedArg");
    }

    public ClassWithProtectedField build() {
        ClassWithProtectedField fragment = new ClassWithProtectedField();
        fragment.setArguments(mArguments);
        return fragment;
    }
}
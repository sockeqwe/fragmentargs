package com.hannesdorfmann.fragmentargs.processor.test;

import android.os.Bundle;

public final class ClassWithInnerClassBuilder {

    private final Bundle mArguments = new Bundle();

    public ClassWithInnerClassBuilder(String arg) {

        mArguments.putString("arg", arg);
    }

    public static ClassWithInnerClass newClassWithInnerClass(String arg) {
        return new ClassWithInnerClassBuilder(arg).build();
    }

    public Bundle buildBundle() {
        return new Bundle(mArguments);
    }

    public static final void injectArguments(ClassWithInnerClass fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            throw new IllegalStateException("No arguments set. Have you set up this Fragment with the corresponding FragmentArgs Builder? ");
        }

        if (!args.containsKey("arg")) {
            throw new IllegalStateException("required argument arg is not set");
        }
        fragment.arg = args.getString("arg");
    }

    public ClassWithInnerClass build() {
        ClassWithInnerClass fragment = new ClassWithInnerClass();
        fragment.setArguments(mArguments);
        return fragment;
    }
}
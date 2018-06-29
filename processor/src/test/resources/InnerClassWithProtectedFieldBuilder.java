package com.hannesdorfmann.fragmentargs.processor.test;

import android.os.Bundle;

public final class InnerClassWithProtectedFieldBuilder {

    private final Bundle mArguments = new Bundle();

    public InnerClassWithProtectedFieldBuilder(String arg) {

        mArguments.putString("arg", arg);
    }

    public static InnerClassWithProtectedField newInnerClassWithProtectedField(String arg) {
        return new InnerClassWithProtectedFieldBuilder(arg).build();
    }

    public Bundle buildBundle() {
        return new Bundle(mArguments);
    }

    public static final void injectArguments(InnerClassWithProtectedField fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            throw new IllegalStateException("No arguments set. Have you set up this Fragment with the corresponding FragmentArgs Builder? ");
        }

        if (!args.containsKey("arg")) {
            throw new IllegalStateException("required argument arg is not set");
        }
        fragment.arg = args.getString("arg");
    }

    public InnerClassWithProtectedField build() {
        InnerClassWithProtectedField fragment = new InnerClassWithProtectedField();
        fragment.setArguments(mArguments);
        return fragment;
    }
}
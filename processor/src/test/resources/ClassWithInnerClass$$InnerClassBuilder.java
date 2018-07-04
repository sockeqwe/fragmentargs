package com.hannesdorfmann.fragmentargs.processor.test;

import android.os.Bundle;
import com.hannesdorfmann.fragmentargs.processor.test.ClassWithInnerClass.InnerClass;

public final class ClassWithInnerClass$$InnerClassBuilder {

    private final Bundle mArguments = new Bundle();

    public ClassWithInnerClass$$InnerClassBuilder(String arg) {

        mArguments.putString("arg", arg);
    }

    public static InnerClass newInnerClass(String arg) {
        return new ClassWithInnerClass$$InnerClassBuilder(arg).build();
    }

    public Bundle buildBundle() {
        return new Bundle(mArguments);
    }

    public static final void injectArguments(InnerClass fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            throw new IllegalStateException("No arguments set. Have you set up this Fragment with the corresponding FragmentArgs Builder? ");
        }

        if (!args.containsKey("arg")) {
            throw new IllegalStateException("required argument arg is not set");
        }
        fragment.arg = args.getString("arg");
    }

    public InnerClass build() {
        InnerClass fragment = new InnerClass();
        fragment.setArguments(mArguments);
        return fragment;
    }
}
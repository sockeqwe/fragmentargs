package com.hannesdorfmann.fragmentargstest.test;

import android.os.Bundle;
import com.hannesdorfmann.fragmentargstest.test.ClassWithGenericsInnerClass.InnerClass;

public final class ClassWithGenericsInnerClass$$InnerClassBuilder<S extends java.io.Serializable> {

    private final Bundle mArguments = new Bundle();

    public ClassWithGenericsInnerClass$$InnerClassBuilder(S generic) {

        mArguments.putSerializable("generic", generic);
    }

    public static <S extends java.io.Serializable>InnerClass newInnerClass(S generic) {
        return new ClassWithGenericsInnerClass$$InnerClassBuilder(generic).build();
    }

    public Bundle buildBundle() {
        return new Bundle(mArguments);
    }

    public static final <S extends java.io.Serializable>void injectArguments(InnerClass fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            throw new IllegalStateException("No arguments set. Have you set up this Fragment with the corresponding FragmentArgs Builder? ");
        }

        if (!args.containsKey("generic")) {
            throw new IllegalStateException("required argument generic is not set");
        }
        fragment.generic = (S) args.getSerializable("generic");
    }

    public InnerClass build() {
        InnerClass fragment = new InnerClass();
        fragment.setArguments(mArguments);
        return fragment;
    }
}
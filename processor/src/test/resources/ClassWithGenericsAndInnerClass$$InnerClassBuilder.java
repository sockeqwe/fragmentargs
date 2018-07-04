package com.hannesdorfmann.fragmentargstest.test;

import android.os.Bundle;
import com.hannesdorfmann.fragmentargstest.test.ClassWithGenericsAndInnerClass.InnerClass;

public final class ClassWithGenericsAndInnerClass$$InnerClassBuilder {

    private final Bundle mArguments = new Bundle();

    public ClassWithGenericsAndInnerClass$$InnerClassBuilder(String generic) {

        mArguments.putString("generic", generic);
    }

    public static InnerClass newInnerClass(String generic) {
        return new ClassWithGenericsAndInnerClass$$InnerClassBuilder(generic).build();
    }

    public Bundle buildBundle() {
        return new Bundle(mArguments);
    }

    public static final void injectArguments(InnerClass fragment) {
        Bundle args = fragment.getArguments();
        if (args == null) {
            throw new IllegalStateException("No arguments set. Have you set up this Fragment with the corresponding FragmentArgs Builder? ");
        }

        if (!args.containsKey("generic")) {
            throw new IllegalStateException("required argument generic is not set");
        }
        fragment.generic = args.getString("generic");
    }

    public InnerClass build() {
        InnerClass fragment = new InnerClass();
        fragment.setArguments(mArguments);
        return fragment;
    }
}
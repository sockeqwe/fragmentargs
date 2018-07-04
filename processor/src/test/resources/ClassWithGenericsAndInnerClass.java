package com.hannesdorfmann.fragmentargstest.test;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class ClassWithGenericsAndInnerClass<A extends Object> {

    @com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
    public class InnerClass extends android.app.Fragment {

        @com.hannesdorfmann.fragmentargs.annotation.Arg
        String generic;

    }
}
package com.hannesdorfmann.fragmentargstest.test;

import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class ClassWithGenericsInnerClass {

    @com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
    public class InnerClass<S extends Serializable> extends android.app.Fragment {

        @com.hannesdorfmann.fragmentargs.annotation.Arg
        S generic;

    }
}
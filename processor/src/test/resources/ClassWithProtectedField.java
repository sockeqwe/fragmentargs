package com.hannesdorfmann.fragmentargs.processor.test;

@com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
public class ClassWithProtectedField extends android.app.Fragment {
    @com.hannesdorfmann.fragmentargs.annotation.Arg
    protected String protectedArg;
}
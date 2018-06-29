package com.hannesdorfmann.fragmentargs.processor.test;

@com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
public class InnerClassWithProtectedField extends android.app.Fragment {

    @com.hannesdorfmann.fragmentargs.annotation.Arg
    protected String arg;

    @com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
    public static class InnerClass extends android.app.Fragment {

        @com.hannesdorfmann.fragmentargs.annotation.Arg
        protected String arg;
    }

}
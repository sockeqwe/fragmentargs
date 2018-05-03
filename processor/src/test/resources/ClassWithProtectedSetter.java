package com.hannesdorfmann.fragmentargs.processor.test;

@com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
public class ClassWithProtectedSetter extends android.app.Fragment {
    @com.hannesdorfmann.fragmentargs.annotation.Arg
    private String privateArg;

    protected void setPrivateArg(String privateArg) {
        this.privateArg = privateArg;
    }
}
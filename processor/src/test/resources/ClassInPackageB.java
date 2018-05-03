package com.hannesdorfmann.fragmentargs.processor.test.B;

@com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs
public class ClassInPackageB extends com.hannesdorfmann.fragmentargs.processor.test.A.ClassInPackageA {
    @com.hannesdorfmann.fragmentargs.annotation.Arg
    String argB;
}
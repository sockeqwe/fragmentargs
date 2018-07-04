package com.hannesdorfmann.fragmentargs.processor;

import org.junit.Test;

import static com.hannesdorfmann.fragmentargs.processor.CompileTest.assertClassCompilesWithoutError;

public class InnerClassTest {

    @Test
    public void innerClass() {
        assertClassCompilesWithoutError("ClassWithInnerClass.java", "ClassWithInnerClass$$InnerClassBuilder.java");
    }

    @Test
    public void innerClassWithProtectedField() {
        assertClassCompilesWithoutError("InnerClassWithProtectedField.java", "InnerClassWithProtectedFieldBuilder.java");
    }
}

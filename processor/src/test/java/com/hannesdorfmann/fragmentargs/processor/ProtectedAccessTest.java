package com.hannesdorfmann.fragmentargs.processor;

import org.junit.Test;

import static com.hannesdorfmann.fragmentargs.processor.CompileTest.assertClassCompilesWithoutError;

public class ProtectedAccessTest {
    @Test
    public void protectedField() {
        assertClassCompilesWithoutError("ClassWithProtectedField.java", "ClassWithProtectedFieldBuilder.java");
    }

    @Test
    public void protectedSetter() {
        assertClassCompilesWithoutError("ClassWithProtectedSetter.java", "ClassWithProtectedSetterBuilder.java");
    }
}

package com.hannesdorfmann.fragmentargs.processor;

import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public class ProtectedAccessTest {
    @Test
    public void protectedField() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource("ClassWithProtectedField.java"))
                .processedWith(new ArgProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("ClassWithProtectedFieldBuilder.java"));
    }

    @Test
    public void protectedSetter() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource("ClassWithProtectedSetter.java"))
                .processedWith(new ArgProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource("ClassWithProtectedSetterBuilder.java"));
    }
}

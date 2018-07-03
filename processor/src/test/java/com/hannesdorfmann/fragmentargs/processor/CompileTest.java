package com.hannesdorfmann.fragmentargs.processor;

import com.google.testing.compile.JavaFileObjects;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public final class CompileTest {

    public static void assertClassCompilesWithoutError(final String classResourceName, final String outputClassResourceName) {
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource(classResourceName))
                .processedWith(new ArgProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(JavaFileObjects.forResource(outputClassResourceName));
    }

}

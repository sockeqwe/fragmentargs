package com.hannesdorfmann.fragmentargs.processor;

import com.google.testing.compile.JavaFileObjects;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

public final class CompileTest {

    private static final String[] PROCESSOR_OPTIONS
            = new String[]{"-AfragmentArgsSupportAnnotations=false"};

    public static void assertClassCompilesWithoutError(final String classResourceName) {
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource(classResourceName))
                .withCompilerOptions(PROCESSOR_OPTIONS)
                .processedWith(new ArgProcessor())
                .compilesWithoutError();
    }

}

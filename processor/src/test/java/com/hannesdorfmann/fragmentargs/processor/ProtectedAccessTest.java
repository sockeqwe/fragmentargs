package com.hannesdorfmann.fragmentargs.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * Created by pamalyshev on 11.06.16.
 */
public class ProtectedAccessTest {
    private static final String[] PROCESSOR_OPTIONS
            = new String[]{"-AfragmentArgsSupportAnnotations=false"};

    @Test
    public void protectedField() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource("ClassWithProtectedField.java"))
                .withCompilerOptions(PROCESSOR_OPTIONS)
                .processedWith(new ArgProcessor())
                .compilesWithoutError();
    }

    @Test
    public void protectedSetter() {
        assert_().about(javaSource())
                .that(JavaFileObjects.forResource("ClassWithProtectedSetter.java"))
                .withCompilerOptions(PROCESSOR_OPTIONS)
                .processedWith(new ArgProcessor())
                .compilesWithoutError();
    }
}

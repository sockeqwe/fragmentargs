package com.hannesdorfmann.fragmentargs.processor;

import com.google.testing.compile.JavaFileObjects;
import org.junit.Test;

import javax.tools.JavaFileObject;

import java.util.Arrays;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

/**
 * Created by pamalyshev on 11.06.16.
 */
public class DifferentPackageInheritanceTest {
    private static final String[] PROCESSOR_OPTIONS
            = new String[]{"-AfragmentArgsSupportAnnotations=false"};

    @Test
    public void differentPackageInheritance() {
        JavaFileObject[] sources = new JavaFileObject[]{
                JavaFileObjects.forResource("ClassInPackageA.java"),
                JavaFileObjects.forResource("ClassInPackageB.java")
        };

        assert_().about(javaSources())
                .that(Arrays.asList(sources))
                .withCompilerOptions(PROCESSOR_OPTIONS)
                .processedWith(new ArgProcessor())
                .compilesWithoutError();
    }
}

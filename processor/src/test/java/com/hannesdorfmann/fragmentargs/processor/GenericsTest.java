package com.hannesdorfmann.fragmentargs.processor;

import org.junit.Test;

import static com.hannesdorfmann.fragmentargs.processor.CompileTest.assertClassCompilesWithoutError;

public class GenericsTest {

    @Test
    public void classWithGenerics() {
        assertClassCompilesWithoutError("ClassWithGenerics.java", "ClassWithGenericsBuilder.java");
    }

}

#2.1.0 (2015-05-01)
 - Added `ArgsBundler` to provide a plugin mechanism for not out of the box supported data types. Two ArgsBundler are already provided `CastedArrayListArgsBundler` and `PacelerArgsBundler`.
 - Removed warning: "Could not load the generated automapping class. However, that may be ok, if you use FragmentArgs in library projects".
 - Better error messages if annotating `@Arg` on unsupported type fields.

#2.0.1 (2014-12-22)
 - Removed the compilation warning: `Warning: The following options were not recognized by any processor: '[fragmentArgsLib]'`
 - Minor bugfix that have occurred on some java 6 enviroments in combination with `@FragmentArgsInherited`

#2.0.0 (2014-12-10)
 - Support for inheritance through included modules, jar and aar
 - Introduced `@FragmentArgsInherited` annotation

#1.2.0 (2014-12-06)
Better support for inheritance and abstract classes

#1.1.0 (2014-11-12)
Support for Android libraries to avoid errors where the auto injector class is generated multiple times which will cause this error
`Multiple dex files define com/hannesdorfmann/fragmentargs/AutoFragmentArgInjector`

#1.0.3 (2014-09-27)
optimization

#1.0.2 (2014-09-22)
Minor bug fix

# 1.0.1 (2014-09-16)
Annotation Processor: support for Java 7 and Java 8


# 1.0.0 (2014-09-15)
First major release
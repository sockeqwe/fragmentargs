# 4.0.0-RC1 (2018-06-20):
- Next major release
- Nullability in Fragment Builder: Non Optional arguments must not be null. Corresponding @Nullable annotations are applied.
- Support for `anroidx.Fragment` [#103](https://github.com/sockeqwe/fragmentargs/pull/103)
- Some minor improvements:
    1. [Missing @override annotation](https://github.com/sockeqwe/fragmentargs/pull/86) 
    2. [If POJO implements Parcelable and Serializeable, prefer Parcelable](https://github.com/sockeqwe/fragmentargs/issues/74)
    3. [Support for protected fields](https://github.com/sockeqwe/fragmentargs/issues/61)
    4. [Create bundle via Builder](https://github.com/sockeqwe/fragmentargs/issues/58)
    5. [Better support for boolean args in kotlin](https://github.com/sockeqwe/fragmentargs/pull/100)
    6. [Annotation Processing option to suppress warnings](https://github.com/sockeqwe/fragmentargs/pull/98)

# 3.0.2 (2016-03--04):
 - Hotfix (#47): There was a bug that Builder has not been generated when Fragment have no (zero) arguments 
 
# 3.0.1 (2015-12-28): 
 - Minor Bugfix (#35): when using custom `Bundler` and setter methods (because annotated field is private) java compiler could not determine the generic type of setter method parameter.
 
# 3.0.0 (2015-11-02): See [this](http://hannesdorfmann.com/android/fragmentargs3/) blog post for more information
 - You now have to annotate the Fragment class itself with `@FragmentWithArgs`. For backward compatibility reasons this is not mandatory. However it's strongly recommended because in further versions of FragmentArgs this could become mandatory to support more features. 
 - Deprecated `@FragmentArgsInherited`. Use `@FragmentWithArgs(inherited = true or false)` instead.
 - Support for setter methods: Still annotate your fields with `@Arg` not the setter method. Now you can annotate `private` fields as well, but you have to provide the corresponding setter method.
 - Kotlin support: Since setter methods are now supported, FragmentArgs support kotlin backing fields out of the box.
 - Generated Builder classes are now per default annotated with `@NonNull` from androids support annotation library. Furthermore, this adds even better kotlin support since kotlin uses this annotations for null safety. You can disable this option by using annotation processor option `fragmentArgsSupportAnnotations false`. See readme for detail information.
 - You can use annotation processor option `fragmentArgsBuilderAnnotations "com.example.MyAnnotation"` to annotate the generated builder classes with additional third party annotations. See readme for details information.

# 2.1.0 (2015-05-01)
 - Added `ArgsBundler` to provide a plugin mechanism for not out of the box supported data types. Two ArgsBundler are already provided `CastedArrayListArgsBundler` and `PacelerArgsBundler`.
 - Removed warning: "Could not load the generated automapping class. However, that may be ok, if you use FragmentArgs in library projects".
 - Better error messages if annotating `@Arg` on unsupported type fields.

# 2.0.1 (2014-12-22)
 - Removed the compilation warning: `Warning: The following options were not recognized by any processor: '[fragmentArgsLib]'`
 - Minor bugfix that have occurred on some java 6 enviroments in combination with `@FragmentArgsInherited`

# 2.0.0 (2014-12-10)
 - Support for inheritance through included modules, jar and aar
 - Introduced `@FragmentArgsInherited` annotation

# 1.2.0 (2014-12-06)
Better support for inheritance and abstract classes

# 1.1.0 (2014-11-12)
Support for Android libraries to avoid errors where the auto injector class is generated multiple times which will cause this error
`Multiple dex files define com/hannesdorfmann/fragmentargs/AutoFragmentArgInjector`

# 1.0.3 (2014-09-27)
optimization

# 1.0.2 (2014-09-22)
Minor bug fix

# 1.0.1 (2014-09-16)
Annotation Processor: support for Java 7 and Java 8


# 1.0.0 (2014-09-15)
First major release
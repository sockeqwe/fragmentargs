package com.hannesdorfmann.fragmentargs.processor;

import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.FragmentArgsInjector;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;
import com.hannesdorfmann.fragmentargs.bundler.ArgsBundler;
import com.hannesdorfmann.fragmentargs.repacked.com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * This is the annotation processor for FragmentArgs
 *
 * @author Hannes Dorfmann
 */
public class ArgProcessor extends AbstractProcessor {

    private static final String CUSTOM_BUNDLER_BUNDLE_KEY =
            "com.hannesdorfmann.fragmentargs.custom.bundler.2312A478rand.";

    private static final Map<String, String> ARGUMENT_TYPES = new HashMap<String, String>(20);

    /**
     * Annotation Processor Option
     */
    private static final String OPTION_IS_LIBRARY = "fragmentArgsLib";

    /**
     * Should the builder be annotated with support annotations?
     */
    private static final String OPTION_SUPPORT_ANNOTATIONS = "fragmentArgsSupportAnnotations";

    /**
     * Pass a list of additional annotations to annotate the generated builder classes
     */
    private static final String OPTION_ADDITIONAL_BUILDER_ANNOTATIONS =
            "fragmentArgsBuilderAnnotations";

    /**
     * Enable/disable warning logs
     */
    private static final String OPTION_LOG_WARNINGS = "fragmentArgsLogWarnings";

    static {
        ARGUMENT_TYPES.put("java.lang.String", "String");
        ARGUMENT_TYPES.put("int", "Int");
        ARGUMENT_TYPES.put("java.lang.Integer", "Int");
        ARGUMENT_TYPES.put("long", "Long");
        ARGUMENT_TYPES.put("java.lang.Long", "Long");
        ARGUMENT_TYPES.put("double", "Double");
        ARGUMENT_TYPES.put("java.lang.Double", "Double");
        ARGUMENT_TYPES.put("short", "Short");
        ARGUMENT_TYPES.put("java.lang.Short", "Short");
        ARGUMENT_TYPES.put("float", "Float");
        ARGUMENT_TYPES.put("java.lang.Float", "Float");
        ARGUMENT_TYPES.put("byte", "Byte");
        ARGUMENT_TYPES.put("java.lang.Byte", "Byte");
        ARGUMENT_TYPES.put("boolean", "Boolean");
        ARGUMENT_TYPES.put("java.lang.Boolean", "Boolean");
        ARGUMENT_TYPES.put("char", "Char");
        ARGUMENT_TYPES.put("java.lang.Character", "Char");
        ARGUMENT_TYPES.put("java.lang.CharSequence", "CharSequence");
        ARGUMENT_TYPES.put("android.os.Bundle", "Bundle");
        ARGUMENT_TYPES.put("android.os.Parcelable", "Parcelable");
    }

    private Types typeUtils;
    private Filer filer;

    private TypeElement TYPE_FRAGMENT;
    private TypeElement TYPE_SUPPORT_FRAGMENT;
    private TypeElement TYPE_ANDROIDX_FRAGMENT;
    private boolean supportAnnotations = true;
    private boolean logWarnings = true;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportTypes = new LinkedHashSet<String>();
        supportTypes.add(Arg.class.getCanonicalName());
        supportTypes.add(FragmentWithArgs.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> supportedOptions = new LinkedHashSet<String>();
        supportedOptions.add(OPTION_IS_LIBRARY);
        supportedOptions.add(OPTION_ADDITIONAL_BUILDER_ANNOTATIONS);
        supportedOptions.add(OPTION_SUPPORT_ANNOTATIONS);
        supportedOptions.add(OPTION_LOG_WARNINGS);
        return supportedOptions;
    }

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);

        Elements elementUtils = env.getElementUtils();
        typeUtils = env.getTypeUtils();
        filer = env.getFiler();

        TYPE_FRAGMENT = elementUtils.getTypeElement("android.app.Fragment");
        TYPE_SUPPORT_FRAGMENT =
                elementUtils.getTypeElement("android.support.v4.app.Fragment");
        TYPE_ANDROIDX_FRAGMENT =
                elementUtils.getTypeElement("androidx.fragment.app.Fragment");
    }

    private String getOperation(ArgumentAnnotatedField arg) {
        String op = ARGUMENT_TYPES.get(arg.getRawType());
        if (op != null) {
            if (arg.isArray()) {
                return op + "Array";
            } else {
                return op;
            }
        }

        Elements elements = processingEnv.getElementUtils();
        TypeMirror type = arg.getElement().asType();
        Types types = processingEnv.getTypeUtils();
        String[] arrayListTypes = new String[]{
                String.class.getName(), Integer.class.getName(), CharSequence.class.getName()
        };
        String[] arrayListOps =
                new String[]{"StringArrayList", "IntegerArrayList", "CharSequenceArrayList"};
        for (int i = 0; i < arrayListTypes.length; i++) {
            TypeMirror tm = getArrayListType(arrayListTypes[i]);
            if (types.isAssignable(type, tm)) {
                return arrayListOps[i];
            }
        }

        if (types.isAssignable(type,
                getWildcardType(ArrayList.class.getName(), "android.os.Parcelable"))) {
            return "ParcelableArrayList";
        }
        TypeMirror sparseParcelableArray =
                getWildcardType("android.util.SparseArray", "android.os.Parcelable");

        if (types.isAssignable(type, sparseParcelableArray)) {
            return "SparseParcelableArray";
        }

        if (types.isAssignable(type, elements.getTypeElement("android.os.Parcelable").asType())) {
            return "Parcelable";
        }

        if (types.isAssignable(type, elements.getTypeElement(Serializable.class.getName()).asType())) {
            return "Serializable";
        }

        return null;
    }

    private TypeMirror getWildcardType(String type, String elementType) {
        TypeElement arrayList = processingEnv.getElementUtils().getTypeElement(type);
        TypeMirror elType = processingEnv.getElementUtils().getTypeElement(elementType).asType();
        return processingEnv.getTypeUtils()
                .getDeclaredType(arrayList, processingEnv.getTypeUtils().getWildcardType(elType, null));
    }

    private TypeMirror getArrayListType(String elementType) {
        TypeElement arrayList = processingEnv.getElementUtils().getTypeElement("java.util.ArrayList");
        TypeMirror elType = processingEnv.getElementUtils().getTypeElement(elementType).asType();
        return processingEnv.getTypeUtils().getDeclaredType(arrayList, elType);
    }

    private void writePutArguments(JavaWriter jw, String sourceVariable, String bundleVariable,
                                   ArgumentAnnotatedField arg) throws IOException, ProcessingException {

        boolean addNullCheck = !arg.isPrimitive() && !arg.isRequired();

        jw.emitEmptyLine();

        if (addNullCheck) {
            jw.beginControlFlow("if (%s != null)", sourceVariable);
        }

        if (arg.hasCustomBundler()) {
            jw.emitStatement("%s.putBoolean(\"%s\", true)", bundleVariable,
                    CUSTOM_BUNDLER_BUNDLE_KEY + arg.getKey());
            jw.emitStatement("%s.put(\"%s\", %s, %s)", arg.getBundlerFieldName(), arg.getKey(),
                    sourceVariable, bundleVariable);
        } else {

            String op = getOperation(arg);

            if (op == null) {
                throw new ProcessingException(arg.getElement(),
                        "Don't know how to put %s in a Bundle. This type is not supported by default. "
                                + "However, you can specify your own %s implementation in @Arg( bundler = YourBundler.class)",
                        arg.getElement().asType().toString(), ArgsBundler.class.getSimpleName());
            }

            if ("Serializable".equals(op)) {
                warn(arg.getElement(),
                    "%1$s will be stored as Serializable",
                    arg.getName()
                );
            }

            jw.emitStatement("%4$s.put%1$s(\"%2$s\", %3$s)", op, arg.getKey(), sourceVariable,
                    bundleVariable);
        }

        if (addNullCheck) {
            jw.endControlFlow();
        }
    }

    private void writePackage(JavaWriter jw, TypeElement type) throws IOException {
        PackageElement pkg = processingEnv.getElementUtils().getPackageOf(type);
        if (!pkg.isUnnamed()) {
            jw.emitPackage(pkg.getQualifiedName().toString());
        } else {
            jw.emitPackage("");
        }
    }

    /**
     * Scans for @Arg annotations in the class itself and all super classes (complete inheritance
     * hierarchy)
     */
    private AnnotatedFragment collectArgumentsForTypeInclSuperClasses(TypeElement type)
            throws ProcessingException {

        AnnotatedFragment fragment = new AnnotatedFragment(type);
        TypeElement currentClass = type;
        do {

            for (Element e : currentClass.getEnclosedElements()) {
                if (e.getKind() != ElementKind.FIELD) {
                    fragment.checkAndAddSetterMethod(e);
                    continue;
                }

                // It's a field
                Arg annotation = null;
                if ((annotation = e.getAnnotation(Arg.class)) != null) {
                    ArgumentAnnotatedField annotatedField =
                            new ArgumentAnnotatedField(e, (TypeElement) e.getEnclosingElement(), annotation);
                    addAnnotatedField(annotatedField, fragment, annotation);
                }
            }

            TypeMirror superClassType = currentClass.getSuperclass();
            if (superClassType.getKind() == TypeKind.NONE) {
                // Basis class (java.lang.Object) reached, so exit
                currentClass = null;
                break;
            } else {
                currentClass = (TypeElement) typeUtils.asElement(superClassType);
            }
        } while (currentClass != null);

        return fragment;
    }

    /**
     * Generates an error String with detailed information about that a field with the same name is
     * already defined in a super class
     */
    private String getErrorMessageDuplicatedField(AnnotatedFragment fragment,
                                                  TypeElement problemClass, String fieldName) {

        String base =
                "A field with the name '%s' in class %s is already annotated with @%s in super class %s ! "
                        + "Fields name must be unique within inheritance hierarchy.";

        // Assumption: The problemClass is already a super class of the real problem,
        // So determine the real problem by searching for the subclass that cause this problem
        TypeElement otherClass = null;
        for (ArgumentAnnotatedField otherField : fragment.getAll()) {
            if (otherField.getVariableName().equals(fieldName)) {
                otherClass = otherField.getClassElement();
                break;
            }
        }

        if (otherClass != null) {
            // Check who is the super class
            TypeElement currentClass = otherClass;

            while (currentClass != null) {
                TypeMirror currentClassSuperclass = currentClass.getSuperclass();
                if (currentClassSuperclass == null || currentClassSuperclass.getKind() == TypeKind.NONE) {
                    // They are not super classes
                    break;
                }

                if (currentClass.getQualifiedName() != null && currentClass.getQualifiedName()
                        .toString()
                        .equals(problemClass.getQualifiedName().toString())) {
                    // The problem causing class is a super class, so we found the superclass
                    // and the sub class that cause the problem
                    return String.format(base, fieldName, otherClass.getQualifiedName().toString(),
                            Arg.class.getSimpleName(), problemClass.getQualifiedName());
                }

                currentClass = (TypeElement) typeUtils.asElement(currentClassSuperclass);
            }
        }

        // Since the previous check wasn't successfull we can assume:
        // The problemClass must be a sub class, so find the super class that contains the field
        TypeMirror superClass = problemClass.getSuperclass();
        TypeElement superClassElement = null;

        if (superClass == null) {
            return String.format(
                    "A field with the name '%s' in class %s is already annotated with @%s in a super class or sub class! "
                            + "Fields name must be unique within inheritance hierarchy.", fieldName,
                    problemClass.getQualifiedName().toString(), Arg.class.getSimpleName());
        }

        boolean superClassFound = false;
        while (superClass != null
                && superClass.getKind() != TypeKind.NONE
                && (superClassElement = (TypeElement) typeUtils.asElement(superClass)) != null) {

            for (Element e : superClassElement.getEnclosedElements()) {
                if (e.getKind() == ElementKind.FIELD && e.getSimpleName() != null &&
                        e.getSimpleName().toString().equals(fieldName)) {
                    superClassFound = true;
                    break;
                }
            }

            if (superClassFound) {
                break;
            }
            superClass = superClassElement.getSuperclass();
        }

        if (superClassElement == null) {
            // Should never be the case, however to ensure we return a error message without superclass
            return String.format(
                    "A field with the name '%s' in class %s is already annotated with @%s in a "
                            + "super class or sub class of %s ! "
                            + "Fields name must be unique within inheritance hierarchy.", fieldName,
                    problemClass.getQualifiedName().toString(), Arg.class.getSimpleName(),
                    problemClass.getQualifiedName().toString());
        }

        return String.format(base, fieldName, problemClass.getQualifiedName().toString(),
                Arg.class.getSimpleName(), superClassElement.getQualifiedName());
    }

    /**
     * Checks if the annotated field can be added to the given fragment. Otherwise a error message
     * will be printed
     */
    private void addAnnotatedField(ArgumentAnnotatedField annotatedField, AnnotatedFragment fragment,
                                   Arg annotation) throws ProcessingException {

        if (fragment.containsField(annotatedField)) {
            // A field already with the name is here
            throw new ProcessingException(annotatedField.getElement(),
                    getErrorMessageDuplicatedField(fragment, annotatedField.getClassElement(),
                            annotatedField.getVariableName()));
        } else if (fragment.containsBundleKey(annotatedField) != null) {
            //  key for bundle is already in use
            ArgumentAnnotatedField otherField = fragment.containsBundleKey(annotatedField);
            throw new ProcessingException(annotatedField.getElement(),
                    "The bundle key '%s' for field %s in %s is already used by another "
                            + "argument in %s (field name is '%s'). Bundle keys must be unique in inheritance hierarchy!",
                    annotatedField.getKey(), annotatedField.getVariableName(),
                    annotatedField.getClassElement().getQualifiedName().toString(),
                    otherField.getClassElement().getQualifiedName().toString(), otherField.getVariableName());
        } else {
            if (annotation.required()) {
                fragment.addRequired(annotatedField);
            } else {
                fragment.addOptional(annotatedField);
            }
        }
    }

    /**
     * Checks if inheritance hiererachy should be scanned for @Args annotations as well
     *
     * @param type The Fragment class
     * @return true if super type should be scanned as well, otherwise false;
     */
    private boolean shouldScanSuperClassesFragmentArgs(TypeElement type) throws ProcessingException {

        boolean scanSuperClasses = true;

        FragmentWithArgs fragmentWithArgs = type.getAnnotation(FragmentWithArgs.class);
        if (fragmentWithArgs != null) {
            scanSuperClasses = fragmentWithArgs.inherited();
        }

        return scanSuperClasses; // Default value
    }

    /**
     * Collects the fields that are annotated by the fragmentarg
     */
    private AnnotatedFragment collectArgumentsForType(TypeElement type) throws ProcessingException {

        // incl. super classes
        if (shouldScanSuperClassesFragmentArgs(type)) {
            return collectArgumentsForTypeInclSuperClasses(type);
        }

        // Without super classes (inheritance)
        AnnotatedFragment fragment = new AnnotatedFragment(type);
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() == ElementKind.FIELD) {
                Arg annotation = element.getAnnotation(Arg.class);
                if (annotation != null) {
                    ArgumentAnnotatedField field = new ArgumentAnnotatedField(element, type, annotation);
                    addAnnotatedField(field, fragment, annotation);
                }
            } else {
                // check for setter
                fragment.checkAndAddSetterMethod(element);
            }
        }
        return fragment;
    }

    @Override
    public boolean process(Set<? extends TypeElement> type, RoundEnvironment env) {

        Types typeUtils = processingEnv.getTypeUtils();
        Filer filer = processingEnv.getFiler();

        //
        // Processor options
        //
        boolean isLibrary = false;
        String fragmentArgsLib = processingEnv.getOptions().get(OPTION_IS_LIBRARY);
        if (fragmentArgsLib != null && fragmentArgsLib.equalsIgnoreCase("true")) {
            isLibrary = true;
        }

        String supportAnnotationsStr = processingEnv.getOptions().get(OPTION_SUPPORT_ANNOTATIONS);
        if (supportAnnotationsStr != null && supportAnnotationsStr.equalsIgnoreCase("false")) {
            supportAnnotations = false;
        }

        String additionalBuilderAnnotations[] = {};
        String builderAnnotationsStr =
                processingEnv.getOptions().get(OPTION_ADDITIONAL_BUILDER_ANNOTATIONS);
        if (builderAnnotationsStr != null && builderAnnotationsStr.length() > 0) {
            additionalBuilderAnnotations = builderAnnotationsStr.split(" "); // White space is delimiter
        }

        String fragmentArgsLogWarnings = processingEnv.getOptions().get(OPTION_LOG_WARNINGS);
        if(fragmentArgsLogWarnings != null && fragmentArgsLogWarnings.equalsIgnoreCase("false")) {
            logWarnings = false;
        }

        String nonNullAnnotationImport = "";
        String nullableAnnotationImport = "";

        if(supportAnnotations) {
            if (isClassAvailable("android.support.annotation.NonNull")) {
                nonNullAnnotationImport = "android.support.annotation.NonNull";
                nullableAnnotationImport = "android.support.annotation.Nullable";

            } else if (isClassAvailable("androidx.annotation.NonNull")) {
                nonNullAnnotationImport = "androidx.annotation.NonNull";
                nullableAnnotationImport = "androidx.annotation.Nullable";

            } else {
                supportAnnotations = false;
                warn(null,
                        "Support annotations have been disabled because neither " +
                                "'android.support.annotation.NonNull' nor " +
                                "'androidx.annotation.NonNull' could be found during processing"
                );
            }
        }

        List<ProcessingException> processingExceptions = new ArrayList<ProcessingException>();

        JavaWriter jw = null;

        // REMEMBER: It's a SET! it uses .equals() .hashCode() to determine if element already in set
        Set<TypeElement> fragmentClasses = new HashSet<TypeElement>();

        Element[] origHelper = null;

        // Search for @Arg fields
        for (Element element : env.getElementsAnnotatedWith(Arg.class)) {

            try {
                TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

                // Check if its a fragment
                if (!isFragmentClass(enclosingElement)) {
                    throw new ProcessingException(element,
                            "@Arg can only be used on fragment fields (%s.%s)",
                            enclosingElement.getQualifiedName(), element);
                }

                if (element.getModifiers().contains(Modifier.FINAL)) {
                    throw new ProcessingException(element,
                            "@Arg fields must not be final (%s.%s)",
                            enclosingElement.getQualifiedName(), element);
                }

                if (element.getModifiers()
                        .contains(Modifier.STATIC)) {
                    throw new ProcessingException(element,
                            "@Arg fields must not be static (%s.%s)",
                            enclosingElement.getQualifiedName(), element);
                }

                // Skip abstract classes
                if (!enclosingElement.getModifiers().contains(Modifier.ABSTRACT)) {
                    fragmentClasses.add(enclosingElement);
                }
            } catch (ProcessingException e) {
                processingExceptions.add(e);
            }
        }

        // Search for "just" @FragmentWithArgs
        for (Element element : env.getElementsAnnotatedWith(FragmentWithArgs.class)) {
            try {
                scanForAnnotatedFragmentClasses(env, FragmentWithArgs.class, fragmentClasses, element);
            } catch (ProcessingException e) {
                processingExceptions.add(e);
            }
        }

        // Store the key - value for the generated FragmentArtMap class
        Map<String, String> autoMapping = new HashMap<String, String>();

        for (TypeElement fragmentClass : fragmentClasses) {

            JavaFileObject jfo = null;
            try {

                AnnotatedFragment fragment = collectArgumentsForType(fragmentClass);

                String builder = fragment.getSimpleName() + "Builder";
                List<Element> originating = new ArrayList<Element>(10);
                originating.add(fragmentClass);
                TypeMirror superClass = fragmentClass.getSuperclass();
                while (superClass.getKind() != TypeKind.NONE) {
                    TypeElement element = (TypeElement) typeUtils.asElement(superClass);
                    if (element.getQualifiedName().toString().startsWith("android.")) {
                        break;
                    }
                    originating.add(element);
                    superClass = element.getSuperclass();
                }

                String qualifiedFragmentName = fragment.getQualifiedName();
                String qualifiedBuilderName = qualifiedFragmentName + "Builder";

                Element[] orig = originating.toArray(new Element[originating.size()]);
                origHelper = orig;

                jfo = filer.createSourceFile(qualifiedBuilderName, orig);
                Writer writer = jfo.openWriter();
                jw = new JavaWriter(writer);
                writePackage(jw, fragmentClass);
                jw.emitImports("android.os.Bundle");
                if (supportAnnotations) {
                    jw.emitImports(nonNullAnnotationImport);
                    if (!fragment.getOptionalFields().isEmpty()) {
                        jw.emitImports(nullableAnnotationImport);
                    }
                }

                jw.emitEmptyLine();

                // Additional builder annotations
                for (String builderAnnotation : additionalBuilderAnnotations) {
                    jw.emitAnnotation(builderAnnotation);
                }

                jw.beginType(builder, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));

                if (!fragment.getBundlerVariableMap().isEmpty()) {
                    jw.emitEmptyLine();
                    for (Map.Entry<String, String> e : fragment.getBundlerVariableMap().entrySet()) {
                        jw.emitField(e.getKey(), e.getValue(),
                                EnumSet.of(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC),
                                "new " + e.getKey() + "()");
                    }
                }
                jw.emitEmptyLine();
                jw.emitField("Bundle", "mArguments", EnumSet.of(Modifier.PRIVATE, Modifier.FINAL),
                        "new Bundle()");
                jw.emitEmptyLine();

                Set<ArgumentAnnotatedField> required = fragment.getRequiredFields();

                String[] args = new String[required.size() * 2];
                int index = 0;

                for (ArgumentAnnotatedField arg : required) {
                    boolean annotate = supportAnnotations && !arg.isPrimitive();
                    args[index++] = annotate ? "@NonNull " + arg.getType() : arg.getType();
                    args[index++] = arg.getVariableName();
                }
                jw.beginMethod(null, builder, EnumSet.of(Modifier.PUBLIC), args);

                for (ArgumentAnnotatedField arg : required) {
                    writePutArguments(jw, arg.getVariableName(), "mArguments", arg);
                }

                jw.endMethod();

                if (!required.isEmpty()) {
                    jw.emitEmptyLine();
                    writeNewFragmentWithRequiredMethod(builder, fragmentClass, jw, args);
                }

                Set<ArgumentAnnotatedField> optionalArguments = fragment.getOptionalFields();

                for (ArgumentAnnotatedField arg : optionalArguments) {
                    writeBuilderMethod(builder, jw, arg);
                }

                jw.emitEmptyLine();
                writeBuildBundleMethod(jw);

                jw.emitEmptyLine();
                writeInjectMethod(jw, fragmentClass, fragment);

                jw.emitEmptyLine();
                writeBuildMethod(jw, fragmentClass);

                jw.endType();

                autoMapping.put(qualifiedFragmentName, qualifiedBuilderName);
            } catch (IOException e) {
                processingExceptions.add(
                        new ProcessingException(fragmentClass, "Unable to write builder for type %s: %s",
                                fragmentClass, e.getMessage()));
            } catch (ProcessingException e) {
                processingExceptions.add(e);
                if (jfo != null) {
                    jfo.delete();
                }
            } finally {
                if (jw != null) {
                    try {
                        jw.close();
                    } catch (IOException e1) {
                        processingExceptions.add(new ProcessingException(fragmentClass,
                                "Unable to close javawriter while generating builder for type %s: %s",
                                fragmentClass, e1.getMessage()));
                    }
                }
            }
        }

        // Write the automapping class
        if (origHelper != null && !isLibrary) {
            try {
                writeAutoMapping(autoMapping, origHelper);
            } catch (ProcessingException e) {
                processingExceptions.add(e);
            }
        }

        // Print errors
        for (ProcessingException e : processingExceptions) {
            error(e);
        }

        return true;
    }

    /**
     * Write the buildBundle() method
     *
     * @param jw The javawriter
     * @throws IOException
     */
    private void writeBuildBundleMethod(JavaWriter jw) throws IOException {
        if (supportAnnotations) jw.emitAnnotation("NonNull");
        jw.beginMethod("Bundle", "buildBundle", EnumSet.of(Modifier.PUBLIC));
        jw.emitStatement("return new Bundle(mArguments)");
        jw.endMethod();
    }

    /**
     * Scans a fragment for a given {@link FragmentWithArgs} annotation
     *
     * @param env             The round environment
     * @param annotationClass The annotation (.class) to scan for
     * @param fragmentClasses The set of classes already scanned (containing annotations)
     * @throws ProcessingException
     */
    private void scanForAnnotatedFragmentClasses(RoundEnvironment env,
                                                 Class<? extends Annotation> annotationClass, Set<TypeElement> fragmentClasses,
                                                 Element element)
            throws ProcessingException {

        if (element.getKind() != ElementKind.CLASS) {
            throw new ProcessingException(element, "%s can only be applied on Fragment classes",
                    annotationClass.getSimpleName());
        }

        TypeElement classElement = (TypeElement) element;

        // Check if its a fragment
        if (!isFragmentClass(element)) {
            throw new ProcessingException(element,
                    "%s can only be used on fragments, but %s is not a subclass of fragment",
                    annotationClass.getSimpleName(), classElement.getQualifiedName());
        }

        // Skip abstract classes
        if (!classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            fragmentClasses.add(classElement);
        }
    }

    /**
     * Checks if the given element is in a valid Fragment class
     */
    private boolean isFragmentClass(Element classElement) {
        List<TypeElement> fragmentTypeElements = Arrays.asList(TYPE_FRAGMENT, TYPE_SUPPORT_FRAGMENT, TYPE_ANDROIDX_FRAGMENT);

        for (TypeElement fragmentTypeElement : fragmentTypeElements) {
            if(fragmentTypeElement != null && typeUtils.isSubtype(classElement.asType(),
                    fragmentTypeElement.asType())) {
                return true;
            }
        }

        return false;
    }

    private boolean isClassAvailable(String className) {
        try  {
            Class.forName(className);
            return true;
        }  catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Key is the fully qualified fragment name, value is the fully qualified Builder class name
     */

    private void writeAutoMapping(Map<String, String> mapping, Element[] element)
            throws ProcessingException {

        try {
            JavaFileObject jfo =
                    filer.createSourceFile(FragmentArgs.AUTO_MAPPING_QUALIFIED_CLASS, element);
            Writer writer = jfo.openWriter();
            JavaWriter jw = new JavaWriter(writer);
            // Package
            jw.emitPackage(FragmentArgs.AUTO_MAPPING_PACKAGE);

            // Class
            jw.beginType(FragmentArgs.AUTO_MAPPING_CLASS_NAME, "class",
                    EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), null,
                    FragmentArgsInjector.class.getCanonicalName());

            jw.emitEmptyLine();
            // The mapping Method
            jw.emitAnnotation("Override");
            jw.beginMethod("void", "inject", EnumSet.of(Modifier.PUBLIC), "Object", "target");

            jw.emitEmptyLine();
            jw.emitStatement("Class<?> targetClass = target.getClass()");
            jw.emitStatement("String targetName = targetClass.getCanonicalName()");
            // TODO should be targetClass.getName()? Inner anonymous class not possible?

            for (Map.Entry<String, String> entry : mapping.entrySet()) {

                jw.emitEmptyLine();
                jw.beginControlFlow("if ( %s.class.getName().equals(targetName) )", entry.getKey());
                jw.emitStatement("%s.injectArguments( ( %s ) target)", entry.getValue(), entry.getKey());
                jw.emitStatement("return");
                jw.endControlFlow();
            }

            // End Mapping method
            jw.endMethod();

            jw.endType();
            jw.close();
        } catch (IOException e) {
            throw new ProcessingException(null,
                    "Unable to write the automapping class for builder to fragment: %s: %s",
                    FragmentArgs.AUTO_MAPPING_QUALIFIED_CLASS, e.getMessage());
        }
    }

    private void writeNewFragmentWithRequiredMethod(String builder, TypeElement element,
                                                    JavaWriter jw, String[] args) throws IOException {

        if (supportAnnotations) jw.emitAnnotation("NonNull");
        jw.beginMethod(element.getQualifiedName().toString(), "new" + element.getSimpleName(),
                EnumSet.of(Modifier.STATIC, Modifier.PUBLIC), args);
        StringBuilder argNames = new StringBuilder();
        for (int i = 1; i < args.length; i += 2) {
            argNames.append(args[i]);
            if (i < args.length - 1) {
                argNames.append(", ");
            }
        }
        jw.emitStatement("return new %1$s(%2$s).build()", builder, argNames);
        jw.endMethod();
    }

    private void writeBuildMethod(JavaWriter jw, TypeElement element) throws IOException {
        if (supportAnnotations) {
            jw.emitAnnotation("NonNull");
        }

        jw.beginMethod(element.getSimpleName().toString(), "build", EnumSet.of(Modifier.PUBLIC));
        jw.emitStatement("%1$s fragment = new %1$s()", element.getSimpleName().toString());
        jw.emitStatement("fragment.setArguments(mArguments)");
        jw.emitStatement("return fragment");
        jw.endMethod();
    }

    private void writeInjectMethod(JavaWriter jw, TypeElement element,
                                   AnnotatedFragment fragment) throws IOException, ProcessingException {

        Set<ArgumentAnnotatedField> allArguments = fragment.getAll();

        String fragmentType = supportAnnotations ? "@NonNull " + element.getSimpleName().toString()
                : element.getSimpleName().toString();

        jw.beginMethod("void", "injectArguments",
                EnumSet.of(Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC),
                fragmentType, "fragment");


        jw.emitStatement("Bundle args = fragment.getArguments()");

        // Check if bundle is null only if at least one required field
        jw.beginControlFlow("if (args == null)");
        jw.emitStatement(
                "throw new IllegalStateException(\"No arguments set. Have you set up this Fragment with the corresponding FragmentArgs Builder? \")");
        jw.endControlFlow();


        int setterAssignmentHelperCounter = 0;
        for (ArgumentAnnotatedField field : allArguments) {
            jw.emitEmptyLine();

            Set<Modifier> modifiers = field.getElement().getModifiers();

            // Check if the given setter is available
            String setterMethod = null;

            // Private fields and non-public fields from a different package need a setter method
            boolean useSetter = modifiers.contains(Modifier.PRIVATE)
                    || (!getPackage(fragment.getClassElement()).equals(getPackage(field.getElement())) && !modifiers.contains(Modifier.PUBLIC));

            if (useSetter) {
                ExecutableElement setterMethodElement = fragment.findSetterForField(field);
                setterMethod = setterMethodElement.getSimpleName().toString();
            }

            // Args Bundler
            if (field.hasCustomBundler()) {

                String setterAssignmentHelperStr = null;
                String assignmentStr;
                if (useSetter) {
                    setterAssignmentHelperStr = field.getType()
                            + " value"
                            + setterAssignmentHelperCounter
                            + " =  %s.get(\"%s\", args)";
                    assignmentStr = "fragment.%s( value" + setterAssignmentHelperCounter + " )";
                    setterAssignmentHelperCounter++;
                } else {
                    assignmentStr = "fragment.%s = %s.get(\"%s\", args)";
                }

                // Required
                if (field.isRequired()) {
                    jw.beginControlFlow("if (!args.containsKey(" + JavaWriter.stringLiteral(
                            CUSTOM_BUNDLER_BUNDLE_KEY + field.getKey()) + "))");
                    jw.emitStatement("throw new IllegalStateException(\"required argument %1$s is not set\")",
                            field.getKey());
                    jw.endControlFlow();
                    if (useSetter) {
                        jw.emitStatement(setterAssignmentHelperStr, field.getBundlerFieldName(),
                                field.getKey());
                        jw.emitStatement(assignmentStr, setterMethod);
                    } else {
                        jw.emitStatement(assignmentStr, field.getName(),
                                field.getBundlerFieldName(), field.getKey());
                    }
                } else {
                    // not required bundler
                    jw.beginControlFlow("if (args.getBoolean(" + JavaWriter.stringLiteral(
                            CUSTOM_BUNDLER_BUNDLE_KEY + field.getKey()) + "))");

                    if (useSetter) {
                        jw.emitStatement(setterAssignmentHelperStr, field.getBundlerFieldName(),
                                field.getKey());
                        jw.emitStatement(assignmentStr, setterMethod);
                    } else {
                        jw.emitStatement(assignmentStr, field.getName(),
                                field.getBundlerFieldName(), field.getKey());
                    }

                    jw.endControlFlow();
                }
            } else {

                // Build in functions
                String op = getOperation(field);
                if (op == null) {
                    throw new ProcessingException(element,
                            "Can't write injector, the type is not supported by default. "
                                    + "However, You can provide your own implementation by providing an %s like this: @Arg( bundler = YourBundler.class )",
                            ArgsBundler.class.getSimpleName());
                }

                String cast = "Serializable".equals(op) ? "(" + field.getType() + ") " : "";
                if (!field.isRequired()) {
                    jw.beginControlFlow(
                            "if (args != null && args.containsKey("
                                    + JavaWriter.stringLiteral(field.getKey())
                                    + "))");
                } else {
                    jw.beginControlFlow(
                            "if (!args.containsKey(" + JavaWriter.stringLiteral(field.getKey()) + "))");
                    jw.emitStatement("throw new IllegalStateException(\"required argument %1$s is not set\")",
                            field.getKey());
                    jw.endControlFlow();
                }

                if (useSetter) {
                    jw.emitStatement(
                            "%1$s value" + setterAssignmentHelperCounter + " = %4$sargs.get%2$s(\"%3$s\")",
                            field.getType(), op,
                            field.getKey(), cast);
                    jw.emitStatement("fragment.%1$s(value" + setterAssignmentHelperCounter + ")",
                            setterMethod);
                    setterAssignmentHelperCounter++;
                } else {
                    jw.emitStatement("fragment.%1$s = %4$sargs.get%2$s(\"%3$s\")", field.getName(), op,
                            field.getKey(), cast);
                }

                if (!field.isRequired()) {
                    jw.endControlFlow();
                }
            }
        }
        jw.endMethod();
    }

    private void writeBuilderMethod(String type, JavaWriter writer, ArgumentAnnotatedField arg)
            throws IOException, ProcessingException {
        writer.emitEmptyLine();
        boolean annotate = supportAnnotations && !arg.isPrimitive();

        String typeStr;
        if (annotate) {
            if (arg.isRequired()) {
                typeStr = "@NonNull " + arg.getType();
            } else {
                typeStr = "@Nullable " + arg.getType();
            }
        } else {
            typeStr = arg.getType();
        }

        if (supportAnnotations) writer.emitAnnotation("NonNull");
        writer.beginMethod(type, arg.getVariableName(), EnumSet.of(Modifier.PUBLIC),
                typeStr, arg.getVariableName());
        writePutArguments(writer, arg.getVariableName(), "mArguments", arg);
        writer.emitStatement("return this");
        writer.endMethod();
    }

    private void error(ProcessingException e) {
        String message = e.getMessage();
        if (e.getMessageArgs().length > 0) {
            message = String.format(message, e.getMessageArgs());
        }
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, e.getElement());
    }

    private void warn(Element element, String message, Object... args) {
        if(logWarnings) {
            if (args.length > 0) {
                message = String.format(message, args);
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, message, element);
        }
    }

    private PackageElement getPackage(Element element) {
        while (element.getKind() != ElementKind.PACKAGE) {
            element = element.getEnclosingElement();
        }
        return (PackageElement) element;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

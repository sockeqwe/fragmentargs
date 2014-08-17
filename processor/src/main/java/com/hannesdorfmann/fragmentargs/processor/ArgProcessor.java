package com.hannesdorfmann.fragmentargs.processor;

import com.hannesdorfmann.fragmentargs.FragmentArgInjector;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.repacked.com.squareup.javawriter.JavaWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
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
 * This is the default Ason annotation processor
 *
 * @author Hannes Dorfmann
 */
@SupportedAnnotationTypes("com.hannesdorfmann.fragmentargs.annotation.Arg")
public class ArgProcessor extends AbstractProcessor {

  private static final Map<String, String> ARGUMENT_TYPES = new HashMap<String, String>(20);

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

  private Elements elementUtils;
  private Types typeUtils;
  private Filer filer;

  @Override
  public synchronized void init(ProcessingEnvironment env) {
    super.init(env);

    elementUtils = env.getElementUtils();
    typeUtils = env.getTypeUtils();
    filer = env.getFiler();
  }

  protected String getOperation(AnnotatedField arg) {
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
    String[] arrayListTypes = new String[] {
        String.class.getName(), Integer.class.getName(), CharSequence.class.getName()
    };
    String[] arrayListOps =
        new String[] { "StringArrayList", "IntegerArrayList", "CharSequenceArrayList" };
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

    if (types.isAssignable(type, elements.getTypeElement(Serializable.class.getName()).asType())) {
      return "Serializable";
    }

    if (types.isAssignable(type, elements.getTypeElement("android.os.Parcelable").asType())) {
      return "Parcelable";
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

  protected void writePutArguments(JavaWriter jw, String sourceVariable, String bundleVariable,
      AnnotatedField arg) throws IOException {
    String op = getOperation(arg);

    if (op == null) {
      error(arg.getElement(), "Don't know how to put %s in a Bundle",
          arg.getElement().asType().toString());
      return;
    }
    if ("Serializable".equals(op)) {
      processingEnv.getMessager()
          .printMessage(Diagnostic.Kind.WARNING,
              String.format("%1$s will be stored as Serializable", arg.getName()),
              arg.getElement());
    }
    jw.emitStatement("%4$s.put%1$s(\"%2$s\", %3$s)", op, arg.getKey(), sourceVariable,
        bundleVariable);
  }

  protected void writePackage(JavaWriter jw, TypeElement type) throws IOException {
    PackageElement pkg = processingEnv.getElementUtils().getPackageOf(type);
    if (!pkg.isUnnamed()) {
      jw.emitPackage(pkg.getQualifiedName().toString());
    } else {
      jw.emitPackage("");
    }
  }

  private Set<AnnotatedField> collectArgumentsForType(Types typeUtil, TypeElement type,
      Map<TypeElement, Set<Element>> fieldsByType, boolean requiredOnly,
      boolean processSuperClass) {
    Set<AnnotatedField> arguments = new TreeSet<AnnotatedField>();
    if (processSuperClass) {
      TypeMirror superClass = type.getSuperclass();
      if (superClass.getKind() != TypeKind.NONE) {
        arguments.addAll(
            collectArgumentsForType(typeUtil, (TypeElement) typeUtil.asElement(superClass),
                fieldsByType, requiredOnly, true));
      }
    }
    Set<Element> fields = fieldsByType.get(type);
    if (fields == null) {
      return arguments;
    }
    for (Element element : fields) {
      if (requiredOnly) {
        Arg arg = element.getAnnotation(Arg.class);
        if (!arg.required()) {
          continue;
        }
      }
      arguments.add(new ArgumentAnnotatedField(element));
    }
    return arguments;
  }

  @Override
  public boolean process(Set<? extends TypeElement> type, RoundEnvironment env) {

    Elements elementUtils = processingEnv.getElementUtils();
    Types typeUtils = processingEnv.getTypeUtils();
    Filer filer = processingEnv.getFiler();
    TypeElement fragmentType = elementUtils.getTypeElement("android.app.Fragment");
    TypeElement supportFragmentType =
        elementUtils.getTypeElement("android.support.v4.app.Fragment");
    Map<TypeElement, Set<Element>> fieldsByType = new HashMap<TypeElement, Set<Element>>(100);

    for (Element element : env.getElementsAnnotatedWith(Arg.class)) {
      TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

      if (!((fragmentType != null && typeUtils.isSubtype(enclosingElement.asType(),
          fragmentType.asType())) || (supportFragmentType != null && typeUtils.isSubtype(
          enclosingElement.asType(), supportFragmentType.asType())))) {
        error(element, "@Arg can only be used on fragment fields (%s.%s)",
            enclosingElement.getQualifiedName(), element);
        continue;
      }

      if (element.getModifiers().contains(Modifier.FINAL) || element.getModifiers()
          .contains(Modifier.STATIC) || element.getModifiers().contains(Modifier.PRIVATE) || element
          .getModifiers()
          .contains(Modifier.PROTECTED)) {
        error(element, "@Arg fields must not be private, protected, final or static (%s.%s)",
            enclosingElement.getQualifiedName(), element);
        continue;
      }
      Set<Element> fields = fieldsByType.get(enclosingElement);
      if (fields == null) {
        fields = new LinkedHashSet<Element>(10);
        fieldsByType.put(enclosingElement, fields);
      }
      fields.add(element);
    }

    // Store the key - value for the generated FragmentArtMap class
    Map<String, String> autoMapping = new HashMap<String, String>();

    for (Map.Entry<TypeElement, Set<Element>> entry : fieldsByType.entrySet()) {
      try {
        String builder = entry.getKey().getSimpleName() + "Builder";
        List<Element> originating = new ArrayList<Element>(10);
        originating.add(entry.getKey());
        TypeMirror superClass = entry.getKey().getSuperclass();
        while (superClass.getKind() != TypeKind.NONE) {
          TypeElement element = (TypeElement) typeUtils.asElement(superClass);
          if (element.getQualifiedName().toString().startsWith("android.")) {
            break;
          }
          originating.add(element);
          superClass = element.getSuperclass();
        }

        String qualifiedFragmentName = entry.getKey().getQualifiedName().toString();
        String qualifiedBuilderName = qualifiedFragmentName + "Builder";

        JavaFileObject jfo = filer.createSourceFile(qualifiedBuilderName,
            originating.toArray(new Element[originating.size()]));
        Writer writer = jfo.openWriter();
        JavaWriter jw = new JavaWriter(writer);
        writePackage(jw, entry.getKey());
        jw.emitImports("android.os.Bundle");
        jw.beginType(builder, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        jw.emitField("Bundle", "mArguments", EnumSet.of(Modifier.PRIVATE, Modifier.FINAL),
            "new Bundle()");
        jw.emitEmptyLine();

        Set<AnnotatedField> required =
            collectArgumentsForType(typeUtils, entry.getKey(), fieldsByType, true, true);

        String[] args = new String[required.size() * 2];
        int index = 0;
        for (AnnotatedField arg : required) {
          args[index++] = arg.getType();
          args[index++] = arg.getVariableName();
        }
        jw.beginMethod(null, builder, EnumSet.of(Modifier.PUBLIC), args);

        for (AnnotatedField arg : required) {
          writePutArguments(jw, arg.getVariableName(), "mArguments", arg);
        }

        jw.endMethod();

        if (!required.isEmpty()) {
          writeNewFragmentWithRequiredMethod(builder, entry.getKey(), jw, args);
        }

        Set<AnnotatedField> allArguments =
            collectArgumentsForType(typeUtils, entry.getKey(), fieldsByType, false, true);
        Set<AnnotatedField> optionalArguments = new HashSet<AnnotatedField>(allArguments);
        optionalArguments.removeAll(required);

        for (AnnotatedField arg : optionalArguments) {
          writeBuilderMethod(builder, jw, arg);
        }

        writeInjectMethod(jw, entry.getKey(),
            collectArgumentsForType(typeUtils, entry.getKey(), fieldsByType, false, false));
        writeBuildMethod(jw, entry.getKey());
        writeBuildSubclassMethod(jw, entry.getKey());
        jw.endType();
        jw.close();

        autoMapping.put(qualifiedFragmentName, qualifiedBuilderName);
      } catch (IOException e) {
        error(entry.getKey(), "Unable to write builder for type %s: %s", entry.getKey(),
            e.getMessage());
        throw new RuntimeException(e);
      }
    }

    // Write the automapping class
    writeAutoMapping(autoMapping);

    return true;
  }

  /**
   * Key is the fully qualified fragment name, value is the fully qualified Builder class name
   */
  private void writeAutoMapping(Map<String, String> mapping) {

    try {
      JavaFileObject jfo = filer.createSourceFile(FragmentArgs.AUTO_MAPPING_QUALIFIED_CLASS);
      Writer writer = jfo.openWriter();
      JavaWriter jw = new JavaWriter(writer);
      // Package
      jw.emitPackage(FragmentArgs.AUTO_MAPPING_PACKAGE);
      // Imports
      jw.emitImports("android.os.Bundle");

      // Class
      jw.beginType(FragmentArgs.AUTO_MAPPING_CLASS_NAME, "class",
          EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), null,
          FragmentArgInjector.class.getCanonicalName());

      // The mapping Method
      jw.beginMethod("void", "inject", EnumSet.of(Modifier.PUBLIC), "String targetClass",
          "Bundle bundle");

      for (Map.Entry<String, String> entry : mapping.entrySet()) {

        jw.emitEmptyLine();
        jw.beginControlFlow("if ( %s.equals(targetClass) )", entry.getKey());
        jw.emitStatement("%s.injectArguments(bundle);", entry.getValue());
        jw.emitStatement("return;");
        jw.endControlFlow();
        jw.emitEmptyLine();

      }

      // End Mapping method
      jw.endMethod();

      jw.endType();
      jw.close();
    } catch (IOException e) {
      error(null, "Unable to write the automapping class for builder to fragment: %s: %s",
          FragmentArgs.AUTO_MAPPING_QUALIFIED_CLASS, e.getMessage());

      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
      throw new RuntimeException(sw.toString(), e);
    }
  }

  private void writeNewFragmentWithRequiredMethod(String builder, TypeElement element,
      JavaWriter jw, String[] args) throws IOException {
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
    jw.beginMethod(element.getSimpleName().toString(), "build", EnumSet.of(Modifier.PUBLIC));
    jw.emitStatement("%1$s fragment = new %1$s()", element.getSimpleName().toString());
    jw.emitStatement("fragment.setArguments(mArguments)");
    jw.emitStatement("return fragment");
    jw.endMethod();
  }

  private void writeBuildSubclassMethod(JavaWriter jw, TypeElement element) throws IOException {
    jw.beginMethod("<F extends " + element.getSimpleName().toString() + "> F", "build",
        EnumSet.of(Modifier.PUBLIC), "F", "fragment");
    jw.emitStatement("fragment.setArguments(mArguments)");
    jw.emitStatement("return fragment");
    jw.endMethod();
  }

  private void writeInjectMethod(JavaWriter jw, TypeElement element,
      Set<AnnotatedField> allArguments) throws IOException {
    jw.beginMethod("void", "injectArguments", EnumSet.of(Modifier.STATIC, Modifier.FINAL),
        element.getSimpleName().toString(), "fragment");

    jw.emitStatement("Bundle args = fragment.getArguments()");
    jw.beginControlFlow("if (args == null)");
    jw.emitStatement("throw new IllegalStateException(\"No arguments set\")");
    jw.endControlFlow();

    for (AnnotatedField type : allArguments) {
      String op = getOperation(type);
      if (op == null) {
        error(element, "Can't write injector, the bundle getter is unknown");
        return;
      }
      String cast = "Serializable".equals(op) ? "(" + type.getType() + ") " : "";
      if (!type.isRequired()) {
        jw.beginControlFlow(
            "if (args.containsKey(" + JavaWriter.stringLiteral(type.getKey()) + "))");
      } else {
        jw.beginControlFlow(
            "if (!args.containsKey(" + JavaWriter.stringLiteral(type.getKey()) + "))");
        jw.emitStatement("throw new IllegalStateException(\"required argument %1$s is not set\")",
            type.getKey());
        jw.endControlFlow();
      }

      jw.emitStatement("fragment.%1$s = %4$sargs.get%2$s(\"%3$s\")", type.getName(), op,
          type.getKey(), cast);

      if (!type.isRequired()) {
        jw.endControlFlow();
      }
    }
    jw.endMethod();
  }

  private void writeBuilderMethod(String type, JavaWriter writer, AnnotatedField arg)
      throws IOException {
    writer.emitEmptyLine();
    writer.beginMethod(type, arg.getVariableName(), EnumSet.of(Modifier.PUBLIC), arg.getType(),
        arg.getVariableName());
    writePutArguments(writer, arg.getVariableName(), "mArguments", arg);
    writer.emitStatement("return this");
    writer.endMethod();
  }

  public void error(Element element, String message, Object... args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.RELEASE_6;
  }
}

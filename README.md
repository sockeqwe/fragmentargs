# FragmentArgs
Annotation Processor to create arguments for android fragments without using reflections.

I have written a blog entry about this library: http://hannesdorfmann.com/android/fragmentargs

# Dependency
Latest version: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hannesdorfmann.fragmentargs/annotation/badge.png)](https://maven-badges.herokuapp.com/maven-central/com.hannesdorfmann.fragmentargs/annotation)
[![Build Status](https://travis-ci.org/sockeqwe/fragmentargs.svg?branch=master)](https://travis-ci.org/sockeqwe/fragmentargs)

```groovy
dependencies {
	compile 'com.hannesdorfmann.fragmentargs:annotation:4.0.0-RC1'
	annotationProcessor 'com.hannesdorfmann.fragmentargs:processor:4.0.0-RC1'
}
```
### SNAPSHOT
Lastest snapshot version is `4.0.0-SNAPSHOT`. You also have to add the url to the snapshot repository:

```gradle
allprojects {
  repositories {
    ...

    maven { url "https://oss.sonatype.org/content/repositories/snapshots/" }
}
```


# Changelog
The changelog can be found [here](https://github.com/sockeqwe/fragmentargs/blob/master/CHANGELOG.md)

# How to use
FragmentArgs generates Java code at compile time. It generates a `Builder` class out of your Fragment class.

 1. Annotate your `Fragment` with `@FragmentWithArgs`.  For backward compatibility reasons this is not mandatory. However it's strongly recommended because in further versions of FragmentArgs this could become mandatory to support more features.
 2. Annotate your fields with `@Arg`. Fields **should** have at least package (default) visibility. Alternatively, you have to provide a setter method with at least package (default) visibility for your private `@Arg` annotated fields. 
 3. In the Fragments `onCreate(Bundle)` method you have to call `FragmentArgs.inject(this)` to read the arguments and set the values. 
 4. Unlike Eclipse Android Studio does not auto compile your project while saving files. So you may have to build your project to start the annotation processor which will generate the `Builder` classes for your annotated fragments.

Example:

```java
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;

@FragmentWithArgs
public class MyFragment extends Fragment {

	@Arg
	int id;
	
	@Arg
	private String title; // private fields requires a setter method

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FragmentArgs.inject(this); // read @Arg fields
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
	
      		Toast.makeText(getActivity(), "Hello " + title,
      				Toast.LENGTH_SHORT).show();
      	
      		return null;
	}
  
	// Setter method for private field
	public void setTitle(String title) {
		this.title = title;
	}
	
}
```

In your Activity you will use the generated `Builder` class _(the name of your fragment with "Builder" suffix)_ instead of `new MyFragment()` or a static `MyFragment.newInstance(int id, String title)` method.

For example:

```java
public class MyActivity extends Activity {

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		int id = 123;
		String title = "test";
		
		// Using the generated Builder
		Fragment fragment = 
			new MyFragmentBuilder(id, title)
			.build();
		
		
		// Fragment Transaction
		getFragmentManager()
			.beginTransaction()
			.replace(R.id.container, fragment)
			.commit();
		
	}

}
```

## Optional Arguments
You can specify a fragment argument to be optional by using `@Arg(required = false)`

For example:
```java
@FragmentWithArgs
public class MyOptionalFragment extends Fragment {

	@Arg
	int id;
	
	@Arg
	String title;
	
	@Arg(required = false) 
	String additionalText;
	
	@Arg(required = false)
	float factor;
	
	@Arg(required = false)
	int mFeatureId;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		FragmentArgs.inject(this); // read @Arg fields
	}
	
}
```

Optional arguments will generate a `Builder` class with additional methods to set optional arguments.

For Example:
```java
public class MyActivity extends Activity {

	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		int id = 123;
		String title = "test";
		
		// Using the generated Builder
		Fragment fragment = 
			new MyFragmentBuilder(id, title) // required args
			.additionalText("foo") 	// Optional arg
			.factor(1.2f)			// Optional arg
			.featureId(42)			// Optional arg
			.build();
		
		
		// Fragment Transaction
		getFragmentManager()
			.beginTransaction()
			.replace(R.id.container, fragment)
			.commit();
	}

}
```

As you have seen optional fragment arguments are part of the `Builder` class as an own methods. Since they are optional you can decide if you want to set optional values or not by calling the corresponding method or skip the corresponding method call. 

Like you have seen from the example above fields named with "m" prefix will be automatically cut by making the method name the sub-string of the original fields name without the "m" prefix. For example the field `int mFeatureId` corresponds to the builders method `featureId(int)`

## Inheritance - Best practice
Wouldn't it be painful to override `onCreate(Bundle)` in every Fragment of your app just to insert `FragmentArgs.inject(this)`.
FragmentArgs are designed to support inheritance. Hence you can override once `onCreate(Bundle)` in your Fragment base class and do not need to override this for every single Fragment.

For example:
```java
public class BaseFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		FragmentArgs.inject(this); // read @Arg fields
	}
}
```

```java
@FragmentWithArgs
public class MyFragment extends BaseFragment {

	@Arg
	String title;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
      
      		Toast.makeText(getActivity(), "Hello " + title, 
      			Toast.LENGTH_SHORT).show();
      }
}
```

```java
@FragmentWithArgs
public class OtherFragment extends BaseFragment {

	@Arg
	String foo;
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
      
      		Toast.makeText(getActivity(), "Hello " + foo, 
      			Toast.LENGTH_SHORT).show();
      }
}
```
`FragmentArgs` also supports inheritance and abstract classes. That means that annotated fields of the supper class are part of the builder of the subclass. Furthermore this also works for special cases where you have a Fragment without any `@Arg` annotation but you want to use the arguments of the super class. For Example:

```java
public class A extends Fragment {

  @Arg int a;
  @Arg String foo;

}

@FragmentArgs
public class B extends A {

  // Arguments will be taken from super class
   
   
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
               Bundle savedInstanceState) {
   
      // Here you can simply access the inherited fields from super class
   }
}
```

There may be special edge cases where you don't want to use the fragment args from super class. Then you can use `@FragmentWithArgs(inherited = false)`. Example:
```java
@FragmentWithArgs(inherited = false)
public class C extends A {

   @Arg int c;

}
```

In this case only `c` will be argument of class C and the arguments of super class A are ignored.

# ArgsBundler
FragmentArgs supports the most common data structures that you can put in a `Bundle` and hence set as arguments for a Fragment. The type of the `@Arg` annotated field is used for that. If you want to set not a out of the box supported data type (like a class you cant make `Parcelable` for whatever reason) as argument you can specify your own `ArgsBundler`.

```java
public class DateArgsBundler implements ArgsBundler<Date>{

    @Override public void put(String key, Date value, Bundle bundle) {
        
        bundle.putLong(key, value.getTime());
    }

    @Override public Date get(String key, Bundle bundle) {
        
        long timestamp = bundle.getLong(key);
        return new Date(timestamp);
    }

}

public class MyFragment extends Fragment {

    @Arg ( bundler = DateArgsBundler.class )
    Date date;

}
```

There are already two `ArgBundler` you may find useful:
```java
@FragmentWithArgs
public class MyFragment {
   
    @Arg ( bundler = CastedArrayListArgsBundler.class )
    List<Foo> fooList;   // Foo implements Parcelable

    @Arg ( bundler =  ParcelerArgsBundler.class)
    Dog dog;   // Dog is @Parcel annotated
}
```

 - `CastedArrayListArgsBundler`: The problem is that in a Bundle supports `java.util.ArrayList` and not `java.util.List`. `CastedArrayListArgsBundler` assumes that the List implementation is `ArrayList` and casts `List` internally to `ArrayList` and put it into a bundle.

 - If you use [Parceler](http://parceler.org/) then you may know that your `@Parcel` annotated class is not implemnting `Parcelable` directly (Parceler generates a wrapper for your class that implements Parcelable). Therefore a `@Parcel` class can not be set directly as fragment argument with `@Arg`. However, there is a ArgsBundler called `ParcelerArgsBundler` that you can use with `@Parcel`.

    ```java
    @Parcel
    public class Dog {
      String name;
    }


    public class MyFragment {

       @Arg ( bundler = ParcelerArgsBundler.class )
       Dog foo;

    }

    ```

While `CastedArrayListArgsBundler` already ships with `compile 'com.hannesdorfmann.fragmentargs:annotation:x.x.x' ` you have to add

``` groovy
compile 'com.hannesdorfmann.fragmentargs:bundler-parceler:x.x.x'
```
as dependency to use `ParcelerArgsBundler`.

# Kotlin support
As starting with `FragmentArgs 3.0.0` the kotlin programming language is supported (use `kapt` instead of `apt`):

```kotlin
@FragmentWithArgs
class KotlinFragment : Fragment() {

    @Arg var foo: String = "foo"
    @Arg(required = false) lateinit var bar: String // works also with lateinit for non primitives

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_kotlin, container, false)

        val tv = view.findViewById(R.id.textView) as TextView

        tv.text = "Foo = ${foo} , bar = ${bar}"
        return view;
    }
}
```

# Support Fragment
Fragments of the support library are supported. Therefore fields in `android.support.v4.app.Fragment` or `android.app.Fragment` can be annotated with `@Arg`.  


# Using in library projects
You can use FragmentArgs in library projects. However, in library project you have to inject the arguments by hand in each Fragment. First of all, you have to specify in your libraries `build.gradle` that FragmentArgs should treat this project as a library project by adding the following lines:

```groovy
apply plugin: 'com.android.library'
apply plugin: 'com.neenbedankt.android-apt'

// Options for annotation processor
apt {
  arguments {
    fragmentArgsLib true
  }
}

android {
  ...
}

dependencies {
  compile 'com.hannesdorfmann.fragmentargs:annotation:x.x.x'
  apt 'com.hannesdorfmann.fragmentargs:processor:x.x.x'
  ...
}
```

So the important thing is `fragmentArgsLib = true`. Otherwise you will get an compile error like this
`Multiple dex files define com/hannesdorfmann/fragmentargs/AutoFragmentArgInjector`  in your app project that uses FrgmentArgs and your library (which uses FragmentArgs as well).

Next you have to manually inject the FragmentArguments in your Fragment which is part of your library. So you **can not use** `FragmentArgs.inject()` but you have to use explicit the generated FragmentBuilder class. Example:
```java 
@FragmentWithArgs
public class FragmenInLib extends Fragment {

  @Arg String foo;
  @Arg int test;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);        
    
    // Use the generated builder class to "inject" the arguments on creation
    FragmenInLibBuilder.injectArguments(this);
  }

}

``` 

# Annotation Processor Options
The FragmentArgs annotation processor supports some options for customization. 

```groovy
// Hugo Visser's APT plugin
apt {
  arguments {
    fragmentArgsLib true
    fragmentArgsSupportAnnotations false
    fragmentArgsBuilderAnnotations "hugo.weaving.DebugLog com.foo.OtherAnnotation"
    fragmentArgsLogWarnings false // Don't print warnings
  }
}

// Kotlin Annotation processor
kapt {
  generateStubs = true
  arguments {
    arg("fragmentArgsLib", true)
    arg("fragmentArgsSupportAnnotations", false)
    arg("fragmentArgsBuilderAnnotations", "hugo.weaving.DebugLog com.foo.OtherAnnotation")
    arg("fragmentArgsLogWarnings", false) // Don't print warnings
  }
}
```

 - **fragmentArgsLib**: Already described in _"Using in library projects"_
 - **fragmentArgsSupportAnnotations**: As default the methods of the generated `Builder` are annotated with the annotations from support library like `@NonNull` etc. You can disable that feature by passing `false`.
 - **fragmentArgsBuilderAnnotations**: You can add additional annotations to the generated `Builder` classes. For example you can add `@DebugLog` annotation to the `Builder` classes to use Jake Wharton's [Hugo](https://github.com/JakeWharton/hugo) for logging in debug builds. You have to pass a string of a full qualified annotation class name. You can supply multiple annotations by using a white space between each one.
 - **fragmentArgsLogWarnings**: You can disable all `warning` logs with this flag. (e.g. `warning: {enumFieldName} will be stored as Serializable`)

# Proguard
```
-keep class com.hannesdorfmann.fragmentargs.** { *; }
```

# Thanks
Parts of the annotation code are based on Hugo Visser's [Bundle](https://bitbucket.org/hvisser/bundles) project. I have added some optimizations and improvements.

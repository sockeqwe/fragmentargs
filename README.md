#FragmentArgs
Annotation Processor to create arguments for android fragments without using reflections.

I have written a blog entry about this library: http://hannesdorfmann.com/android/fragmentargs/

#Dependency
Latest version: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.hannesdorfmann.fragmentargs/annotation/badge.png)](https://maven-badges.herokuapp.com/maven-central/com.hannesdorfmann.fragmentargs/annotation)

To generate the Builder classes android annotation processor will be used. In gradle / android studio you need to apply Hugo Visser's awesome [android-apt](https://bitbucket.org/hvisser/android-apt) gradle plugin to run annotation processing.

```groovy
dependencies {
	compile 'com.hannesdorfmann.fragmentargs:annotation:x.x.x'
	apt 'com.hannesdorfmann.fragmentargs:processor:x.x.x'
}
```
where you have to replace x.x.x with the latest version.

# Changelog
The changelog can be found [here](https://github.com/sockeqwe/fragmentargs/blob/master/CHANGELOG.md)

#How to use
FragmentArgs generates Java code at compile time. It generates a `Builder` class out of your Fragment class.

There are three important things to note:
 1. Fields **MUST** have at least package (default) visibility. That means no private, protected or static fields can be annotated with `@Arg`. The generated Builder class is in the same package as the Fragment is. Therefore it needs at least package visibility to access the annotated fields.
 2. In the Fragments `onCreate(Bundle)` method you have to call `FragmentArgs.inject(this)` to read the arguments and set the values. 
 3. Unlike Eclipse Android Studio does not auto compile your project while saving files. So you may have to build your project to start the annotation processor which will generate the `Builder` classes for your annotated fragments.

Example:

```java
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;

public class MyFragment extends Fragment {

	@Arg
	int id;
	
	@Arg
	String title;

	@Override
	public void onCreate(Bundle savedInstanceState){
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

##Optional Arguments
You can specify a fragment argument to be optional by using `@Arg(required = false)`

For example:
```java
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

Optional Arguments will generate a `Builder` class with additional methods to set optional arguments.

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
`FragmentArgs` also supports inheritance and abstract classes. That means that annotated fields of the supper class are part of the builder of the subclass. Furthermore there are special cases where you have a Fragment without any `@Arg` annotation but you want to use the arguments of the super class. For this special case you can use `@FragmentArgsInherited`. For Example:

```java
public class A extends Fragment {

  @Arg int a;
  @Arg String foo;

}

@FragmentArgsInherited
public class B extends A {

  // Arguments will be taken from super class
   
   
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
               Bundle savedInstanceState) {
   
      // Here you can simply access the inherited fields from super class
   }
}
```

There may be special edge cases where you don't want to use the fragment args from super class. Then you can use `@FragmentArgsInherited(false)`. Example:
```java
@FragmentArgsInherited(false)
public class C extends A {

   @Arg int c;

}
```

In this case only `c` will be argument of class C and the arguments of super class A are ignored.

# ArgsBundler
FragmentArgs supports the most common data structures that you can put in a `Bundle` and hence set as arguments for a Fragment. The type of the `@Arg` annotated field is used for that. If you want to set not a out of the box supported data type (like a class you cant make `Parcelable` for whatever reason) as argument you can specify your own `ArgsBundler`.

```java
public class DateArgsBundler implements DateArgsBundler<Foo>{

    @Override public void put(String key, Date value, Bundle bundle) {
        
        bundle.putLong(key, value.getTime());
    }

    @Override public Foo get(String key, Bundle bundle) {
        
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
    fragmentArgsLib = true
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
`Multiple dex files define com/hannesdorfmann/fragmentargs/AutoFragmentArgInjector`  in your app project that uses FragmentArgs and your library (which uses FragmentArgs as well).

Next you have to manually inject the FragmentArguments in your Fragment which is part of your library. So you **can not use** `FragmentArgs.inject()` but you have to use explicit the generated FragmentBuilder class. Example:
```java 
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

#Proguard
```
-keep class com.hannesdorfmann.fragmentargs.** { *; }
-keepclasseswithmembernames class * {
    @com.hannesdorfmann.fragmentargs.annotation.** <fields>;
}
```

#Thanks
Parts of the annotation code are based on Hugo Visser's [Bundle](https://bitbucket.org/hvisser/bundles) project. I have added some optimizations and improvements.

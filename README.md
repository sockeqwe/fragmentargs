#FragmentArgs
Annotation Processor to create arguments for android fragments without using reflections.

I have written a blog entry about this library: http://hannesdorfmann.com/android/fragmentargs/

#Dependency
Check [GradlePlease](http://gradleplease.appspot.com/#com.hannesdorfmann.fragmentargs) to get the latest version number.

To generate the Builder classes android annotation processor will be used. In gradle / android studio you need to apply Hugo Visser's awesome [android-apt](https://bitbucket.org/hvisser/android-apt) gradle plugin to run annotation processing.

```groovy
dependencies {
	compile 'com.hannesdorfmann.fragmentargs:annotation:1.0.3'
	apt 'com.hannesdorfmann.fragmentargs:processor:1.0.3'
}
```

#How to use
FragmentArgs generates java code at compile time. It generates a `Builder` class out of your Fragment class.

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

Optional Arguments will genearete a `Builder` class with additional methods to set optional arguments.

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

As you have seen optional fragment arguments are part of the `Builder` class as an own methods. Since they are optional you can decide if you want to set optinal values or not by calling the corresponding method or skip the corresponding method call. 

Like you have seen from the example above fields named with "m" prefix will be automatically cut by making the method name the substring of the original fields name without the "m" prefix. For example the field `int mFeatureId` corresponds to the builders method `featureId(int)`

## Inheritance - Best practice
Wouldn't it be painful to overide `onCreate(Bundle)` in every Fragment of your app just to insert `FragmentArgs.inject(this)`.
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


#Support Fragment
Fragments of the support library are supported. Therefore fields in `android.support.v4.app.Fragment` or `android.app.Fragment` can be annotated with `@Arg`.  


#Thanks
Many parts of the annotation code are based on Hugo Visser's [Bundle](https://bitbucket.org/hvisser/bundles) project. I have added some optimizations. 

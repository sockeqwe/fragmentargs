#FragmentArgs
Annotation Processor to create arguments for android fragments.

##How to use
FragmentArgs generates java code at compile time. It generates a Builder class out of your Fragment class.

For example:

```java
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;

public class MyFragment extends Fragment {

	@Arg
	int id;
	
	@Arg
	String title;

	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		FragmentArgs.inject(this);
	}
}

```
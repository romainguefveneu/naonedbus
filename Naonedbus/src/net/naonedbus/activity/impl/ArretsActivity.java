package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.RootActivity;
import net.naonedbus.fragment.impl.ArretsFragment;
import net.naonedbus.intent.IIntentParamKey;
import android.os.Bundle;

public class ArretsActivity extends RootActivity {

	public static enum Param implements IIntentParamKey {
		idLigne
	};

	private static int[] titles = new int[] { R.string.title_fragment_arrets };

	private static Class<?>[] classes = new Class<?>[] { ArretsFragment.class };

	public ArretsActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			addFragments(titles, classes);
		}
	}

}

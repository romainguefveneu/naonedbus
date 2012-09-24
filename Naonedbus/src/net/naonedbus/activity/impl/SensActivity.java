package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.RootActivity;
import net.naonedbus.fragment.impl.CommentairesFragment;
import net.naonedbus.fragment.impl.SensFragment;
import net.naonedbus.fragment.impl.TanActuFragment;
import net.naonedbus.intent.IIntentParamKey;
import android.os.Bundle;

public class SensActivity extends RootActivity {

	public static enum Param implements IIntentParamKey {
		idLigne
	};

	private static int[] titles = new int[] { R.string.title_fragment_tan_actu, R.string.title_fragment_sens,
			R.string.title_fragment_en_direct };

	private static Class<?>[] classes = new Class<?>[] { TanActuFragment.class, SensFragment.class,
			CommentairesFragment.class };

	public SensActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			addFragments(titles, classes);
			getSupportActionBar().selectTab(getSupportActionBar().getTabAt(1));
		}
	}

}

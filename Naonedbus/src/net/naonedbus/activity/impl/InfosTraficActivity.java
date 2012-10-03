package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.SlidingMenuActivity;
import net.naonedbus.fragment.impl.CommentairesFragment;
import net.naonedbus.fragment.impl.TanActuFragment;
import android.os.Bundle;

public class InfosTraficActivity extends SlidingMenuActivity {

	private static int[] titles = new int[] { R.string.title_fragment_en_direct, R.string.title_fragment_tan_actu };

	private static Class<?>[] classes = new Class<?>[] { CommentairesFragment.class, TanActuFragment.class };

	public InfosTraficActivity() {
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

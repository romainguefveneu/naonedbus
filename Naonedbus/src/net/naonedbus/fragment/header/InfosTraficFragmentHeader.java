package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.nested.CommentairesFragment;
import net.naonedbus.fragment.impl.nested.TanActuFragment;
import android.content.Context;

public class InfosTraficFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { CommentairesFragment.class, TanActuFragment.class };

	private final int[] mTitles = new int[] { R.string.title_fragment_en_direct, R.string.title_fragment_tan_actu };

	@Override
	public int[] getFragmentsTitles() {
		return mTitles;
	}

	@Override
	public Class<?>[] getFragmentsClasses() {
		return mFragments;
	}

	@Override
	public int getSelectedPosition(final Context context) {
		return 0;
	}

}

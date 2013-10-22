package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.StopBookmarksFragment;
import net.naonedbus.fragment.impl.RoutesFragment;
import net.naonedbus.fragment.impl.NearByFragment;
import net.naonedbus.manager.impl.StopBookmarkManager;
import android.content.Context;

public class MainFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { RoutesFragment.class, StopBookmarksFragment.class,
			NearByFragment.class };

	private final int[] mTitles = new int[] { R.string.title_fragment_lignes, R.string.title_fragment_favoris,
			R.string.title_fragment_proximite };

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
		final StopBookmarkManager favoriManager = StopBookmarkManager.getInstance();
		final int count = favoriManager.getAll(context.getContentResolver()).size();
		return count > 0 ? 1 : 0;
	}

}

package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.nested.CoVoituragesFragment;
import net.naonedbus.fragment.impl.nested.LilasFragment;
import net.naonedbus.fragment.impl.nested.MargueritesFragment;
import android.content.Context;

public class EquipementsFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { MargueritesFragment.class, CoVoituragesFragment.class,
			LilasFragment.class };

	private final int[] mTitles = new int[] { R.string.title_fragment_marguerites, R.string.title_fragment_covoiturage,
			R.string.title_fragment_lila };

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

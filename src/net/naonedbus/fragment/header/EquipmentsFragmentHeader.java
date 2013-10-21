package net.naonedbus.fragment.header;

import net.naonedbus.R;
import net.naonedbus.fragment.impl.CarPoolFragment;
import net.naonedbus.fragment.impl.LilasFragment;
import net.naonedbus.fragment.impl.MargueritesFragment;
import android.content.Context;

public class EquipmentsFragmentHeader implements FragmentHeader {

	private final Class<?>[] mFragments = new Class<?>[] { MargueritesFragment.class, CarPoolFragment.class,
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

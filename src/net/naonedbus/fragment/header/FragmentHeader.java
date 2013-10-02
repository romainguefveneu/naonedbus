package net.naonedbus.fragment.header;

import android.content.Context;

public interface FragmentHeader {
	int[] getFragmentsTitles();

	Class<?>[] getFragmentsClasses();

	int getSelectedPosition(Context context);
}

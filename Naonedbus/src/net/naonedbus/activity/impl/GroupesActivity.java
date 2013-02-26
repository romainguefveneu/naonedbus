package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.GroupesFragment;
import android.os.Bundle;

public class GroupesActivity extends OneFragmentActivity {

	public GroupesActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			addFragment(GroupesFragment.class, null);
		}
	}

}

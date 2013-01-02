package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.fragment.impl.ParcoursFragment;
import net.naonedbus.intent.IIntentParamKey;
import android.os.Bundle;

public class ParcoursActivity extends OneFragmentActivity {

	public static enum Param implements IIntentParamKey {
		idStation
	};

	public ParcoursActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int idStation = (Integer) getParamValue(Param.idStation);

		final Bundle bundle = new Bundle();
		bundle.putInt(ParcoursFragment.PARAM_ID_STATION, idStation);

		if (savedInstanceState == null) {
			addFragment(ParcoursFragment.class, bundle);
		}
	}

}

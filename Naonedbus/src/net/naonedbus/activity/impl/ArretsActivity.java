package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.RootActivity;
import net.naonedbus.fragment.impl.ArretsFragment;
import net.naonedbus.intent.IIntentParamKey;
import android.os.Bundle;

public class ArretsActivity extends RootActivity {

	public static enum Param implements IIntentParamKey {
		idSens
	};

	private static int[] titles = new int[] { R.string.title_fragment_arrets };

	private static Class<?>[] classes = new Class<?>[] { ArretsFragment.class };

	private Bundle[] bundles;

	public ArretsActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		int idSens = (Integer) getParamValue(Param.idSens);

		final Bundle bundle = new Bundle();
		bundle.putInt(ArretsFragment.PARAM_ID_SENS, idSens);

		bundles = new Bundle[1];
		bundles[0] = bundle;

		if (savedInstanceState == null) {
			addFragments(titles, classes, bundles);
		}
	}

}

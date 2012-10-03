package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.SimpleFragmentActivity;
import net.naonedbus.fragment.impl.ArretsFragment;
import net.naonedbus.intent.IIntentParamKey;
import android.os.Bundle;

import com.actionbarsherlock.app.ActionBar;

public class HoraireActivity extends SimpleFragmentActivity {

	public static enum Param implements IIntentParamKey {
		idArret
	};

	private static int[] titles = new int[] { R.string.title_fragment_arrets };

	private static Class<?>[] classes = new Class<?>[] { ArretsFragment.class };

	private Bundle[] bundles;

	public HoraireActivity() {
		super(R.layout.activity_main);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int idSens = (Integer) getParamValue(Param.idArret);

		final Bundle bundle = new Bundle();
		bundle.putInt(ArretsFragment.PARAM_ID_SENS, idSens);

		bundles = new Bundle[1];
		bundles[0] = bundle;

		if (savedInstanceState == null) {
			addFragments(titles, classes, bundles);
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}

	}

}

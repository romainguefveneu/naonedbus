package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.fragment.impl.PlanFragment;
import android.os.Bundle;

public class PlanActivity extends OneFragmentActivity {

	public static final String PARAM_LIGNE = "ligne";

	private Ligne mLigne;

	public PlanActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLigne = getIntent().getParcelableExtra(PARAM_LIGNE);
		setTitle(getString(R.string.title_activity_plan, mLigne.lettre, mLigne.nom));

		final Bundle bundle = new Bundle();
		bundle.putString(PlanFragment.PARAM_CODE_LIGNE, mLigne.code);

		if (savedInstanceState == null) {
			addFragment(PlanFragment.class, bundle);
		}

	}
}

package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.fragment.impl.PlanFragment;
import net.naonedbus.manager.impl.LigneManager;
import android.os.Bundle;

public class PlanActivity extends OneFragmentActivity {

	public static final String PARAM_CODE_LIGNE = "codeLigne";

	public PlanActivity() {
		super(R.layout.activity_one_fragment);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String codeLigne = getIntent().getStringExtra(PARAM_CODE_LIGNE);
		final Ligne ligne = LigneManager.getInstance().getSingle(getContentResolver(), codeLigne);

		setTitle(getString(R.string.title_activity_plan, ligne.lettre, ligne.nom));

		final Bundle bundle = new Bundle();
		bundle.putString(PlanFragment.PARAM_CODE_LIGNE, codeLigne);

		if (savedInstanceState == null) {
			addFragment(PlanFragment.class, bundle);
		}

	}
}

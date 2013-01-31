package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.fragment.impl.PlanFragment;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.manager.impl.LigneManager;
import android.os.Bundle;

public class PlanActivity extends OneFragmentActivity {

	public PlanActivity() {
		super(R.layout.activity_one_fragment);
	}

	public static enum Param implements IIntentParamKey {
		codeLigne
	}

	private String mCodeLigne;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mCodeLigne = (String) getParamValue(Param.codeLigne);

		final Bundle bundle = new Bundle();
		bundle.putString(PlanFragment.PARAM_CODE_LIGNE, mCodeLigne);

		if (savedInstanceState == null) {
			addFragment(PlanFragment.class, bundle);
		}

		final LigneManager ligneManager = LigneManager.getInstance();
		final Ligne ligne = ligneManager.getSingle(getContentResolver(), mCodeLigne);

		setTitle(getString(R.string.title_activity_plan, ligne.lettre, ligne.nom));
	}
}

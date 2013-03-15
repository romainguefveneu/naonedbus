package net.naonedbus.activity.impl;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.fragment.impl.ArretDetailFragment;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.SymbolesUtils;
import android.os.Bundle;

public class ArretDetailActivity extends OneFragmentActivity {

	private static final String LOG_TAG = "ArretDetailActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	public static final String PARAM_LIGNE = "ligne";
	public static final String PARAM_SENS = "sens";
	public static final String PARAM_ARRET = "arret";

	private HeaderHelper mHeaderHelper;

	public ArretDetailActivity() {
		super(R.layout.activity_horaires);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Arret arret = getIntent().getParcelableExtra(PARAM_ARRET);
		Ligne ligne = getIntent().getParcelableExtra(PARAM_LIGNE);
		Sens sens = getIntent().getParcelableExtra(PARAM_SENS);

		if (ligne == null) {
			final LigneManager ligneManager = LigneManager.getInstance();
			ligne = ligneManager.getSingle(getContentResolver(), arret.codeLigne);
		}
		if (sens == null) {
			final SensManager sensManager = SensManager.getInstance();
			sens = sensManager.getSingle(getContentResolver(), arret.codeLigne, arret.codeSens);
		}

		if (savedInstanceState == null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(ArretDetailFragment.PARAM_LIGNE, ligne);
			bundle.putParcelable(ArretDetailFragment.PARAM_SENS, sens);
			bundle.putParcelable(ArretDetailFragment.PARAM_ARRET, arret);

			addFragment(ArretDetailFragment.class, bundle);
		}

		mHeaderHelper = new HeaderHelper(this);
		mHeaderHelper.setBackgroundColor(ligne.couleurBackground, ligne.couleurTexte);
		mHeaderHelper.setCode(ligne.lettre);
		mHeaderHelper.setTitle(arret.nomArret);
		mHeaderHelper.setSubTitle(SymbolesUtils.formatSens(sens.text));
	}
}

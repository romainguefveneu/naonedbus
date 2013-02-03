package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.fragment.impl.HorairesFragment;
import net.naonedbus.fragment.impl.HorairesFragment.OnSensChangeListener;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.SymbolesUtils;
import android.os.Bundle;

public class HoraireActivity extends OneFragmentActivity implements OnSensChangeListener {

	public static enum Param implements IIntentParamKey {
		idArret
	};

	private HeaderHelper mHeaderHelper;

	public HoraireActivity() {
		super(R.layout.activity_horaires);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		int idArret = (Integer) getParamValue(Param.idArret);

		final Bundle bundle = new Bundle();
		bundle.putInt(HorairesFragment.PARAM_ID_ARRET, idArret);

		if (savedInstanceState == null) {
			addFragment(HorairesFragment.class, bundle);
			((HorairesFragment) getCurrentFragment()).setOnChangeSensListener(this);
		}

		final SensManager sensManager = SensManager.getInstance();
		final LigneManager ligneManager = LigneManager.getInstance();
		final ArretManager arretManager = ArretManager.getInstance();

		final Arret arret = arretManager.getSingle(this.getContentResolver(), idArret);
		final Sens sens = sensManager.getSingle(this.getContentResolver(), arret.codeLigne, arret.codeSens);
		final Ligne ligne = ligneManager.getSingle(this.getContentResolver(), arret.codeLigne);

		mHeaderHelper = new HeaderHelper(this);
		mHeaderHelper.setBackgroundColor(ligne.couleurBackground, ligne.couleurTexte);
		mHeaderHelper.setCode(ligne.lettre);
		mHeaderHelper.setTitle(arret.nomArret);
		mHeaderHelper.setSubTitle(SymbolesUtils.formatSens(sens.text));
	}

	@Override
	public void onSensChange(Sens newSens) {
		mHeaderHelper.setSubTitleAnimated(SymbolesUtils.formatSens(newSens.text));
	}

}

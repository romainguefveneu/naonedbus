package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.fragment.impl.HorairesFragment;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.SymbolesUtils;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HoraireActivity extends OneFragmentActivity {

	public static enum Param implements IIntentParamKey {
		idArret
	};

	public HoraireActivity() {
		super(R.layout.activity_horaires);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int idArret = (Integer) getParamValue(Param.idArret);

		final Bundle bundle = new Bundle();
		bundle.putInt(HorairesFragment.PARAM_ID_ARRET, idArret);

		if (savedInstanceState == null) {
			addFragment(HorairesFragment.class, bundle);
		}

		final Typeface robotoLight = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");

		final SensManager sensManager = SensManager.getInstance();
		final LigneManager ligneManager = LigneManager.getInstance();
		final ArretManager arretManager = ArretManager.getInstance();

		final Arret arret = arretManager.getSingle(this.getContentResolver(), idArret);
		final Sens sens = sensManager.getSingle(this.getContentResolver(), arret.codeLigne, arret.codeSens);
		final Ligne ligne = ligneManager.getSingle(this.getContentResolver(), arret.codeLigne);

		final View header = findViewById(R.id.ligneDialogHeader);
		header.setBackgroundDrawable(ColorUtils.getGradiant(ligne.couleurBackground));

		final TextView code = (TextView) findViewById(R.id.itemCode);
		code.setText(ligne.lettre);
		code.setTextColor(ligne.couleurTexte);
		code.setTypeface(robotoLight);

		final TextView title = (TextView) findViewById(R.id.itemTitle);
		title.setText(arret.nom);
		title.setTextColor(ligne.couleurTexte);
		title.setTypeface(robotoLight);

		getSupportActionBar().setTitle(SymbolesUtils.formatSens(sens.text));
	}

}

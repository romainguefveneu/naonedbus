package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.SimpleFragmentActivity;
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

import com.actionbarsherlock.app.ActionBar;

public class HoraireActivity extends SimpleFragmentActivity {

	public static enum Param implements IIntentParamKey {
		idArret
	};

	private static int[] titles = new int[] { R.string.title_fragment_arrets };

	private static Class<?>[] classes = new Class<?>[] { HorairesFragment.class };

	private Bundle[] bundles;

	public HoraireActivity() {
		super(R.layout.activity_horaires);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int idArret = (Integer) getParamValue(Param.idArret);

		final Bundle bundle = new Bundle();
		bundle.putInt(HorairesFragment.PARAM_ID_ARRET, idArret);

		bundles = new Bundle[1];
		bundles[0] = bundle;

		if (savedInstanceState == null) {
			addFragments(titles, classes, bundles);
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
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

		final TextView code = (TextView) findViewById(R.id.ligneDialogCode);
		code.setText(ligne.lettre);
		code.setTextColor(ligne.couleurTexte);
		code.setTypeface(robotoLight);

		final TextView title = (TextView) findViewById(R.id.itemTitle);
		title.setText(arret.nom);
		title.setTextColor(ligne.couleurTexte);
		title.setTypeface(robotoLight);

		final TextView subTitle = (TextView) findViewById(R.id.itemSubTitle);
		subTitle.setText(SymbolesUtils.formatSens(sens.text));
		subTitle.setTextColor(ligne.couleurTexte);
		subTitle.setTypeface(robotoLight);

	}

}

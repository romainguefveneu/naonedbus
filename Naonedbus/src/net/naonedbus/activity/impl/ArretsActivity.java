package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.SimpleFragmentActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.fragment.impl.ArretsFragment;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.ColorUtils;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;

public class ArretsActivity extends SimpleFragmentActivity {

	public static enum Param implements IIntentParamKey {
		idSens
	};

	private static int[] titles = new int[] { R.string.title_fragment_arrets };
	private static Class<?>[] classes = new Class<?>[] { ArretsFragment.class };
	private Bundle[] bundles;

	public ArretsActivity() {
		super(R.layout.activity_arrets);
	}

	@SuppressWarnings("deprecation")
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
			getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}

		final Typeface robotoLight = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");

		final SensManager sensManager = SensManager.getInstance();
		final LigneManager ligneManager = LigneManager.getInstance();

		final Sens sens = sensManager.getSingle(this.getContentResolver(), idSens);
		final Ligne ligne = ligneManager.getSingle(this.getContentResolver(), sens.codeLigne);

		final View header = findViewById(R.id.ligneDialogHeader);
		header.setBackgroundDrawable(ColorUtils.getGradiant(ligne.couleurBackground));

		final TextView code = (TextView) findViewById(R.id.ligneDialogCode);
		code.setText(ligne.lettre);
		code.setTextColor(ligne.couleurTexte);
		code.setTypeface(robotoLight);

		final TextView sensTitle = (TextView) findViewById(R.id.itemTitle);
		sensTitle.setText(sens.text);
		sensTitle.setTextColor(ligne.couleurTexte);
		sensTitle.setTypeface(robotoLight);
	}

}

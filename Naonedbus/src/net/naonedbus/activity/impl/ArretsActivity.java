package net.naonedbus.activity.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.fragment.impl.ArretsFragment;
import net.naonedbus.helper.StateHelper;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.widget.adapter.impl.SensSpinnerAdapter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.espian.showcaseview.ShowcaseView;

public class ArretsActivity extends OneFragmentActivity {

	public static enum Param implements IIntentParamKey {
		codeLigne
	};

	public static interface OnChangeSens {
		void onChangeSens(Sens sens);
	}

	private StateHelper mStateHelper;
	private OnChangeSens mOnChangeSens;
	private String mCodeLigne;
	private Sens mCurrentSens;

	public ArretsActivity() {
		super(R.layout.activity_arrets);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		final SensManager sensManager = SensManager.getInstance();
		final LigneManager ligneManager = LigneManager.getInstance();

		mStateHelper = new StateHelper(getApplicationContext());

		mCodeLigne = (String) getParamValue(Param.codeLigne);

		final List<Sens> sensList = sensManager.getAll(getContentResolver(), mCodeLigne);
		final int idSens = mStateHelper.getSens(mCodeLigne, sensList.get(0)._id);

		final Sens sens = sensManager.getSingle(this.getContentResolver(), idSens);
		final Ligne ligne = ligneManager.getSingle(this.getContentResolver(), sens.codeLigne);

		final Bundle bundle = new Bundle();
		bundle.putInt(ArretsFragment.PARAM_ID_LIGNE, ligne._id);

		if (savedInstanceState == null) {
			addFragment(ArretsFragment.class, bundle);
		}

		final Typeface robotoLight = FontUtils.getRobotoLight(getApplicationContext());
		final Typeface robotoMedium = FontUtils.getRobotoMedium(getApplicationContext());

		final View header = findViewById(R.id.headerView);
		header.setBackgroundDrawable(ColorUtils.getGradiant(ligne.couleurBackground));

		final TextView code = (TextView) findViewById(R.id.itemCode);
		code.setText(ligne.lettre);
		code.setTextColor(ligne.couleurTexte);
		code.setTypeface(robotoMedium);

		final Spinner sensTitle = (Spinner) findViewById(R.id.itemTitle);
		sensTitle.setAdapter(new SensSpinnerAdapter(this, sensList, ligne.couleurTexte, robotoLight));
		sensTitle.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int location, long arg3) {
				final Sens sens = sensList.get(location);
				mOnChangeSens.onChangeSens(sens);
				mCurrentSens = sens;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});
		sensTitle.setSelection(getSensPosition(sensList, idSens));

		final ShowcaseView sv = (ShowcaseView) findViewById(R.id.showcase);
		sv.setShowcaseView(sensTitle);
		sv.setShotType(ShowcaseView.TYPE_ONE_SHOT);
		sv.show();

		sensTitle.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				sv.hide();
				return false;
			}
		});
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mOnChangeSens = (OnChangeSens) getCurrentFragment();
	}

	@Override
	protected void onStop() {
		mStateHelper.setSens(mCodeLigne, mCurrentSens._id);
		super.onStop();
	}

	private int getSensPosition(List<Sens> sensList, int id) {
		Sens sens;
		for (int i = 0; i < sensList.size(); i++) {
			sens = sensList.get(i);
			if (sens._id == id)
				return i;
		}
		return 0;
	}

}

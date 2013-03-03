package net.naonedbus.activity.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.OneFragmentActivity;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.fragment.impl.ArretsFragment;
import net.naonedbus.helper.StateHelper;
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

	public static final String PARAM_LIGNE = "ligne";

	public static interface OnChangeSens {
		void onChangeSens(Sens sens);
	}

	private StateHelper mStateHelper;
	private OnChangeSens mOnChangeSens;
	private Ligne mLigne;
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

		mStateHelper = new StateHelper(getApplicationContext());

		mLigne = getIntent().getParcelableExtra(PARAM_LIGNE);

		final List<Sens> sensList = sensManager.getAll(getContentResolver(), mLigne.code);
		final int idSens = mStateHelper.getSens(mLigne.code, sensList.get(0)._id);

		if (savedInstanceState == null) {
			final Bundle bundle = new Bundle();
			bundle.putParcelable(ArretsFragment.PARAM_LIGNE, mLigne);

			addFragment(ArretsFragment.class, bundle);
		}

		final Typeface robotoBold = FontUtils.getRobotoBoldCondensed(getApplicationContext());

		final View header = findViewById(R.id.headerView);
		header.setBackgroundDrawable(ColorUtils.getGradiant(mLigne.couleurBackground));

		final TextView code = (TextView) findViewById(R.id.itemCode);
		code.setText(mLigne.lettre);
		code.setTextColor(mLigne.couleurTexte);
		code.setTypeface(robotoBold);

		final Spinner sensTitle = (Spinner) findViewById(R.id.itemTitle);
		sensTitle.setAdapter(new SensSpinnerAdapter(this, sensList, mLigne.couleurTexte, robotoBold));
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
		if (mCurrentSens != null)
			mStateHelper.setSens(mLigne.code, mCurrentSens._id);
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

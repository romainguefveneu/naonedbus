package net.naonedbus.fragment.impl;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.card.Card;
import net.naonedbus.card.impl.HoraireCard;
import net.naonedbus.card.impl.SimpleCard;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;

public class ArretDetailFragment extends Fragment {
	private static final String LOG_TAG = "ArretDetailFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	public static final String PARAM_LIGNE = "ligne";
	public static final String PARAM_SENS = "sens";
	public static final String PARAM_ARRET = "arret";

	private Ligne mLigne;
	private Sens mSens;
	private Arret mArret;

	private ViewGroup mViewGroup;

	public ArretDetailFragment() {
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		final View view = inflater.inflate(R.layout.fragment_arret_detail, container, false);
		mViewGroup = (ViewGroup) view.findViewById(android.R.id.content);

		final Animation slide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_from_bottom);
		final LayoutAnimationController controller = new LayoutAnimationController(slide);
		controller.setInterpolator(new DecelerateInterpolator());
		controller.setDelay(0.3f);
		mViewGroup.setLayoutAnimation(controller);

		return view;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLigne = getArguments().getParcelable(PARAM_LIGNE);
		mSens = getArguments().getParcelable(PARAM_SENS);
		mArret = getArguments().getParcelable(PARAM_ARRET);

		new CardLoader().execute();
	}

	private class CardLoader extends AsyncTask<Void, View, Void> {

		@Override
		protected Void doInBackground(final Void... params) {

			final Card horaireCard = new HoraireCard(getActivity(), mArret);
			publishProgress(horaireCard.getView(getActivity(), mViewGroup));

			final Card helloWorldCard = new SimpleCard("Hello World !", "Bonjour le monde !");

			publishProgress(helloWorldCard.getView(getActivity(), mViewGroup));
			publishProgress(helloWorldCard.getView(getActivity(), mViewGroup));
			publishProgress(helloWorldCard.getView(getActivity(), mViewGroup));

			return null;
		}

		@Override
		protected void onProgressUpdate(final View... values) {
			for (final View view : values) {
				mViewGroup.postDelayed(new Runnable() {
					@Override
					public void run() {
						mViewGroup.addView(view);
					}
				}, 100);
			}
		}

	}
}

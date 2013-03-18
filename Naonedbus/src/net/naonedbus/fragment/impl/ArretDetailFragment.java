package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.card.Card;
import net.naonedbus.card.impl.HoraireCard;
import net.naonedbus.card.impl.TraficCard;
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

	private final List<String> mFragmentsTags;
	private boolean isNew;

	private Ligne mLigne;
	private Sens mSens;
	private Arret mArret;

	private ViewGroup mViewGroup;

	public ArretDetailFragment() {
		mFragmentsTags = new ArrayList<String>();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLigne = getArguments().getParcelable(PARAM_LIGNE);
		mSens = getArguments().getParcelable(PARAM_SENS);
		mArret = getArguments().getParcelable(PARAM_ARRET);

		isNew = (savedInstanceState == null);

		setRetainInstance(true);
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
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Card horaireCard = new HoraireCard(getActivity(), getLoaderManager(), mArret);
		final Card traficCard = new TraficCard(getActivity(), getLoaderManager(), mLigne);

		mViewGroup.addView(horaireCard.getView(mViewGroup));
		mViewGroup.addView(traficCard.getView(mViewGroup));
	}

}

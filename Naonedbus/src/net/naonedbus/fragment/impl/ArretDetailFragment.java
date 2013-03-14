package net.naonedbus.fragment.impl;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.card.Card;
import net.naonedbus.card.impl.SimpleCard;
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

	private ViewGroup mViewGroup;

	public ArretDetailFragment() {
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Card helloWorldCard = new SimpleCard("Hello World !", "Bonjour le monde !");

		final Card helloWorldCard2 = new SimpleCard("Hello World 2 !", "Bonjour le monde 2 !\nBonjour le monde 2 !");

		mViewGroup.addView(helloWorldCard.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard2.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard2.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard2.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard2.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard2.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard.getView(getActivity(), mViewGroup));
		mViewGroup.addView(helloWorldCard2.getView(getActivity(), mViewGroup));

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
}

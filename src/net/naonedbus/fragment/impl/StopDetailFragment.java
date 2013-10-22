/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.fragment.impl;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.SendNewsActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.Direction;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Stop;
import net.naonedbus.bean.StopBookmark;
import net.naonedbus.card.Card;
import net.naonedbus.card.impl.CommentairesCard;
import net.naonedbus.card.impl.HoraireCard;
import net.naonedbus.card.impl.MapCard;
import net.naonedbus.card.impl.TraficCard;
import net.naonedbus.manager.impl.DirectionManager;
import net.naonedbus.manager.impl.StopBookmarkManager;
import net.naonedbus.manager.impl.StopManager;
import net.naonedbus.provider.impl.MyLocationProvider;

import org.joda.time.DateMidnight;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class StopDetailFragment extends SherlockFragment {

	public static interface OnDirectionChangedListener {
		void onDirectionChanged(Direction newDirection);
	}

	public static interface OnStopChangedListener {
		void onStopChanged(Stop newStop);
	}

	public static final String PARAM_ROUTE = "route";
	public static final String PARAM_DIRECTION = "direction";
	public static final String PARAM_STOP = "stop";

	private Route mRoute;
	private Direction mDirection;
	private Stop mStop;

	private final StopManager mStopManager;
	private final DirectionManager mDirectionManager;
	private final StopBookmarkManager mStopBookmarkManager;
	private final List<OnDirectionChangedListener> mOnDirectionChangedListeners;
	private OnStopChangedListener mOnStopChangedListener;

	private ViewGroup mViewGroup;
	private final List<Card<?>> mCards;

	public StopDetailFragment() {
		mStopBookmarkManager = StopBookmarkManager.getInstance();
		mStopManager = StopManager.getInstance();
		mDirectionManager = DirectionManager.getInstance();

		mCards = new ArrayList<Card<?>>();
		mOnDirectionChangedListeners = new ArrayList<StopDetailFragment.OnDirectionChangedListener>();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mRoute = getArguments().getParcelable(PARAM_ROUTE);
		mDirection = getArguments().getParcelable(PARAM_DIRECTION);
		mStop = getArguments().getParcelable(PARAM_STOP);

		setHasOptionsMenu(true);

		final HoraireCard scheduleCard = new HoraireCard(getActivity(), getLoaderManager(), getChildFragmentManager(),
				mStop);
		final TraficCard tanNewsCard = new TraficCard(getActivity(), getLoaderManager(), getChildFragmentManager(),
				mRoute);
		final CommentairesCard liveNewsCard = new CommentairesCard(getActivity(), getLoaderManager(),
				getChildFragmentManager());
		liveNewsCard.setLigne(mRoute);
		liveNewsCard.setSens(mDirection);
		liveNewsCard.setArret(mStop);

		final MapCard mapCard = new MapCard(getActivity(), getLoaderManager(), getChildFragmentManager(),
				mStop.getLatitude(), mStop.getLongitude());
		final MyLocationProvider locationProvider = NBApplication.getLocationProvider();
		mapCard.setCurrentLocation(locationProvider.getLastKnownLocation());

		mCards.add(scheduleCard);
		mCards.add(tanNewsCard);
		mCards.add(liveNewsCard);
		mCards.add(mapCard);

		mOnStopChangedListener = scheduleCard;
		mOnDirectionChangedListeners.add(liveNewsCard);
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		mOnDirectionChangedListeners.add((OnDirectionChangedListener) activity);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_stop_detail, menu);
		final MenuItem menuFavori = menu.findItem(R.id.menu_favori);

		final int icon = isFavori() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important;
		menuFavori.setIcon(icon);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		final MenuItem menuFavori = menu.findItem(R.id.menu_favori);

		final int icon = isFavori() ? R.drawable.ic_action_important : R.drawable.ic_action_not_important;
		menuFavori.setIcon(icon);

		super.onPrepareOptionsMenu(menu);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		final View view = inflater.inflate(R.layout.fragment_stop_detail, container, false);
		mViewGroup = (ViewGroup) view.findViewById(android.R.id.content);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			final LayoutTransition layoutTransition = mViewGroup.getLayoutTransition();
			layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		}

		final Animation slide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_from_bottom);
		final LayoutAnimationController controller = new LayoutAnimationController(slide);
		controller.setInterpolator(new DecelerateInterpolator());
		controller.setDelay(0.1f);
		mViewGroup.setLayoutAnimation(controller);

		return view;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		for (final Card<?> card : mCards) {
			mViewGroup.addView(card.getView(mViewGroup));
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		for (final Card<?> card : mCards) {
			card.setContext(getActivity());
			card.onStart();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		for (final Card<?> card : mCards) {
			card.onResume();
		}
	}

	@Override
	public void onPause() {
		for (final Card<?> card : mCards) {
			card.onPause();
		}
		super.onPause();
	}

	@Override
	public void onStop() {
		for (final Card<?> card : mCards) {
			card.onStop();
		}
		super.onStop();
	}

	@Override
	public void onDestroy() {
		for (final Card<?> card : mCards) {
			card.onDestroy();
		}
		super.onDestroy();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_favori:
			onStarClick();
			break;
		case R.id.menu_place:
			showArretPlan();
			break;
		case R.id.menu_comment:
			menuComment();
			break;
		case R.id.menu_show_plan:
			menuShowPlan();
			break;
		case R.id.menu_sens:
			menuChangeSens();
			break;
		default:
			break;
		}
		return false;
	}

	private boolean isFavori() {
		final StopBookmark item = mStopBookmarkManager.getSingle(getActivity().getContentResolver(), mStop.getId());
		return (item != null);
	}

	@SuppressLint("NewApi")
	private void onStarClick() {
		if (isFavori()) {
			removeFromFavoris();
			Toast.makeText(getActivity(), R.string.toast_favori_retire, Toast.LENGTH_SHORT).show();
		} else {
			addToFavoris();
			Toast.makeText(getActivity(), R.string.toast_favori_ajout, Toast.LENGTH_SHORT).show();
		}

		getSherlockActivity().invalidateOptionsMenu();
	}

	private void addToFavoris() {
		mStopBookmarkManager.addFavori(getActivity().getContentResolver(), mStop);
	}

	private void removeFromFavoris() {
		mStopBookmarkManager.removeFavori(getActivity().getContentResolver(), mStop.getId());
	}

	protected void showArretPlan() {
		final Intent intent = new Intent(getActivity(), MapActivity.class);
		intent.putExtra(MapFragment.PARAM_ITEM_ID, mStop.getIdStation());
		intent.putExtra(MapFragment.PARAM_ITEM_TYPE, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	private void menuComment() {
		final Intent intent = new Intent(getActivity(), SendNewsActivity.class);
		intent.putExtra(SendNewsActivity.PARAM_LIGNE, mRoute);
		intent.putExtra(SendNewsActivity.PARAM_SENS, mDirection);
		intent.putExtra(SendNewsActivity.PARAM_ARRET, mStop);
		startActivity(intent);
	}

	private void menuShowPlan() {
		final Intent intent = new Intent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.PARAM_CODE_LIGNE, mRoute.getCode());
		startActivity(intent);
	}

	@SuppressLint("NewApi")
	private void menuChangeSens() {
		Direction autreSens = null;

		// Inverser le direction
		final List<Direction> direction = mDirectionManager.getAll(getActivity().getContentResolver(), mRoute.getCode());
		for (final Direction sensItem : direction) {
			if (sensItem.getId() != mDirection.getId()) {
				autreSens = sensItem;
				break;
			}
		}

		if (autreSens != null) {
			// Chercher l'arrêt dans le nouveau direction
			final Stop stop = mStopManager.getSingle(getActivity().getContentResolver(), mRoute.getCode(),
					autreSens.getCode(), mStop.getNormalizedNom());

			if (stop != null) {
				mDirection = autreSens;
				mStop = stop;

				for (final OnDirectionChangedListener listener : mOnDirectionChangedListeners) {
					listener.onDirectionChanged(mDirection);
				}
				mOnStopChangedListener.onStopChanged(mStop);

				getSherlockActivity().invalidateOptionsMenu();
				return;
			}
		}
		Toast.makeText(getActivity(), getString(R.string.toast_autre_sens), Toast.LENGTH_SHORT).show();
	}

	/**
	 * Clear data and reload with a new date
	 * 
	 * @param date
	 */
	public void changeDate(final DateMidnight date) {
	}

}

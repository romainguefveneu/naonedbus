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

import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.activity.impl.MapActivity;
import net.naonedbus.activity.impl.PlanActivity;
import net.naonedbus.activity.map.overlay.TypeOverlayItem;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Favori;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.card.Card;
import net.naonedbus.card.impl.CommentairesCard;
import net.naonedbus.card.impl.HoraireCard;
import net.naonedbus.card.impl.TraficCard;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.manager.impl.SensManager;

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

public class ArretDetailFragment extends SherlockFragment {

	public static interface OnSensChangeListener {
		void onSensChange(Sens newSens);
	}

	public static interface OnArretChangeListener {
		void onArretChange(Arret newArret);
	}

	public static final String PARAM_LIGNE = "ligne";
	public static final String PARAM_SENS = "sens";
	public static final String PARAM_ARRET = "arret";

	private Ligne mLigne;
	private Sens mSens;
	private Arret mArret;

	private final ArretManager mArretManager;
	private final SensManager mSensManager;
	private final FavoriManager mFavoriManager;
	private final List<OnSensChangeListener> mOnSensChangeListeners;
	private OnArretChangeListener mOnArretChangeListener;

	private ViewGroup mViewGroup;
	private final List<Card<?>> mCards;

	public ArretDetailFragment() {
		mFavoriManager = FavoriManager.getInstance();
		mArretManager = ArretManager.getInstance();
		mSensManager = SensManager.getInstance();

		mCards = new ArrayList<Card<?>>();
		mOnSensChangeListeners = new ArrayList<ArretDetailFragment.OnSensChangeListener>();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mLigne = getArguments().getParcelable(PARAM_LIGNE);
		mSens = getArguments().getParcelable(PARAM_SENS);
		mArret = getArguments().getParcelable(PARAM_ARRET);

		setRetainInstance(true);
		setHasOptionsMenu(true);

		final HoraireCard horaireCard = new HoraireCard(getActivity(), getLoaderManager(), mArret);
		final TraficCard traficCard = new TraficCard(getActivity(), getLoaderManager(), mLigne);
		final CommentairesCard commentairesCard = new CommentairesCard(getActivity(), getLoaderManager());
		commentairesCard.setLigne(mLigne);
		commentairesCard.setSens(mSens);
		commentairesCard.setArret(mArret);

		// final MapCard mapCard = new MapCard(getActivity(),
		// getLoaderManager(), mArret.latitude, mArret.longitude);
		// final MyLocationProvider locationProvider =
		// NBApplication.getLocationProvider();
		// mapCard.setCurrentLocation(locationProvider.getLastKnownLocation());

		mCards.add(horaireCard);
		mCards.add(traficCard);
		mCards.add(commentairesCard);
		// mCards.add(mapCard);

		mOnArretChangeListener = horaireCard;
		mOnSensChangeListeners.add(commentairesCard);
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		mOnSensChangeListeners.add((OnSensChangeListener) activity);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.fragment_arret_detail, menu);
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

		final View view = inflater.inflate(R.layout.fragment_arret_detail, container, false);
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
		final Favori item = mFavoriManager.getSingle(getActivity().getContentResolver(), mArret._id);
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
		mFavoriManager.addFavori(getActivity().getContentResolver(), mArret);
	}

	private void removeFromFavoris() {
		mFavoriManager.removeFavori(getActivity().getContentResolver(), mArret._id);
	}

	protected void showArretPlan() {
		final ParamIntent intent = new ParamIntent(getActivity(), MapActivity.class);
		intent.putExtra(MapActivity.Param.itemId, mArret.idStation);
		intent.putExtra(MapActivity.Param.itemType, TypeOverlayItem.TYPE_STATION.getId());
		startActivity(intent);
	}

	private void menuComment() {
		final Intent intent = new Intent(getActivity(), CommentaireActivity.class);
		intent.putExtra(CommentaireActivity.PARAM_LIGNE, mLigne);
		intent.putExtra(CommentaireActivity.PARAM_SENS, mSens);
		intent.putExtra(CommentaireActivity.PARAM_ARRET, mArret);
		startActivity(intent);
	}

	private void menuShowPlan() {
		final Intent intent = new Intent(getActivity(), PlanActivity.class);
		intent.putExtra(PlanActivity.PARAM_CODE_LIGNE, mLigne.code);
		startActivity(intent);
	}

	@SuppressLint("NewApi")
	private void menuChangeSens() {
		Sens autreSens = null;

		// Inverser le sens
		final List<Sens> sens = mSensManager.getAll(getActivity().getContentResolver(), mLigne.code);
		for (final Sens sensItem : sens) {
			if (sensItem._id != mSens._id) {
				autreSens = sensItem;
				break;
			}
		}

		if (autreSens != null) {
			// Chercher l'arrêt dans le nouveau sens
			final Arret arret = mArretManager.getSingle(getActivity().getContentResolver(), mLigne.code,
					autreSens.code, mArret.normalizedNom);

			if (arret != null) {
				mSens = autreSens;
				mArret = arret;

				for (final OnSensChangeListener listener : mOnSensChangeListeners) {
					listener.onSensChange(mSens);
				}
				mOnArretChangeListener.onArretChange(mArret);

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

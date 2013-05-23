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

import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.helper.ItineraryViewHelper;
import net.naonedbus.loader.ItineraryLoader;
import net.naonedbus.widget.adapter.impl.AddressArrayAdapter;
import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.MenuItem;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import fr.ybo.opentripplanner.client.modele.Itinerary;

public class ItineraireFragment extends CustomFragment implements LoaderCallbacks<AsyncResult<List<Itinerary>>> {

	private static final String TAG = "ItineraireFragment";
	private static final boolean DBG = BuildConfig.DEBUG;

	private AutoCompleteTextView mFromTextView;
	private AutoCompleteTextView mToTextView;

	private ItineraryViewHelper mItineraryViewHelper;

	private LocationEditManager mFromLocationEditManager;
	private LocationEditManager mToLocationEditManager;

	private Button mGoButton;
	private ProgressBar mProgressBar;
	private LinearLayout mListContainer;

	private boolean mListShown;

	private final OnLocationEditChange mOnLocationChange = new OnLocationEditChange() {

		@Override
		public void onLocationNotFound() {
			mGoButton.setEnabled(false);
		}

		@Override
		public void onLocationFound() {
			mGoButton.setEnabled(mFromLocationEditManager.hasLocation() && mToLocationEditManager.hasLocation());
		}
	};

	public ItineraireFragment() {
		super(R.layout.fragment_itineraire);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(false);
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		mItineraryViewHelper = new ItineraryViewHelper(activity);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		mFromLocationEditManager.saveInstanceState(outState);
		mToLocationEditManager.saveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void bindView(final View view, final Bundle savedInstanceState) {
		mGoButton = (Button) view.findViewById(android.R.id.button1);
		mGoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				sendRequest();
			}
		});

		mProgressBar = (ProgressBar) view.findViewById(android.R.id.progress);
		mListContainer = (LinearLayout) view.findViewById(android.R.id.list);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			final LayoutTransition layoutTransition = mListContainer.getLayoutTransition();
			layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
		}
		final Animation slide = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_from_bottom);
		final LayoutAnimationController controller = new LayoutAnimationController(slide);
		controller.setInterpolator(new DecelerateInterpolator());
		controller.setDelay(0.1f);
		mListContainer.setLayoutAnimation(controller);

		mFromTextView = (AutoCompleteTextView) view.findViewById(R.id.itineraireFrom);
		mToTextView = (AutoCompleteTextView) view.findViewById(R.id.itineraireTo);

		setDecoratedHint(mFromTextView, getString(R.string.search_hint_itineraire_depart));
		setDecoratedHint(mToTextView, getString(R.string.search_hint_itineraire_arrivee));

		mFromLocationEditManager = new LocationEditManager(mFromTextView, mOnLocationChange, savedInstanceState);
		mToLocationEditManager = new LocationEditManager(mToTextView, mOnLocationChange, savedInstanceState);

		mFromTextView.setAdapter(new AddressArrayAdapter(getActivity()));
		mToTextView.setAdapter(new AddressArrayAdapter(getActivity()));

	}

	private void setDecoratedHint(final TextView textview, final CharSequence hintText) {
		final SpannableStringBuilder ssb = new SpannableStringBuilder("   "); // for
																				// the
																				// icon
		ssb.append(hintText);

		final Drawable searchIcon = getActivity().getResources().getDrawable(R.drawable.ic_action_location_gray);
		final int textSize = (int) (textview.getTextSize() * 1.25);
		searchIcon.setBounds(0, 0, textSize, textSize);
		ssb.setSpan(new ImageSpan(searchIcon), 1, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

		textview.setHint(ssb);
	}

	private void sendRequest() {
		final Bundle bundle = new Bundle();
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LATITUDE, mFromLocationEditManager.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_FROM_LONGITUDE, mFromLocationEditManager.getLongitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LATITUDE, mToLocationEditManager.getLatitude());
		bundle.putDouble(ItineraryLoader.PARAM_TO_LONGITUDE, mToLocationEditManager.getLongitude());

		setListShown(false, false);
		getLoaderManager().initLoader(0, bundle, this);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return false;
	}

	public interface OnLocationEditChange {
		void onLocationFound();

		void onLocationNotFound();
	}

	private static class LocationEditManager implements TextWatcher, OnItemClickListener {

		private static final String BUNDLE_LATITUDE = "LocationEditManager:latitude";
		private static final String BUNDLE_LONGITUDE = "LocationEditManager:longitude";

		private final AutoCompleteTextView mAutoCompleteTextView;
		private final OnLocationEditChange mOnLocationEditChange;
		private double mLatitude;
		private double mLongitude;
		private boolean mFirstUse = true;

		public LocationEditManager(final AutoCompleteTextView autoCompleteTextView,
				final OnLocationEditChange onLocationEditChange, final Bundle saveInstanceState) {
			mAutoCompleteTextView = autoCompleteTextView;
			mAutoCompleteTextView.addTextChangedListener(this);
			mAutoCompleteTextView.setOnItemClickListener(this);

			mOnLocationEditChange = onLocationEditChange;

			if (saveInstanceState != null) {
				mLatitude = saveInstanceState.getDouble(getBundleKeyLatitude(), 0);
				mLongitude = saveInstanceState.getDouble(getBundleKeyLongitude(), 0);
				if (mLatitude != 0 && mLongitude != 0) {
					mAutoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
							R.drawable.ic_checkmark_holo_light, 0);
				}
			}
		}

		private String getBundleKeyLatitude() {
			return BUNDLE_LATITUDE + mAutoCompleteTextView.getId();
		}

		private String getBundleKeyLongitude() {
			return BUNDLE_LONGITUDE + mAutoCompleteTextView.getId();
		}

		public void saveInstanceState(final Bundle outState) {
			outState.putDouble(getBundleKeyLatitude(), mLatitude);
			outState.putDouble(getBundleKeyLongitude(), mLongitude);
		}

		public boolean hasLocation() {
			return mLatitude != 0;
		}

		public double getLatitude() {
			return mLatitude;
		}

		public double getLongitude() {
			return mLongitude;
		}

		@Override
		public void afterTextChanged(final Editable s) {

		}

		@Override
		public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

		}

		@Override
		public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
			mLatitude = 0;
			if (mFirstUse == false) {
				mAutoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
						R.drawable.ic_dialog_alert_holo_light, 0);
			}
			mOnLocationEditChange.onLocationNotFound();
		}

		@Override
		public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
			final Address address = (Address) adapter.getItemAtPosition(position);
			mLatitude = address.getLatitude();
			mLongitude = address.getLongitude();
			mAutoCompleteTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_checkmark_holo_light, 0);
			mOnLocationEditChange.onLocationFound();
			mFirstUse = false;
		}
	}

	@Override
	public Loader<AsyncResult<List<Itinerary>>> onCreateLoader(final int idLoader, final Bundle bundle) {
		return new ItineraryLoader(getActivity(), bundle);
	}

	@Override
	public void onLoadFinished(final Loader<AsyncResult<List<Itinerary>>> loader,
			final AsyncResult<List<Itinerary>> result) {
		if (DBG)
			Log.d(TAG, "onLoadFinished");

		mListContainer.removeAllViews();

		if (result.getException() != null) {
			final Exception e = result.getException();
			Crouton.makeText(getActivity(), e.getLocalizedMessage(), Style.ALERT).show();

			e.printStackTrace();
		} else {
			final List<Itinerary> itineraries = result.getResult();
			if (itineraries != null) {
				Crouton.makeText(getActivity(), itineraries.size() + " itinéraires", Style.INFO).show();

				for (final Itinerary itinerary : itineraries) {
					final View view = mItineraryViewHelper.createItineraryView(itinerary, mListContainer);
					mListContainer.addView(view);
				}
			} else {
				Crouton.makeText(getActivity(), "Aucun itinéraire", Style.ALERT).show();
			}
		}
		setListShown(true, false);

	}

	@Override
	public void onLoaderReset(final Loader<AsyncResult<List<Itinerary>>> loader) {

	}

	/**
	 * Control whether the list is being displayed. You can make it not
	 * displayed if you are waiting for the initial data to show in it. During
	 * this time an indeterminant progress indicator will be shown instead.
	 * 
	 * @param shown
	 *            If true, the list view is shown; if false, the progress
	 *            indicator. The initial value is true.
	 * @param animate
	 *            If true, an animation will be used to transition to the new
	 *            state.
	 */
	private void setListShown(final boolean shown, final boolean animate) {
		if (DBG)
			Log.d(TAG, "setListShown(" + shown + "," + animate + ")");

		if (mProgressBar == null) {
			throw new IllegalStateException("Can't be used with a custom content view");
		}

		mListShown = shown;
		if (shown) {
			if (animate) {
				mProgressBar.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
			} else {
				mProgressBar.clearAnimation();
				mListContainer.clearAnimation();
			}
			mProgressBar.setVisibility(View.GONE);
			mListContainer.setVisibility(View.VISIBLE);
		} else {
			if (animate) {
				mProgressBar.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
				mListContainer.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
			} else {
				mProgressBar.clearAnimation();
				mListContainer.clearAnimation();
			}
			mProgressBar.setVisibility(View.VISIBLE);
			mListContainer.setVisibility(View.GONE);
		}
	}
}

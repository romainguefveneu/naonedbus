package net.naonedbus.activity.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.ItineraryWrapper;
import net.naonedbus.fragment.impl.ItineraryDetailFragment;
import net.naonedbus.utils.ColorUtils;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.astuetz.viewpager.extensions.PagerSlidingTabStrip;

public class ItineraryDetailActivity extends SherlockFragmentActivity {

	public static final String PARAM_BUNDLE = "bundle";
	public static final String PARAM_ITINERARIES = "itineraries";
	public static final String PARAM_FROM_PLACE = "fromPlace";
	public static final String PARAM_TO_PLACE = "toPlace";
	public static final String PARAM_DATE = "date";
	public static final String PARAM_SELECTED_INDEX = "selectedIndex";

	public static final String PARAM_ICON_FROM_COLOR = "iconFromColor";
	public static final String PARAM_ICON_TO_COLOR = "iconToColor";
	public static final String PARAM_ICON_FROM_RES = "iconFromRes";
	public static final String PARAM_ICON_TO_RES = "iconToRes";

	private ViewPager mViewPager;
	private PagerSlidingTabStrip mPagerSlidingTabStrip;

	private String mFrom;
	private String mTo;

	private int mIconPadding;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_itiniraries_detail);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mIconPadding = getResources().getDimensionPixelSize(R.dimen.itinerary_icon_padding);

		mViewPager = (ViewPager) findViewById(R.id.viewPager);
		mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);

		final Bundle bundle = getIntent().getBundleExtra(PARAM_BUNDLE);

		mFrom = bundle.getString(PARAM_FROM_PLACE);
		mTo = bundle.getString(PARAM_TO_PLACE);

		final List<ItineraryWrapper> wrappers = bundle.getParcelableArrayList(PARAM_ITINERARIES);
		final int selected = bundle.getInt(PARAM_SELECTED_INDEX);

		final TextView fromPlace = (TextView) findViewById(R.id.fromPlace);
		final TextView toPlace = (TextView) findViewById(R.id.toPlace);
		final TextView date = (TextView) findViewById(R.id.itemDate);

		fromPlace.setText(mFrom);
		toPlace.setText(mTo);
		date.setText(bundle.getString(PARAM_DATE));

		final int iconFromResId = bundle.getInt(PARAM_ICON_FROM_RES);
		final int iconToResId = bundle.getInt(PARAM_ICON_TO_RES);
		final int iconFromColor = bundle.getInt(PARAM_ICON_FROM_COLOR);
		final int iconToColor = bundle.getInt(PARAM_ICON_TO_COLOR);

		setIcon((ImageView) findViewById(R.id.formIconFrom), iconFromResId, iconFromColor);
		setIcon((ImageView) findViewById(R.id.formIconTo), iconToResId, iconToColor);

		mViewPager.setAdapter(new TabsAdapter(getSupportFragmentManager(), wrappers));
		mPagerSlidingTabStrip.setViewPager(mViewPager);
		mPagerSlidingTabStrip.setShouldExpand(!getResources().getBoolean(R.bool.isTablet));

		mViewPager.setCurrentItem(selected);
	}

	private void setIcon(final ImageView imageView, final int icoRes, final int color) {
		imageView.setImageResource(icoRes);
		if (color == Color.TRANSPARENT) {
			imageView.setBackgroundDrawable(null);
			imageView.setPadding(0, 0, 0, 0);
		} else {
			imageView.setBackgroundDrawable(ColorUtils.getRoundedGradiant(color));
			imageView.setPadding(mIconPadding, mIconPadding, mIconPadding, mIconPadding);
		}
	}

	/**
	 * Show the menu when home icon is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	public class TabsAdapter extends FragmentPagerAdapter {

		private final List<ItineraryWrapper> mWrappers;

		public TabsAdapter(final FragmentManager fm, final List<ItineraryWrapper> wrappers) {
			super(fm);
			mWrappers = wrappers;
		}

		@Override
		public Fragment getItem(final int position) {

			final Bundle bundle = new Bundle();
			bundle.putParcelable(ItineraryDetailFragment.PARAM_ITINERARY_WRAPPER, mWrappers.get(position));
			bundle.putString(ItineraryDetailFragment.PARAM_ITINERARY_FROM, mFrom);
			bundle.putString(ItineraryDetailFragment.PARAM_ITINERARY_TO, mTo);

			final Fragment fragment = Fragment.instantiate(ItineraryDetailActivity.this,
					ItineraryDetailFragment.class.getName(), bundle);
			fragment.setRetainInstance(true);
			return fragment;
		}

		@Override
		public int getCount() {
			return mWrappers.size();
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			return mWrappers.get(position).getTime();
		}

	}

	@Override
	public void finish() {
		super.finish();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			overridePendingTransition(R.anim.half_fade_in, R.anim.slide_out_to_right);
	}
}

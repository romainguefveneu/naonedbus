package net.naonedbus.activity;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TabsAdapter extends FragmentPagerAdapter implements ActionBar.TabListener, ViewPager.OnPageChangeListener {

	private final SherlockFragmentActivity mActivity;
	private final ViewPager mViewPager;
	private final ActionBar mActionBar;
	private final ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

	static final class TabInfo {
		private final Object tag;
		private final Class<?> clss;
		private final Bundle args;

		TabInfo(Object _tag, Class<?> _class, Bundle _args) {
			tag = _tag;
			clss = _class;
			args = _args;
		}
	}

	public TabsAdapter(SherlockFragmentActivity activity, ActionBar actionBar, ViewPager pager) {
		super(activity.getSupportFragmentManager());
		mActivity = activity;
		mActionBar = actionBar;
		mViewPager = pager;
		mViewPager.setAdapter(this);
		mViewPager.setOnPageChangeListener(this);
	}

	public void addTab(ActionBar.Tab tab, Class<?> clss, Bundle args) {
		final Object tag = tab.getText().toString();
		final TabInfo info = new TabInfo(tag, clss, args);

		tab.setTabListener(this);
		tab.setTag(tag);

		mTabs.add(info);
		// mActionBar.addTab(tab);

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mTabs.size();
	}

	@Override
	public Fragment getItem(int position) {
		TabInfo info = mTabs.get(position);
		return Fragment.instantiate(mActivity, info.clss.getName(), info.args);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}

	@Override
	public void onPageSelected(int position) {
//		mActionBar.setSelectedNavigationItem(position);
		mActivity.invalidateOptionsMenu();
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		final int position = tab.getPosition();
		final TabInfo tabInfo = mTabs.get(position);

		final Fragment fragment = mActivity.getSupportFragmentManager().findFragmentByTag(tabInfo.clss.getName());

		if (fragment.isAdded()) {
			ft.show(fragment);
		} else {
			ft.attach(fragment);
			ft.add(android.R.id.content, fragment);
		}

		mViewPager.setCurrentItem(tab.getPosition());
		mActivity.invalidateOptionsMenu();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}
}

package net.naonedbus.activity;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public abstract class SlidingMenuActivity extends SlidingSherlockFragmentActivity {

	private static String BUNDLE_TABS_CURRENT = "tabsCurrent";

	private int mLayoutId;
	private ViewPager mViewPager;
	private TabsAdapter mTabsAdapter;

	/**
	 * Sert à la détection du changement de thème.
	 */
	private int currentTheme = NBApplication.THEME;

	/**
	 * Gestion du menu latéral.
	 */
	private SlidingMenuHelper slidingMenuHelper;

	public SlidingMenuActivity(int layoutId) {
		this.mLayoutId = layoutId;
		this.slidingMenuHelper = new SlidingMenuHelper(this);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEME);
		super.onCreate(savedInstanceState);

		setContentView(mLayoutId);
		mViewPager = (ViewPager) findViewById(R.id.viewPager);

		mTabsAdapter = new TabsAdapter(this, getSupportActionBar(), mViewPager);

		setBehindContentView(R.layout.menu);
		slidingMenuHelper.setupActionBar(getSupportActionBar());
		slidingMenuHelper.setupSlidingMenu(getSlidingMenu());
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		slidingMenuHelper.onPostCreate(getIntent(), getSlidingMenu(), savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final Fragment fragment = getCurrentFragment();

		if (fragment.isAdded() && fragment instanceof CustomFragmentActions) {
			final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
			customListFragment.onCreateOptionsMenu(menu);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final Fragment fragment = getCurrentFragment();

		if (fragment instanceof CustomFragmentActions) {
			final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
			customListFragment.onPrepareOptionsMenu(menu);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Show the menu when home icon is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		default:
			final Fragment fragment = getCurrentFragment();
			if (fragment instanceof CustomFragmentActions) {
				final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
				return customListFragment.onOptionsItemSelected(item);
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(BUNDLE_TABS_CURRENT, getSupportActionBar().getSelectedNavigationIndex());
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(BUNDLE_TABS_CURRENT)) {
			final int selectedPosition = savedInstanceState.getInt(BUNDLE_TABS_CURRENT);
			getSupportActionBar().setSelectedNavigationItem(selectedPosition);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		// Gérer le changement de thème;
		if (hasFocus && (currentTheme != NBApplication.THEME)) {
			final Intent intent = new Intent(this, this.getClass());
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			overridePendingTransition(0, 0);
			startActivity(intent);
			finish();
		}

		slidingMenuHelper.onWindowFocusChanged(hasFocus, getSlidingMenu());
	}

	/**
	 * Show the menu when menu button pressed.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toggle();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Ajouter les information de fragments.
	 * 
	 * @param titles
	 *            Les titres (ressources).
	 * @param classes
	 *            Les classes des fragments.
	 * @param bundles
	 *            Les bundles des fragments.
	 */
	protected void addFragments(int[] titles, Class<?>[] classes, Bundle[] bundles) {
		final ActionBar actionBar = getSupportActionBar();
		// final FragmentManager fragmentManager = getSupportFragmentManager();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// final FragmentTransaction transaction =
		// fragmentManager.beginTransaction();
		for (int i = 0; i < titles.length; i++) {
			// final Fragment fragment = Fragment.instantiate(this,
			// classes[i].getName(), bundles[i]);
			// transaction.add(fragment, classes[i].getName());

			mTabsAdapter.addTab(actionBar.newTab().setText(titles[i]), classes[i], bundles[i]);
		}
		// transaction.commit();
		// fragmentManager.executePendingTransactions();

	}

	/**
	 * Ajouter les information de fragments.
	 * 
	 * @param titles
	 *            Les titres (ressources).
	 * @param classes
	 *            Les classes des fragments.
	 */
	protected void addFragments(int[] titles, Class<?>[] classes) {
		final ActionBar actionBar = getSupportActionBar();
		// final FragmentManager fragmentManager = getSupportFragmentManager();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		for (int i = 0; i < titles.length; i++) {
			// final Fragment fragment = Fragment.instantiate(this,
			// classes[i].getName(), null);
			// fragmentManager.putFragment(null, classes[i].getName(),
			// fragment);
			mTabsAdapter.addTab(actionBar.newTab().setText(titles[i]), classes[i], null);
		}
	}

	/**
	 * Get the current Fragment.
	 * 
	 * @return the current Fragment, or <code>null</code> if we can't find it.
	 */
	private Fragment getCurrentFragment() {
		final Tab tab = getSupportActionBar().getSelectedTab();
		if (tab != null) {
			return mTabsAdapter.getItem(tab.getPosition());
		}
		return null;
	}

	/**
	 * Renvoyer la valeur du paramètre de l'intent
	 * 
	 * @param key
	 * @return
	 */
	protected Object getParamValue(IIntentParamKey key) {
		return getIntent().getSerializableExtra(key.toString());
	}

}

package net.naonedbus.activity;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.activity.impl.SearchActivity;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public abstract class SimpleFragmentActivity extends SherlockFragmentActivity implements TabListener {

	private static String BUNDLE_TABS_CURRENT = "tabsCurrent";
	private static String BUNDLE_TABS_TITLES = "tabsTitles";
	private static String BUNDLE_TABS_CLASSES = "tabsClasses";
	private static String BUNDLE_TABS_BUNDLES = "tabsBundles";

	private int layoutId;

	/**
	 * Liste des fragments
	 */
	private int[] titles;
	private String[] classes;
	private Bundle[] bundles;

	/** Sert à la détection du changement de thème. */
	private int currentTheme = NBApplication.THEME;

	public SimpleFragmentActivity(int layoutId) {
		this.layoutId = layoutId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEMES_RES[NBApplication.THEME]);
		super.onCreate(savedInstanceState);
		setContentView(layoutId);

		final SlidingMenuHelper slidingMenuHelper = new SlidingMenuHelper(this);
		slidingMenuHelper.setupActionBar(getSupportActionBar());

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_launcher);
		actionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final Fragment fragment = getCurrentFragment();

		if (fragment instanceof CustomFragmentActions) {
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
		if (item.getItemId() == android.R.id.home) {
			finish();
		}

		final Fragment fragment = getCurrentFragment();
		if (fragment instanceof CustomFragmentActions) {
			final CustomFragmentActions customListFragment = (CustomFragmentActions) fragment;
			return customListFragment.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(BUNDLE_TABS_CURRENT, getSupportActionBar().getSelectedNavigationIndex());
		outState.putIntArray(BUNDLE_TABS_TITLES, titles);
		outState.putStringArray(BUNDLE_TABS_CLASSES, classes);
		outState.putSerializable(BUNDLE_TABS_BUNDLES, bundles);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(BUNDLE_TABS_TITLES)) {
			titles = savedInstanceState.getIntArray(BUNDLE_TABS_TITLES);
			classes = savedInstanceState.getStringArray(BUNDLE_TABS_CLASSES);
			bundles = (Bundle[]) savedInstanceState.getSerializable(BUNDLE_TABS_BUNDLES);

			final int selectedPosition = savedInstanceState.getInt(BUNDLE_TABS_CURRENT);

			addFragments(titles, classes, bundles);
			if (selectedPosition > 0) {
				getSupportActionBar().setSelectedNavigationItem(selectedPosition);
			}

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

	}

	protected void addFragments(int[] titles, String[] classes) {
		final ActionBar actionBar = getSupportActionBar();
		this.classes = classes;
		this.titles = titles;

		final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		for (int i = 0; i < titles.length; i++) {
			final Fragment fragment = Fragment.instantiate(this, this.classes[i]);
			transaction.add(fragment, this.classes[i]);
			transaction.detach(fragment);
		}
		transaction.commit();
		getSupportFragmentManager().executePendingTransactions();

		for (int i = 0; i < titles.length; i++) {
			actionBar.addTab(actionBar.newTab().setText(titles[i]).setTabListener(this));
		}

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		if (titles.length == 1) {
			actionBar.setSelectedNavigationItem(0);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
	}

	protected void addFragments(int[] titles, String[] classes, Bundle[] bundles) {
		final ActionBar actionBar = getSupportActionBar();
		this.classes = classes;
		this.titles = titles;
		this.bundles = bundles;

		final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		for (int i = 0; i < titles.length; i++) {
			final Fragment fragment = Fragment.instantiate(this, classes[i], bundles[i]);
			transaction.add(fragment, classes[i]);
			transaction.detach(fragment);
		}
		transaction.commit();
		getSupportFragmentManager().executePendingTransactions();

		for (int i = 0; i < titles.length; i++) {
			actionBar.addTab(actionBar.newTab().setText(titles[i]).setTabListener(this));
		}

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		if (titles.length == 1) {
			actionBar.setSelectedNavigationItem(0);
			actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		}
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
		this.classes = new String[classes.length];
		for (int i = 0; i < classes.length; i++) {
			this.classes[i] = classes[i].getName();
		}
		addFragments(titles, this.classes);
	}

	/**
	 * Ajouter les information de fragments.
	 * 
	 * @param titles
	 *            Les titres (ressources).
	 * @param classes
	 *            Les classes des fragments.
	 */
	protected void addFragments(int[] titles, Class<?>[] classes, Bundle[] bundles) {
		this.classes = new String[classes.length];
		for (int i = 0; i < classes.length; i++) {
			this.classes[i] = classes[i].getName();
		}
		addFragments(titles, this.classes, bundles);
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		final int position = tab.getPosition();
		final Fragment fragment = getSupportFragmentManager().findFragmentByTag(this.classes[position]);

		if (fragment.isAdded()) {
			ft.show(fragment);
		} else {
			ft.attach(fragment);
			ft.add(R.id.fragmentContent, fragment);
		}

		invalidateOptionsMenu();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		final int position = tab.getPosition();
		final Fragment fragment = getSupportFragmentManager().findFragmentByTag(this.classes[position]);
		ft.hide(fragment);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
	}

	/**
	 * Get the current Fragment.
	 * 
	 * @return the current Fragment, or <code>null</code> if we can't find it.
	 */
	private Fragment getCurrentFragment() {
		final Tab tab = getSupportActionBar().getSelectedTab();
		if (tab != null) {
			return getSupportFragmentManager().findFragmentByTag(this.classes[tab.getPosition()]);
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

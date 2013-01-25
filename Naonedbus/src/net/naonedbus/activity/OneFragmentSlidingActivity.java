package net.naonedbus.activity;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import net.simonvt.menudrawer.MenuDrawer;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public abstract class OneFragmentSlidingActivity extends SherlockFragmentActivity {

	private static String BUNDLE_TABS_CLASSES = "tabsClasses";
	private static String BUNDLE_TABS_BUNDLES = "tabsBundles";

	private int layoutId;

	/**
	 * Liste des fragments
	 */
	private String mClasse;
	private Bundle mBundle;

	private Fragment mFragment;

	/** Gestion du menu latéral. */
	private MenuDrawer mMenuDrawer;
	/** Gestion du menu latéral. */
	private SlidingMenuHelper mSlidingMenuHelper;

	/** Sert à la détection du changement de thème. */
	private int currentTheme = NBApplication.THEME;

	public OneFragmentSlidingActivity(int layoutId) {
		this.layoutId = layoutId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEMES_MENU_RES[NBApplication.THEME]);
		super.onCreate(savedInstanceState);
		setContentView(layoutId);

		mMenuDrawer = MenuDrawer.attach(this, MenuDrawer.MENU_DRAG_WINDOW);

		mSlidingMenuHelper = new SlidingMenuHelper(this);
		mSlidingMenuHelper.setupActionBar(getSupportActionBar());
		mSlidingMenuHelper.setupSlidingMenu(mMenuDrawer);

		final ActionBar actionBar = getSupportActionBar();
		actionBar.setIcon(R.drawable.ic_launcher);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mSlidingMenuHelper.onPostCreate(getIntent(), mMenuDrawer, savedInstanceState);
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
		switch (item.getItemId()) {
		case android.R.id.home:
			mMenuDrawer.toggleMenu();
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
		outState.putString(BUNDLE_TABS_CLASSES, mClasse);
		outState.putParcelable(BUNDLE_TABS_BUNDLES, mBundle);
		mSlidingMenuHelper.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(BUNDLE_TABS_CLASSES)) {
			mClasse = savedInstanceState.getString(BUNDLE_TABS_CLASSES);
			mBundle = savedInstanceState.getParcelable(BUNDLE_TABS_BUNDLES);

			addFragment(mClasse, mBundle);
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

		mSlidingMenuHelper.onWindowFocusChanged(hasFocus, mMenuDrawer);
	}

	/**
	 * Show the menu when menu button pressed, hide it when back is pressed
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU || (mMenuDrawer.isMenuVisible() && keyCode == KeyEvent.KEYCODE_BACK)) {
			mMenuDrawer.toggleMenu();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	protected void addFragment(String classe) {
		final ActionBar actionBar = getSupportActionBar();
		this.mClasse = classe;

		mFragment = Fragment.instantiate(this, this.mClasse);
		mFragment.setRetainInstance(true);
		getSupportFragmentManager().beginTransaction().add(R.id.fragmentContent, mFragment).commit();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	protected void addFragment(String classe, Bundle bundle) {
		final ActionBar actionBar = getSupportActionBar();
		this.mClasse = classe;
		this.mBundle = bundle;

		mFragment = Fragment.instantiate(this, classe, bundle);
		mFragment.setRetainInstance(true);
		getSupportFragmentManager().beginTransaction().add(R.id.fragmentContent, mFragment).commit();

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	/**
	 * Ajouter les information de fragments.
	 * 
	 * @param classes
	 *            Les classes des fragments.
	 */
	protected void addFragment(Class<?> classes) {
		this.mClasse = classes.getName();
		addFragment(this.mClasse);
	}

	/**
	 * Ajouter les information de fragments.
	 * 
	 * @param classe
	 *            Les classes des fragments.
	 */
	protected void addFragment(Class<?> classe, Bundle bundle) {
		this.mClasse = classe.getName();
		addFragment(this.mClasse, bundle);
	}

	/**
	 * Get the current Fragment.
	 * 
	 * @return the current Fragment, or <code>null</code> if we can't find it.
	 */
	protected Fragment getCurrentFragment() {
		return mFragment;
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

	protected SlidingMenuHelper getSlidingMenuHelper() {
		return mSlidingMenuHelper;
	}

}

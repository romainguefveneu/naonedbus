package net.naonedbus.activity;

import net.naonedbus.NBApplication;
import net.naonedbus.R;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public abstract class OneFragmentActivity extends SherlockFragmentActivity {

	private static String BUNDLE_TABS_CLASSES = "tabsClasses";
	private static String BUNDLE_TABS_BUNDLES = "tabsBundles";

	private int layoutId;

	/**
	 * Liste des fragments
	 */
	private String mClasse;
	private Bundle mBundle;

	private Fragment mFragment;

	private SlidingMenuHelper mSlidingMenuHelper;

	/** Sert à la détection du changement de thème. */
	private int currentTheme = NBApplication.THEME;

	public OneFragmentActivity(int layoutId) {
		this.layoutId = layoutId;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setTheme(NBApplication.THEMES_RES[NBApplication.THEME]);
		super.onCreate(savedInstanceState);
		setContentView(layoutId);

		final ActionBar actionBar = getSupportActionBar();
		mSlidingMenuHelper = new SlidingMenuHelper(this);
		mSlidingMenuHelper.setupActionBar(actionBar);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
	}

	/**
	 * Show the menu when home icon is clicked.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(BUNDLE_TABS_CLASSES, mClasse);
		outState.putParcelable(BUNDLE_TABS_BUNDLES, mBundle);
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

	}

	protected void addFragment(String classe) {
		mClasse = classe;

		final FragmentManager fragmentManager = getSupportFragmentManager();
		if ((mFragment = fragmentManager.findFragmentByTag(classe)) == null) {
			mFragment = Fragment.instantiate(this, mClasse);
			mFragment.setRetainInstance(true);
			fragmentManager.beginTransaction().add(R.id.fragmentContent, mFragment, classe).commit();
		}
	}

	protected void addFragment(String classe, Bundle bundle) {
		mClasse = classe;
		mBundle = bundle;

		final FragmentManager fragmentManager = getSupportFragmentManager();
		if ((mFragment = fragmentManager.findFragmentByTag(classe)) == null) {
			mFragment = Fragment.instantiate(this, classe, bundle);
			mFragment.setRetainInstance(true);
			fragmentManager.beginTransaction().add(R.id.fragmentContent, mFragment, classe).commit();
		}
	}

	/**
	 * Ajouter les information de fragments.
	 * 
	 * @param clazz
	 *            La classe du fragment.
	 */
	protected void addFragment(final Class<?> classe) {
		addFragment(classe.getName());
	}

	/**
	 * Ajouter les information de fragments.
	 * 
	 * @param classe
	 *            La classe du fragment.
	 */
	protected void addFragment(final Class<?> classe, final Bundle bundle) {
		addFragment(classe.getName(), bundle);
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

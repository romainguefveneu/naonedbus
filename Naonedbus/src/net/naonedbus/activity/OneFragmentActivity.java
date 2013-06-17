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
package net.naonedbus.activity;

import net.naonedbus.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public abstract class OneFragmentActivity extends SherlockFragmentActivity {

	private static String BUNDLE_TABS_CLASSES = "tabsClasses";
	private static String BUNDLE_TABS_BUNDLES = "tabsBundles";

	private final int layoutId;

	/**
	 * Liste des fragments
	 */
	private String mClasse;
	private Bundle mBundle;

	private Fragment mFragment;

	public OneFragmentActivity(final int layoutId) {
		this.layoutId = layoutId;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(layoutId);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(BUNDLE_TABS_CLASSES, mClasse);
		outState.putParcelable(BUNDLE_TABS_BUNDLES, mBundle);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedInstanceState) {
		if (savedInstanceState.containsKey(BUNDLE_TABS_CLASSES)) {
			mClasse = savedInstanceState.getString(BUNDLE_TABS_CLASSES);
			mBundle = savedInstanceState.getParcelable(BUNDLE_TABS_BUNDLES);

			addFragment(mClasse, mBundle);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}

	protected void addFragment(final String classe) {
		mClasse = classe;

		final FragmentManager fragmentManager = getSupportFragmentManager();
		if ((mFragment = fragmentManager.findFragmentByTag(classe)) == null) {
			mFragment = Fragment.instantiate(this, mClasse);
			mFragment.setRetainInstance(true);
			fragmentManager.beginTransaction().add(R.id.fragmentContent, mFragment, classe).commit();
		}
	}

	protected void addFragment(final String classe, final Bundle bundle) {
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

}

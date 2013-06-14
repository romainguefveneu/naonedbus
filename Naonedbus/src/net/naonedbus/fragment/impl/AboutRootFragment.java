package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.fragment.ViewPagerFragment;
import net.naonedbus.fragment.impl.nested.AboutFragment;
import net.naonedbus.fragment.impl.nested.VersionsFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class AboutRootFragment extends ViewPagerFragment {

	private static int[] titles = new int[] { R.string.title_fragment_about, R.string.title_fragment_versions };
	private static Class<?>[] classes = new Class<?>[] { AboutFragment.class, VersionsFragment.class };

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		if (savedInstanceState == null) {
			addFragments(titles, classes);
		}
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.activity_about, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_rate:
			rateMe();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Lancer le Play Store pour faire voter l'utilisateur.
	 */
	private void rateMe() {
		final Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
		final Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(goToMarket);
	}

}

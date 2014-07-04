package net.naonedbus.activity.impl;

import net.naonedbus.R;
import net.naonedbus.activity.FragmentsActivity;
import net.naonedbus.fragment.impl.AboutFragment;
import net.naonedbus.fragment.impl.VersionsFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class AboutActivity extends FragmentsActivity {

	private static int[] titles = new int[] { R.string.title_fragment_about, R.string.title_fragment_versions };

	private static Class<?>[] classes = new Class<?>[] { AboutFragment.class, VersionsFragment.class };

	public AboutActivity() {
		super(R.layout.activity_tabs);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addFragments(titles, classes);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.activity_about, menu);
		return super.onCreateOptionsMenu(menu);

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_rate:
			rateMe();
			break;
		default:
			super.onOptionsItemSelected(item);
			break;
		}
		return true;
	}

	/**
	 * Lancer le Play Store pour faire voter l'utilisateur.
	 */
	private void rateMe() {
		final Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
		final Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
		goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		startActivity(goToMarket);
	}
}

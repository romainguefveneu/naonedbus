package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.fragment.ViewPagerFragment;
import net.naonedbus.fragment.impl.nested.CommentairesFragment;
import net.naonedbus.fragment.impl.nested.TanActuFragment;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class InfoTraficRootFragment extends ViewPagerFragment {
	private static int[] titles = new int[] { R.string.title_fragment_en_direct, R.string.title_fragment_tan_actu };
	private static Class<?>[] classes = new Class<?>[] { CommentairesFragment.class, TanActuFragment.class };

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
		inflater.inflate(R.menu.activity_en_direct, menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_comment:
			startActivity(new Intent(getActivity(), CommentaireActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

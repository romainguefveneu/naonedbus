package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListAdapter;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class FavorisFragment extends CustomListFragment implements CustomFragmentActions {

	public FavorisFragment() {
		super(R.string.title_fragment_favoris, R.layout.fragment_listview);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_edit:
			startActivity(new Intent(getActivity(), CommentaireActivity.class));
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSherlockActivity().getSupportMenuInflater();
		menuInflater.inflate(R.menu.fragment_favoris, menu);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setEmptyMessageValues(R.string.error_title_empty_favori, R.string.error_summary_empty_favori, R.drawable.favori);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		return null;
	}

}

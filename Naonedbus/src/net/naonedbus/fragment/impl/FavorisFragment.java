package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.CommentaireActivity;
import net.naonedbus.activity.impl.HoraireActivity;
import net.naonedbus.bean.Favori;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.widget.adapter.impl.FavoriArrayAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class FavorisFragment extends CustomListFragment implements CustomFragmentActions {

	private FavoriManager mFavoriManager;

	public FavorisFragment() {
		super(R.string.title_fragment_favoris, R.layout.fragment_listview);
		mFavoriManager = FavoriManager.getInstance();
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
	public void onListItemClick(ListView l, View v, int position, long id) {
		final Favori item = (Favori) l.getItemAtPosition(position);
		final ParamIntent intent = new ParamIntent(getActivity(), HoraireActivity.class);
		intent.putExtra(HoraireActivity.Param.idArret, item._id);
		startActivity(intent);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setEmptyMessageValues(R.string.error_title_empty_favori, R.string.error_summary_empty_favori, R.drawable.favori);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		final List<Favori> favoris = mFavoriManager.getAll(context.getContentResolver());
		final FavoriArrayAdapter adapter = new FavoriArrayAdapter(context, favoris);
		result.setResult(adapter);

		return result;
	}
}

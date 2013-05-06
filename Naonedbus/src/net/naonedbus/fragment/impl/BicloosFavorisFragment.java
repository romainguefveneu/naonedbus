package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.FavoriBiclooManager;
import net.naonedbus.widget.adapter.impl.BiclooArrayAdapter;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListAdapter;

public class BicloosFavorisFragment extends CustomListFragment {

	public BicloosFavorisFragment() {
		super(R.string.title_fragment_favoris, R.layout.fragment_listview);
	}

	@Override
	public void onStart() {
		super.onStart();
		loadContent();
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context, final Bundle bundle) {
		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		final FavoriBiclooManager manager = FavoriBiclooManager.getInstance();
		final List<Bicloo> bicloos = manager.getAll(context.getContentResolver());

		final BiclooArrayAdapter adapter = new BiclooArrayAdapter(context, bicloos);
		result.setResult(adapter);

		return result;
	}

}

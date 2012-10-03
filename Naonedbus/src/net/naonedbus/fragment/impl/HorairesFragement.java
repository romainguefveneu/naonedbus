package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomListFragment;
import android.content.Context;
import android.widget.ListAdapter;

import com.actionbarsherlock.view.Menu;

public class HorairesFragement extends CustomListFragment {

	public HorairesFragement() {
		super(R.string.title_activity_horaires, R.layout.fragment_listview_section);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub

	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(Context context) {
		// TODO Auto-generated method stub
		return null;
	}

}

package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.widget.adapter.impl.ArretCursorAdapter;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.actionbarsherlock.view.MenuItem;

public class ItineraireFragment extends CustomFragment {

	private AutoCompleteTextView mFromTextView;
	private AutoCompleteTextView mToTextView;

	public ItineraireFragment() {
		super(R.string.title_fragment_versions, R.layout.fragment_itineraire);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {
		mFromTextView = (AutoCompleteTextView) view.findViewById(R.id.itineraireFrom);
		mToTextView = (AutoCompleteTextView) view.findViewById(R.id.itineraireTo);

		final ArretManager arretManager = ArretManager.getInstance();
		final ArretCursorAdapter adapter = new ArretCursorAdapter(getActivity(), arretManager.getCursor(getActivity()
				.getContentResolver()), null);

		mFromTextView.setAdapter(adapter);
		mToTextView.setAdapter(adapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return false;
	}

}

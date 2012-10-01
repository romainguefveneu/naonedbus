package net.naonedbus.fragment.impl;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Sens;
import net.naonedbus.bean.async.AsyncResult;
import net.naonedbus.fragment.CustomFragmentActions;
import net.naonedbus.fragment.CustomListFragment;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter;
import android.content.Context;
import android.widget.ListAdapter;

import com.actionbarsherlock.view.Menu;

public class ArretsFragment extends CustomListFragment implements CustomFragmentActions {

	public static final String PARAM_ID_SENS = "idSens";

	private Sens mSens;

	public ArretsFragment() {
		super(R.string.title_fragment_arrets, R.layout.fragment_listview);
	}

	@Override
	protected AsyncResult<ListAdapter> loadContent(final Context context) {

		final SensManager sensManager = SensManager.getInstance();
		final int idSens = getArguments().getInt(PARAM_ID_SENS);

		mSens = sensManager.getSingle(getActivity().getContentResolver(), idSens);

		final AsyncResult<ListAdapter> result = new AsyncResult<ListAdapter>();
		try {
			final ArretManager arretManager = ArretManager.getInstance();

			final List<Arret> arrets = arretManager.getAll(context.getContentResolver(), mSens.codeLigne, mSens.code);

			final ArretArrayAdapter adapter = new ArretArrayAdapter(context, arrets);
			result.setResult(adapter);

		} catch (Exception e) {
			result.setException(e);
		}
		return result;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu) {

	}

}

package net.naonedbus.loader;

import java.util.List;

import net.naonedbus.bean.Equipment;
import net.naonedbus.manager.impl.EquipmentManager;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class EquipmentLoader extends AsyncTaskLoader<List<Equipment>> {

	private final Equipment.Type mType;
	private List<Equipment> mResult;

	public EquipmentLoader(final Context context, final Equipment.Type type) {
		super(context);
		mType = type;
	}

	@Override
	public List<Equipment> loadInBackground() {
		final EquipmentManager manager = EquipmentManager.getInstance();
		return manager.getByType(getContext().getContentResolver(), mType);
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(final List<Equipment> result) {
		mResult = result;

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			try {
				super.deliverResult(result);
			} catch (final NullPointerException e) {

			}
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mResult != null) {
			// If we currently have a result available, deliver it
			// immediately.
			deliverResult(mResult);
		}

		if (takeContentChanged() || mResult == null) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

}

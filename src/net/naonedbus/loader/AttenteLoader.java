package net.naonedbus.loader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.json.JSONException;

import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.horaire.Attente;
import net.naonedbus.rest.controller.impl.AttenteController;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

public class AttenteLoader extends AsyncTaskLoader<List<Attente>> {

	public static Bundle create(Equipement equipement) {
		Bundle result = new Bundle();
		result.putString(PARAM_CODE_EQUIPEMENT, equipement.getCode());
		return result;
	}

	private static final String PARAM_CODE_EQUIPEMENT = "codeEquipement";

	private List<Attente> mResult;
	private String mCodeEquipement;

	public AttenteLoader(Context context, Bundle args) {
		super(context);

		mCodeEquipement = args.getString(PARAM_CODE_EQUIPEMENT);
	}

	@Override
	public List<Attente> loadInBackground() {
		final AttenteController attenteController = new AttenteController();
		List<Attente> attentes = null;
		try {
			attentes = attenteController.getAll(mCodeEquipement);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return attentes;
	}

	/**
	 * Called when there is new data to deliver to the client. The super class
	 * will take care of delivering it; the implementation here just adds a
	 * little more logic.
	 */
	@Override
	public void deliverResult(final List<Attente> result) {
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

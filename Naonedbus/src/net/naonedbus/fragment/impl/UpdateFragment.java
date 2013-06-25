package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.activity.impl.MainActivity;
import net.naonedbus.manager.impl.HoraireManager;
import net.naonedbus.manager.impl.UpdaterManager;
import net.naonedbus.provider.DatabaseActionObserver;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.VersionUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class UpdateFragment extends SherlockFragment {
	private final DatabaseActionObserver mListener = new DatabaseActionObserver(new Handler()) {

		@Override
		public void onUpgrade(final int oldVersion) {
		}

		@Override
		public void onCreate() {

		}

		@Override
		public void onUpgradeDone(final boolean success) {

		}
	};

	private UpdateCard mUpdateCard;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new UpdateAndCleanTask().execute();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (container == null) // must put this in
			return null;

		final View view = inflater.inflate(R.layout.fragment_update, container, false);
		mUpdateCard = new UpdateCard(getActivity(), view.findViewById(R.id.updateView), new OnClickListener() {
			@Override
			public void onClick(final View v) {
				((MainActivity) getActivity()).onUpgradeDone();
			}
		});
		mUpdateCard.show();

		return view;
	}

	private static class UpdateCard {

		private final Context mContext;
		private final OnClickListener mOnNextClickListener;
		private final View mUpdateView;

		public UpdateCard(final Context context, final View view, final OnClickListener onNextClickListener) {
			mContext = context;
			mUpdateView = view;
			mOnNextClickListener = onNextClickListener;
		}

		public void show() {
			mUpdateView.setVisibility(View.VISIBLE);

			final Typeface robotoTypeface = FontUtils.getRobotoLight(mContext);
			((TextView) mUpdateView.findViewById(android.R.id.title)).setTypeface(robotoTypeface);
			((TextView) mUpdateView.findViewById(R.id.codename)).setTypeface(robotoTypeface);

			final String version = mContext.getString(R.string.version_number, VersionUtils.getVersion(mContext));
			((TextView) mUpdateView.findViewById(R.id.version)).setText(version);

			final String versionNotes = VersionUtils.getCurrentVersionNotes(mContext);
			((TextView) mUpdateView.findViewById(R.id.versionNotes)).setText(versionNotes);

			mUpdateView.findViewById(R.id.nextButton).setOnClickListener(mOnNextClickListener);
			mUpdateView.findViewById(R.id.nextButton).setEnabled(false);
		}

		public void setComplete() {
			((TextView) mUpdateView.findViewById(android.R.id.title)).setText(R.string.updating_complete);
			mUpdateView.findViewById(android.R.id.progress).setVisibility(View.GONE);
			mUpdateView.findViewById(R.id.nextButton).setEnabled(true);
		}

	}

	/**
	 * Task chargée de déclencher une éventuelle mise à jour et de vider le
	 * cache horaire.
	 * 
	 * @author romain.guefveneu
	 * 
	 */
	private class UpdateAndCleanTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

			// Déclencher une éventuelle mise à jour
			final UpdaterManager updaterManager = new UpdaterManager();
			updaterManager.triggerUpdate(getActivity());

			// Vider les anciens horaires
			final HoraireManager horaireManager = HoraireManager.getInstance();
			horaireManager.clearOldHoraires(getActivity().getContentResolver());

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			mUpdateCard.setComplete();
		}

	}

}

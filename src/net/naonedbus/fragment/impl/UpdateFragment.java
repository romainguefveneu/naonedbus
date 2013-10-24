package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.activity.impl.MainActivity;
import net.naonedbus.fragment.CustomFragment;
import net.naonedbus.manager.impl.ScheduleManager;
import net.naonedbus.manager.impl.UpdaterManager;
import net.naonedbus.manager.impl.UpdaterManager.UpdateType;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.VersionUtils;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class UpdateFragment extends CustomFragment {

	private UpdateCard mUpdateCard;

	public UpdateFragment() {
		super(R.layout.fragment_update);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new UpdateAndCleanTask().execute();
	}

	@Override
	protected void bindView(View view, Bundle savedInstanceState) {
		mUpdateCard = new UpdateCard(getActivity(), view.findViewById(R.id.updateView), new OnClickListener() {
			@Override
			public void onClick(final View v) {
				final MainActivity mainActivity = (MainActivity) getActivity();
				mainActivity.onUpgradeDone();
			}
		});

		final UpdaterManager updaterManager = new UpdaterManager();
		final UpdateType updateType = updaterManager.needUpdate(getActivity());
		if (UpdateType.FIRST_LAUNCH.equals(updateType)) {
			mUpdateCard.setTitles(R.string.setup, R.string.setup_complete);
		} else {
			mUpdateCard.setTitles(R.string.updating, R.string.updating_complete);
		}
		mUpdateCard.setUpdateType(updateType);

		mUpdateCard.show();
	}

	private static class UpdateCard {

		private final Context mContext;
		private final OnClickListener mOnNextClickListener;
		private final View mUpdateView;
		private UpdateType mUpdateType;

		private int mTitleProgressId;
		private int mTitleCompleteId;

		public UpdateCard(final Context context, final View view, final OnClickListener onNextClickListener) {
			mContext = context;
			mUpdateView = view;
			mOnNextClickListener = onNextClickListener;
		}

		public void setUpdateType(UpdateType updateType) {
			mUpdateType = updateType;
		}

		public void show() {
			if (UpdateType.UPGRADE.equals(mUpdateType)) {
				final Typeface robotoTypeface = FontUtils.getRobotoLight(mContext);

				TextView titleView = (TextView) mUpdateView.findViewById(android.R.id.title);
				TextView codenameView = (TextView) mUpdateView.findViewById(R.id.codename);
				TextView versionView = (TextView) mUpdateView.findViewById(R.id.version);
				TextView versionNotesView = (TextView) mUpdateView.findViewById(R.id.versionNotes);

				titleView.setTypeface(robotoTypeface);
				codenameView.setTypeface(robotoTypeface);

				titleView.setText(mTitleProgressId);

				final String version = mContext.getString(R.string.format_version, VersionUtils.getVersion(mContext));
				versionView.setText(version);

				final String versionNotes = VersionUtils.getCurrentVersionNotes(mContext);
				versionNotesView.setText(versionNotes);

				mUpdateView.findViewById(R.id.nextButton).setOnClickListener(mOnNextClickListener);
				mUpdateView.setVisibility(View.VISIBLE);
			}
		}

		public void setComplete() {
			if (UpdateType.FIRST_LAUNCH.equals(mUpdateType)) {
				mOnNextClickListener.onClick(mUpdateView);
			} else {
				((TextView) mUpdateView.findViewById(android.R.id.title)).setText(mTitleCompleteId);
				mUpdateView.findViewById(android.R.id.progress).setVisibility(View.GONE);

				final View nextButton = mUpdateView.findViewById(R.id.nextButton);
				nextButton.setVisibility(View.VISIBLE);
				nextButton.startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
			}
		}

		public void setTitles(final int titleProgressId, final int titleCompleteId) {
			mTitleProgressId = titleProgressId;
			mTitleCompleteId = titleCompleteId;
		}

	}

	/**
	 * Task chargée de déclencher une éventuelle mise à jour et de vider le
	 * cache schedule.
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
			final ScheduleManager horaireManager = ScheduleManager.getInstance();
			horaireManager.clearOldSchedules(getActivity().getContentResolver());

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			mUpdateCard.setComplete();
		}

	}

}

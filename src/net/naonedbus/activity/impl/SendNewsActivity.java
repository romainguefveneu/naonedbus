/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.activity.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.bean.Direction;
import net.naonedbus.bean.LiveNews;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Stop;
import net.naonedbus.manager.impl.DirectionManager;
import net.naonedbus.manager.impl.RouteManager;
import net.naonedbus.manager.impl.StopManager;
import net.naonedbus.rest.controller.impl.LiveNewsController;
import net.naonedbus.security.KeyType;
import net.naonedbus.security.RSAUtils;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.InfoDialogUtils;
import net.naonedbus.validator.CommentaireContentTypeValidator;
import net.naonedbus.validator.CommentaireSizeValidator;
import net.naonedbus.validator.CommentaireValidator;
import net.naonedbus.widget.adapter.impl.DirectionArrayAdapter;
import net.naonedbus.widget.adapter.impl.RouteArrayAdapter;
import net.naonedbus.widget.adapter.impl.StopArrayAdapter;

import org.apache.http.HttpException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;

public class SendNewsActivity extends SherlockActivity {

	public static final String ACTION_COMMENTAIRE_SENT = "net.naonedbus.action.COMMENTAIRE_SENT";

	public static final String PARAM_LIGNE = "route";
	public static final String PARAM_SENS = "direction";
	public static final String PARAM_ARRET = "stop";

	private static final String LOG_TAG = "CommentaireActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final String BUNDLE_KEY_LIGNE = "route";
	private static final String BUNDLE_KEY_SENS = "direction";
	private static final String BUNDLE_KEY_ARRET = "stop";

	private EditText mCommentText;

	private RouteManager mRouteManager;
	private DirectionManager mDirectionManager;
	private StopManager mStopManager;

	private Route mRoute;
	private Direction mDirection;
	private Stop mStop;

	private Route mAllLignes;
	private Direction mAllSens;
	private Stop mAllArrets;

	private View mBtnChangeLigne;
	private View mBtnChangeSens;
	private View mBtnChangeArret;

	private TextView mRouteView;
	private TextView mTextSens;
	private TextView mTextArret;

	private ListAdapter mRoutesAdapter;
	private ListAdapter mDirectionAdapter;
	private ListAdapter mArretsAdapter;

	private int mSelectedLignePosition;
	private int mSelectedSensPosition;
	private int mSelectedArretPosition;

	private SendTask mSendTask;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_livenews_send);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mRouteManager = RouteManager.getInstance();
		mDirectionManager = DirectionManager.getInstance();
		mStopManager = StopManager.getInstance();

		mAllLignes = Route.buildAllLigneItem(this);
		mAllSens = new Direction(-1, getString(R.string.all_directions));
		mAllArrets = new Stop.Builder().setId(-1).setNomArret(getString(R.string.target_tous_arrets)).build();

		mRoutesAdapter = getLignesAdapter();

		mCommentText = (EditText) findViewById(android.R.id.input);
		mRouteView = (TextView) findViewById(R.id.route);
		mTextSens = (TextView) findViewById(R.id.direction);
		mTextArret = (TextView) findViewById(R.id.stop);

		mBtnChangeLigne = findViewById(R.id.commentaireLigneSpinner);
		mBtnChangeLigne.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showSelectLigneDialog();
			}
		});
		mBtnChangeSens = findViewById(R.id.direction);
		mBtnChangeSens.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showSelectSensDialog(mRoute.getCode());
			}
		});
		mBtnChangeArret = findViewById(R.id.stop);
		mBtnChangeArret.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showSelectArretDialog(mRoute.getCode(), mDirection.getCode());
			}
		});

		Route route;
		Direction direction;
		Stop stop;

		if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_KEY_LIGNE)) {
			route = (Route) savedInstanceState.getParcelable(BUNDLE_KEY_LIGNE);
			direction = (Direction) savedInstanceState.getParcelable(BUNDLE_KEY_SENS);
			stop = (Stop) savedInstanceState.getParcelable(BUNDLE_KEY_ARRET);
		} else {
			route = getIntent().getParcelableExtra(PARAM_LIGNE);
			direction = getIntent().getParcelableExtra(PARAM_SENS);
			stop = getIntent().getParcelableExtra(PARAM_ARRET);
		}

		if (route != null) {
			setLigne(route);
		}
		if (direction != null) {
			setSens(direction);
		}
		if (stop != null) {
			setArret(stop);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_livenews_send, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(BUNDLE_KEY_LIGNE, mRoute);
		outState.putParcelable(BUNDLE_KEY_SENS, mDirection);
		outState.putParcelable(BUNDLE_KEY_ARRET, mStop);
	}

	@Override
	public boolean onMenuItemSelected(final int featureId, final MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_send:
			prepareAndSendComment();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private ListAdapter getLignesAdapter() {
		final List<Route> lignes = mRouteManager.getAll(getContentResolver());
		lignes.add(0, mAllLignes);
		final RouteArrayAdapter adapter = new RouteArrayAdapter(this, lignes);
		adapter.setHideDivider(true);
		return adapter;
	}

	private ListAdapter getSensAdapter(final String routeCode) {
		final List<Direction> direction = mDirectionManager.getAll(getContentResolver(), routeCode);
		direction.add(0, mAllSens);
		return new DirectionArrayAdapter(this, direction);
	}

	private ListAdapter getArretsAdapter(final String routeCode, final String directionCode) {
		final List<Stop> arrets = mStopManager.getAll(getContentResolver(), routeCode, directionCode);
		arrets.add(0, mAllArrets);
		return new StopArrayAdapter(this, arrets);
	}

	/**
	 * Afficher la dialog de sélection de la route.
	 */
	private void showSelectLigneDialog() {
		showSelectDialog(R.string.route, mRoutesAdapter, mSelectedLignePosition, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				mSelectedLignePosition = which;
				setLigne((Route) mRoutesAdapter.getItem(which));
				dialog.dismiss();
			}
		});
	}

	/**
	 * Afficher la dialog de sélection du direction.
	 */
	private void showSelectSensDialog(final String routeCode) {
		if (!routeCode.equals(mDirection.getCode()) || mDirectionAdapter == null) {
			mDirectionAdapter = getSensAdapter(routeCode);
			mSelectedSensPosition = -1;
		}
		showSelectDialog(R.string.direction, mDirectionAdapter, mSelectedSensPosition,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						mSelectedSensPosition = which;
						setSens((Direction) mDirectionAdapter.getItem(which));
						dialog.dismiss();
					}
				});
	}

	/**
	 * Afficher la dialog de sélection de l'stop.
	 */
	private void showSelectArretDialog(final String routeCode, final String directionCode) {
		if (!(routeCode.equals(mStop.getCodeLigne()) && directionCode.equals(mStop.getCodeSens()))
				|| mArretsAdapter == null) {
			mArretsAdapter = getArretsAdapter(routeCode, directionCode);
			mSelectedArretPosition = -1;
		}
		showSelectDialog(R.string.stop, mArretsAdapter, mSelectedArretPosition, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				mSelectedArretPosition = which;
				setArret((Stop) mArretsAdapter.getItem(which));
				dialog.dismiss();
			}
		});
	}

	/**
	 * Afficher une dialog de selection.
	 * 
	 * @param title
	 *            Le titre
	 * @param adapter
	 *            L'adapter
	 * @param defaultPosition
	 *            La position de l'élément sélectionné par défaut
	 * @param onClickListener
	 *            Le callback
	 */
	private void showSelectDialog(final int title, final ListAdapter adapter, final int defaultPosition,
			final DialogInterface.OnClickListener onClickListener) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setSingleChoiceItems(adapter, defaultPosition, onClickListener);
		builder.show();
	}

	/**
	 * Envoyer le liveNews s'il a passé les test de
	 * {@link #validateComment(LiveNews)}
	 */
	private void prepareAndSendComment() {
		final LiveNews commentaireItem = new LiveNews();
		if (mStop != null) {
			commentaireItem.setCodeArret(mStop.getCodeArret());
		}
		if (mDirection != null) {
			commentaireItem.setCodeSens(mDirection.getCode());
		}
		if (mRoute != null) {
			commentaireItem.setCodeLigne(mRoute.getCode());
		}
		commentaireItem.setMessage(mCommentText.getText().toString().trim());

		if (validateComment(commentaireItem)) {
			sendComment(commentaireItem);
		} else {
			InfoDialogUtils.show(this, R.string.message_too_short, R.string.please_enter_more_than_2_words);
		}

	}

	/**
	 * Définir la route du liveNews et changer la valeur du selecteur.
	 * 
	 * @param route
	 *            Le nouvelle route
	 */
	private void setLigne(final Route route) {
		this.mRoute = route;
		mRouteView.setText(route.getLetter());
		mRouteView.setTextColor(route.getFrontColor());
		if (route.getBackColor() == 0) {
			mRouteView.setBackgroundResource(R.drawable.item_symbole_back);
		} else {
			ColorUtils.setBackgroundGradiant(mRouteView, route.getBackColor());
		}

		setSens(mAllSens);

		mBtnChangeSens.setEnabled(!route.equals(mAllLignes));
	}

	/**
	 * Définir le direction du liveNews et changer la valeur du selecteur.
	 * 
	 * @param direction
	 *            Le nouveau direction
	 */
	private void setSens(final Direction direction) {
		this.mDirection = direction;
		mTextSens.setText(direction.getName());

		setArret(mAllArrets);

		mBtnChangeArret.setEnabled(!direction.equals(mAllSens));
	}

	/**
	 * Définir l'stop du liveNews et changer la valeur du selecteur.
	 * 
	 * @param stop
	 *            Le nouvel stop
	 */
	private void setArret(final Stop stop) {
		mStop = stop;
		mTextArret.setText(stop.getName());
	}

	/**
	 * @return vrai si validé, faux s'il semble louche
	 */
	private boolean validateComment(final LiveNews comment) {
		boolean ret = true;
		final List<CommentaireValidator> validators = new ArrayList<CommentaireValidator>();
		validators.add(new CommentaireSizeValidator());
		validators.add(new CommentaireContentTypeValidator());

		for (final CommentaireValidator commentaireValidator : validators) {
			ret &= commentaireValidator.validate(comment.getMessage());
		}

		return ret;
	}

	private void sendComment(final LiveNews liveNews) {
		if (mSendTask == null || mSendTask.getStatus() == AsyncTask.Status.FINISHED) {
			mSendTask = (SendTask) new SendTask().execute(liveNews);
		}
	}

	/**
	 * Classe d'envoi du message
	 * 
	 * @author romain
	 * 
	 */
	private class SendTask extends AsyncTask<LiveNews, Void, Boolean> {
		private ProgressDialog progressDialog;
		private Exception exception;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(SendNewsActivity.this, "",
					getString(R.string.transmission_in_progress), true);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(final LiveNews... args) {
			try {
				final LiveNews messageItem = args[0];

				final PrivateKey privateKey = genKey();
				final String messageHashCode = getMessageHashCode(messageItem, privateKey);

				final LiveNewsController naoNewController = new LiveNewsController();
				naoNewController.post(messageItem.getCodeLigne(), messageItem.getCodeSens(),
						messageItem.getCodeArret(), messageItem.getMessage(), messageHashCode);

				return true;

			} catch (final Exception e) {
				this.exception = e;
				return false;
			}
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			progressDialog.dismiss();

			if (!success) {
				final int msgError = (this.exception instanceof HttpException) ? R.string.livenews_sending_fail
						: R.string.livenews_error_key_msg;

				InfoDialogUtils.show(SendNewsActivity.this, R.string.message_not_delivered, msgError);

				if (DBG)
					Log.e(LOG_TAG, "Erreur lors de l'envoi du message.", this.exception);
				BugSenseHandler.sendExceptionMessage("Erreur lors de l'envoi du message.", null, this.exception);
			} else {

				final Intent actionCommentaireSent = new Intent(ACTION_COMMENTAIRE_SENT);
				sendBroadcast(actionCommentaireSent);

				setResult(RESULT_OK, null);

				finish();
			}
			super.onPostExecute(success);
		}

		/**
		 * Générer la clé privée
		 */
		private PrivateKey genKey() {
			BigInteger mod;
			BigInteger exp;
			PrivateKey privateKey = null;

			mod = new BigInteger(getApplicationContext().getString(R.string.mod));
			exp = new BigInteger(getApplicationContext().getString(R.string.exp));

			try {
				privateKey = (PrivateKey) RSAUtils.genNaonedbusKey(KeyType.PRIVATE, mod, exp);
			} catch (final GeneralSecurityException e) {
				BugSenseHandler.sendExceptionMessage("Erreur lors de la génération de la clé.", null, e);
			}

			return privateKey;
		}

		/**
		 * Générer les hashCode du message
		 * 
		 * @param messageItem
		 * @return
		 * @throws GeneralSecurityException
		 * @throws UnsupportedEncodingException
		 */
		private String getMessageHashCode(final LiveNews messageItem, final PrivateKey privateKey)
				throws UnsupportedEncodingException, GeneralSecurityException {
			String concatHashCode;
			String result = null;

			concatHashCode = RSAUtils.getConcatHashCode(messageItem.getCodeLigne(), messageItem.getCodeSens(),
					messageItem.getCodeArret(), messageItem.getMessage());

			result = RSAUtils.encryptBase64(concatHashCode, privateKey);
			return result;
		}

	}
}

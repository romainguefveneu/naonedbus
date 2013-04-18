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
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.rest.controller.impl.CommentaireController;
import net.naonedbus.security.KeyType;
import net.naonedbus.security.RSAUtils;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.InfoDialogUtils;
import net.naonedbus.validator.CommentaireContentTypeValidator;
import net.naonedbus.validator.CommentaireSizeValidator;
import net.naonedbus.validator.CommentaireValidator;
import net.naonedbus.widget.adapter.impl.ArretArrayAdapter;
import net.naonedbus.widget.adapter.impl.LignesArrayAdapter;
import net.naonedbus.widget.adapter.impl.SensArrayAdapter;

import org.apache.http.HttpException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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

public class CommentaireActivity extends SherlockActivity {

	public static final String ACTION_COMMENTAIRE_SENT = "net.naonedbus.action.COMMENTAIRE_SENT";

	public static final String PARAM_LIGNE = "ligne";
	public static final String PARAM_SENS = "sens";
	public static final String PARAM_ARRET = "arret";

	private static final String LOG_TAG = "CommentaireActivity";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final String BUNDLE_KEY_LIGNE = "ligne";
	private static final String BUNDLE_KEY_SENS = "sens";
	private static final String BUNDLE_KEY_ARRET = "arret";

	private EditText mCommentText;

	private LigneManager mLigneManager;
	private SensManager mSensManager;
	private ArretManager mArretManager;

	private Ligne mLigne;
	private Sens mSens;
	private Arret mArret;

	private Ligne mAllLignes;
	private Sens mAllSens;
	private Arret mAllArrets;

	private View mBtnChangeLigne;
	private View mBtnChangeSens;
	private View mBtnChangeArret;

	private TextView mTextLigne;
	private TextView mTextSens;
	private TextView mTextArret;

	private ListAdapter mLignesAdapter;
	private ListAdapter mSensAdapter;
	private ListAdapter mArretsAdapter;

	private int mSelectedLignePosition;
	private int mSelectedSensPosition;
	private int mSelectedArretPosition;

	private SendTask mSendTask;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment);

		final SlidingMenuHelper slidingMenuHelper = new SlidingMenuHelper(this);
		slidingMenuHelper.setupActionBar(getSupportActionBar());

		mLigneManager = LigneManager.getInstance();
		mSensManager = SensManager.getInstance();
		mArretManager = ArretManager.getInstance();

		mAllLignes = new Ligne(-1, getString(R.string.target_toutes_lignes),
				getString(R.string.target_toutes_lignes_symbole));
		mAllLignes.couleurTexte = Color.BLACK;
		mAllSens = new Sens(-1, getString(R.string.target_tous_sens));
		mAllArrets = new Arret(-1, getString(R.string.target_tous_arrets));

		mLignesAdapter = getLignesAdapter();

		mCommentText = (EditText) findViewById(android.R.id.input);
		mTextLigne = (TextView) findViewById(R.id.commentaireLigne);
		mTextSens = (TextView) findViewById(R.id.commentaireSens);
		mTextArret = (TextView) findViewById(R.id.commentaireArret);

		mBtnChangeLigne = findViewById(R.id.commentaireLigneSpinner);
		mBtnChangeLigne.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showSelectLigneDialog();
			}
		});
		mBtnChangeSens = findViewById(R.id.commentaireSens);
		mBtnChangeSens.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showSelectSensDialog(mLigne.code);
			}
		});
		mBtnChangeArret = findViewById(R.id.commentaireArret);
		mBtnChangeArret.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				showSelectArretDialog(mLigne.code, mSens.code);
			}
		});

		Ligne ligne;
		Sens sens;
		Arret arret;

		if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_KEY_LIGNE)) {
			ligne = (Ligne) savedInstanceState.getParcelable(BUNDLE_KEY_LIGNE);
			sens = (Sens) savedInstanceState.getParcelable(BUNDLE_KEY_SENS);
			arret = (Arret) savedInstanceState.getParcelable(BUNDLE_KEY_ARRET);
		} else {
			ligne = getIntent().getParcelableExtra(PARAM_LIGNE);
			sens = getIntent().getParcelableExtra(PARAM_SENS);
			arret = getIntent().getParcelableExtra(PARAM_ARRET);
		}

		if (ligne != null) {
			setLigne(ligne);
		}
		if (sens != null) {
			setSens(sens);
		}
		if (arret != null) {
			setArret(arret);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_commentaire, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(BUNDLE_KEY_LIGNE, mLigne);
		outState.putParcelable(BUNDLE_KEY_SENS, mSens);
		outState.putParcelable(BUNDLE_KEY_ARRET, mArret);
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
		final List<Ligne> lignes = mLigneManager.getAll(getContentResolver());
		lignes.add(0, mAllLignes);
		final LignesArrayAdapter adapter = new LignesArrayAdapter(this, lignes);
		adapter.setHideDivider(true);
		return adapter;
	}

	private ListAdapter getSensAdapter(final String codeLigne) {
		final List<Sens> sens = mSensManager.getAll(getContentResolver(), codeLigne);
		sens.add(0, mAllSens);
		return new SensArrayAdapter(this, sens);
	}

	private ListAdapter getArretsAdapter(final String codeLigne, final String codeSens) {
		final List<Arret> arrets = mArretManager.getAll(getContentResolver(), codeLigne, codeSens);
		arrets.add(0, mAllArrets);
		return new ArretArrayAdapter(this, arrets);
	}

	/**
	 * Afficher la dialog de sélection de la ligne.
	 */
	private void showSelectLigneDialog() {
		showSelectDialog(R.string.target_ligne, mLignesAdapter, mSelectedLignePosition,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						mSelectedLignePosition = which;
						setLigne((Ligne) mLignesAdapter.getItem(which));
						dialog.dismiss();
					}
				});
	}

	/**
	 * Afficher la dialog de sélection du sens.
	 */
	private void showSelectSensDialog(final String codeLigne) {
		if (!codeLigne.equals(mSens.codeLigne) || mSensAdapter == null) {
			mSensAdapter = getSensAdapter(codeLigne);
			mSelectedSensPosition = -1;
		}
		showSelectDialog(R.string.target_sens, mSensAdapter, mSelectedSensPosition,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						mSelectedSensPosition = which;
						setSens((Sens) mSensAdapter.getItem(which));
						dialog.dismiss();
					}
				});
	}

	/**
	 * Afficher la dialog de sélection de l'arret.
	 */
	private void showSelectArretDialog(final String codeLigne, final String codeSens) {
		if (!(codeLigne.equals(mArret.codeLigne) && codeSens.equals(mArret.codeSens)) || mArretsAdapter == null) {
			mArretsAdapter = getArretsAdapter(codeLigne, codeSens);
			mSelectedArretPosition = -1;
		}
		showSelectDialog(R.string.target_arret, mArretsAdapter, mSelectedArretPosition,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int which) {
						mSelectedArretPosition = which;
						setArret((Arret) mArretsAdapter.getItem(which));
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
	 * Envoyer le commentaire s'il a passé les test de
	 * {@link #validateComment(Commentaire)}
	 */
	private void prepareAndSendComment() {
		final Commentaire commentaireItem = new Commentaire();
		if (mArret != null) {
			commentaireItem.setCodeArret(mArret.codeArret);
		}
		if (mSens != null) {
			commentaireItem.setCodeSens(mSens.code);
		}
		if (mLigne != null) {
			commentaireItem.setCodeLigne(mLigne.code);
		}
		commentaireItem.setMessage(mCommentText.getText().toString().trim());

		if (validateComment(commentaireItem)) {
			sendComment(commentaireItem);
		} else {
			InfoDialogUtils.show(this, R.string.dialog_title_invalid_comment, R.string.msg_warning_send_comment);
		}

	}

	/**
	 * Définir la ligne du commentaire et changer la valeur du selecteur.
	 * 
	 * @param ligne
	 *            Le nouvelle ligne
	 */
	private void setLigne(final Ligne ligne) {
		this.mLigne = ligne;
		mTextLigne.setText(ligne.lettre);
		mTextLigne.setTextColor(ligne.couleurTexte);
		if (ligne.couleurBackground == 0) {
			mTextLigne.setBackgroundResource(R.drawable.item_symbole_back);
		} else {
			mTextLigne.setBackgroundDrawable(ColorUtils.getRoundedGradiant(ligne.couleurBackground));
		}

		setSens(mAllSens);

		mBtnChangeSens.setEnabled(!ligne.equals(mAllLignes));
	}

	/**
	 * Définir le sens du commentaire et changer la valeur du selecteur.
	 * 
	 * @param sens
	 *            Le nouveau sens
	 */
	private void setSens(final Sens sens) {
		this.mSens = sens;
		mTextSens.setText(sens.text);

		setArret(mAllArrets);

		mBtnChangeArret.setEnabled(!sens.equals(mAllSens));
	}

	/**
	 * Définir l'arret du commentaire et changer la valeur du selecteur.
	 * 
	 * @param arret
	 *            Le nouvel arret
	 */
	private void setArret(final Arret arret) {
		this.mArret = arret;
		mTextArret.setText(arret.nomArret);
	}

	/**
	 * @return vrai si validé, faux s'il semble louche
	 */
	private boolean validateComment(final Commentaire comment) {
		boolean ret = true;
		final List<CommentaireValidator> validators = new ArrayList<CommentaireValidator>();
		validators.add(new CommentaireSizeValidator());
		validators.add(new CommentaireContentTypeValidator());

		for (final CommentaireValidator commentaireValidator : validators) {
			ret &= commentaireValidator.validate(comment.getMessage());
		}

		return ret;
	}

	private void sendComment(final Commentaire commentaire) {
		if (mSendTask == null || mSendTask.getStatus() == AsyncTask.Status.FINISHED) {
			mSendTask = (SendTask) new SendTask().execute(commentaire);
		}
	}

	/**
	 * Classe d'envoi du message
	 * 
	 * @author romain
	 * 
	 */
	private class SendTask extends AsyncTask<Commentaire, Void, Boolean> {
		private ProgressDialog progressDialog;
		private Exception exception;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(CommentaireActivity.this, "",
					getString(R.string.commentaire_action_transmission), true);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(final Commentaire... args) {
			try {
				final Commentaire messageItem = args[0];

				final PrivateKey privateKey = genKey();
				final String messageHashCode = getMessageHashCode(messageItem, privateKey);

				final CommentaireController commentaireController = new CommentaireController();
				commentaireController.post(messageItem.getCodeLigne(), messageItem.getCodeSens(),
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
				final int msgError = (this.exception instanceof HttpException) ? R.string.dialog_content_comment_sending_error
						: R.string.dialog_content_key_error;

				InfoDialogUtils.show(CommentaireActivity.this, R.string.dialog_title_comment_sending_error, msgError);

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
		private String getMessageHashCode(final Commentaire messageItem, final PrivateKey privateKey)
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

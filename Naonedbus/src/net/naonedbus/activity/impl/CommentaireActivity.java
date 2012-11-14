package net.naonedbus.activity.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

	public static enum Param implements IIntentParamKey {
		idLigne, idSens, idArret
	};

	private static final String BUNDLE_KEY_LIGNE = "ligne";
	private static final String BUNDLE_KEY_SENS = "sens";
	private static final String BUNDLE_KEY_ARRET = "arret";

	private EditText commentText;

	private LigneManager ligneManager;
	private SensManager sensManager;
	private ArretManager arretManager;

	private Ligne ligne;
	private Sens sens;
	private Arret arret;

	private Ligne allLignes;
	private Sens allSens;
	private Arret allArrets;

	private View btnChangeLigne;
	private View btnChangeSens;
	private View btnChangeArret;

	private TextView textLigne;
	private TextView textSens;
	private TextView textArret;

	private ListAdapter lignesAdapter;
	private ListAdapter sensAdapter;
	private ListAdapter arretsAdapter;

	private int selectedLignePosition;
	private int selectedSensPosition;
	private int selectedArretPosition;

	private SendTask sendTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment);

		final SlidingMenuHelper slidingMenuHelper = new SlidingMenuHelper(this);
		slidingMenuHelper.setupActionBar(getSupportActionBar());

		getSupportActionBar().setIcon(R.drawable.ic_launcher);

		ligneManager = LigneManager.getInstance();
		sensManager = SensManager.getInstance();
		arretManager = ArretManager.getInstance();

		allLignes = new Ligne(-1, getString(R.string.target_toutes_lignes),
				getString(R.string.target_toutes_lignes_symbole));
		allLignes.couleurTexte = Color.BLACK;
		allSens = new Sens(-1, getString(R.string.target_tous_sens));
		allArrets = new Arret(-1, getString(R.string.target_tous_arrets));

		lignesAdapter = getLignesAdapter();

		commentText = (EditText) findViewById(android.R.id.input);
		textLigne = (TextView) findViewById(R.id.commentaireLigne);
		textSens = (TextView) findViewById(R.id.commentaireSens);
		textArret = (TextView) findViewById(R.id.commentaireArret);

		btnChangeLigne = findViewById(R.id.commentaireLigneSpinner);
		btnChangeLigne.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showSelectLigneDialog();
			}
		});
		btnChangeSens = findViewById(R.id.commentaireSens);
		btnChangeSens.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showSelectSensDialog(ligne.code);
			}
		});
		btnChangeArret = findViewById(R.id.commentaireArret);
		btnChangeArret.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showSelectArretDialog(ligne.code, sens.code);
			}
		});

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(BUNDLE_KEY_LIGNE)) {
				ligne = (Ligne) savedInstanceState.getSerializable(BUNDLE_KEY_LIGNE);
				if (ligne != null)
					setLigne(ligne);
			}
			if (savedInstanceState.containsKey(BUNDLE_KEY_SENS)) {
				sens = (Sens) savedInstanceState.getSerializable(BUNDLE_KEY_SENS);
				if (sens != null)
					setSens(sens);
			}
			if (savedInstanceState.containsKey(BUNDLE_KEY_ARRET)) {
				arret = (Arret) savedInstanceState.getSerializable(BUNDLE_KEY_ARRET);
				if (arret != null)
					setArret(arret);
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_commentaire, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BUNDLE_KEY_LIGNE, ligne);
		outState.putSerializable(BUNDLE_KEY_SENS, sens);
		outState.putSerializable(BUNDLE_KEY_ARRET, arret);
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

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
		final List<Ligne> lignes = ligneManager.getAll(getContentResolver());
		lignes.add(0, allLignes);
		return new LignesArrayAdapter(this, lignes);
	}

	private ListAdapter getSensAdapter(final String codeLigne) {
		final List<Sens> sens = sensManager.getAll(getContentResolver(), codeLigne);
		sens.add(0, allSens);
		return new SensArrayAdapter(this, sens);
	}

	private ListAdapter getArretsAdapter(final String codeLigne, final String codeSens) {
		final List<Arret> arrets = arretManager.getAll(getContentResolver(), codeLigne, codeSens);
		arrets.add(0, allArrets);
		return new ArretArrayAdapter(this, arrets);
	}

	/**
	 * Afficher la dialog de sélection de la ligne.
	 */
	private void showSelectLigneDialog() {
		showSelectDialog(R.string.target_ligne, lignesAdapter, selectedLignePosition,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedLignePosition = which;
						setLigne((Ligne) lignesAdapter.getItem(which));
						dialog.dismiss();
					}
				});
	}

	/**
	 * Afficher la dialog de sélection du sens.
	 */
	private void showSelectSensDialog(String codeLigne) {
		if (!codeLigne.equals(sens.codeLigne) || sensAdapter == null) {
			sensAdapter = getSensAdapter(codeLigne);
			selectedSensPosition = -1;
		}
		showSelectDialog(R.string.target_sens, sensAdapter, selectedSensPosition,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedSensPosition = which;
						setSens((Sens) sensAdapter.getItem(which));
						dialog.dismiss();
					}
				});
	}

	/**
	 * Afficher la dialog de sélection de l'arret.
	 */
	private void showSelectArretDialog(String codeLigne, String codeSens) {
		if (!(codeLigne.equals(arret.codeLigne) && codeSens.equals(arret.codeSens)) || arretsAdapter == null) {
			arretsAdapter = getArretsAdapter(codeLigne, codeSens);
			selectedArretPosition = -1;
		}
		showSelectDialog(R.string.target_arret, arretsAdapter, selectedArretPosition,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						selectedArretPosition = which;
						setArret((Arret) arretsAdapter.getItem(which));
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
	private void showSelectDialog(int title, ListAdapter adapter, int defaultPosition,
			DialogInterface.OnClickListener onClickListener) {
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
		if (arret != null) {
			commentaireItem.setCodeArret(arret.codeArret);
		}
		if (sens != null) {
			commentaireItem.setCodeSens(sens.code);
		}
		if (ligne != null) {
			commentaireItem.setCodeLigne(ligne.lettre);
		}
		commentaireItem.setMessage(commentText.getText().toString().trim());

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
		this.ligne = ligne;
		textLigne.setText(ligne.lettre);
		textLigne.setTextColor(ligne.couleurTexte);
		if (ligne.couleurBackground == 0) {
			textLigne.setBackgroundResource(R.drawable.item_symbole_back);
		} else {
			textLigne.setBackgroundDrawable(ColorUtils.getRoundedGradiant(ligne.couleurBackground));
		}

		setSens(allSens);

		btnChangeSens.setEnabled(!ligne.equals(allLignes));
	}

	/**
	 * Définir le sens du commentaire et changer la valeur du selecteur.
	 * 
	 * @param sens
	 *            Le nouveau sens
	 */
	private void setSens(final Sens sens) {
		this.sens = sens;
		textSens.setText(sens.text);

		setArret(allArrets);

		btnChangeArret.setEnabled(!sens.equals(allSens));
	}

	/**
	 * Définir l'arret du commentaire et changer la valeur du selecteur.
	 * 
	 * @param arret
	 *            Le nouvel arret
	 */
	private void setArret(final Arret arret) {
		this.arret = arret;
		textArret.setText(arret.nomArret);
	}

	/**
	 * @return vrai si validé, faux s'il semble louche
	 */
	private boolean validateComment(Commentaire comment) {
		boolean ret = true;
		List<CommentaireValidator> validators = new ArrayList<CommentaireValidator>();
		validators.add(new CommentaireSizeValidator());
		validators.add(new CommentaireContentTypeValidator());

		for (CommentaireValidator commentaireValidator : validators) {
			ret &= commentaireValidator.validate(comment.getMessage());
		}

		return ret;
	}

	private void sendComment(Commentaire commentaire) {
		if (sendTask == null || sendTask.getStatus() == AsyncTask.Status.FINISHED) {
			sendTask = (SendTask) new SendTask().execute(commentaire);
		}
	}

	/**
	 * Classe d'envoi du message
	 * 
	 * @author romain
	 * 
	 */
	private class SendTask extends AsyncTask<Commentaire, Void, Boolean> {
		private ProgressDialog progressDialog = null;
		private Exception e;

		@Override
		protected void onPreExecute() {
			progressDialog = ProgressDialog.show(CommentaireActivity.this, "",
					getString(R.string.commentaire_action_transmission), true);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(Commentaire... args) {
			try {
				final Commentaire messageItem = args[0];

				final PrivateKey privateKey = genKey();
				final String messageHashCode = getMessageHashCode(messageItem, privateKey);

				final CommentaireController commentaireController = new CommentaireController();
				commentaireController.post(messageItem.getCodeLigne(), messageItem.getCodeSens(),
						messageItem.getCodeArret(), messageItem.getMessage(), messageHashCode);

				return true;

			} catch (Exception e) {
				this.e = e;
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean success) {
			progressDialog.dismiss();

			if (!success) {
				int msgError = (this.e instanceof HttpException) ? R.string.dialog_content_comment_sending_error
						: R.string.dialog_content_key_error;

				InfoDialogUtils.show(CommentaireActivity.this, R.string.dialog_content_comment_sending_error, msgError);
				BugSenseHandler.sendExceptionMessage("Erreur lors de l'envoi du message.", null, this.e);
			} else {
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
			} catch (GeneralSecurityException e) {
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
		private String getMessageHashCode(Commentaire messageItem, PrivateKey privateKey)
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

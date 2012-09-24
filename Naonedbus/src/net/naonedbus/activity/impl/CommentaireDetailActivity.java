package net.naonedbus.activity.impl;

import java.text.DateFormat;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.intent.IIntentParamKey;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.SmileyParser;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CommentaireDetailActivity extends SherlockActivity {

	public static enum Param implements IIntentParamKey {
		commentaire
	};

	private SlidingMenuHelper slidingMenuHelper;

	private TextView pageTitle;
	private TextView pageSubTitle;
	private TextView ligneCode;
	private ImageView ligneCodeBackground;

	private Commentaire commentaire;
	private TextView itemDescription;
	private TextView itemDate;
	private TextView itemSource;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_detail);

		slidingMenuHelper = new SlidingMenuHelper(this);
		slidingMenuHelper.setupActionBar(getSupportActionBar());

		getSupportActionBar().setIcon(R.drawable.ic_launcher);

		final Typeface robotoLight = Typeface.createFromAsset(this.getAssets(), "fonts/Roboto-Light.ttf");

		pageTitle = (TextView) findViewById(R.id.pageTitle);
		pageSubTitle = (TextView) findViewById(R.id.pageSubTitle);
		ligneCode = (TextView) findViewById(R.id.ligneCode);
		ligneCodeBackground = (ImageView) findViewById(R.id.ligneCodeBackground);

		itemDescription = (TextView) findViewById(R.id.itemDescription);
		itemDescription.setTypeface(robotoLight);
		itemDate = (TextView) findViewById(R.id.itemDate);
		itemSource = (TextView) findViewById(R.id.itemSource);
		commentaire = (Commentaire) getIntent().getSerializableExtra(Param.commentaire.toString());

		if (commentaire != null) {
			SmileyParser.init(getApplicationContext());
			final SmileyParser simSmileyParser = SmileyParser.getInstance();

			final DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
			final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());

			itemDescription.setText(simSmileyParser.addSmileySpans(commentaire.getMessage()));
			itemDate.setText(dateFormat.format(commentaire.getTimestamp()) + " "
					+ timeFormat.format(commentaire.getTimestamp()));

			// itemSource.setText(getString(R.string.source_prefix) + " "
			// +
			// getString(TimeLineConverter.getSourceTitle(commentaire.getSource())));

			if (NaonedbusClient.TWITTER_TAN_TRAFIC.name().equals(commentaire.getSource())) {

				ligneCodeBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.logo_tan));
				ligneCodeBackground.setVisibility(View.VISIBLE);
				ligneCode.setVisibility(View.GONE);
				setPageTitle(getString(R.string.commentaire_tan_info_trafic));

			} else if (NaonedbusClient.NAONEDBUS_SERVICE.name().equals(commentaire.getSource())) {

				ligneCodeBackground.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_launcher));
				ligneCodeBackground.setVisibility(View.VISIBLE);
				ligneCode.setVisibility(View.GONE);
				setPageTitle(getString(R.string.commentaire_message_service));

			} else {

				setLigneSensArret(commentaire);

			}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_commentaire_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.menu_share:
			shareComment();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Proposer de partager l'information
	 */
	private void shareComment() {
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				getCommentaireTitle(commentaire) + "\n" + commentaire.getMessage());

		startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
	}

	/**
	 * Gérer l'affichage de l'information sur la ligne, sens et arrêt
	 * 
	 * @param idLigne
	 * @param idSens
	 * @param idArret
	 */
	protected void setLigneSensArret(Commentaire commentaire) {
		final Ligne ligne = commentaire.getLigne();
		final Sens sens = commentaire.getSens();
		final Arret arret = commentaire.getArret();

		if (ligne != null) {
			setLineColor(ligne.couleurBackground, ligne.code);
			setPageTitle(ligne.nom);
		} else {
			setLineColor(Color.WHITE, "\u221E");
		}

		if (arret == null && sens == null && ligne == null) {
			setPageTitle("Tout le réseau");
		} else {
			if (arret != null) {
				setPageTitle(arret.text);
			}
			if (sens != null) {
				if (arret == null) {
					setPageTitle("\u2192 " + sens.text);
					setPageSubTitle("");
				} else {
					setPageSubTitle("\u2192 " + sens.text);
				}
			} else {
				setPageSubTitle("");
			}
		}

	}

	public void setLineColor(int color, String codeLigne) {
		this.ligneCode.setVisibility(View.VISIBLE);
		this.ligneCode.setBackgroundDrawable(ColorUtils.getRoundedGradiant(color));
		this.ligneCode.setTextColor(ColorUtils.isLightColor(color) ? Color.BLACK : Color.WHITE);
		this.ligneCode.setText(codeLigne);
	}

	protected void setPageTitle(CharSequence title) {
		if (title.length() > 0) {
			this.pageTitle.setVisibility(View.VISIBLE);
			this.pageTitle.setText(title);
		} else {
			this.pageTitle.setVisibility(View.GONE);
		}
	}

	protected void setPageSubTitle(CharSequence title) {
		if (title.length() > 0) {
			this.pageSubTitle.setVisibility(View.VISIBLE);
			this.pageSubTitle.setText(title);
		} else {
			this.pageSubTitle.setVisibility(View.GONE);
		}
	}

	protected String getCommentaireTitle(Commentaire commentaire) {
		String title = "";

		if (NaonedbusClient.TWITTER_TAN_TRAFIC.name().equals(commentaire.getSource())) {
			title = getString(R.string.commentaire_tan_info_trafic);
		} else if (NaonedbusClient.NAONEDBUS_SERVICE.name().equals(commentaire.getSource())) {
			title = getString(R.string.commentaire_message_service);
		} else {
			final Ligne ligne = commentaire.getLigne();
			final Sens sens = commentaire.getSens();
			final Arret arret = commentaire.getArret();

			if (arret == null && sens == null && ligne == null) {
				title = "Tout le réseau";
			} else if (ligne != null) {
				title = "Ligne " + ligne.lettre;
				if (arret != null) {
					title += ", " + arret.text;
				}
				if (sens != null) {
					title += "\u2192 " + sens.text;
				}
			}
		}

		return title;
	}

}

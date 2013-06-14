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

import java.text.DateFormat;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.formatter.CommentaireFomatter;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.utils.SmileyParser;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class CommentaireDetailActivity extends SherlockActivity {

	public static final String PARAM_COMMENTAIRE = "commentaire";

	private HeaderHelper mHeaderHelper;

	private Commentaire mCommentaire;
	private TextView mItemDescription;
	private TextView mItemDate;
	private TextView mItemSource;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_detail);

		final DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(getApplicationContext());
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
		SmileyParser.init(getApplicationContext());
		final SmileyParser simSmileyParser = SmileyParser.getInstance();

		final Typeface robotoMedium = FontUtils.getRobotoMedium(getApplicationContext());

		mHeaderHelper = new HeaderHelper(this);

		mItemDescription = (TextView) findViewById(R.id.itemDescription);
		mItemDate = (TextView) findViewById(R.id.itemDate);
		mItemSource = (TextView) findViewById(R.id.itemSource);

		mCommentaire = getIntent().getParcelableExtra(PARAM_COMMENTAIRE);

		mItemDescription.setText(simSmileyParser.addSmileySpans(mCommentaire.getMessage()));

		mItemDate.setTypeface(robotoMedium);
		mItemDate.setText(dateFormat.format(mCommentaire.getTimestamp()) + " "
				+ timeFormat.format(mCommentaire.getTimestamp()));

		final String source = mCommentaire.getSource();

		mItemSource.setText(getString(R.string.source, getString(CommentaireFomatter.getSourceResId(source))));

		if (NaonedbusClient.TWITTER_TAN_TRAFIC.name().equals(source)) {

			mHeaderHelper.setTitleIcon(R.drawable.logo_tan);
			mHeaderHelper.setTitle(getString(R.string.commentaire_tan_info_trafic));
			mHeaderHelper.setCode(null);
			setHeaderBackgroundColor(getResources().getColor(R.color.message_tan_header));

		} else if (NaonedbusClient.TWITTER_TAN_ACTUS.name().equals(source)) {

			mHeaderHelper.setTitleIcon(R.drawable.logo_tan);
			mHeaderHelper.setTitle(getString(R.string.commentaire_tan_actus));
			mHeaderHelper.setCode(null);
			setHeaderBackgroundColor(getResources().getColor(R.color.message_tan_header));

		} else if (NaonedbusClient.TWITTER_TAN_INFOS.name().equals(source)) {

			mHeaderHelper.setTitleIcon(R.drawable.logo_taninfos);
			mHeaderHelper.setTitle(getString(R.string.commentaire_tan_infos));
			mHeaderHelper.setCode(null);
			setHeaderBackgroundColor(getResources().getColor(R.color.message_taninfos_header));

		} else if (NaonedbusClient.NAONEDBUS_SERVICE.name().equals(source)) {

			mHeaderHelper.setTitleIcon(R.drawable.ic_launcher);
			mHeaderHelper.setTitle(getString(R.string.commentaire_message_service));
			mHeaderHelper.setCode(null);
			setHeaderBackgroundColor(getResources().getColor(R.color.message_service_header));

		} else {

			setLigneSensArret(mCommentaire);

		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater menuInflater = getSupportMenuInflater();
		menuInflater.inflate(R.menu.activity_commentaire_detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
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
		final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				getCommentaireTitle(mCommentaire) + "\n" + mCommentaire.getMessage());

		startActivity(Intent.createChooser(shareIntent, getString(R.string.action_share)));
	}

	/**
	 * Gérer l'affichage de l'information sur la ligne, sens et arrêt
	 * 
	 * @param idLigne
	 * @param idSens
	 * @param idArret
	 */
	protected void setLigneSensArret(final Commentaire commentaire) {
		final Ligne ligne = commentaire.getLigne();
		final Sens sens = commentaire.getSens();
		final Arret arret = commentaire.getArret();

		if (ligne != null) {
			setLineColor(ligne.getCouleur(), ligne.getLettre());
			mHeaderHelper.setTitle(ligne.getNom());
		} else {
			setLineColor(Color.TRANSPARENT, "");
		}

		if (arret == null && sens == null && ligne == null) {
			mHeaderHelper.setTitle(R.string.commentaire_tout);
		} else {
			if (arret != null) {
				mHeaderHelper.setTitle(arret.getNomArret());
			}
			if (sens != null) {
				if (arret == null) {
					mHeaderHelper.setTitle(FormatUtils.formatSens(sens.text));
					mHeaderHelper.setSubTitle(null);
				} else {
					mHeaderHelper.setSubTitle(FormatUtils.formatSens(sens.text));
				}
			} else {
				mHeaderHelper.setSubTitle(null);
			}
		}

	}

	public void setLineColor(final int color, final String lettre) {
		setHeaderBackgroundColor(color);
		mHeaderHelper.setCode(lettre);
	}

	private void setHeaderBackgroundColor(final int color) {
		if (color != Color.TRANSPARENT) {
			mHeaderHelper.setBackgroundColor(color);
		}
	}

	protected String getCommentaireTitle(final Commentaire commentaire) {
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
				title = getString(R.string.commentaire_tout);
			} else if (ligne != null) {
				title = getString(R.string.commentaire_ligne) + " " + ligne.getLettre();
				if (arret != null) {
					title += ", " + arret.getNomArret();
				}
				if (sens != null) {
					title += FormatUtils.formatSens(sens.text);
				}
			}
		}

		return title;
	}

}

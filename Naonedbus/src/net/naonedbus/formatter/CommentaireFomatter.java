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
package net.naonedbus.formatter;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.SensManager;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.SmileyParser;
import net.naonedbus.widget.adapter.impl.CommentaireArrayAdapter;
import net.naonedbus.widget.indexer.impl.CommentaireIndexer;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.content.Context;

import com.ocpsoft.pretty.time.PrettyTime;

public class CommentaireFomatter {

	private static final Map<String, Integer> sSource = new HashMap<String, Integer>();
	static {
		sSource.put(NaonedbusClient.NAONEDBUS.name(), R.string.source_naonedbus);
		sSource.put(NaonedbusClient.TWITTER_TAN_TRAFIC.name(), R.string.source_tan_trafic);
		sSource.put(NaonedbusClient.TWITTER_TAN_ACTUS.name(), R.string.source_tan_actus);
		sSource.put(NaonedbusClient.TWITTER_TAN_INFOS.name(), R.string.source_taninfos);
		sSource.put(NaonedbusClient.NAONEDBUS_SERVICE.name(), R.string.source_naonedbus_service);
	}

	private static final Map<String, Integer> sTitle = new HashMap<String, Integer>();
	static {
		sTitle.put(NaonedbusClient.NAONEDBUS.name(), R.string.source_naonedbus);
		sTitle.put(NaonedbusClient.TWITTER_TAN_TRAFIC.name(), R.string.commentaire_tan_info_trafic);
		sTitle.put(NaonedbusClient.TWITTER_TAN_ACTUS.name(), R.string.commentaire_tan_actus);
		sTitle.put(NaonedbusClient.TWITTER_TAN_INFOS.name(), R.string.commentaire_tan_infos);
		sTitle.put(NaonedbusClient.NAONEDBUS_SERVICE.name(), R.string.commentaire_message_service);
	}

	private final PrettyTime mPrettyTime;

	private final Context mContext;
	private final SmileyParser mSmileyParser;

	final DateMidnight mNow;
	final DateMidnight mYesterday;
	final LigneManager mLigneManager;
	final SensManager mSensManager;
	final ArretManager mArretManager;

	public CommentaireFomatter(final Context context) {
		this.mContext = context;

		SmileyParser.init(context);
		mPrettyTime = new PrettyTime(Locale.getDefault());

		mNow = new DateMidnight();
		mYesterday = mNow.minusDays(1);

		mSmileyParser = SmileyParser.getInstance();
		mLigneManager = LigneManager.getInstance();
		mSensManager = SensManager.getInstance();
		mArretManager = ArretManager.getInstance();
	}

	/**
	 * Générer un ItemAdpater à partir d'une liste de commentaires
	 * 
	 * @param commentaires
	 * @return
	 */
	public void appendToAdapter(final CommentaireArrayAdapter adapter, final List<Commentaire> commentaires) {
		for (final Commentaire commentaire : commentaires) {
			formatValues(commentaire);
			adapter.add(commentaire);
		}
	}

	/**
	 * Récupérer un CommentaireItem avec la ligne, sens et arrêt d'un
	 * Commentaire
	 * 
	 * @param commentaire
	 * @return
	 */

	public void formatValues(final Commentaire commentaire) {
		commentaire.setMessage(mSmileyParser.addSmileySpans(commentaire.getMessage()).toString());
		commentaire.setDateTime(new DateTime(commentaire.getTimestamp()));
		commentaire.setDelay(mPrettyTime.format(commentaire.getDateTime().toDate()));
		commentaire.setSection(getCommentaireSection(commentaire));
		setCommentaireLigne(commentaire);
		setCommentaireSens(commentaire);
		setCommentaireArret(commentaire);
	}

	private Object getCommentaireSection(final Commentaire commentaire) {
		final DateMidnight date = commentaire.getDateTime().toDateMidnight();
		if (date.isAfterNow()) {
			// A venir
			return CommentaireIndexer.SECTION_FUTURE;
		} else if (date.isEqual(mNow)) {
			// Maintenant
			return CommentaireIndexer.SECTION_NOW;
		} else if (date.equals(mYesterday)) {
			// Hier
			return CommentaireIndexer.SECTION_YESTERDAY;
		} else {
			// Précédement
			return CommentaireIndexer.SECTION_PAST;
		}
	}

	/**
	 * Associer les données de la ligne (code & couleur)
	 * 
	 * @param commentaire
	 * @param commentaire
	 */
	private void setCommentaireLigne(final Commentaire commentaire) {
		if (commentaire.getCodeLigne() != null) {
			final Ligne ligne = mLigneManager.getSingle(mContext.getContentResolver(), commentaire.getCodeLigne());
			commentaire.setLigne(ligne);
		}
	}

	/**
	 * Associer les données du sens (nom)
	 * 
	 * @param commentaire
	 * @param commentaireItem
	 */
	private void setCommentaireSens(final Commentaire commentaire) {
		if (commentaire.getCodeSens() != null) {
			final Sens sens = mSensManager.getSingle(mContext.getContentResolver(), commentaire.getCodeLigne(),
					commentaire.getCodeSens());
			commentaire.setSens(sens);
		}
	}

	/**
	 * Associer les données de l'arrêt (nom)
	 * 
	 * @param commentaire
	 * @param commentaireItem
	 */
	private void setCommentaireArret(final Commentaire commentaire) {
		if (commentaire.getCodeArret() != null) {
			final Arret arret = mArretManager.getSingle(mContext.getContentResolver(), commentaire.getCodeArret());
			commentaire.setArret(arret);
		}
	}

	/**
	 * Renvoyer l'id de la resource de la source
	 * 
	 * @param source
	 */
	public static int getSourceResId(final String source) {
		int res = R.string.source_unknown;

		if (sSource.containsKey(source)) {
			res = sSource.get(source);
		}

		return res;
	}

	/**
	 * Renvoyer l'id de la resource du titre de la source
	 * 
	 * @param source
	 */
	public static int getTitleResId(final String source) {
		int res = R.string.source_unknown;

		if (sTitle.containsKey(source)) {
			res = sTitle.get(source);
		}

		return res;
	}
}

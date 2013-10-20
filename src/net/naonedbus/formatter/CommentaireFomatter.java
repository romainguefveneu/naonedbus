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
import java.util.Map;

import net.naonedbus.R;
import net.naonedbus.bean.Stop;
import net.naonedbus.bean.Comment;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Direction;
import net.naonedbus.manager.impl.StopManager;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.manager.impl.DirectionManager;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.SmileyParser;
import net.naonedbus.widget.adapter.impl.CommentArrayAdapter;
import net.naonedbus.widget.indexer.impl.CommentsIndexer;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.content.Context;
import android.text.format.DateUtils;

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

	private final Context mContext;
	private final SmileyParser mSmileyParser;

	final DateMidnight mNow;
	final DateMidnight mYesterday;
	final LigneManager mLigneManager;
	final DirectionManager mSensManager;
	final StopManager mArretManager;

	public CommentaireFomatter(final Context context) {
		this.mContext = context;

		SmileyParser.init(context);

		mNow = new DateMidnight();
		mYesterday = mNow.minusDays(1);

		mSmileyParser = SmileyParser.getInstance();
		mLigneManager = LigneManager.getInstance();
		mSensManager = DirectionManager.getInstance();
		mArretManager = StopManager.getInstance();
	}

	/**
	 * Générer un ItemAdpater à partir d'une liste de commentaires
	 * 
	 * @param commentaires
	 * @return
	 */
	public void appendToAdapter(final CommentArrayAdapter adapter, final List<Comment> commentaires) {
		for (final Comment commentaire : commentaires) {
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

	public void formatValues(final Comment commentaire) {
		commentaire.setMessage(mSmileyParser.addSmileySpans(commentaire.getMessage()).toString());
		commentaire.setDateTime(new DateTime(commentaire.getTimestamp()));
		commentaire.setDelay(DateUtils.getRelativeTimeSpanString(commentaire.getDateTime().getMillis(),
				System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString());
		commentaire.setSection(getCommentaireSection(commentaire));
		setCommentaireLigne(commentaire);
		setCommentaireSens(commentaire);
		setCommentaireArret(commentaire);
	}

	private Object getCommentaireSection(final Comment commentaire) {
		final DateMidnight date = commentaire.getDateTime().toDateMidnight();
		if (date.isAfterNow()) {
			// A venir
			return CommentsIndexer.SECTION_FUTURE;
		} else if (date.isEqual(mNow)) {
			// Maintenant
			return CommentsIndexer.SECTION_NOW;
		} else if (date.equals(mYesterday)) {
			// Hier
			return CommentsIndexer.SECTION_YESTERDAY;
		} else {
			// Précédement
			return CommentsIndexer.SECTION_PAST;
		}
	}

	/**
	 * Associer les données de la ligne (code & couleur)
	 * 
	 * @param commentaire
	 * @param commentaire
	 */
	private void setCommentaireLigne(final Comment commentaire) {
		if (commentaire.getCodeLigne() != null) {
			final Route ligne = mLigneManager.getSingle(mContext.getContentResolver(), commentaire.getCodeLigne());
			commentaire.setRoute(ligne);
		}
	}

	/**
	 * Associer les données du sens (nom)
	 * 
	 * @param commentaire
	 * @param commentaireItem
	 */
	private void setCommentaireSens(final Comment commentaire) {
		if (commentaire.getCodeSens() != null) {
			final Direction sens = mSensManager.getSingle(mContext.getContentResolver(), commentaire.getCodeLigne(),
					commentaire.getCodeSens());
			commentaire.setDirection(sens);
		}
	}

	/**
	 * Associer les données de l'arrêt (nom)
	 * 
	 * @param commentaire
	 * @param commentaireItem
	 */
	private void setCommentaireArret(final Comment commentaire) {
		if (commentaire.getCodeArret() != null) {
			final Stop arret = mArretManager.getSingle(mContext.getContentResolver(), commentaire.getCodeArret());
			commentaire.setStop(arret);
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

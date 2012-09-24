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

	private static final Map<String, Integer> sourceTitle = new HashMap<String, Integer>();
	static {
		sourceTitle.put(NaonedbusClient.NAONEDBUS.name(), R.string.source_naonedbus);
		sourceTitle.put(NaonedbusClient.SIMPLETAN.name(), R.string.source_simpletan);
		sourceTitle.put(NaonedbusClient.TWITTER_TAN_TRAFIC.name(), R.string.source_twitter);
		sourceTitle.put(NaonedbusClient.NAONEDBUS_SERVICE.name(), R.string.source_naonedbus_service);
	}

	private PrettyTime prettyTime;

	private Context context;
	private SmileyParser smileyParser;

	final DateMidnight now = new DateMidnight();
	final DateMidnight yesterday = now.minusDays(1);
	final LigneManager ligneManager;
	final SensManager sensManager;
	final ArretManager arretManager;

	public CommentaireFomatter(Context context) {
		this.context = context;

		SmileyParser.init(context);
		prettyTime = new PrettyTime(new Locale("fr"));

		smileyParser = SmileyParser.getInstance();
		ligneManager = LigneManager.getInstance();
		sensManager = SensManager.getInstance();
		arretManager = ArretManager.getInstance();
	}

	/**
	 * Générer un ItemAdpater à partir d'une liste de commentaires
	 * 
	 * @param commentaires
	 * @return
	 */
	public void appendToAdapter(CommentaireArrayAdapter adapter, List<Commentaire> commentaires) {
		for (Commentaire commentaire : commentaires) {
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

	public void formatValues(Commentaire commentaire) {
		commentaire.setMessage(smileyParser.addSmileySpans(commentaire.getMessage()).toString());
		commentaire.setDateTime(new DateTime(commentaire.getTimestamp()));
		commentaire.setDelay(prettyTime.format(commentaire.getDateTime().toDate()));
		commentaire.setSection(getCommentaireSection(commentaire));
		setCommentaireLigne(commentaire);
		setCommentaireSens(commentaire);
		setCommentaireArret(commentaire);
	}

	private Object getCommentaireSection(Commentaire commentaire) {
		final DateMidnight date = commentaire.getDateTime().toDateMidnight();
		if (date.isAfterNow()) {
			// A venir
			return CommentaireIndexer.SECTION_FUTURE;
		} else if (date.isEqual(now)) {
			// Maintenant
			return CommentaireIndexer.SECTION_NOW;
		} else if (date.equals(yesterday)) {
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
	private void setCommentaireLigne(Commentaire commentaire) {
		if (commentaire.getCodeLigne() != null) {
			final Ligne ligne = ligneManager.getSingle(context.getContentResolver(), commentaire.getCodeLigne());
			commentaire.setLigne(ligne);
		}
	}

	/**
	 * Associer les données du sens (nom)
	 * 
	 * @param commentaire
	 * @param commentaireItem
	 */
	private void setCommentaireSens(Commentaire commentaire) {
		if (commentaire.getCodeSens() != null) {
			final Sens sens = sensManager.getSingle(context.getContentResolver(), commentaire.getCodeLigne(),
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
	private void setCommentaireArret(Commentaire commentaire) {
		if (commentaire.getCodeArret() != null) {
			final Arret arret = arretManager.getSingle(context.getContentResolver(), commentaire.getCodeArret());
			commentaire.setArret(arret);
		}
	}

	/**
	 * Renvoyer l'id de la resource du titre de la source
	 * 
	 * @param source
	 */
	public static int getSourceTitle(String source) {
		int res = R.string.source_unknown;

		if (sourceTitle.containsKey(source)) {
			res = sourceTitle.get(source);
		}

		return res;
	}

}

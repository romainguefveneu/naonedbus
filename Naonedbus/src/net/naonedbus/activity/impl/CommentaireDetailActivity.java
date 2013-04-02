package net.naonedbus.activity.impl;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.helper.HeaderHelper;
import net.naonedbus.helper.SlidingMenuHelper;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.SmileyParser;
import net.naonedbus.utils.SymbolesUtils;
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

	private static final Map<String, Integer> sourceTitle = new HashMap<String, Integer>();
	static {
		sourceTitle.put(NaonedbusClient.NAONEDBUS.name(), R.string.source_naonedbus);
		sourceTitle.put(NaonedbusClient.TWITTER_TAN_TRAFIC.name(), R.string.source_tan_trafic);
		sourceTitle.put(NaonedbusClient.TWITTER_TAN_ACTUS.name(), R.string.source_tan_actus);
		sourceTitle.put(NaonedbusClient.TWITTER_TAN_INFOS.name(), R.string.source_taninfos);
		sourceTitle.put(NaonedbusClient.NAONEDBUS_SERVICE.name(), R.string.source_naonedbus_service);
	}

	private SlidingMenuHelper mSlidingMenuHelper;
	private HeaderHelper mHeaderHelper;

	private Commentaire mCommentaire;
	private TextView mItemDescription;
	private TextView mItemDate;
	private TextView mItemSource;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comment_detail);

		mSlidingMenuHelper = new SlidingMenuHelper(this);
		mSlidingMenuHelper.setupActionBar(getSupportActionBar());

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

		mItemSource.setText(getString(R.string.source, getString(sourceTitle.get(source))));

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
			setLineColor(ligne.couleurBackground, ligne.code);
			mHeaderHelper.setTitle(ligne.nom);
		} else {
			setLineColor(Color.TRANSPARENT, "");
		}

		if (arret == null && sens == null && ligne == null) {
			mHeaderHelper.setTitle(R.string.commentaire_tout);
		} else {
			if (arret != null) {
				mHeaderHelper.setTitle(arret.nomArret);
			}
			if (sens != null) {
				if (arret == null) {
					mHeaderHelper.setTitle(SymbolesUtils.formatSens(sens.text));
					mHeaderHelper.setSubTitle(null);
				} else {
					mHeaderHelper.setSubTitle(SymbolesUtils.formatSens(sens.text));
				}
			} else {
				mHeaderHelper.setSubTitle(null);
			}
		}

	}

	public void setLineColor(final int color, final String codeLigne) {
		setHeaderBackgroundColor(color);
		mHeaderHelper.setCode(codeLigne);
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
				title = getString(R.string.commentaire_ligne) + " " + ligne.lettre;
				if (arret != null) {
					title += ", " + arret.nomArret;
				}
				if (sens != null) {
					title += SymbolesUtils.formatSens(sens.text);
				}
			}
		}

		return title;
	}

}

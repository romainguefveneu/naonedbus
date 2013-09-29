package net.naonedbus.fragment.impl;

import java.text.DateFormat;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.Sens;
import net.naonedbus.formatter.CommentaireFomatter;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.utils.SmileyParser;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class CommentaireDetailDialogFragment extends SherlockDialogFragment {

	public static final String PARAM_COMMENTAIRE = "commentaire";

	private TextView mTitle;
	private TextView mItemCode;
	private ImageView mItemSymbole;
	private View mHeaderDivider;

	private Commentaire mCommentaire;
	private TextView mItemDescription;
	private TextView mItemDate;
	private TextView mItemSource;

	public CommentaireDetailDialogFragment() {
		setStyle(STYLE_NO_FRAME, R.style.ItineraryDialog);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mCommentaire = getArguments().getParcelable(PARAM_COMMENTAIRE);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_comment_detail, null);

		final DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(getActivity());
		final DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
		SmileyParser.init(getActivity());
		final SmileyParser simSmileyParser = SmileyParser.getInstance();

		final Typeface robotoMedium = FontUtils.getRobotoMedium(getActivity());

		mTitle = (TextView) view.findViewById(R.id.itemTitle);
		mItemCode = (TextView) view.findViewById(R.id.itemCode);
		mItemSymbole = (ImageView) view.findViewById(R.id.itemSymbole);
		mHeaderDivider = view.findViewById(R.id.headerDivider);
		view.findViewById(R.id.menu_share).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				shareComment();
			}
		});

		mItemDescription = (TextView) view.findViewById(R.id.itemDescription);
		mItemDate = (TextView) view.findViewById(R.id.itemDate);
		mItemSource = (TextView) view.findViewById(R.id.itemSource);

		mItemDescription.setText(simSmileyParser.addSmileySpans(mCommentaire.getMessage()));

		mItemDate.setTypeface(robotoMedium);
		mItemDate.setText(dateFormat.format(mCommentaire.getTimestamp()) + " "
				+ timeFormat.format(mCommentaire.getTimestamp()));

		final String source = mCommentaire.getSource();

		mItemSource.setText(getString(R.string.source, getString(CommentaireFomatter.getSourceResId(source))));

		if (NaonedbusClient.TWITTER_TAN_TRAFIC.name().equals(source)) {

			setSymbole(R.drawable.logo_tan, getResources().getColor(R.color.message_tan_header));
			setTitle(getString(R.string.commentaire_tan_info_trafic));

		} else if (NaonedbusClient.TWITTER_TAN_ACTUS.name().equals(source)) {

			setSymbole(R.drawable.logo_tan, getResources().getColor(R.color.message_tan_header));
			setTitle(getString(R.string.commentaire_tan_actus));

		} else if (NaonedbusClient.TWITTER_TAN_INFOS.name().equals(source)) {

			setSymbole(R.drawable.logo_taninfos, getResources().getColor(R.color.message_taninfos_header));
			setTitle(getString(R.string.commentaire_tan_infos));

		} else if (NaonedbusClient.NAONEDBUS_SERVICE.name().equals(source)) {

			setSymbole(R.drawable.ic_launcher, getResources().getColor(R.color.message_service_header));
			setTitle(getString(R.string.commentaire_message_service));

		} else {

			setLigneSensArret(mCommentaire);

		}

		return view;
	}

	public void setCode(final CharSequence code, final int backColor, final int textColor) {
		mItemCode.setText(code);
		mItemCode.setBackgroundDrawable(ColorUtils.getRoundedGradiant(backColor));
		mItemCode.setTextColor(textColor);

		setDividerColor(backColor);

		mItemSymbole.setVisibility(View.GONE);
	}

	public void setSymbole(final int resId, final int backColor) {
		mItemSymbole.setImageResource(resId);
		mItemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(backColor));

		setDividerColor(backColor);

		mItemCode.setVisibility(View.INVISIBLE);
	}

	public void setDividerColor(int color) {
		if (color == Color.TRANSPARENT) {
			mHeaderDivider.setBackgroundColor(Color.LTGRAY);
		} else {
			mHeaderDivider.setBackgroundColor(color);
		}
	}

	private void setTitle(final String title) {
		mTitle.setText(title);
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

		String title = "";
		String subtitle = "";

		if (ligne != null) {
			setCode(ligne.getLettre(), ligne.getCouleur(), ligne.getCouleurTexte());
			title = ligne.getNom();
		} else {
			setCode(FormatUtils.TOUT_LE_RESEAU, Color.TRANSPARENT, Color.BLACK);
		}

		if (arret == null && sens == null && ligne == null) {
			title = getString(R.string.commentaire_tout);
		} else {
			if (arret != null) {
				title = arret.getNomArret();
			}
			if (sens != null) {
				if (arret == null) {
					title = FormatUtils.formatSens(sens.text);
				} else {
					subtitle = FormatUtils.formatSens(sens.text);
				}
			}
		}

		setTitle(title + (subtitle == "" ? "" : "\n" + subtitle));
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

package net.naonedbus.fragment.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Direction;
import net.naonedbus.bean.LiveNews;
import net.naonedbus.bean.Route;
import net.naonedbus.bean.Stop;
import net.naonedbus.formatter.LiveNewsFomatter;
import net.naonedbus.security.NaonedbusClient;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FontUtils;
import net.naonedbus.utils.FormatUtils;
import net.naonedbus.utils.SmileyParser;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class LiveNewsDetailDialogFragment extends SherlockDialogFragment {

	public static final String PARAM_LIVENEWS = "liveNews";

	private View mHeaderView;
	private TextView mTitle;
	private TextView mSubtitle;
	private TextView mItemCode;
	private ImageView mItemSymbole;
	private View mHeaderDivider;
	private ImageView mImageView;

	private LiveNews mLiveNews;
	private TextView mItemDescription;
	private TextView mItemDate;
	private TextView mItemSource;

	public LiveNewsDetailDialogFragment() {
		setStyle(STYLE_NO_FRAME, R.style.ItineraryDialog);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLiveNews = getArguments().getParcelable(PARAM_LIVENEWS);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.dialog_livenews_detail, null);

		SmileyParser.init(getActivity());
		final SmileyParser simSmileyParser = SmileyParser.getInstance();

		final Typeface condensed = FontUtils.getRobotoBoldCondensed(getSherlockActivity());

		mHeaderView = view.findViewById(R.id.headerView);
		mTitle = (TextView) view.findViewById(R.id.headerTitle);
		mTitle.setTypeface(condensed);
		mSubtitle = (TextView) view.findViewById(R.id.headerSubTitle);
		mSubtitle.setTypeface(condensed);
		mItemCode = (TextView) view.findViewById(R.id.itemCode);
		mItemCode.setTypeface(condensed);
		mItemSymbole = (ImageView) view.findViewById(R.id.itemSymbole);
		mHeaderDivider = view.findViewById(R.id.headerDivider);
		mImageView = (ImageView) view.findViewById(R.id.menu_share);
		mImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				share();
			}
		});

		mItemDescription = (TextView) view.findViewById(R.id.itemDescription);
		mItemDate = (TextView) view.findViewById(R.id.itemDate);
		mItemSource = (TextView) view.findViewById(R.id.itemSource);

		mItemDescription.setText(simSmileyParser.addSmileySpans(mLiveNews.getMessage()));

		mItemDate.setText(DateUtils.formatDateTime(view.getContext(), mLiveNews.getTimestamp(),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME));

		final String source = mLiveNews.getSource();

		mItemSource.setText(getString(R.string.format_sent_from, getString(LiveNewsFomatter.getSourceResId(source))));

		final Resources res = getResources();

		if (NaonedbusClient.TWITTER_TAN_TRAFIC.name().equals(source)) {

			setSymbole(R.drawable.logo_tan, res.getColor(R.color.message_tan_header),
					res.getColor(R.color.message_tan_text));
			setTitle(getString(R.string.tan_info_trafic));

		} else if (NaonedbusClient.TWITTER_TAN_ACTUS.name().equals(source)) {

			setSymbole(R.drawable.logo_tan, res.getColor(R.color.message_tan_header),
					res.getColor(R.color.message_tan_text));
			setTitle(getString(R.string.tan_actus));

		} else if (NaonedbusClient.TWITTER_TAN_INFOS.name().equals(source)) {

			setSymbole(R.drawable.logo_taninfos, res.getColor(R.color.message_taninfos_header),
					res.getColor(R.color.message_taninfos_text));
			setTitle(getString(R.string.tan_infos));

		} else if (NaonedbusClient.NAONEDBUS_SERVICE.name().equals(source)) {

			setSymbole(R.drawable.ic_launcher, res.getColor(R.color.message_service_header),
					res.getColor(R.color.message_service_text));
			setTitle(getString(R.string.service_message));

		} else {

			setRouteDirectionStop(mLiveNews);

		}

		return view;
	}

	public void setCode(final CharSequence code, final int backColor, final int textColor) {
		mItemCode.setText(code);
		mItemCode.setTextColor(textColor);
		mTitle.setTextColor(textColor);
		mSubtitle.setTextColor(textColor & 0xaaffffff);

		setHeaderColor(backColor, textColor);

		mItemSymbole.setVisibility(View.GONE);
	}

	public void setSymbole(final int resId, final int backColor, final int textColor) {
		mItemSymbole.setImageResource(resId);
		mTitle.setTextColor(textColor);
		mSubtitle.setVisibility(View.GONE);

		setHeaderColor(backColor, textColor);

		mItemCode.setVisibility(View.INVISIBLE);
	}

	public void setHeaderColor(final int backColor, final int textColor) {
		ColorUtils.setBackgroundGradiant(mHeaderView, backColor);
		mHeaderDivider.setBackgroundColor(ColorUtils.getDarkerColor(backColor));

		if (textColor == Color.WHITE) {
			mImageView.setImageResource(R.drawable.ic_action_share);
		} else {
			mImageView.setImageResource(R.drawable.ic_action_share_light);
		}
	}

	private void setTitle(final String title) {
		mTitle.setText(title);
	}

	/**
	 * Proposer de partager l'information
	 */
	private void share() {
		final Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT,
				getCommentaireTitle(mLiveNews) + "\n" + mLiveNews.getMessage());

		startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
	}

	protected void setRouteDirectionStop(final LiveNews liveNews) {
		final Route route = liveNews.getRoute();
		final Direction direction = liveNews.getDirection();
		final Stop stop = liveNews.getStop();

		String title = "";
		String subtitle = "";

		if (route != null) {
			setCode(route.getLetter(), route.getBackColor(), route.getFrontColor());
			title = route.getName();
		} else {
			setCode(FormatUtils.TOUT_LE_RESEAU, Color.WHITE, Color.BLACK);
		}

		if (stop == null && direction == null && route == null) {
			title = getString(R.string.entire_network);
		} else {
			if (stop != null) {
				title = stop.getName();
			}
			if (direction != null) {
				if (stop == null) {
					title = FormatUtils.formatSens(direction.getName());
				} else {
					subtitle = FormatUtils.formatSens(direction.getName());
				}
			}
		}

		mTitle.setText(title);
		if (TextUtils.isEmpty(subtitle)) {
			mSubtitle.setVisibility(View.GONE);
		} else {
			mSubtitle.setText(subtitle);
		}

	}

	protected String getCommentaireTitle(final LiveNews liveNews) {
		String title = "";

		if (NaonedbusClient.TWITTER_TAN_TRAFIC.name().equals(liveNews.getSource())) {
			title = getString(R.string.tan_info_trafic);
		} else if (NaonedbusClient.NAONEDBUS_SERVICE.name().equals(liveNews.getSource())) {
			title = getString(R.string.service_message);
		} else {
			final Route route = liveNews.getRoute();
			final Direction direction = liveNews.getDirection();
			final Stop stop = liveNews.getStop();

			if (stop == null && direction == null && route == null) {
				title = getString(R.string.entire_network);
			} else if (route != null) {
				title = getString(R.string.route) + " " + route.getLetter();
				if (stop != null) {
					title += ", " + stop.getName();
				}
				if (direction != null) {
					title += FormatUtils.formatSens(direction.getName());
				}
			}
		}

		return title;
	}
}

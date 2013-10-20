package net.naonedbus.map.layer;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Equipment;
import net.naonedbus.map.ItemSelectedInfo;
import net.naonedbus.utils.FontUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public abstract class MapLayer {

	private final View mContentView;
	private final TextView mTitle;
	private final TextView mDescription;
	private final ImageView mMoreAction;
	private final LinearLayout mSubView;
	private final LayoutInflater mLayoutInflater;

	public MapLayer(final LayoutInflater inflater) {
		mLayoutInflater = inflater;
		mContentView = inflater.inflate(R.layout.map_info_window, null);

		mTitle = (TextView) mContentView.findViewById(R.id.itemTitle);
		mDescription = (TextView) mContentView.findViewById(R.id.itemDescription);
		mSubView = (LinearLayout) mContentView.findViewById(R.id.lignes);
		mMoreAction = (ImageView) mContentView.findViewById(R.id.moreAction);

		Typeface roboto = FontUtils.getRobotoBoldCondensed(inflater.getContext());
		mTitle.setTypeface(roboto);
	}

	public LayoutInflater getLayoutInflater() {
		return mLayoutInflater;
	}

	public final View getInfoContents(final Object item) {
		final ItemSelectedInfo itemSelectedInfo = getItemSelectedInfo(mContentView.getContext(), (Equipment) item);

		mTitle.setText(itemSelectedInfo.getTitle());

		final List<View> subviews = itemSelectedInfo.getSubview(mSubView);
		if (subviews == null || subviews.isEmpty()) {
			setInfoDescription(itemSelectedInfo.getDescription(mContentView.getContext()));
		} else {
			setSubViews(subviews);
		}

		if (itemSelectedInfo.getIntent(mContentView.getContext()) != null) {
			if (itemSelectedInfo.getResourceAction() != null) {
				mMoreAction.setImageResource(itemSelectedInfo.getResourceAction());
			} else {
				mMoreAction.setImageResource(R.drawable.balloon_disclosure);
			}
			mMoreAction.setVisibility(View.VISIBLE);
		} else {
			mMoreAction.setVisibility(View.INVISIBLE);
		}

		return mContentView;
	}

	public final Intent getClickIntent(final Context context, final Object tag) {
		final ItemSelectedInfo itemSelectedInfo = getItemSelectedInfo(mContentView.getContext(), (Equipment) tag);
		return itemSelectedInfo.getIntent(context);
	}

	private final void setInfoDescription(final CharSequence description) {
		mDescription.setText(description);
		if (TextUtils.isEmpty(description)) {
			mDescription.setVisibility(View.GONE);
		} else {
			mDescription.setVisibility(View.VISIBLE);
		}
		mSubView.setVisibility(View.GONE);
	}

	private final void setSubViews(final List<View> views) {
		mDescription.setVisibility(View.GONE);
		mSubView.removeAllViews();
		for (final View view : views) {
			mSubView.addView(view);
		}
		mSubView.setVisibility(View.VISIBLE);
	}

	protected abstract ItemSelectedInfo getItemSelectedInfo(Context context, final Equipment item);

	public abstract void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint);

}

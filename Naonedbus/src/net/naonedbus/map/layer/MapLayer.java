package net.naonedbus.map.layer;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.map.ItemSelectedInfo;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public abstract class MapLayer<T> {

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
	}

	public LayoutInflater getLayoutInflater() {
		return mLayoutInflater;
	}

	@SuppressWarnings("unchecked")
	public final View getInfoContents(final Object item) {
		final ItemSelectedInfo itemSelectedInfo = getItemSelectedInfo(mContentView.getContext(), (T) item);

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

	@SuppressWarnings("unchecked")
	public final Intent getClickIntent(final Context context, final Object tag) {
		final ItemSelectedInfo itemSelectedInfo = getItemSelectedInfo(mContentView.getContext(), (T) tag);
		return itemSelectedInfo.getIntent(context);
	}

	private final void setInfoDescription(final CharSequence description) {
		mDescription.setText(description);
		if (TextUtils.isEmpty(description)) {
			mDescription.setVisibility(View.GONE);
		} else {
			mDescription.setVisibility(View.VISIBLE);
		}
	}

	private final void setSubViews(final List<View> views) {
		mDescription.setVisibility(View.GONE);
		mSubView.removeAllViews();
		for (final View view : views) {
			mSubView.addView(view);
		}
		mSubView.setVisibility(View.VISIBLE);
	}

	protected abstract ItemSelectedInfo getItemSelectedInfo(Context context, final T item);

	public abstract void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint);

}

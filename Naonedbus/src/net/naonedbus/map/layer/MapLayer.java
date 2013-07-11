package net.naonedbus.map.layer;

import net.naonedbus.R;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public abstract class MapLayer<T> {

	private final View mContentView;
	private final TextView mTitle;
	private final TextView mDescription;

	public MapLayer(final LayoutInflater inflater) {
		mContentView = inflater.inflate(R.layout.map_info_window, null);

		mTitle = ((TextView) mContentView.findViewById(R.id.itemTitle));
		mDescription = ((TextView) mContentView.findViewById(R.id.itemDescription));
	}

	@SuppressWarnings("unchecked")
	public final View getInfoContents(final Object item) {
		bindInfoContents(mContentView.getContext(), (T) item);
		return mContentView;
	}

	protected abstract void bindInfoContents(Context context, final T item);

	public abstract void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint);

	protected final void setInfoTitle(final CharSequence title) {
		mTitle.setText(title);
	}

	protected final void setInfoDescription(final CharSequence description) {
		mDescription.setText(description);
		if (TextUtils.isEmpty(description)) {
			mDescription.setVisibility(View.GONE);
		} else {
			mDescription.setVisibility(View.VISIBLE);
		}
	}

}

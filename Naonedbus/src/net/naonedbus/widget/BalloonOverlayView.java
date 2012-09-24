/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package net.naonedbus.widget;

import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.map.layerloader.ItemSelectedInfo;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * A view representing a MapView marker information balloon.
 * 
 * @author Jeff Gilfelt
 * 
 */
public class BalloonOverlayView extends FrameLayout {

	private LinearLayout layout;
	private RelativeLayout balloon_main_layout;
	private LinearLayout lignesView;
	private ImageView itemSymbole;
	private ImageView moreAction;
	private TextView itemTitle;
	private TextView itemDescription;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context
	 *            - The activity context.
	 * @param balloonBottomOffset
	 *            - The bottom padding (in pixels) to be applied when rendering
	 *            this view.
	 */
	public BalloonOverlayView(Context context, int balloonBottomOffset) {

		super(context);

		setPadding(10, 0, 10, balloonBottomOffset);

		layout = new LimitLinearLayout(context);
		layout.setVisibility(VISIBLE);

		setupView(context, layout);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}

	/**
	 * Inflate and initialize the BalloonOverlayView UI. Override this method to
	 * provide a custom view/layout for the balloon.
	 * 
	 * @param context
	 *            - The activity context.
	 * @param parent
	 *            - The root layout into which you must inflate your view.
	 */
	protected void setupView(Context context, final ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_overlay, parent);
		balloon_main_layout = (RelativeLayout) v.findViewById(R.id.balloon_main_layout);
		itemSymbole = (ImageView) v.findViewById(R.id.itemSymbole);
		moreAction = (ImageView) v.findViewById(R.id.moreAction);
		itemTitle = (TextView) v.findViewById(R.id.itemTitle);
		itemDescription = (TextView) v.findViewById(R.id.itemDescription);
		lignesView = (LinearLayout) v.findViewById(R.id.lignes);
	}

	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item
	 *            - The overlay item containing the relevant view data.
	 */
	public void setData(ItemSelectedInfo item) {
		layout.setVisibility(VISIBLE);
		setBalloonData(item, layout);
	}

	/**
	 * Sets the view data from a given overlay item. Override this method to
	 * create your own data/view mappings.
	 * 
	 * @param item
	 *            - The overlay item containing the relevant view data.
	 * @param parent
	 *            - The parent layout for this BalloonOverlayView.
	 */
	protected void setBalloonData(final ItemSelectedInfo info, ViewGroup parent) {
		itemTitle.setText(info.getTitle());

		// Icône
		final Integer drawableId = info.getResourceDrawable();
		final Integer drawableColorId = info.getResourceColor();
		if (drawableId != null) {
			final Drawable drawable = getResources().getDrawable(drawableId);
			if (drawableColorId == null) {
				itemSymbole.setBackgroundResource(R.drawable.item_symbole_back);
			} else {
				itemSymbole.setBackgroundDrawable(ColorUtils.getRoundedGradiant(getResources()
						.getColor(drawableColorId)));
			}
			itemSymbole.setVisibility(View.VISIBLE);
			itemSymbole.setImageDrawable(drawable);
		} else {
			itemSymbole.setVisibility(View.GONE);
		}

		final List<View> views = info.getSubview(lignesView);
		if (views != null) {
			// Liste des vues
			lignesView.removeAllViews();
			for (final View view : views) {
				lignesView.addView(view);
			}
			lignesView.setVisibility(View.VISIBLE);
			itemDescription.setVisibility(View.GONE);
		} else {
			// Description
			final String description = info.getDescription(getContext());
			lignesView.setVisibility(View.GONE);
			if (description == null) {
				itemDescription.setVisibility(View.GONE);
			} else {
				itemDescription.setText(description);
				itemDescription.setVisibility(View.VISIBLE);
			}
		}

		// Intent
		final Intent intent = info.getIntent(getContext());
		if (intent == null) {
			balloon_main_layout.setOnClickListener(null);
			balloon_main_layout.setClickable(false);
			moreAction.setVisibility(View.GONE);
		} else {
			final Integer actionResource = info.getResourceAction();
			balloon_main_layout.setClickable(true);
			moreAction.setImageResource((actionResource != null) ? actionResource : R.drawable.balloon_disclosure);
			moreAction.setVisibility(View.VISIBLE);
			balloon_main_layout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					getContext().startActivity(info.getIntent(getContext()));
				}
			});
		}

	}

	private class LimitLinearLayout extends LinearLayout {

		private static final int MAX_WIDTH_DP = 280;

		final float SCALE = getContext().getResources().getDisplayMetrics().density;

		public LimitLinearLayout(Context context) {
			super(context);
		}

		public LimitLinearLayout(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			int mode = MeasureSpec.getMode(widthMeasureSpec);
			int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
			int adjustedMaxWidth = (int) (MAX_WIDTH_DP * SCALE + 0.5f);
			int adjustedWidth = Math.min(measuredWidth, adjustedMaxWidth);
			int adjustedWidthMeasureSpec = MeasureSpec.makeMeasureSpec(adjustedWidth, mode);
			super.onMeasure(adjustedWidthMeasureSpec, heightMeasureSpec);
		}
	}

}

package net.naonedbus.map.layer;

import java.util.ArrayList;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.ParcoursActivity;
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.bean.Ligne;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.LigneManager;
import net.naonedbus.map.ItemSelectedInfo;
import net.naonedbus.utils.ColorUtils;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.twotoasters.clusterkraf.ClusterPoint;

public class EquipementMapLayer extends MapLayer<Equipement> {

	public EquipementMapLayer(final LayoutInflater inflater) {
		super(inflater);
	}

	@Override
	public void chooseMarker(final MarkerOptions markerOptions, final ClusterPoint clusterPoint) {
		final Equipement equipement = (Equipement) clusterPoint.getPointAtOffset(0).getTag();
		final Equipement.Type type = equipement.getType();
		final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(type.getMapPin());
		final String title = equipement.getNom();

		markerOptions.icon(icon);
		markerOptions.title(title);
	}

	@Override
	protected ItemSelectedInfo getItemSelectedInfo(final Context context, final Equipement item) {
		return new ItemSelectedInfo() {

			@Override
			public String getTitle() {
				return item.getNom();
			}

			@Override
			public String getDescription(final Context context) {
				return item.getDetails();
			}

			@Override
			public List<View> getSubview(final ViewGroup root) {
				if (item.getType() == Type.TYPE_ARRET) {
					return getStationInfoContents(context, root, item);
				} else {
					return null;
				}
			}

			@Override
			public Integer getResourceAction() {
				return null;
			}

			@Override
			public Intent getIntent(final Context context) {
				if (item.getType() == Type.TYPE_ARRET) {
					final ParamIntent intent = new ParamIntent(context, ParcoursActivity.class);
					intent.putExtra(ParcoursActivity.PARAM_ID_SATION, item.getId());
					return intent;
				} else {
					return null;
				}
			}

		};
	}

	private List<View> getStationInfoContents(final Context context, final ViewGroup root, final Equipement station) {
		final LayoutInflater layoutInflater = getLayoutInflater();
		final List<View> views = new ArrayList<View>();
		final LigneManager ligneManager = LigneManager.getInstance();
		final List<Ligne> lignes = ligneManager.getLignesFromStation(context.getContentResolver(), station.getId());

		for (final Ligne ligneItem : lignes) {
			final TextView textView = (TextView) layoutInflater.inflate(R.layout.ligne_code_item, root, false);
			textView.setTextColor(ligneItem.getCouleurTexte());
			textView.setBackgroundDrawable(ColorUtils.getGradiant(ligneItem.getCouleur()));
			textView.setText(ligneItem.getCode());
			views.add(textView);
		}

		return views;
	}

}

package net.naonedbus.widget.indexer.impl;

import net.naonedbus.R;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.bean.parking.pub.ParkingPublicStatut;
import net.naonedbus.utils.ParkingUtils;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;
import android.util.SparseArray;

public class ParkingPlaceIndexer extends ArraySectionIndexer<ParkingPublic> {

	private static final int SECTION_PLEIN = R.color.parking_state_red;
	private static final int SECTION_LIMITE = R.color.parking_state_orange;
	private static final int SECTION_OUVERT = R.color.parking_state_blue;
	private static final int SECTION_ABONNES = ParkingPublicStatut.ABONNES.getValue();
	private static final int SECTION_FERME = ParkingPublicStatut.FERME.getValue();
	private static final int SECTION_INVALIDE = ParkingPublicStatut.INVALIDE.getValue();

	private static SparseArray<Integer> labels = new SparseArray<Integer>();
	static {
		labels.append(SECTION_PLEIN, R.string.parking_places_disponibles_zero);
		labels.append(SECTION_LIMITE, R.string.parking_section_limite);
		labels.append(SECTION_OUVERT, R.string.parking_section_disponible);
		labels.append(SECTION_ABONNES, R.string.parking_abonne);
		labels.append(SECTION_FERME, R.string.parking_ferme);
		labels.append(SECTION_INVALIDE, R.string.parking_invalide);
	}

	@Override
	protected String getSectionLabel(Context context, ParkingPublic item) {
		return context.getString(labels.get((Integer) item.getSection()));
	}

	@Override
	protected void prepareSection(ParkingPublic item) {
		if (item.getStatut() == ParkingPublicStatut.OUVERT) {
			final int placesDisponibles = item.getPlacesDisponibles();
			item.setSection(ParkingUtils.getSeuilCouleurId(placesDisponibles));
		} else {
			item.setSection(item.getStatut().getValue());
		}
	}

}

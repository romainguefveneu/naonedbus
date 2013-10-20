package net.naonedbus.comparator;

import java.util.Comparator;

import net.naonedbus.bean.Bicloo;
import net.naonedbus.bean.Equipment;
import android.location.Location;

public class BiclooDistanceComparator implements Comparator<Bicloo> {

	/**
	 * Référentiel servant à la comparaison via la méthode
	 * {@link #referentielCompare(Equipment, Equipment)}.
	 */
	private Location mReferentiel;

	public void setReferentiel(final Location referentiel) {
		mReferentiel = referentiel;
	}

	@Override
	public int compare(final Bicloo e1, final Bicloo e2) {
		if (e1 == null || e2 == null)
			return 0;

		if (mReferentiel == null)
			return 0;

		return referentielCompare(e1, e2);
	}

	/**
	 * Comparer les 2 équipements par rapport au {@link #mReferentiel}
	 * 
	 * @param e1
	 * @param e2
	 * @return
	 */
	private int referentielCompare(final Bicloo e1, final Bicloo e2) {
		if (e1.getLocation() == null || e1.getLocation() == null)
			return 0;

		final Float distance1 = mReferentiel.distanceTo(e1.getLocation());
		final Float distance2 = mReferentiel.distanceTo(e2.getLocation());

		return distance1.compareTo(distance2);
	}

}

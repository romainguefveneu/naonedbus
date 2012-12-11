package net.naonedbus.comparator;

import java.util.Comparator;

import net.naonedbus.bean.Ligne;

public class LigneLettreComparator implements Comparator<Ligne> {

	@Override
	public int compare(Ligne a, Ligne b) {
		return a.lettre.compareTo(b.lettre);
	}

}

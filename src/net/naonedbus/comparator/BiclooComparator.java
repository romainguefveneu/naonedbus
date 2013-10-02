package net.naonedbus.comparator;

import java.util.Comparator;

import net.naonedbus.bean.Bicloo;

public class BiclooComparator implements Comparator<Bicloo> {

	@Override
	public int compare(final Bicloo item1, final Bicloo item2) {
		if (item1 == null || item1.getName() == null || item2 == null)
			return 0;

		return item1.getName().compareTo(item2.getName());
	}

}

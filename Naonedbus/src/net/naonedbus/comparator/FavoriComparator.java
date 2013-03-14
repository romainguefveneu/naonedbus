/**
 *  Copyright (C) 2011 Romain Guefveneu
 *  
 *  This file is part of naonedbus.
 *  
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.comparator;

import java.util.Comparator;

import net.naonedbus.bean.Favori;

/**
 * @author romain.guefveneu
 * 
 */
public class FavoriComparator implements Comparator<Favori> {

	@Override
	public int compare(final Favori favori1, final Favori favori2) {
		if (favori1 == null || favori2 == null)
			return 0;

		if (favori1.section != null && favori2.section != null) {
			if (!favori1.section.equals(favori2.section)) {
				return Integer.valueOf(favori1.ordre).compareTo(favori2.ordre);
			}
		}

		if (favori1.codeLigne.equals(favori2.codeLigne)) {
			final String nom1 = (favori1.nomFavori == null) ? favori1.nomArret : favori1.nomFavori;
			final String nom2 = (favori2.nomFavori == null) ? favori2.nomArret : favori2.nomFavori;
			if (nom1 == null || nom2 == null) {
				return 0;
			}
			return nom1.compareTo(nom2);
		} else {
			return favori1.codeLigne.compareTo(favori2.codeLigne);
		}
	}

}

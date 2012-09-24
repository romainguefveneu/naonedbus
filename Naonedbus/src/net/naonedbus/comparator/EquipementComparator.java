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

import net.naonedbus.bean.Equipement;

/**
 * @author romain.guefveneu
 * 
 */
public class EquipementComparator<T extends Equipement> implements Comparator<T> {

	@Override
	public int compare(T item1, T item2) {
		if (item1 == null || item1.getNom() == null || item2 == null)
			return 0;

		return item1.getNom().compareTo(item2.getNom());
	}

}

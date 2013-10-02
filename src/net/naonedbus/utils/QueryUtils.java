/**
 * Copyright (C) 2013 Romain Guefveneu.
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
package net.naonedbus.utils;

import java.util.List;

public abstract class QueryUtils {
	private QueryUtils() {
	};

	/**
	 * Transformer une liste d'object en statement "in" d'une requête SQL. <br />
	 * Par exemple, la liste {@code [1,2,3]} se transforme en "(1,2,3)".
	 * 
	 * @param items
	 *            Les éléments
	 * @return Une chaîne représentant les éléments pouvant être utilisée dans
	 *         un IN de requête SQL.
	 */
	public static String listToInStatement(List<?> items) {
		final StringBuilder sb = new StringBuilder();
		final String sep = ",";
		for (int i = 0; i < items.size(); i++) {
			sb.append(items.get(i));
			if (i < items.size() - 1)
				sb.append(sep);
		}
		sb.toString();

		return sb.toString();
	}
}

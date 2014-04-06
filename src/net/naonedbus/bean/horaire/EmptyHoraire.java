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
package net.naonedbus.bean.horaire;

import org.joda.time.DateMidnight;

public class EmptyHoraire extends Horaire {

	private final int mTextId;

	public EmptyHoraire(final int textId, final DateMidnight date) {
		mTextId = textId;
		setJour(date);
		setHoraire(date.toDateTime());
		setSection(date);
	}

	public int getTextId() {
		return mTextId;
	}

}

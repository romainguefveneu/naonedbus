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
package net.naonedbus.provider.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.joda.time.tz.Provider;

/**
 * @author romain.guefveneu
 * 
 */
public class FastDateTimeZoneProvider implements Provider {
	public static final Set<String> AVAILABLE_IDS = new HashSet<String>();

	static {
		AVAILABLE_IDS.addAll(Arrays.asList(TimeZone.getAvailableIDs()));
	}

	public DateTimeZone getZone(String id) {
		if (id == null) {
			return DateTimeZone.UTC;
		}

		TimeZone tz = TimeZone.getTimeZone(id);
		if (tz == null) {
			return DateTimeZone.UTC;
		}

		int rawOffset = tz.getRawOffset();

		// sub-optimal. could be improved to only create a new Date every few
		// minutes
		if (tz.inDaylightTime(new Date())) {
			rawOffset += tz.getDSTSavings();
		}

		return DateTimeZone.forOffsetMillis(rawOffset);
	}

	public Set<String> getAvailableIDs() {
		return AVAILABLE_IDS;
	}
}

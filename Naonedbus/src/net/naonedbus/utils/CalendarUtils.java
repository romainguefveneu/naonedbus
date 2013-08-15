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

import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;

/**
 * @author romain.guefveneu
 * 
 */
public class CalendarUtils {

	/**
	 * Retourner la liste des calendriers
	 * 
	 * @return Liste des calendriers
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static Map<Integer, String> getCalendars(ContentResolver contentResolver) {
		Map<Integer, String> calendars = new HashMap<Integer, String>();
		String[] projection;
		Uri calendarUri;
		Cursor cursor;
		String accessLevelCol;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			calendarUri = CalendarContract.Calendars.CONTENT_URI;
			projection = new String[] { CalendarContract.Calendars._ID,
					CalendarContract.Calendars.CALENDAR_DISPLAY_NAME };
			accessLevelCol = CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL;
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			calendarUri = Uri.parse("content://com.android.calendar/calendars");
			projection = new String[] { "_id", "displayname" };
			accessLevelCol = "ACCESS_LEVEL";
		} else {
			calendarUri = Uri.parse("content://calendar/calendars");
			projection = new String[] { "_id", "displayname" };
			accessLevelCol = "ACCESS_LEVEL";
		}

		cursor = contentResolver.query(calendarUri, projection, accessLevelCol + "=700", null, null);

		if (cursor != null && cursor.moveToFirst()) {
			while (cursor.isAfterLast() == false) {
				calendars.put(cursor.getInt(0), cursor.getString(1));
				cursor.moveToNext();
			}
			cursor.close();
		}

		return calendars;
	}

	/**
	 * Retourner le nom de calendrier
	 * 
	 * @param id
	 * @return
	 */
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public static String getCalendarName(ContentResolver contentResolver, String id) {
		String name = null;
		Cursor cursor;
		String[] projection;
		Uri calendarUri;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			calendarUri = CalendarContract.Calendars.CONTENT_URI;
			projection = new String[] { CalendarContract.Calendars._ID,
					CalendarContract.Calendars.CALENDAR_DISPLAY_NAME };
		} else if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			calendarUri = Uri.parse("content://com.android.calendar/calendars");
			projection = new String[] { "_id", "displayname" };
		} else {
			calendarUri = Uri.parse("content://calendar/calendars");
			projection = new String[] { "_id", "displayname" };
		}

		cursor = contentResolver.query(calendarUri, projection, "_id = ?", new String[] { id }, null);

		if (cursor.moveToFirst()) {
			name = cursor.getString(1);
		}

		cursor.close();

		return name;
	}

}

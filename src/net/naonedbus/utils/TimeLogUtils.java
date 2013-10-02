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

import android.util.Log;

/**
 * @author romain
 * 
 */
public class TimeLogUtils {

	private long start = 0;
	private long startSilence = 0;
	private String tag = "TimeLog";
	private long total = 0;

	public TimeLogUtils(String tag) {
		this.tag = tag;
	}

	public void start() {
		start = android.os.SystemClock.uptimeMillis();
	}

	public void step(String name) {
		long time = (android.os.SystemClock.uptimeMillis() - start);
		Log.i(tag, "    " + name + " : " + time + "ms");
	}

	public void silentStepFlush(String name) {
		Log.i(tag, "    " + name + " : " + total + "ms");
		total = 0;
	}

	public void silentStepStart() {
		startSilence = android.os.SystemClock.uptimeMillis();
	}

	public void silentStepEnd() {
		total += (android.os.SystemClock.uptimeMillis() - startSilence);
	}

	public long getTotal() {
		return total;
	}

	public void reset() {
		start();
	}

}

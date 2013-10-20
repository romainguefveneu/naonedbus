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
package net.naonedbus.bean;

import android.content.Context;

public class NextHoraireTask {

	private Context mContext;
	private int mId;
	private Stop mArret;
	private int mLimit;
	private String mActionCallback;
	private Throwable mThrowable;

	public Context getContext() {
		return mContext;
	}

	public NextHoraireTask setContext(final Context context) {
		mContext = context;
		return this;
	}

	public int getId() {
		return mId;
	}

	public NextHoraireTask setId(final int id) {
		mId = id;
		return this;
	}

	public Stop getArret() {
		return mArret;
	}

	public NextHoraireTask setArret(final Stop arret) {
		mArret = arret;
		return this;
	}

	public int getLimit() {
		return mLimit;
	}

	public NextHoraireTask setLimit(final int limit) {
		mLimit = limit;
		return this;
	}

	public String getActionCallback() {
		return mActionCallback;
	}

	public NextHoraireTask setActionCallback(final String actionCallback) {
		mActionCallback = actionCallback;
		return this;
	}

	public void setThrowable(final Throwable t) {
		mThrowable = t;
	}

	public Throwable getThrowable() {
		return mThrowable;
	}

	@Override
	public String toString() {
		return String.format("%d|%s|%d|%s", mId, mArret.toString(), mLimit, mActionCallback);
	}

}

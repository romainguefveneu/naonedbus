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
package net.naonedbus.bean;

import android.content.Context;

/**
 * @author romain.guefveneu
 * 
 */
public class NextHoraireTask {

	private Context mContext;
	private int mId;
	private Arret mArret;
	private int mLimit;
	private String mActionCallback;
	private Throwable mThrowable;

	public Context getContext() {
		return mContext;
	}

	public NextHoraireTask setContext(Context context) {
		this.mContext = context;
		return this;
	}

	public int getId() {
		return mId;
	}

	public NextHoraireTask setId(int id) {
		this.mId = id;
		return this;
	}

	public Arret getArret() {
		return mArret;
	}

	public NextHoraireTask setArret(Arret arret) {
		this.mArret = arret;
		return this;
	}

	public int getLimit() {
		return mLimit;
	}

	public NextHoraireTask setLimit(int limit) {
		this.mLimit = limit;
		return this;
	}

	public String getActionCallback() {
		return mActionCallback;
	}

	public NextHoraireTask setActionCallback(String actionCallback) {
		this.mActionCallback = actionCallback;
		return this;
	}

	public void setThrowable(Throwable t) {
		mThrowable = t;
	}

	public Throwable getThrowable() {
		return mThrowable;
	}

	public String toString() {
		return String.format("%d|%s|%d|%s", this.mId, this.mArret.codeArret, this.mLimit, this.mActionCallback);
	}

}

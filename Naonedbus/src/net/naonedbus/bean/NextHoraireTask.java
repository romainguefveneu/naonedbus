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

	private Context context;
	private int id;
	private Arret arret;
	private int limit;
	private String actionCallback;

	public Context getContext() {
		return context;
	}

	public NextHoraireTask setContext(Context context) {
		this.context = context;
		return this;
	}

	public int getId() {
		return id;
	}

	public NextHoraireTask setId(int id) {
		this.id = id;
		return this;
	}

	public Arret getArret() {
		return arret;
	}

	public NextHoraireTask setArret(Arret arret) {
		this.arret = arret;
		return this;
	}

	public int getLimit() {
		return limit;
	}

	public NextHoraireTask setLimit(int limit) {
		this.limit = limit;
		return this;
	}

	public String getActionCallback() {
		return actionCallback;
	}

	public NextHoraireTask setActionCallback(String actionCallback) {
		this.actionCallback = actionCallback;
		return this;
	}
	
	public String toString(){
		return String.format("%d|%s|%d|%s", this.id, this.arret.codeArret, this.limit, this.actionCallback);  
	}

}

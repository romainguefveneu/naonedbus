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

public class Groupe {

	private int mId;
	private String mNom;
	private int mOrdre;
	private int mVisibility;

	public int getId() {
		return mId;
	}

	public void setId(final int id) {
		mId = id;
	}

	public String getNom() {
		return mNom;
	}

	public void setNom(final String nom) {
		mNom = nom;
	}

	public int getOrdre() {
		return mOrdre;
	}

	public void setOrdre(final int ordre) {
		mOrdre = ordre;
	}

	public int getVisibility() {
		return mVisibility;
	}

	public void setVisibility(final int visibility) {
		mVisibility = visibility;
	}

	@Override
	public String toString() {
		return mId + " #" + mOrdre + " - " + mNom;
	}

}

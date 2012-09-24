/*
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

import java.io.Serializable;

import net.naonedbus.widget.item.SectionItem;
import android.graphics.drawable.Drawable;

public class Ligne implements SectionItem, Serializable {
	private static final long serialVersionUID = -3011831995969644561L;

	public int _id;
	public String code;
	public String lettre;
	public String depuis;
	public String vers;
	public String nom;
	public Drawable background;
	public int couleurBackground;
	public int couleurTexte;
	public Object section;

	public Ligne() {
	}

	public Ligne(final int id, final String nom) {
		this._id = id;
		this.nom = nom;
	}

	public Ligne(final int id, final String nom, final String lettre) {
		this(id, nom);
		this.lettre = lettre;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Ligne))
			return false;

		final Ligne autreLigne = (Ligne) o;
		return autreLigne._id == _id;
	}

	@Override
	public Object getSection() {
		return this.section;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(_id).append(";").append(code).append(";").append(depuis).append(";")
				.append(vers).append("]").toString();
	}

}

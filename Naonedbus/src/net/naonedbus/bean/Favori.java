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

import net.naonedbus.widget.item.SectionItem;
import android.graphics.drawable.Drawable;

public class Favori extends Arret implements SectionItem {

	public String nomFavori;
	public String nomSens;
	public int couleurBackground;
	public int couleurTexte;
	public int idGroupe;
	public String nomGroupe;
	public Integer nextHoraire;

	public Drawable background;
	public String delay;

	public Integer section;

	@Override
	public Object getSection() {
		return section;
	}

}

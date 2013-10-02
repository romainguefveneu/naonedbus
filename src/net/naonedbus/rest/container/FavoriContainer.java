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
package net.naonedbus.rest.container;

import java.util.ArrayList;
import java.util.List;

public class FavoriContainer {

	public static final int VERSION = 2;

	public MetaNode meta;
	public List<Groupe> groupes;
	public List<Favori> favoris;

	public FavoriContainer() {
		meta = new MetaNode();
		groupes = new ArrayList<FavoriContainer.Groupe>();
		favoris = new ArrayList<FavoriContainer.Favori>();
	}

	public static class MetaNode {
		public int version = VERSION;
	}

	public static class Groupe {
		public int id;
		public String nom;
		public int ordre;
	}

	public static class Favori {
		public String codeLigne;
		public String codeSens;
		public String codeArret;
		public String nomFavori;
		public List<Integer> idGroupes;
	}

	public void addGroupe(final int id, final String nom, final int ordre) {
		final Groupe groupe = new Groupe();
		groupe.id = id;
		groupe.nom = nom;
		groupe.ordre = ordre;

		groupes.add(groupe);
	}

	public void addFavori(final String codeLigne, final String codeSens, final String codeArret,
			final String nomFavori, final List<Integer> idGroupes) {
		final Favori favori = new Favori();
		favori.codeArret = codeArret;
		favori.codeSens = codeSens;
		favori.codeLigne = codeLigne;
		favori.nomFavori = nomFavori;
		favori.idGroupes = idGroupes;

		favoris.add(favori);
	}

}

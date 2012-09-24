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

/**
 * @author romain
 * 
 */
public class Favori {

	private String codeLigne;
	private String codeArret;
	private String codeSens;
	private String nomFavori;

	public String getCodeLigne() {
		return codeLigne;
	}

	public void setCodeLigne(String codeLigne) {
		this.codeLigne = codeLigne;
	}

	public String getCodeArret() {
		return codeArret;
	}

	public void setCodeArret(String codeArret) {
		this.codeArret = codeArret;
	}

	public String getCodeSens() {
		return codeSens;
	}

	public void setCodeSens(String codeSens) {
		this.codeSens = codeSens;
	}

	public String getNomFavori() {
		return nomFavori;
	}

	public void setNomFavori(String nomFavori) {
		this.nomFavori = nomFavori;
	}

}

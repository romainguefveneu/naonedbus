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
package net.naonedbus.rest.controller.impl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.rest.controller.NodRestController;

import com.google.gson.reflect.TypeToken;

public class InfoTraficController extends NodRestController<List<InfoTrafic>> {
	private static final String API_PREVISIONNEL = "getInfoTraficTANPrevisionnel";
	private static final String API_REEL = "getInfoTraficTANTempsReel";

	@Override
	protected Type getCollectionType() {
		return new TypeToken<List<InfoTrafic>>() {
		}.getType();
	}

	public List<InfoTrafic> getAll() throws IOException {
		final List<InfoTrafic> infosTrafics = new ArrayList<InfoTrafic>();

		final List<InfoTrafic> infosReel = super.getAll(API_REEL, "ROOT", "LISTE_INFOTRAFICS", "INFOTRAFIC");
		if (infosReel != null) {
			infosTrafics.addAll(infosReel);
		}
		final List<InfoTrafic> infosPrevisionnel = super.getAll(API_PREVISIONNEL, "ROOT", "LISTE_INFOTRAFICS",
				"INFOTRAFIC");
		if (infosPrevisionnel != null) {
			infosTrafics.addAll(infosPrevisionnel);
		}
		return infosTrafics;
	}
}

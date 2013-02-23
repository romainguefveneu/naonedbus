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
import java.util.ArrayList;
import java.util.List;

import net.naonedbus.bean.InfoTrafic;
import net.naonedbus.rest.container.HoraireContainer;
import net.naonedbus.rest.controller.NodRestController;

import org.json.JSONException;
import org.json.JSONObject;

public class InfoTraficController extends NodRestController<InfoTrafic> {

	public InfoTraficController() {
		super("opendata", "answer", "data", "ROOT", "LISTE_INFOTRAFICS", "INFOTRAFIC");
	}

	private static final String API_PREVISIONNEL = "getInfoTraficTANPrevisionnel";
	private static final String API_REEL = "getInfoTraficTANTempsReel";

	private static final String TAG_CODE = "CODE";
	private static final String TAG_INTITULE = "INTITULE";
	private static final String TAG_RESUME = "RESUME";
	private static final String TAG_TEXTE_VOCALE = "TEXTE_VOCAL";
	private static final String TAG_DATE_DEBUT = "DATE_DEBUT";
	private static final String TAG_DATE_FIN = "DATE_FIN";
	private static final String TAG_HEURE_DEBUT = "HEURE_DEBUT";
	private static final String TAG_HEURE_FIN = "HEURE_FIN";
	private static final String TAG_PERTURBATION_TERMINEE = "PERTURBATION_TERMINEE";
	private static final String TAG_TRONCONS = "TRONCONS";

	public List<InfoTrafic> getAll() throws IOException {
		final List<InfoTrafic> infosTrafics = new ArrayList<InfoTrafic>();

		final List<InfoTrafic> infosReel = super.getAll(API_REEL);
		if (infosReel != null) {
			infosTrafics.addAll(infosReel);
		}
		final List<InfoTrafic> infosPrevisionnel = super.getAll(API_PREVISIONNEL);
		if (infosPrevisionnel != null) {
			infosTrafics.addAll(infosPrevisionnel);
		}
		return infosTrafics;
	}

	@Override
	protected InfoTrafic parseJsonObject(final JSONObject object) throws JSONException {
		final InfoTrafic infoTrafic = new InfoTrafic();

		infoTrafic.setCode(object.getString(TAG_CODE));
		infoTrafic.setIntitule(object.getString(TAG_INTITULE));
		infoTrafic.setResume(object.getString(TAG_RESUME));
		infoTrafic.setTexteVocal(object.getString(TAG_TEXTE_VOCALE));
		infoTrafic.setDateDebutString(object.getString(TAG_DATE_DEBUT));
		infoTrafic.setDateFinString(object.getString(TAG_DATE_FIN));
		infoTrafic.setHeureDebutString(object.getString(TAG_HEURE_DEBUT));
		infoTrafic.setHeureFinString(object.getString(TAG_HEURE_FIN));
		infoTrafic.setPerturbationTerminee("1".equals(object.getString(TAG_PERTURBATION_TERMINEE)));
		infoTrafic.setTroncons(object.getString(TAG_TRONCONS));

		return infoTrafic;
	}

	@Override
	protected JSONObject toJson(InfoTrafic item) throws JSONException {
		return null;
	}
}

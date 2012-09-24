package net.naonedbus.bean;

import java.util.List;

public class Station extends Equipement {

	private List<Ligne> lignes;

	public List<Ligne> getLignes() {
		return lignes;
	}

	public void setLignes(List<Ligne> lignes) {
		this.lignes = lignes;
	}

}

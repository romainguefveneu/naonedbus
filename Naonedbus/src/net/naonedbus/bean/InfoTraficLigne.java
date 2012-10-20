package net.naonedbus.bean;

import java.util.ArrayList;
import java.util.List;

public class InfoTraficLigne {

	private String numLigne;
	private String libelleTrafic;
	private int etatTrafic;
	private int typeLigne;
	private List<InfoTraficDetail> infosTrafic = new ArrayList<InfoTraficDetail>();

	public String getNumLigne() {
		return numLigne;
	}

	public String getLibelleTrafic() {
		return libelleTrafic;
	}

	public int getEtatTrafic() {
		return etatTrafic;
	}

	public int getTypeLigne() {
		return typeLigne;
	}

	public List<InfoTraficDetail> getInfosTrafic() {
		return infosTrafic;
	}

}

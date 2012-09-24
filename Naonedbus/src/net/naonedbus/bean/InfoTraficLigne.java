package net.naonedbus.bean;

import java.util.List;

import net.naonedbus.widget.item.SectionItem;

public class InfoTraficLigne implements SectionItem {

	private String numLigne;
	private String libelleTrafic;
	private int etatTrafic;
	private int typeLigne;
	private List<InfoTraficDetail> infosTrafic;

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

	public class InfoTraficDetail {
		private int id;
		private String titre;
		private String periode;
		private String type;

		public int getId() {
			return id;
		}

		public String getTitre() {
			return titre;
		}

		public String getPeriode() {
			return periode;
		}

		public String getType() {
			return type;
		}

	}

	@Override
	public Object getSection() {
		return null;
	}

}

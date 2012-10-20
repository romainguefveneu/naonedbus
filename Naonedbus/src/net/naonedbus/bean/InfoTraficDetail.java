package net.naonedbus.bean;

import com.google.gson.annotations.SerializedName;

import net.naonedbus.widget.item.SectionItem;

public class InfoTraficDetail implements SectionItem {
	@SerializedName("id")
	private int mId;
	@SerializedName("titre")
	private String mTitre;
	@SerializedName("periode")
	private String mPeriode;
	@SerializedName("type")
	private String mType;
	private InfoTraficLigne mInfoTraficLigne;

	public int getId() {
		return mId;
	}

	public String getTitre() {
		return mTitre;
	}

	public String getPeriode() {
		return mPeriode;
	}

	public String getType() {
		return mType;
	}

	public String getNumLigne() {
		return mInfoTraficLigne.getNumLigne();
	}

	public void setInfoTraficLigne(InfoTraficLigne infoTraficLigne) {
		this.mInfoTraficLigne = infoTraficLigne;
	}

	@Override
	public Object getSection() {
		return mInfoTraficLigne.getNumLigne();
	}
}
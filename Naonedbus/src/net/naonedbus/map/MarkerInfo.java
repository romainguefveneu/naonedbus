package net.naonedbus.map;

import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;

public class MarkerInfo {

	private int mId;
	private final String mTitle;
	private final Equipement.Type mType;

	public MarkerInfo(final int id, final String title, final Type type) {
		mId = id;
		mTitle = title;
		mType = type;
	}
	
	public int getId(){
		return mId;
	}

	public String getTitle() {
		return mTitle;
	}

	public Equipement.Type getType() {
		return mType;
	}

}

package net.naonedbus.map;

import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;

public class MarkerInfo {

	private final String mTitle;
	private final Equipement.Type mType;

	public MarkerInfo(final String title, final Type type) {
		mTitle = title;
		mType = type;
	}

	public String getTitle() {
		return mTitle;
	}

	public Equipement.Type getType() {
		return mType;
	}

}

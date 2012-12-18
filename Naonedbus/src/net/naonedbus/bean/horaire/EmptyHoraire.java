package net.naonedbus.bean.horaire;

import java.util.Date;

public class EmptyHoraire extends Horaire {

	private static final long serialVersionUID = 7502560532410916394L;

	private int mTextId;

	public EmptyHoraire(int textId, Date date) {
		mTextId = textId;
		setDate(date);
		setSection(date);
	}

	public int getTextId() {
		return mTextId;
	}

}

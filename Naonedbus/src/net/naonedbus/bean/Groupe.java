package net.naonedbus.bean;

public class Groupe {

	private int mId;
	private String mNom;
	private int mOrdre;
	private int mVisibility;

	public int getId() {
		return mId;
	}

	public void setId(final int id) {
		mId = id;
	}

	public String getNom() {
		return mNom;
	}

	public void setNom(final String nom) {
		mNom = nom;
	}

	public int getOrdre() {
		return mOrdre;
	}

	public void setOrdre(final int ordre) {
		mOrdre = ordre;
	}

	public int getVisibility() {
		return mVisibility;
	}

	public void setVisibility(final int visibility) {
		mVisibility = visibility;
	}

	@Override
	public String toString() {
		return mId + ":" + mNom;
	}

}

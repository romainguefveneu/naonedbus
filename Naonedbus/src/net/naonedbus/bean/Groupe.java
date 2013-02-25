package net.naonedbus.bean;

public class Groupe {

	private long mId;
	private String mNom;
	private int mVisibility;

	public long getId() {
		return mId;
	}

	public void setId(long id) {
		mId = id;
	}

	public String getNom() {
		return mNom;
	}

	public void setNom(String nom) {
		mNom = nom;
	}

	public int getVisibility() {
		return mVisibility;
	}

	public void setVisibility(int visibility) {
		mVisibility = visibility;
	}

}

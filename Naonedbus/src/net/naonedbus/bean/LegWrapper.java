package net.naonedbus.bean;

import fr.ybo.opentripplanner.client.modele.Place;

public class LegWrapper {

	public enum Type {
		IN, OUT;
	}

	private Ligne mLigne;

	private final Type mType;

	private String mHeadsign;
	private String mMode;
	private Place mPlace;
	private String mTime;
	private String mDuration;
	private String mDistance;

	public LegWrapper(final Type type) {
		mType = type;
	}

	public Type getType() {
		return mType;
	}

	public Ligne getLigne() {
		return mLigne;
	}

	public String getTime() {
		return mTime;
	}

	public void setLigne(final Ligne ligne) {
		mLigne = ligne;
	}

	public void setTime(final String time) {
		mTime = time;
	}

	public String getDuration() {
		return mDuration;
	}

	public void setDuration(final String duration) {
		mDuration = duration;
	}

	public Place getPlace() {
		return mPlace;
	}

	public void setPlace(final Place place) {
		mPlace = place;
	}

	public String getMode() {
		return mMode;
	}

	public void setMode(final String mode) {
		mMode = mode;
	}

	public String getHeadsign() {
		return mHeadsign;
	}

	public void setHeadsign(final String headsign) {
		mHeadsign = headsign;
	}

	public String getDistance() {
		return mDistance;
	}

	public void setDistance(final String distance) {
		mDistance = distance;
	}

}

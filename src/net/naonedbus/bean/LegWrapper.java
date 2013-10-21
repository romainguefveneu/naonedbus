package net.naonedbus.bean;

import fr.ybo.opentripplanner.client.modele.Place;

public class LegWrapper {

	public enum Type {
		IN, OUT;
	}

	private Route mRoute;

	private final Type mType;

	private String mHeadsign;
	private String mMode;
	private Place mPlace;
	private String mTime;
	private String mDuration;
	private String mDistance;
	private boolean mIsTrip;

	public LegWrapper(final Type type) {
		mType = type;
	}

	public Type getType() {
		return mType;
	}

	public Route getLigne() {
		return mRoute;
	}

	public String getTime() {
		return mTime;
	}

	public void setLigne(final Route route) {
		mRoute = route;
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

	public void setIsTrip(final boolean isTrip) {
		mIsTrip = isTrip;
	}

	public boolean isTrip() {
		return mIsTrip;
	}

}

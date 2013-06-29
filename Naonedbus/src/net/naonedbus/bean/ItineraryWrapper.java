package net.naonedbus.bean;

import java.io.Serializable;
import java.util.List;

import fr.ybo.opentripplanner.client.modele.Itinerary;

public class ItineraryWrapper implements Serializable {

	private static final long serialVersionUID = 5387553827665311443L;

	private final Itinerary mItinerary;
	private String mTime;
	private String mDate;
	private String mWalkTime;
	private transient List<Ligne> mLignes;

	public ItineraryWrapper(final Itinerary itinerary) {
		mItinerary = itinerary;
	}

	public String getTime() {
		return mTime;
	}

	public void setTime(final String time) {
		mTime = time;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(final String date) {
		mDate = date;
	}

	public String getWalkTime() {
		return mWalkTime;
	}

	public void setWalkTime(final String walkTime) {
		mWalkTime = walkTime;
	}

	public List<Ligne> getLignes() {
		return mLignes;
	}

	public void setLignes(final List<Ligne> lignes) {
		mLignes = lignes;
	}

	public Itinerary getItinerary() {
		return mItinerary;
	}

}

package net.naonedbus.bean;

import java.util.List;

import fr.ybo.opentripplanner.client.modele.Itinerary;

public class ItineraryWrapper {

	private final Itinerary mItinerary;
	private String mTitle;
	private String mDate;
	private String mWalkTime;
	private List<Ligne> mLignes;

	public ItineraryWrapper(final Itinerary itinerary) {
		mItinerary = itinerary;
	}

	public String getTitle() {
		return mTitle;
	}

	public void setTitle(final String title) {
		mTitle = title;
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

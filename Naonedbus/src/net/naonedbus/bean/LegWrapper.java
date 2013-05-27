package net.naonedbus.bean;

import fr.ybo.opentripplanner.client.modele.Leg;

public class LegWrapper {

	private final Leg mLeg;
	private Ligne mLigne;

	private String mTime;
	private String mFromTime;
	private String mToTime;

	public LegWrapper(final Leg leg) {
		mLeg = leg;
	}

	public Leg getLeg() {
		return mLeg;
	}

	public Ligne getLigne() {
		return mLigne;
	}

	public String getTime() {
		return mTime;
	}

	public String getFromTime() {
		return mFromTime;
	}

	public String getToTime() {
		return mToTime;
	}

	public void setLigne(final Ligne ligne) {
		mLigne = ligne;
	}

	public void setTime(final String time) {
		mTime = time;
	}

	public void setFromTime(final String fromTime) {
		mFromTime = fromTime;
	}

	public void setToTime(final String toTime) {
		mToTime = toTime;
	}

}

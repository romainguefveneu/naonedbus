package net.naonedbus.bean;

import android.location.Location;

public class Bicloo {
	private enum Status {
		CLOSED, OPEN;
	}

	private int mNumber;
	private String mName;
	private String mAddress;
	private Location mLocation;
	private boolean mBanking;
	private boolean mBonus;
	private Status mStatus;
	private int mBikeStands;
	private int mAvailableBikeStands;
	private int mAvailableBike;
	private long mLastUpdate;

	public int getNumber() {
		return mNumber;
	}

	public void setNumber(final int number) {
		mNumber = number;
	}

	public String getName() {
		return mName;
	}

	public void setName(final String name) {
		mName = name;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setAddress(final String address) {
		mAddress = address;
	}

	public Location getLocation() {
		return mLocation;
	}

	public void setLocation(final Location location) {
		mLocation = location;
	}

	public boolean isBanking() {
		return mBanking;
	}

	public void setBanking(final boolean banking) {
		mBanking = banking;
	}

	public boolean isBonus() {
		return mBonus;
	}

	public void setBonus(final boolean bonus) {
		mBonus = bonus;
	}

	public Status getStatus() {
		return mStatus;
	}

	public void setStatus(final Status status) {
		mStatus = status;
	}

	public void setStatus(final String status) {
		for (final Status s : Status.values()) {
			if (s.name().equals(status)) {
				mStatus = s;
				break;
			}
		}
	}

	public int getBikeStands() {
		return mBikeStands;
	}

	public void setBikeStands(final int bikeStands) {
		mBikeStands = bikeStands;
	}

	public int getAvailableBikeStands() {
		return mAvailableBikeStands;
	}

	public void setAvailableBikeStands(final int availableBikeStands) {
		mAvailableBikeStands = availableBikeStands;
	}

	public int getAvailableBike() {
		return mAvailableBike;
	}

	public void setAvailableBike(final int availableBike) {
		mAvailableBike = availableBike;
	}

	public long getLastUpdate() {
		return mLastUpdate;
	}

	public void setLastUpdate(final long lastUpdate) {
		mLastUpdate = lastUpdate;
	}

}

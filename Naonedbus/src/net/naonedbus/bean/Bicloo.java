package net.naonedbus.bean;

import net.naonedbus.widget.item.SectionItem;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class Bicloo implements SectionItem, Comparable<Bicloo>, Parcelable {
	public static enum Status {
		UNKNOWN, CLOSED, OPEN;
	}

	private int mId;
	private int mNumber;
	private String mName;
	private String mAddress;
	private Location mLocation;
	private boolean mBanking;
	private boolean mBonus;
	private Status mStatus = Status.UNKNOWN;
	private int mBikeStands;
	private int mAvailableBikeStands;
	private int mAvailableBike;
	private long mLastUpdate;

	private Object mSection;
	private Float mDistance;

	public Bicloo() {
	}

	protected Bicloo(final Parcel in) {
		mId = in.readInt();
		mNumber = in.readInt();
		mName = in.readString();
		mAddress = in.readString();
		mLocation = in.readParcelable(Location.class.getClassLoader());
		mBanking = in.readInt() == 1;
		mBonus = in.readInt() == 1;
		mStatus = Status.values()[in.readInt()];
		mBikeStands = in.readInt();
		mAvailableBikeStands = in.readInt();
		mAvailableBike = in.readInt();
		mLastUpdate = in.readLong();
	}

	public void set(final Bicloo bicloo) {
		mId = bicloo.getId();
		mNumber = bicloo.getNumber();
		mName = bicloo.getName();
		mAddress = bicloo.getAddress();
		mLocation = bicloo.getLocation();
		mBanking = bicloo.isBanking();
		mBonus = bicloo.isBonus();
		mStatus = bicloo.getStatus();
		mBikeStands = bicloo.getBikeStands();
		mAvailableBikeStands = bicloo.getAvailableBikeStands();
		mAvailableBike = bicloo.getAvailableBike();
		mLastUpdate = bicloo.getLastUpdate();
	}

	public int getId() {
		return mId;
	}

	public void setId(final int id) {
		mId = id;
	}

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

	public Float getDistance() {
		return mDistance;
	}

	public void setDistance(final Float distance) {
		mDistance = distance;
	}

	@Override
	public int compareTo(final Bicloo another) {
		if (another == null || another.getName() == null || getName() == null) {
			return 0;
		}
		return getName().compareTo(another.getName());
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	public void setSection(final Object section) {
		mSection = section;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(mId);
		dest.writeInt(mNumber);
		dest.writeString(mName);
		dest.writeString(mAddress);
		dest.writeParcelable(mLocation, 0);
		dest.writeInt(mBanking ? 1 : 0);
		dest.writeInt(mBonus ? 1 : 0);
		dest.writeInt(mStatus.ordinal());
		dest.writeInt(mBikeStands);
		dest.writeInt(mAvailableBikeStands);
		dest.writeInt(mAvailableBike);
		dest.writeLong(mLastUpdate);
	}

	public static final Parcelable.Creator<Bicloo> CREATOR = new Parcelable.Creator<Bicloo>() {
		@Override
		public Bicloo createFromParcel(final Parcel in) {
			return new Bicloo(in);
		}

		@Override
		public Bicloo[] newArray(final int size) {
			return new Bicloo[size];
		}
	};

}

package net.naonedbus.bean;

import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.widget.item.SectionItem;

public class AddressResult implements SectionItem {

	private final String mTitle;
	private final String mDescription;
	private final int mIcon;
	private final int mColor;
	private final Double mLatitude;
	private final Double mLongitude;
	private final Equipment.Type mType;
	private boolean mCurrentLocation;

	private String mAddress;
	private Object mSection;

	public AddressResult(final String title, final String description, final Type type, final int icon, final int color,
			final Double latitude, final Double longitude) {
		mTitle = title;
		mDescription = description;
		mType = type;
		mIcon = icon;
		mColor = color;
		mLatitude = latitude;
		mLongitude = longitude;
	}

	public String getTitle() {
		return mTitle;
	}

	public String getDescription() {
		return mDescription;
	}

	public Equipment.Type getType() {
		return mType;
	}

	public int getIcon() {
		return mIcon;
	}

	public int getColor() {
		return mColor;
	}

	public Double getLatitude() {
		return mLatitude;
	}

	public Double getLongitude() {
		return mLongitude;
	}

	public String getAddress() {
		return mAddress == null ? mTitle : mAddress;
	}

	public void setAddress(final String address) {
		mAddress = address;
	}

	public void setSection(final Object section) {
		mSection = section;
	}

	public boolean isCurrentLocation() {
		return mCurrentLocation;
	}

	public void setCurrentLocation(final boolean currentLocation) {
		mCurrentLocation = currentLocation;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

}

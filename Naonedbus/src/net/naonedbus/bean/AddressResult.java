package net.naonedbus.bean;


public class AddressResult {

	private final String mTitle;
	private final String mDescription;
	private final int mIcon;
	private final int mColor;
	private final Double mLatitude;
	private final Double mLongitude;

	public AddressResult(final String title, final String description, final int icon, final int color,
			final Double latitude, final Double longitude) {
		mTitle = title;
		mDescription = description;
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

}

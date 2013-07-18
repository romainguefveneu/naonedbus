package net.naonedbus.bean;

import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.widget.item.SectionItem;

public class AddressResult implements SectionItem {

	private final String mTitle;
	private final String mDescription;
	private final int mIcon;
	private final int mColor;
	private final Double mLatitude;
	private final Double mLongitude;
	private final Equipement.Type mType;
	
	private String mAddress;
	private Object mSection;

	public AddressResult(final String title, final String description, Type type, final int icon, final int color,
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
	
	public Equipement.Type getType(){
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

	@Override
	public Object getSection() {
		return mSection;
	}

}

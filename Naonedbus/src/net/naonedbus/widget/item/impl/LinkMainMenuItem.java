package net.naonedbus.widget.item.impl;

public class LinkMainMenuItem extends MainMenuItem {

	private String mUrl;

	public LinkMainMenuItem(int title, String url, int resIcon, Integer section) {
		super(title, null, resIcon, section);
		mUrl = url;
	}

	public String getUrl() {
		return mUrl;
	}

}

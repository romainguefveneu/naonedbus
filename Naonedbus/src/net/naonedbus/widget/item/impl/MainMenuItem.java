package net.naonedbus.widget.item.impl;

import net.naonedbus.widget.item.SectionItem;

public class MainMenuItem implements SectionItem {

	private int title;
	private Class<?> intentClass;
	private int resIcon;
	private Object section;

	public MainMenuItem(int title, Class<?> intentClass, int resIcon, Integer section) {
		this.title = title;
		this.intentClass = intentClass;
		this.resIcon = resIcon;
		this.section = section;
	}

	@Override
	public Object getSection() {
		return this.section;
	}

	public int getTitle() {
		return title;
	}

	public void setTitle(int title) {
		this.title = title;
	}

	public Class<?> getIntentClass() {
		return intentClass;
	}

	public void setIntentClass(Class<?> intentClass) {
		this.intentClass = intentClass;
	}

	public int getResIcon() {
		return resIcon;
	}

	public void setResIcon(int resIcon) {
		this.resIcon = resIcon;
	}

	public void setSection(Object section) {
		this.section = section;
	}

}

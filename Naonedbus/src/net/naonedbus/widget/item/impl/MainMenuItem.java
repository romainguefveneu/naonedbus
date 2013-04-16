/**
 * Copyright (C) 2013 Romain Guefveneu.
 *   
 *  This file is part of naonedbus.
 *   
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

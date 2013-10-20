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
package net.naonedbus.bean;

public class StopPath {
	private int mId;
	private int mRouteId;
	private int mDirectionId;
	private String mDirectionName;
	private String mRouteCode;
	private String mRouteLetter;
	private int mBackColor;
	private int mFrontColor;

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
	}

	public int getRouteId() {
		return mRouteId;
	}

	public void setRouteId(int routeId) {
		mRouteId = routeId;
	}

	public int getDirectionId() {
		return mDirectionId;
	}

	public void setDirectionId(int directionId) {
		mDirectionId = directionId;
	}

	public String getDirectionName() {
		return mDirectionName;
	}

	public void setDirectionName(String directionName) {
		mDirectionName = directionName;
	}

	public String getRouteCode() {
		return mRouteCode;
	}

	public void setRouteCode(String routeCode) {
		mRouteCode = routeCode;
	}

	public String getRouteLetter() {
		return mRouteLetter;
	}

	public void setRouteLetter(String routeLetter) {
		mRouteLetter = routeLetter;
	}

	public int getBackColor() {
		return mBackColor;
	}

	public void setBackColor(int backColor) {
		mBackColor = backColor;
	}

	public int getFrontColor() {
		return mFrontColor;
	}

	public void setFrontColor(int frontColor) {
		mFrontColor = frontColor;
	}

}

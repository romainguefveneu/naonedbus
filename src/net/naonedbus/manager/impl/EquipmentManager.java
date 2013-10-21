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
package net.naonedbus.manager.impl;

import java.util.List;
import java.util.Set;

import net.naonedbus.R;
import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.Equipment.Type;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.EquipmentProvider;
import net.naonedbus.provider.table.EquipmentTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.location.Location;
import android.net.Uri;

public class EquipmentManager extends SQLiteManager<Equipment> {

	private static EquipmentManager sInstance;

	public static enum SubType {
		PUBLIC_PARK(1, R.drawable.map_layer_parking), INCENTIVE_PARK(2, R.drawable.map_layer_parking_relai);

		private int mValue;
		private int mDrawableRes;

		private SubType(final int value, final int drawableRes) {
			mValue = value;
			mDrawableRes = drawableRes;
		}

		public int getValue() {
			return mValue;
		}

		public int getDrawableRes() {
			return mDrawableRes;
		}

		public static SubType getTypeByValue(final int id) {
			for (final SubType type : SubType.values()) {
				if (type.getValue() == id) {
					return type;
				}
			}
			return null;
		}

	}

	private int mColId;
	private int mColTypeId;
	private int mColSubtypeId;
	private int mColName;
	private int mColNormalizedName;
	private int mColAddress;
	private int mColDetails;
	private int mColPhone;
	private int mColUrl;
	private int mColLatitude;
	private int mColLongitude;

	public static synchronized EquipmentManager getInstance() {
		if (sInstance == null) {
			sInstance = new EquipmentManager();
		}
		return sInstance;
	}

	protected EquipmentManager() {
		super(EquipmentProvider.CONTENT_URI);
	}

	public Type getTypeByEquipmentId(final ContentResolver contentResolver, final int id) {
		final Equipment equipment = getSingle(contentResolver, id);
		return equipment.getType();
	}

	public Equipment getSingle(final ContentResolver contentResolver, final Type type, final int id) {
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(id));

		if (type != null) {
			selection = EquipmentTable.TYPE_ID + "=?";
			selectionArgs = new String[] { String.valueOf(type.getId()) };
		}

		final Cursor c = contentResolver.query(builder.build(), null, selection, selectionArgs, null);
		return getFirstFromCursor(c);
	}

	public Cursor getCursorByType(final ContentResolver contentResolver, final Type type, final String sortOrder) {
		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(type.getId()));

		return contentResolver.query(builder.build(), null, null, null, sortOrder);
	}

	public Cursor getCursorByLocation(final ContentResolver contentResolver, final Type type, final Location location) {
		final String latitude = String.valueOf(location.getLatitude());
		final String longitude = String.valueOf(location.getLongitude());
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_LOCATION_URI_PATH_QUERY);

		builder.appendQueryParameter("latitude", latitude);
		builder.appendQueryParameter("longitude", longitude);
		if (type != null) {
			selection = EquipmentTable.TYPE_ID + "=?";
			selectionArgs = new String[] { String.valueOf(type.getId()) };
		}

		return contentResolver.query(builder.build(), null, selection, selectionArgs, null);
	}

	public List<Equipment> getByType(final ContentResolver contentResolver, final Type type) {
		final Cursor c = getCursorByType(contentResolver, type, null);
		return getFromCursor(c);
	}

	public List<Equipment> getByType(final ContentResolver contentResolver, final Type type, final SubType subtype) {
		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(type.getId()));

		final Cursor c = contentResolver.query(builder.build(), null, EquipmentTable.SUBTYPE_ID + "=?",
				new String[] { String.valueOf(subtype.getValue()) }, null);
		return getFromCursor(c);
	}

	public Cursor getCursorByName(final ContentResolver contentResolver, final Type type, final String name) {
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_URI_PATH_QUERY);
		builder.appendPath(name);

		if (type != null) {
			selection = EquipmentTable.TYPE_ID + "=?";
			selectionArgs = new String[] { String.valueOf(type.getId()) };
		}

		return contentResolver.query(builder.build(), null, selection, selectionArgs, null);
	}

	public List<Equipment> getByName(final ContentResolver contentResolver, final Type type, final String name) {
		final Cursor c = getCursorByName(contentResolver, type, name);
		return getFromCursor(c);
	}

	public List<Equipment> getByLocation(final ContentResolver contentResolver, final Type type,
			final Location location, final Integer limit) {
		final String latitude = String.valueOf(location.getLatitude());
		final String longitude = String.valueOf(location.getLongitude());
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_LOCATION_URI_PATH_QUERY);

		builder.appendQueryParameter("latitude", latitude);
		builder.appendQueryParameter("longitude", longitude);
		if (limit != null) {
			builder.appendQueryParameter("limit", limit.toString());
		}
		if (type != null) {
			selection = EquipmentTable.TYPE_ID + "=?";
			selectionArgs = new String[] { String.valueOf(type.getId()) };
		}

		final Cursor c = contentResolver.query(builder.build(), null, selection, selectionArgs, null);
		return getFromCursor(c);
	}

	public List<Equipment> getByLocation(final ContentResolver contentResolver, final Set<Type> types,
			final Location location, final Integer limit) {
		final String latitude = String.valueOf(location.getLatitude());
		final String longitude = String.valueOf(location.getLongitude());
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_LOCATION_URI_PATH_QUERY);

		builder.appendQueryParameter("latitude", latitude);
		builder.appendQueryParameter("longitude", longitude);
		if (limit != null) {
			builder.appendQueryParameter("limit", limit.toString());
		}
		if (types != null && types.size() > 0) {
			String valueMark = "";
			selectionArgs = new String[types.size()];
			int i = 0;
			for (final Type type : types) {
				selectionArgs[i++] = String.valueOf(type.getId());
				valueMark += "?";
				if (i < types.size()) {
					valueMark += ",";
				}
			}
			selection = EquipmentTable.TYPE_ID + " IN (" + valueMark + ")";
		}

		final Cursor c = contentResolver.query(builder.build(), null, selection, selectionArgs, null);
		return getFromCursor(c);
	}

	public List<Equipment> getByLocationExcludeType(final ContentResolver contentResolver, final Type excludeType,
			final Location location, final Integer limit) {
		final String latitude = String.valueOf(location.getLatitude());
		final String longitude = String.valueOf(location.getLongitude());
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_LOCATION_URI_PATH_QUERY);

		builder.appendQueryParameter("latitude", latitude);
		builder.appendQueryParameter("longitude", longitude);
		if (limit != null) {
			builder.appendQueryParameter("limit", limit.toString());
		}
		if (excludeType != null) {
			selection = EquipmentTable.TYPE_ID + "!=?";
			selectionArgs = new String[] { String.valueOf(excludeType.getId()) };
		}

		final Cursor c = contentResolver.query(builder.build(), null, selection, selectionArgs, null);
		return getFromCursor(c);
	}

	public List<Equipment> getParks(final ContentResolver contentResolver) {
		return getByType(contentResolver, Type.TYPE_PARK);
	}

	public Cursor getParksCursor(final ContentResolver contentResolver, final String sortOrder) {
		return getCursorByType(contentResolver, Type.TYPE_PARK, sortOrder);
	}

	public List<Equipment> getParks(final ContentResolver contentResolver, final SubType subtype) {
		final Uri.Builder builder = EquipmentProvider.CONTENT_URI.buildUpon();
		builder.path(EquipmentProvider.EQUIPEMENTS_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(Type.TYPE_PARK.getId()));

		final Cursor c = contentResolver.query(builder.build(), null, EquipmentTable.SUBTYPE_ID + "=?",
				new String[] { String.valueOf(subtype.getValue()) }, null);
		return getFromCursor(c);
	}

	public List<Equipment> getBicloos(final ContentResolver contentResolver) {
		return getByType(contentResolver, Type.TYPE_BICLOO);
	}

	public Cursor getBicloosCursor(final ContentResolver contentResolver, final String sortOrder) {
		return getCursorByType(contentResolver, Type.TYPE_BICLOO, sortOrder);
	}

	public List<Equipment> getMarguerites(final ContentResolver contentResolver) {
		return getByType(contentResolver, Type.TYPE_MARGUERITE);
	}

	public Cursor getMargueritesCursor(final ContentResolver contentResolver, final String sortOrder) {
		return getCursorByType(contentResolver, Type.TYPE_MARGUERITE, sortOrder);
	}

	public List<Equipment> getCovoiturages(final ContentResolver contentResolver) {
		return getByType(contentResolver, Type.TYPE_CARPOOL);
	}

	public Cursor getCarPoolCursor(final ContentResolver contentResolver, final String sortOrder) {
		return getCursorByType(contentResolver, Type.TYPE_CARPOOL, sortOrder);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(EquipmentTable._ID);
		mColTypeId = c.getColumnIndex(EquipmentTable.TYPE_ID);
		mColSubtypeId = c.getColumnIndex(EquipmentTable.SUBTYPE_ID);
		mColName = c.getColumnIndex(EquipmentTable.EQUIPMENT_NAME);
		mColNormalizedName = c.getColumnIndex(EquipmentTable.NORMALIZED_NAME);
		mColAddress = c.getColumnIndex(EquipmentTable.ADDRESS);
		mColDetails = c.getColumnIndex(EquipmentTable.DETAILS);
		mColPhone = c.getColumnIndex(EquipmentTable.PHONE);
		mColUrl = c.getColumnIndex(EquipmentTable.URL);
		mColLatitude = c.getColumnIndex(EquipmentTable.LATITUDE);
		mColLongitude = c.getColumnIndex(EquipmentTable.LONGITUDE);
	}

	@Override
	public Equipment getSingleFromCursor(final Cursor c) {
		final Equipment item = new Equipment();
		item.setId(c.getInt(mColId));
		item.setType(c.getInt(mColTypeId));
		item.setSubType(c.getInt(mColSubtypeId));
		item.setName(c.getString(mColName));
		item.setNormalizedName(c.getString(mColNormalizedName));
		item.setAddress(c.getString(mColAddress));
		item.setDetails(c.getString(mColDetails));
		item.setPhone(c.getString(mColPhone));
		item.setUrl(c.getString(mColUrl));
		item.setLatitude(c.getDouble(mColLatitude));
		item.setLongitude(c.getDouble(mColLongitude));
		return item;
	}

	public Equipment getSingleFromCursorWrapper(final CursorWrapper c) {
		onIndexCursor(c);
		return getSingleFromCursor(c);
	}

	@Override
	protected ContentValues getContentValues(final Equipment item) {
		return null;
	}

}

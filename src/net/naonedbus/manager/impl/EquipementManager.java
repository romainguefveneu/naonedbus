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
import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.Equipement.Type;
import net.naonedbus.manager.SQLiteManager;
import net.naonedbus.provider.impl.EquipementProvider;
import net.naonedbus.provider.table.EquipementTable;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.location.Location;
import android.net.Uri;

public class EquipementManager extends SQLiteManager<Equipement> {

	private static EquipementManager sInstance;

	/**
	 * Sous-type d'équipement
	 * 
	 * @author romain
	 * 
	 */
	public static enum SousType {
		PARKING_PUBLIC(1, R.drawable.ic_local_parking), PARKING_RELAI(2, R.drawable.ic_local_parking);

		private int value;
		private int drawableRes;

		private SousType(final int value, final int drawableRes) {
			this.value = value;
			this.drawableRes = drawableRes;
		}

		public int getValue() {
			return this.value;
		}

		public int getDrawableRes() {
			return drawableRes;
		}

		public static SousType getTypeByValue(final int id) {
			for (final SousType type : SousType.values()) {
				if (type.getValue() == id) {
					return type;
				}
			}
			return null;
		}

	}

	private int mColId;
	private int mColIdType;
	private int mColIdSousType;
	private int mColCode;
	private int mColNom;
	private int mColNormalizedNom;
	private int mColAdresse;
	private int mColDetails;
	private int mColTelephone;
	private int mColUrl;
	private int mColLatitude;
	private int mColLongitude;

	public static synchronized EquipementManager getInstance() {
		if (sInstance == null) {
			sInstance = new EquipementManager();
		}
		return sInstance;
	}

	protected EquipementManager() {
		super(EquipementProvider.CONTENT_URI);
	}

	/**
	 * Récupérer le type d'un élément selon son id.
	 * 
	 * @param contentResolver
	 * @param id
	 * @param Type
	 * @return un élément
	 */
	public Type getTypeByElementId(final ContentResolver contentResolver, final int id) {
		final Equipement equipement = getSingle(contentResolver, id);
		return equipement.getType();
	}

	/**
	 * Récupérer un élément selon on id et ton type.
	 * 
	 * @param contentResolver
	 * @param type
	 * @param id
	 * @return un élément
	 */
	public Equipement getSingle(final ContentResolver contentResolver, final Type type, final int id) {
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(id));

		if (type != null) {
			selection = EquipementTable.ID_TYPE + "=?";
			selectionArgs = new String[] { String.valueOf(type.getId()) };
		}

		final Cursor c = contentResolver.query(builder.build(), null, selection, selectionArgs, null);
		return getFirstFromCursor(c);
	}

	/**
	 * Récupérer un curseur d'equipements selon un type donné.
	 * 
	 * @param contentResolver
	 * @param type
	 * @return Les equipements correspondants au type
	 */
	public Cursor getEquipementCursorByType(final ContentResolver contentResolver, final Type type,
			final String sortOrder) {
		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(type.getId()));

		return contentResolver.query(builder.build(), null, null, null, sortOrder);
	}

	/**
	 * Récupérer un curseur d'equipements selon un type donné.
	 * 
	 * @param contentResolver
	 * @param type
	 *            Type d'équipement.
	 * @param location
	 *            Position de l'utilisateur.
	 * @return Les equipements correspondants au type trié par distance comparé
	 *         à la location.
	 */
	public Cursor getEquipementCursorByLocation(final ContentResolver contentResolver, final Type type,
			final Location location) {
		final String latitude = String.valueOf(location.getLatitude());
		final String longitude = String.valueOf(location.getLongitude());
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_LOCATION_URI_PATH_QUERY);

		builder.appendQueryParameter("latitude", latitude);
		builder.appendQueryParameter("longitude", longitude);
		if (type != null) {
			selection = EquipementTable.ID_TYPE + "=?";
			selectionArgs = new String[] { String.valueOf(type.getId()) };
		}

		return contentResolver.query(builder.build(), null, selection, selectionArgs, null);
	}

	/**
	 * Récupérer les equipements selon un type donné.
	 * 
	 * @param contentResolver
	 * @param idType
	 * @param sortOrder
	 * @return Les equipements correspondants au type
	 */
	public List<Equipement> getEquipementsByType(final ContentResolver contentResolver, final Type type) {
		final Cursor c = getEquipementCursorByType(contentResolver, type, null);
		return getFromCursor(c);
	}

	/**
	 * Récupérer les équipements selon un type et sous type donnée.
	 * 
	 * @param contentResolver
	 * @param type
	 * @param sousType
	 * @return Les equipements correspondants au type et au sous type.
	 */
	public List<Equipement> getEquipementsByType(final ContentResolver contentResolver, final Type type,
			final SousType sousType) {
		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(type.getId()));

		final Cursor c = contentResolver.query(builder.build(), null, EquipementTable.ID_SOUS_TYPE + "=?",
				new String[] { String.valueOf(sousType.getValue()) }, null);
		return getFromCursor(c);
	}

	/**
	 * Récupérer les équipements à partir d'un nom.
	 * 
	 * @param contentResolver
	 * @param type
	 *            Peut être null.
	 * @param name
	 * @return Les équipements dont le nom correspond
	 */
	public Cursor getEquipementsCursorByName(final ContentResolver contentResolver, final Type type, final String name) {
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_URI_PATH_QUERY);
		builder.appendPath(name);

		if (type != null) {
			selection = EquipementTable.ID_TYPE + "=?";
			selectionArgs = new String[] { String.valueOf(type.getId()) };
		}

		return contentResolver.query(builder.build(), null, selection, selectionArgs, null);
	}

	/**
	 * Récupérer les équipements à partir d'un nom.
	 * 
	 * @param contentResolver
	 * @param type
	 *            Peut être null.
	 * @param name
	 * @return Les équipements dont le nom correspond
	 */
	public List<Equipement> getEquipementsByName(final ContentResolver contentResolver, final Type type,
			final String name) {
		final Cursor c = getEquipementsCursorByName(contentResolver, type, name);
		return getFromCursor(c);
	}

	/**
	 * Réupérer les équipements à proximité d'un point, selon un type
	 * facultatif.
	 * 
	 * @param contentResolver
	 * @param type
	 *            Peut être null.
	 * @param location
	 * @param limit
	 * @return Les equipements à proximité de location.
	 */
	public List<Equipement> getEquipementsByLocation(final ContentResolver contentResolver, final Type type,
			final Location location, final Integer limit) {
		final String latitude = String.valueOf(location.getLatitude());
		final String longitude = String.valueOf(location.getLongitude());
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_LOCATION_URI_PATH_QUERY);

		builder.appendQueryParameter("latitude", latitude);
		builder.appendQueryParameter("longitude", longitude);
		if (limit != null) {
			builder.appendQueryParameter("limit", limit.toString());
		}
		if (type != null) {
			selection = EquipementTable.ID_TYPE + "=?";
			selectionArgs = new String[] { String.valueOf(type.getId()) };
		}

		final Cursor c = contentResolver.query(builder.build(), null, selection, selectionArgs, null);
		return getFromCursor(c);
	}

	/**
	 * Réupérer les équipements à proximité d'un point, selon un type
	 * facultatif.
	 * 
	 * @param contentResolver
	 * @param types
	 *            Peut être null.
	 * @param location
	 * @param limit
	 * @return Les equipements à proximité de location.
	 */
	public List<Equipement> getEquipementsByLocation(final ContentResolver contentResolver, final Set<Type> types,
			final Location location, final Integer limit) {
		final String latitude = String.valueOf(location.getLatitude());
		final String longitude = String.valueOf(location.getLongitude());
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_LOCATION_URI_PATH_QUERY);

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
			selection = EquipementTable.ID_TYPE + " IN (" + valueMark + ")";
		}

		final Cursor c = contentResolver.query(builder.build(), null, selection, selectionArgs, null);
		return getFromCursor(c);
	}

	/**
	 * Réupérer les équipements de tout type saud celui indiqué, à proximité
	 * d'un point.
	 * 
	 * @param contentResolver
	 * @param overlayType
	 *            Peut être null.
	 * @param location
	 * @param limit
	 * @return Les equipements à proximité de location.
	 */
	public List<Equipement> getEquipementsByLocationExcludeType(final ContentResolver contentResolver,
			final Type excludeType, final Location location, final Integer limit) {
		final String latitude = String.valueOf(location.getLatitude());
		final String longitude = String.valueOf(location.getLongitude());
		String selection = null;
		String[] selectionArgs = null;

		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_LOCATION_URI_PATH_QUERY);

		builder.appendQueryParameter("latitude", latitude);
		builder.appendQueryParameter("longitude", longitude);
		if (limit != null) {
			builder.appendQueryParameter("limit", limit.toString());
		}
		if (excludeType != null) {
			selection = EquipementTable.ID_TYPE + "!=?";
			selectionArgs = new String[] { String.valueOf(excludeType.getId()) };
		}

		final Cursor c = contentResolver.query(builder.build(), null, selection, selectionArgs, null);
		return getFromCursor(c);
	}

	/**
	 * Récupérer les parkings.
	 * 
	 * @param contentResolver
	 * @return La liste des parking publics et relai
	 */
	public List<Equipement> getParkings(final ContentResolver contentResolver) {
		return getEquipementsByType(contentResolver, Type.TYPE_PARKING);
	}

	/**
	 * Récupérer les parkings.
	 * 
	 * @param contentResolver
	 * @return La liste des parking publics et relai
	 */
	public Cursor getParkingsCursor(final ContentResolver contentResolver, final String sortOrder) {
		return getEquipementCursorByType(contentResolver, Type.TYPE_PARKING, sortOrder);
	}

	/**
	 * Récupérer les parkings en précisant le tag.
	 * 
	 * @param contentResolver
	 * @return La liste des parking correspondant au tag défini.
	 */
	public List<Equipement> getParkings(final ContentResolver contentResolver, final SousType tag) {
		final Uri.Builder builder = EquipementProvider.CONTENT_URI.buildUpon();
		builder.path(EquipementProvider.EQUIPEMENTS_TYPE_URI_PATH_QUERY);
		builder.appendPath(Integer.toString(Type.TYPE_PARKING.getId()));

		final Cursor c = contentResolver.query(builder.build(), null, EquipementTable.ID_SOUS_TYPE + "=?",
				new String[] { String.valueOf(tag.getValue()) }, null);
		return getFromCursor(c);
	}

	/**
	 * Récupérer les stations de Bicloo.
	 * 
	 * @param contentResolver
	 * @return La liste des stations Bicloo
	 */
	public List<Equipement> getBicloos(final ContentResolver contentResolver) {
		return getEquipementsByType(contentResolver, Type.TYPE_BICLOO);
	}

	/**
	 * Récupérer les stations de Bicloo.
	 * 
	 * @param contentResolver
	 * @return Le curseur des stations Bicloo
	 */
	public Cursor getBicloosCursor(final ContentResolver contentResolver, final String sortOrder) {
		return getEquipementCursorByType(contentResolver, Type.TYPE_BICLOO, sortOrder);
	}

	@Override
	public void onIndexCursor(final Cursor c) {
		mColId = c.getColumnIndex(EquipementTable._ID);
		mColIdType = c.getColumnIndex(EquipementTable.ID_TYPE);
		mColIdSousType = c.getColumnIndex(EquipementTable.ID_SOUS_TYPE);
		mColCode = c.getColumnIndex(EquipementTable.CODE);
		mColNom = c.getColumnIndex(EquipementTable.NOM);
		mColNormalizedNom = c.getColumnIndex(EquipementTable.NORMALIZED_NOM);
		mColAdresse = c.getColumnIndex(EquipementTable.ADRESSE);
		mColDetails = c.getColumnIndex(EquipementTable.DETAILS);
		mColTelephone = c.getColumnIndex(EquipementTable.TELEPHONE);
		mColUrl = c.getColumnIndex(EquipementTable.URL);
		mColLatitude = c.getColumnIndex(EquipementTable.LATITUDE);
		mColLongitude = c.getColumnIndex(EquipementTable.LONGITUDE);
	}

	@Override
	public Equipement getSingleFromCursor(final Cursor c) {
		final Equipement item = new Equipement();
		item.setId(c.getInt(mColId));
		item.setType(c.getInt(mColIdType));
		item.setSousType(c.getInt(mColIdSousType));
		if (mColCode > -1)
			item.setCode(c.getString(mColCode));
		item.setNom(c.getString(mColNom));
		item.setNormalizedNom(c.getString(mColNormalizedNom));
		item.setAdresse(c.getString(mColAdresse));
		item.setDetails(c.getString(mColDetails));
		item.setTelephone(c.getString(mColTelephone));
		item.setUrl(c.getString(mColUrl));
		item.setLatitude(c.getDouble(mColLatitude));
		item.setLongitude(c.getDouble(mColLongitude));
		return item;
	}

	public Equipement getSingleFromCursorWrapper(final CursorWrapper c) {
		onIndexCursor(c);
		return getSingleFromCursor(c);
	}

	@Override
	protected ContentValues getContentValues(final Equipement item) {
		return null;
	}

}

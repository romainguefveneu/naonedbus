/**
 *  Copyright (C) 2011 Romain Guefveneu
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
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;

public class EquipementManager extends SQLiteManager<Equipement> {

	private static EquipementManager instance;

	/**
	 * Sous-type d'équipement
	 * 
	 * @author romain
	 * 
	 */
	public static enum SousType {
		PARKING_PUBLIC(1, R.drawable.map_layer_parking), PARKING_RELAI(2, R.drawable.map_layer_parking_relai);

		private int value;
		private int drawableRes;

		private SousType(int value, int drawableRes) {
			this.value = value;
			this.drawableRes = drawableRes;
		}

		public int getValue() {
			return this.value;
		}

		public int getDrawableRes() {
			return drawableRes;
		}

		public static SousType getTypeByValue(int id) {
			for (SousType type : SousType.values()) {
				if (type.getValue() == id) {
					return type;
				}
			}
			return null;
		}

	}

	public static synchronized EquipementManager getInstance() {
		if (instance == null) {
			instance = new EquipementManager();
		}
		return instance;
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
	public Type getTypeByElementId(ContentResolver contentResolver, int id) {
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
	public Equipement getSingle(ContentResolver contentResolver, Type type, int id) {
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
	public Cursor getEquipementCursorByType(ContentResolver contentResolver, Type type, String sortOrder) {
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
	public Cursor getEquipementCursorByLocation(ContentResolver contentResolver, Type type, Location location) {
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
	public List<Equipement> getEquipementsByType(ContentResolver contentResolver, Type type) {
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
	public List<Equipement> getEquipementsByType(ContentResolver contentResolver, Type type, SousType sousType) {
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
	public Cursor getEquipementsCursorByName(ContentResolver contentResolver, Type type, String name) {
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
	public List<Equipement> getEquipementsByName(ContentResolver contentResolver, Type type, String name) {
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
	public List<Equipement> getEquipementsByLocation(ContentResolver contentResolver, Type type, Location location,
			Integer limit) {
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
	public List<Equipement> getEquipementsByLocation(ContentResolver contentResolver, Set<Type> types,
			Location location, Integer limit) {
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
			for (Type type : types) {
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
	 * @param type
	 *            Peut être null.
	 * @param location
	 * @param limit
	 * @return Les equipements à proximité de location.
	 */
	public List<Equipement> getEquipementsByLocationExcludeType(ContentResolver contentResolver, Type excludeType,
			Location location, Integer limit) {
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
	public List<Equipement> getParkings(ContentResolver contentResolver) {
		return getEquipementsByType(contentResolver, Type.TYPE_PARKING);
	}

	/**
	 * Récupérer les parkings.
	 * 
	 * @param contentResolver
	 * @return La liste des parking publics et relai
	 */
	public Cursor getParkingsCursor(ContentResolver contentResolver, String sortOrder) {
		return getEquipementCursorByType(contentResolver, Type.TYPE_PARKING, sortOrder);
	}

	/**
	 * Récupérer les parkings en précisant le tag.
	 * 
	 * @param contentResolver
	 * @return La liste des parking correspondant au tag défini.
	 */
	public List<Equipement> getParkings(ContentResolver contentResolver, SousType tag) {
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
	public List<Equipement> getBicloos(ContentResolver contentResolver) {
		return getEquipementsByType(contentResolver, Type.TYPE_BICLOO);
	}

	/**
	 * Récupérer les stations de Bicloo.
	 * 
	 * @param contentResolver
	 * @return Le curseur des stations Bicloo
	 */
	public Cursor getBicloosCursor(ContentResolver contentResolver, String sortOrder) {
		return getEquipementCursorByType(contentResolver, Type.TYPE_BICLOO, sortOrder);
	}

	/**
	 * Récupérer les stations Marguerite.
	 * 
	 * @param contentResolver
	 * @return La liste des stations Marguerite
	 */
	public List<Equipement> getMarguerites(ContentResolver contentResolver) {
		return getEquipementsByType(contentResolver, Type.TYPE_MARGUERITE);
	}

	/**
	 * Récupérer les stations Marguerite.
	 * 
	 * @param contentResolver
	 * @return Le curseur stations Marguerite
	 */
	public Cursor getMargueritesCursor(ContentResolver contentResolver, String sortOrder) {
		return getEquipementCursorByType(contentResolver, Type.TYPE_MARGUERITE, sortOrder);
	}

	/**
	 * Récupérer les stations de covoiturage.
	 * 
	 * @param contentResolver
	 * @return La liste des stations de covoiturage
	 */
	public List<Equipement> getCovoiturages(ContentResolver contentResolver) {
		return getEquipementsByType(contentResolver, Type.TYPE_COVOITURAGE);
	}

	/**
	 * Récupérer les stations de covoiturage.
	 * 
	 * @param contentResolver
	 * @return Le cursuer des stations de covoiturage
	 */
	public Cursor getCovoituresCursor(ContentResolver contentResolver, String sortOrder) {
		return getEquipementCursorByType(contentResolver, Type.TYPE_COVOITURAGE, sortOrder);
	}

	@Override
	protected Equipement getSingleFromCursor(Cursor c) {
		Equipement item = new Equipement();
		item.setId(c.getInt(c.getColumnIndex(EquipementTable._ID)));
		item.setType(c.getInt(c.getColumnIndex(EquipementTable.ID_TYPE)));
		item.setSousType(c.getInt(c.getColumnIndex(EquipementTable.ID_SOUS_TYPE)));
		item.setNom(c.getString(c.getColumnIndex(EquipementTable.NOM)));
		item.setNormalizedNom(c.getString(c.getColumnIndex(EquipementTable.NORMALIZED_NOM)));
		item.setAdresse(c.getString(c.getColumnIndex(EquipementTable.ADRESSE)));
		item.setDetails(c.getString(c.getColumnIndex(EquipementTable.DETAILS)));
		item.setTelephone(c.getString(c.getColumnIndex(EquipementTable.TELEPHONE)));
		item.setUrl(c.getString(c.getColumnIndex(EquipementTable.URL)));
		item.setLatitude(c.getDouble(c.getColumnIndex(EquipementTable.LATITUDE)));
		item.setLongitude(c.getDouble(c.getColumnIndex(EquipementTable.LONGITUDE)));
		return item;
	}

}

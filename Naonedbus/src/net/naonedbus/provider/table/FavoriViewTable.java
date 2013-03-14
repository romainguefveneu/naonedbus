package net.naonedbus.provider.table;

import android.provider.BaseColumns;

public interface FavoriViewTable extends BaseColumns {
	public static final String TABLE_NAME = "favorisView";

	public static final String ID_STATION = "idStation";
	public static final String ID_GROUPE = "idGroupe";

	public static final String CODE_LIGNE = "codeLigne";
	public static final String CODE_SENS = "codeSens";
	public static final String CODE_ARRET = "codeArret";
	public static final String CODE_EQUIPEMENT = "codeEquipement";

	public static final String NOM_FAVORI = "nomFavori";
	public static final String NOM_ARRET = "nomArret";
	public static final String NOM_NORMALIZED = "normalizedNom";
	public static final String NON_SENS = "nomSens";
	public static final String NOM_GROUPE = "nomGroupe";

	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String COULEUR = "ligneCouleur";
	public static final String LETTRE = "ligneLettre";
	public static final String TYPE = "ligneType";
	
	public static final String ORDRE_GROUPE = "ordreGroupe";
	
	public static final String NEXT_HORAIRE = "nextHoraire";

	//@formatter:off
	public static final String ORDER = 
			ORDRE_GROUPE + "," + 
			TYPE + 
			",CAST(" + CODE_LIGNE + " as numeric)," + 
			NOM_FAVORI	+ "," +
			NOM_ARRET;
	//@formatter:on

	public static final String WHERE = ID_GROUPE + " IN (%s) OR NOT EXISTS (SELECT 1 FROM "
			+ FavorisGroupesTable.TABLE_NAME + " WHERE " + FavorisGroupesTable.ID_FAVORI + " = " + TABLE_NAME + "."
			+ _ID + ")";
}

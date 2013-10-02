package net.naonedbus.provider.table;

import android.provider.BaseColumns;

public interface CommentaireTable extends BaseColumns {
	public static final String TABLE_NAME = "commentaires";

	public static final String CODE_LIGNE = "codeLigne";
	public static final String CODE_SENS = "codeSens";
	public static final String CODE_ARRET = "codeArret";
	public static final String MESSAGE = "message";
	public static final String SOURCE = "source";
	public static final String TIMESTAMP = "timestamp";

}

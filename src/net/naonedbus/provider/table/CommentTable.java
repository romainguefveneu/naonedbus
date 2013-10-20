package net.naonedbus.provider.table;

import android.provider.BaseColumns;

public interface CommentTable extends BaseColumns {
	public static final String TABLE_NAME = "comments";

	public static final String ROUTE_CODE = "routeCode";
	public static final String DIRECTION_CODE = "directionCode";
	public static final String STOP_CODE = "stopCode";
	public static final String MESSAGE = "message";
	public static final String SOURCE = "source";
	public static final String TIMESTAMP = "timestamp";
}

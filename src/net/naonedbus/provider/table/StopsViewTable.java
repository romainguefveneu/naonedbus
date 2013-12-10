package net.naonedbus.provider.table;

import android.provider.BaseColumns;

public interface StopsViewTable extends BaseColumns {
	public static final String TABLE_NAME = "stopsView";

	public static final String ROUTE_CODE = "routeCode";
	public static final String ROUTE_LETTER = "letter";
	public static final String SERVICE_ID = "serviceId";
	public static final String DIRECTION_CODE = "directionCode";
	public static final String STOP_CODE = "stopCode";
	public static final String STOP_ORDER = "stopOrder";
	public static final String STEP_TYPE = "stepType";
	public static final String EQUIPMENT_ID = "equipmentId";
	public static final String EQUIPMENT_CODE = "equipmentCode";
	public static final String NAME = "equipmentName";
	public static final String NORMALIZED_NAME = "equipmentName";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
}

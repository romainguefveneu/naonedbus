package net.naonedbus.provider.table;

public interface StopDirectionTable {
	public static final String TABLE_NAME = "stopDirectionsView";

	public static final String STOP_ID = "stopId";
	public static final String ROUTE_CODE = "routeCode";
	public static final String ROUTE_ID = "routeId";
	public static final String ROUTE_LETTER = "letter";
	public static final String ROUTE_BACK_COLOR = "backColor";
	public static final String ROUTE_FRONT_COLOR = "frontColor";
	public static final String ROUTE_TYPE_ID = "routeTypeId";
	public static final String DIRECTION_ID = "directionId";
	public static final String DIRECTION_NAME = "directionName";
	public static final String NORMALIZED_NAME = "normalizedName";

	public static final String GROUP_BY = ROUTE_ID + "," + DIRECTION_ID;
	public static final String ORDER_BY = ROUTE_TYPE_ID + "," + ROUTE_ID + ", CAST(" + ROUTE_CODE + " AS NUMERIC)";
}

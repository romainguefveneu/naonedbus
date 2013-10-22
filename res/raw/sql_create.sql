CREATE TABLE IF NOT EXISTS routeTypes (
	_id INT PRIMARY KEY, 
	typeName text);
CREATE INDEX IF NOT EXISTS routeTypes_id ON routeTypes (_id);

CREATE TABLE IF NOT EXISTS routes (
	_id INT PRIMARY KEY, 
	typeId SMALLINT,
	routeCode TEXT, 
	letter TEXT, 
	backColor INT, 
    frontColor INT, 
	headsignFrom TEXT, 
	headsignTo TEXT);
CREATE INDEX IF NOT EXISTS routes_id ON routes (_id);
CREATE INDEX IF NOT EXISTS routes_code ON routes (routeCode);
CREATE INDEX IF NOT EXISTS routes_letter ON routes (letter);

CREATE TABLE IF NOT EXISTS equipmentTypes (
	_id INT PRIMARY KEY, 
	typeName TEXT NOT NULL);
CREATE INDEX IF NOT EXISTS equipmentTypes_id ON equipmentTypes (_id);

CREATE TABLE IF NOT EXISTS equipments (
	_id INT NOT NULL, 
	typeId INT NOT NULL,
	subtypeId INT, 
	equipmentCode TEXT,
	equipmentName TEXT NOT NULL, 
	normalizedName TEXT NOT NULL, 
	details TEXT, 
	address TEXT,
	phone TEXT,
	url TEXT,
	latitude REAL, 
	longitude REAL);
CREATE INDEX IF NOT EXISTS equipments_id ON equipments (_id);
CREATE INDEX IF NOT EXISTS equipments_type ON equipments (typeId);
CREATE INDEX IF NOT EXISTS equipments_equipmentName ON equipments (equipmentName collate nocase);
CREATE INDEX IF NOT EXISTS equipments_normalizedName ON equipments (normalizedName collate nocase);
CREATE INDEX IF NOT EXISTS equipments_coordinates ON equipments (latitude, longitude);
CREATE INDEX IF NOT EXISTS equipments_equipmentCode ON equipments (equipmentCode);
CREATE INDEX IF NOT EXISTS equipments_type_station_code ON equipments (typeId, _id, equipmentCode);
  
CREATE TABLE IF NOT EXISTS stops (
  _id INTEGER PRIMARY KEY, 
  equipmentId INT NOT NULL,
  routeCode TEXT, 
  directionCode TEXT, 
  stopCode TEXT, 
  stopOrder INT NOT NULL);
CREATE INDEX IF NOT EXISTS stops_id ON stops (_id);
CREATE INDEX IF NOT EXISTS stops_stopCode ON stops (stopCode);
CREATE INDEX IF NOT EXISTS stops_routeCode ON stops (routeCode);
CREATE INDEX IF NOT EXISTS stops_directionCode ON stops (directionCode);
CREATE INDEX IF NOT EXISTS stops_routeDirectionCode ON stops (routeCode, directionCode);
CREATE INDEX IF NOT EXISTS stops_station ON stops (equipmentId);

CREATE TABLE IF NOT EXISTS directions (
	_id INTEGER PRIMARY KEY, 
	routeCode TEXT NOT NULL, 
	directionCode TEXT NOT NULL, 
	directionName TEXT NOT NULL);
CREATE INDEX IF NOT EXISTS directions_id ON directions (_id);
CREATE INDEX IF NOT EXISTS directions_routeCode ON directions (routeCode);
CREATE INDEX IF NOT EXISTS directions_directionCode ON directions (directionCode);

CREATE TABLE IF NOT EXISTS schedules (
	_id INTEGER PRIMARY KEY AUTOINCREMENT, 
	headsign NVARCHAR(255), 
	dayTrip INTEGER NOT NULL,
	timestamp INTEGER NOT NULL, 
	stopId INTEGER NOT NULL);
CREATE INDEX IF NOT EXISTS schedules_id ON schedules (_id);
CREATE INDEX IF NOT EXISTS schedules_dayTrip ON schedules (dayTrip);
CREATE INDEX IF NOT EXISTS schedules_stopId_dayTrip ON schedules (stopId, dayTrip);
CREATE INDEX IF NOT EXISTS schedules_stopId_dayTrip_timestamp ON schedules (stopId, dayTrip, timestamp);

CREATE TABLE IF NOT EXISTS stopBookmarks (
	_id INTEGER PRIMARY KEY, 
	routeCode TEXT NOT NULL, 
	directionCode TEXT NOT NULL, 
	stopCode TEXT NOT NULL, 
	bookmarkName TEXT);
CREATE INDEX IF NOT EXISTS stopBookmarks_id ON stopBookmarks (_id);

CREATE TABLE IF NOT EXISTS stopBookmarkGroups (
	_id INTEGER PRIMARY KEY AUTOINCREMENT, 
	groupName TEXT NOT NULL, 
	groupOrder INTEGER NOT NULL);
CREATE INDEX IF NOT EXISTS stopBookmarkGroups_id ON stopBookmarkGroups (_id);

CREATE TABLE IF NOT EXISTS stopBookmarkGroupLinks (
	stopBookmarkId INTEGER NOT NULL REFERENCES stopBookmarks(_id) ON DELETE CASCADE, 
	groupId  INTEGER NOT NULL REFERENCES stopBookmarkGroups(_id) ON DELETE CASCADE,
	CONSTRAINT uc_ids UNIQUE (stopBookmarkId, groupId));
CREATE INDEX IF NOT EXISTS stopBookmarkGroupLinks_stopBookmarkId ON stopBookmarkGroupLinks (stopBookmarkId);
CREATE INDEX IF NOT EXISTS stopBookmarkGroupLinks_groupId ON stopBookmarkGroupLinks (groupId);

CREATE TABLE IF NOT EXISTS liveNews (
    _id INTEGER PRIMARY KEY,
    routeCode TEXT,
    directionCode TEXT,
	stopCode TEXT,
    message TEXT NOT NULL,
    source TEXT NOT NULL,
    timestamp LONG NOT NULL
);
CREATE INDEX IF NOT EXISTS liveNews_route ON liveNews(routeCode);
CREATE INDEX IF NOT EXISTS liveNews_route_direction ON liveNews(routeCode, directionCode);
CREATE INDEX IF NOT EXISTS liveNews_route_direction_stop ON liveNews(routeCode, directionCode, stopCode);

CREATE TABLE IF NOT EXISTS biclooBookmarks (
    _id INTEGER PRIMARY KEY,
    equipmentName TEXT NOT NULL
);

CREATE VIEW IF NOT EXISTS stopBookmarksView AS
SELECT
    stopBookmarks._id,
    stopBookmarks.routeCode, 
    stopBookmarks.directionCode, 
    stopBookmarks.stopCode,
    stopBookmarks.bookmarkName, 
    equipments.equipmentName, 
    equipments.normalizedName, 
    equipments.equipmentCode,
    stops.equipmentId, 
    equipments.latitude, 
    equipments.longitude, 
    directions.directionName,
    routes.typeId,
    routes.frontColor, 
    routes.backColor, 
    routes.letter,
    stopBookmarkGroups._id AS stopBookmarkGroupId,
    stopBookmarkGroups.groupName,
    stopBookmarkGroups.groupOrder,
    (SELECT (timestamp/1000 - ((strftime('%s','now')) / 60) * 60) / 60 FROM schedules WHERE schedules.stopId = stopBookmarks._id AND timestamp/1000 >= (strftime('%s','now')) / 60 * 60 LIMIT 1) as nextSchedule
FROM
    stopBookmarks 
    LEFT JOIN stops ON stopBookmarks._id = stops._id
    LEFT JOIN equipments ON equipments.typeId = 0 AND equipments._id = stops.equipmentId 
    LEFT JOIN routes ON routes.routeCode = stopBookmarks.routeCode
    LEFT JOIN directions ON directions.routeCode = stopBookmarks.routeCode AND directions.directionCode = stopBookmarks.directionCode
    LEFT JOIN stopBookmarkGroupLinks ON stopBookmarks._id = stopBookmarkGroupLinks.stopBookmarkId
    LEFT JOIN stopBookmarkGroups ON stopBookmarkGroups._id = stopBookmarkGroupLinks.groupId;

CREATE VIEW IF NOT EXISTS stopDirectionsView AS
SELECT stops._id AS stopId, 
       routes.routecode, 
       routes._id AS routeId, 
       routes.letter, 
       routes.backColor, 
       routes.frontColor, 
       routes.typeId AS routeTypeId,
       directions._id AS directionId,
       directions.directionName,
       equipments.normalizedName
FROM   equipments
       LEFT JOIN stops 
              ON stops.equipmentId = equipments._id 
       LEFT JOIN directions
              ON directions.directionCode = stops.directionCode
                 AND directions.routeCode = stops.routeCode
       LEFT JOIN routes 
              ON routes.routeCode = stops.routeCode 
WHERE  routes.routeCode IS NOT NULL 
       AND equipments.typeId = 0;

PRAGMA foreign_keys = ON;

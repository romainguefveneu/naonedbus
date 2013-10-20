INSERT INTO stopBookmarks SELECT _id, routeCode, directionCode, stopCode, bookmarkName FROM stopBookmarks_backup;
DROP TABLE stopBookmarks_backup;

DELETE FROM stopBookmarks WHERE NOT EXISTS (SELECT 1 FROM routes WHERE stopBookmarks.routeCode = routes.routeCode);
DELETE FROM stopBookmarks WHERE _id IN (SELECT stopBookmarks._id FROM stopBookmarks LEFT JOIN stops ON stops._id = stopBookmarks._id LEFT JOIN equipments ON equipments.typeId = 0 AND equipments._id = stops.equipmentId WHERE equipmentCode IS NULL);
UPDATE stopBookmarks SET _id = (SELECT stops._id FROM stops WHERE stops.stopCode = stopBookmarks.stopCode AND stops.directionCode = stopBookmarks.directionCode AND stops.routeCode = stopBookmarks.routeCode);

IF OLD_VERSION > 10
	INSERT INTO stopBookmarkGroups 
		SELECT stopBookmarks._id,  stopBookmarkGroups_backup.idGroupe FROM stopBookmarkGroups_backup 
		LEFT JOIN stopBookmarks 
			ON stopBookmarks.routeCode = stopBookmarkGroups_backup.routeCode 
			AND stopBookmarks.directionCode = stopBookmarkGroups_backup.directionCode 
			AND stopBookmarks.stopCode = stopBookmarkGroups_backup.stopCode;
	DROP TABLE stopBookmarkGroups_backup;
END
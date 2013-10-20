IF OLD_VERSION > 10
	CREATE TABLE IF NOT EXISTS stopBookmarks_backup(idFavori, codeLigne, codeSens, codeArret, idGroupe);
	DELETE FROM stopBookmarks_backup;
	INSERT INTO stopBookmarks_backup 
		SELECT bookrmarkId, routeCode, directionCode, stopCode, groupId 
		FROM stopBookmarkGroups 
		LEFT JOIN stopBookmarks ON stopBookmarks._id = stopBookmarkGroups.stopBookmarkId;
	DROP TABLE stopBookmarkGroups;
END

CREATE TEMPORARY TABLE stopBookmarks_backup(_id, routeCode, directionCode, stopCode, bookmarkName);
INSERT INTO stopBookmarks_backup SELECT _id, routeCode, directionCode, stopCode, bookmarkName FROM stopBookmarks;

DROP TABLE IF EXISTS stopBookmarks;
DROP TABLE IF EXISTS routeTypes;
DROP TABLE IF EXISTS routes;
DROP TABLE IF EXISTS directions;
DROP TABLE IF EXISTS stop;
DROP TABLE IF EXISTS equiomentTypes;
DROP TABLE IF EXISTS equipments;
DROP TABLE IF EXISTS schedules;

DROP VIEW IF EXISTS stopBookmarksView;
DROP VIEW IF EXISTS stopDirectionsView;
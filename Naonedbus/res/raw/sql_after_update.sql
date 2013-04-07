INSERT INTO favoris SELECT _id, codeLigne, codeSens, codeArret, nomFavori FROM favoris_backup;
DROP TABLE favoris_backup;

DELETE FROM favoris WHERE NOT EXISTS (SELECT 1 FROM lignes WHERE code = codeLigne);
DELETE FROM favoris WHERE _id IN (SELECT favoris._id FROM favoris LEFT JOIN arrets ON arrets._id = favoris._id LEFT JOIN equipements ON equipements.idType = 0 AND equipements._id = arrets.idStation WHERE codeEquipement IS NULL);
UPDATE favoris SET _id = (SELECT arrets._id FROM arrets WHERE arrets.code = favoris.codeArret AND arrets.codeSens = favoris.codeSens AND arrets.codeLigne = favoris.codeLigne);


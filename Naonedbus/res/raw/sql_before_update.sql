CREATE TEMPORARY TABLE favorisGroupes_backup(idFavori, codeLigne, codeSens, codeArret, idGroupe);
INSERT INTO favorisGroupes_backup SELECT idFavori, codeLigne, codeSens, codeArret, idGroupe FROM favorisGroupes LEFT JOIN favoris ON favoris._id = favorisGroupes.idFavori;
DROP TABLE IF EXISTS favorisGroupes;

CREATE TEMPORARY TABLE favoris_backup(_id, codeLigne, codeSens, codeArret, nomFavori);
INSERT INTO favoris_backup SELECT _id, codeLigne, codeSens, codeArret, nomFavori FROM favoris;
DROP TABLE favoris;

DROP TABLE IF EXISTS typesLignes;
DROP TABLE IF EXISTS lignes;
DROP TABLE IF EXISTS sens;
DROP TABLE IF EXISTS arrets;
DROP TABLE IF EXISTS typesEquipements;
DROP TABLE IF EXISTS equipements;
DROP TABLE IF EXISTS horaires;

DROP TABLE IF EXISTS stations;
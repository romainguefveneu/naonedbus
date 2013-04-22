CREATE TABLE favorisGroupes_backup(idFavori, codeLigne, codeSens, codeArret, idGroupe);
INSERT INTO favorisGroupes_backup SELECT idFavori, codeLigne, codeSens, codeArret, idGroupe FROM favorisGroupes LEFT JOIN favoris ON favoris._id = favorisGroupes.idFavori;
DROP TABLE favorisGroupes;
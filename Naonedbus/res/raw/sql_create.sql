-- Décrire TYPESLIGNES
CREATE TABLE IF NOT EXISTS typesLignes (
	_id smallint PRIMARY KEY, 
	nom text);
CREATE INDEX IF NOT EXISTS typesLignes_code ON typesLignes (_id);

-- Décrire LIGNES
CREATE TABLE IF NOT EXISTS lignes (
	_id INT PRIMARY KEY, 
	code TEXT, 
	lettre TEXT, 
	couleur INT, 
	depuis TEXT, 
	vers TEXT, 
	type SMALLINT);
CREATE INDEX IF NOT EXISTS lignes_id ON lignes (_id);
CREATE INDEX IF NOT EXISTS lignes_code ON lignes (code);
CREATE INDEX IF NOT EXISTS lignes_lettre ON lignes (lettre);

-- Décrire TYPESEQUIPEMENTS
CREATE TABLE IF NOT EXISTS typesEquipements (
	_id INT  NOT NULL, 
	nom TEXT NOT NULL);
CREATE INDEX IF NOT EXISTS typesEquipements_id ON typesEquipements (_id);

-- Décrire EQUIPEMENTS
CREATE TABLE IF NOT EXISTS equipements (
	_id INT NOT NULL, 
	idType INT NOT NULL,
	idSousType INT, 
	codeEquipement TEXT,
	nom TEXT NOT NULL, 
	normalizedNom TEXT NOT NULL, 
	details TEXT, 
	adresse TEXT,
	telephone TEXT,
	url TEXT,
	latitude REAL, 
	longitude REAL,
	tag INT);
CREATE INDEX IF NOT EXISTS equipements_id ON equipements (_id);
CREATE INDEX IF NOT EXISTS equipements_type ON equipements (idType);
CREATE INDEX IF NOT EXISTS equipements_nom ON equipements (nom collate nocase);
CREATE INDEX IF NOT EXISTS equipements_normalizedNom ON equipements (normalizedNom collate nocase);
CREATE INDEX IF NOT EXISTS equipements_coordonnees ON equipements (latitude, longitude);
CREATE INDEX IF NOT EXISTS equipements_code ON equipements (codeEquipement);
CREATE INDEX IF NOT EXISTS equipements_type_station_code ON equipements (idType, _id, codeEquipement);
  
-- Décrire ARRETS	
CREATE TABLE IF NOT EXISTS arrets (
  _id INTEGER PRIMARY KEY, 
  code TEXT, 
  codeSens TEXT, 
  codeLigne TEXT, 
  idStation INT NOT NULL,
  ordre INT NOT NULL);
CREATE INDEX IF NOT EXISTS arrets_id ON arrets (_id);
CREATE INDEX IF NOT EXISTS arrets_code ON arrets (code);
CREATE INDEX IF NOT EXISTS arrets_codeLigne ON arrets (codeLigne);
CREATE INDEX IF NOT EXISTS arrets_codeSens ON arrets (codeSens);
CREATE INDEX IF NOT EXISTS arrets_codeLigneSens ON arrets (codeLigne, codeSens);
CREATE INDEX IF NOT EXISTS arrets_station ON arrets (idStation);

-- Décrire SENS
CREATE TABLE IF NOT EXISTS sens (
	_id INTEGER PRIMARY KEY, 
	codeLigne text, 
	code text, 
	nomSens text);
CREATE INDEX IF NOT EXISTS sens_id ON sens (_id);
CREATE INDEX IF NOT EXISTS sens_codeLigne ON sens (codeLigne);
CREATE INDEX IF NOT EXISTS sens_code ON sens (code);

-- Décrire HORAIRES
CREATE TABLE IF NOT EXISTS horaires (
	_id INTEGER PRIMARY KEY AUTOINCREMENT, 
	terminus NVARCHAR(255), 
	dayTrip INTEGER NOT NULL,
	timestamp INTEGER NOT NULL, 
	idArret INTEGER NOT NULL);
CREATE INDEX IF NOT EXISTS horaires_id ON horaires (_id);
CREATE INDEX IF NOT EXISTS horaires_dayTrip ON horaires (dayTrip);
CREATE INDEX IF NOT EXISTS horaires_idArret_dayTrip ON horaires (idArret, dayTrip);
CREATE INDEX IF NOT EXISTS horaires_idArret_dayTrip_timestamp ON horaires (idArret, dayTrip, timestamp);

-- Décrire FAVORIS
CREATE TABLE IF NOT EXISTS favoris (
	_id INTEGER PRIMARY KEY, 
	codeLigne TEXT NOT NULL, 
	codeSens TEXT NOT NULL, 
	codeArret TEXT NOT NULL, 
	nom TEXT);
CREATE INDEX IF NOT EXISTS favoris_id ON favoris (_id);

-- Décrire GROUPES
CREATE TABLE IF NOT EXISTS groupes (
	_id INTEGER PRIMARY KEY AUTOINCREMENT, 
	nom TEXT NOT NULL, 
	ordre INTEGER NOT NULL,
	visibilite INTEGER NOT NULL);
CREATE INDEX IF NOT EXISTS groupes_id ON groupes (_id);

-- Décrire FAVORISGROUPES
CREATE TABLE IF NOT EXISTS favorisGroupes (
	idFavori INTEGER NOT NULL REFERENCES favoris(_id) ON DELETE CASCADE, 
	idGroupe  INTEGER NOT NULL REFERENCES groupes(_id) ON DELETE CASCADE,
	CONSTRAINT uc_ids UNIQUE (idFavori, idGroupe));

CREATE INDEX IF NOT EXISTS favorisGroupes_idFavori ON favorisGroupes (idFavori);
CREATE INDEX IF NOT EXISTS favorisGroupes_idGroupe ON favorisGroupes (idGroupe);

-- Décrire FAVORISVIEW
CREATE VIEW IF NOT EXISTS favorisView AS
SELECT
    f._id,
    f.codeLigne, 
    f.codeSens, 
    f.codeArret,
    f.nom AS nomFavori, 
    st.nom AS nomArret, 
    st.normalizedNom, 
    st.codeEquipement,
    a.idStation, 
    st.latitude, 
    st.longitude, 
    s.nomSens,
    l.type AS ligneType,
    l.couleur AS ligneCouleur, 
    l.lettre AS ligneLettre,
    g.nom AS nomGroupe,
    g._id AS idGroupe,
    (SELECT (timestamp/1000 - ((strftime('%s','now')) / 60) * 60) / 60 FROM horaires WHERE horaires.idArret = f._id AND timestamp/1000 >= (strftime('%s','now')) / 60 * 60 LIMIT 1) as nextHoraire
FROM
    favoris f 
    LEFT JOIN arrets a ON f._id = a._id
    LEFT JOIN equipements st ON st.idType = 0 AND st._id = a.idStation 
    LEFT JOIN lignes l ON l.code = f.codeLigne
    LEFT JOIN sens s ON s.codeLigne = f.codeLigne AND s.code = f.codeSens
    LEFT JOIN favorisGroupes fg ON f._id = fg.idFavori
    LEFT JOIN groupes g ON g._id = fg.idGroupe;

PRAGMA foreign_keys = ON;

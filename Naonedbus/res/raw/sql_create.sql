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

-- Décrire TYPESEQUIPEMENTS
CREATE TABLE IF NOT EXISTS typesEquipements (
	_id INT NOT NULL, 
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

  
-- Décrire ARRETS	
CREATE TABLE IF NOT EXISTS arrets (
  _id INTEGER PRIMARY KEY, 
  code TEXT, 
  codeSens TEXT, 
  codeLigne TEXT, 
  idStation INT NOT NULL);
CREATE INDEX IF NOT EXISTS arrets_id ON arrets (_id);
CREATE INDEX IF NOT EXISTS arrets_code ON arrets (code);

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
	timestamp INTEGER NOT NULL, 
	idArret INTEGER NOT NULL);
CREATE INDEX IF NOT EXISTS horaires_id ON horaires (_id);
CREATE INDEX IF NOT EXISTS horaires_timestamp ON horaires (timestamp);
CREATE INDEX IF NOT EXISTS horaires_idArret ON horaires (idArret);

-- Décrire FAVORIS
CREATE TABLE IF NOT EXISTS favoris (
	_id INTEGER, 
	codeLigne TEXT NOT NULL, 
	codeSens TEXT NOT NULL, 
	codeArret TEXT NOT NULL, 
	nomFavori TEXT);
CREATE INDEX IF NOT EXISTS favoris_id ON favoris (_id);

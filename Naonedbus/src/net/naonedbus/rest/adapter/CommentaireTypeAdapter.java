package net.naonedbus.rest.adapter;

import java.io.IOException;

import net.naonedbus.bean.Commentaire;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

public class CommentaireTypeAdapter extends TypeAdapter<Commentaire> {

	@Override
	public Commentaire read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}

		final Commentaire commentaire = new Commentaire();
		reader.beginObject();
		String name;
		while (reader.hasNext()) {
			name = reader.nextName();
			if ("id".equals(name)) {
				commentaire.setId(reader.nextInt());
			} else if ("codeArret".equals(name)) {
				commentaire.setCodeArret(reader.nextString());
			} else if ("codeLigne".equals(name)) {
				commentaire.setCodeLigne(reader.nextString());
			} else if ("codeSens".equals(name)) {
				commentaire.setCodeSens(reader.nextString());
			} else if ("message".equals(name)) {
				commentaire.setMessage(reader.nextString());
			} else if ("source".equals(name)) {
				commentaire.setSource(reader.nextString());
			} else if ("timestamp".equals(name)) {
				commentaire.setTimestamp(reader.nextLong());
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();

		return commentaire;
	}

	// id: 1115,
	// codeArret: "OMRL2",
	// codeLigne: 3,
	// codeSens: 2,
	// message:
	// "controleur a l'arret. En attente pour monté ds le prochain tramway !",
	// source: "NAONEDBUS",
	// timestamp: 1360052853000,
	// tweetId: 298709436749447200

	@Override
	public void write(JsonWriter arg0, Commentaire arg1) throws IOException {

	}

}

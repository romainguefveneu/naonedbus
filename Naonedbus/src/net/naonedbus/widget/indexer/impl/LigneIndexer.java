package net.naonedbus.widget.indexer.impl;

import java.util.List;

import net.naonedbus.bean.Ligne;
import net.naonedbus.bean.TypeLigne;
import net.naonedbus.widget.indexer.CustomSectionIndexer;
import android.content.Context;
import android.util.SparseArray;

public class LigneIndexer extends CustomSectionIndexer<Ligne> {

	private SparseArray<String> typesLignes;

	public LigneIndexer(final List<TypeLigne> typesLignes) {
		this.typesLignes = new SparseArray<String>();
		for (final TypeLigne typeLigne : typesLignes) {
			this.typesLignes.put(typeLigne._id, typeLigne.nom);
		}
	}

	@Override
	protected String getSectionLabel(Context context, Ligne item) {
		return this.typesLignes.get((Integer) item.section);
	}

	@Override
	protected void prepareSection(Ligne item) {
	}

}
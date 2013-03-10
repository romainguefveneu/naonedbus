package net.naonedbus.widget.indexer.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Favori;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;
import android.util.SparseArray;

public class FavoriArrayIndexer extends ArraySectionIndexer<Favori> {

	private final SparseArray<String> mGroupes;

	public FavoriArrayIndexer(final SparseArray<String> groupes) {
		mGroupes = groupes;
	}

	@Override
	protected void prepareSection(final Favori item) {
	}

	@Override
	protected String getSectionLabel(final Context context, final Favori item) {
		final Integer section = (Integer) item.getSection();
		if (section == -1) {
			return context.getString(R.string.section_groupe_aucun);
		} else {
			return mGroupes.get(section);
		}
	}

}

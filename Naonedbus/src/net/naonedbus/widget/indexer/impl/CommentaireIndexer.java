package net.naonedbus.widget.indexer.impl;

import net.naonedbus.R;
import net.naonedbus.bean.Commentaire;
import net.naonedbus.widget.indexer.ArraySectionIndexer;
import android.content.Context;
import android.util.SparseArray;

public class CommentaireIndexer extends ArraySectionIndexer<Commentaire> {

	public static final Integer SECTION_FUTURE = 0;
	public static final Integer SECTION_NOW = 1;
	public static final Integer SECTION_YESTERDAY = 2;
	public static final Integer SECTION_PAST = 3;

	private static final SparseArray<Integer> SECTIONS_LABELS = new SparseArray<Integer>();
	static {
		SECTIONS_LABELS.put(SECTION_FUTURE, R.string.time_line_after);
		SECTIONS_LABELS.put(SECTION_NOW, R.string.time_line_now);
		SECTIONS_LABELS.put(SECTION_YESTERDAY, R.string.time_line_yesterday);
		SECTIONS_LABELS.put(SECTION_PAST, R.string.time_line_before);
	}

	@Override
	protected String getSectionLabel(Context context, Commentaire item) {
		return context.getString(SECTIONS_LABELS.get((Integer) item.getSection()));
	}

	@Override
	protected void prepareSection(Commentaire item) {
	}

}

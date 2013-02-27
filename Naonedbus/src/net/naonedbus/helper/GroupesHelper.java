package net.naonedbus.helper;

import net.naonedbus.R;
import net.naonedbus.bean.Favori;
import net.naonedbus.manager.impl.GroupeManager;
import net.naonedbus.provider.table.FavorisGroupesTable;
import net.naonedbus.provider.table.GroupeTable;
import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.view.WindowManager;

public class GroupesHelper {

	private Context mContext;
	private GroupeManager mGroupeManager;

	public GroupesHelper(final Context context) {
		mContext = context;
		mGroupeManager = GroupeManager.getInstance();
	}

	public void linkFavori(final Favori favori) {
		final Cursor c = mGroupeManager.getCursor(mContext.getContentResolver(), favori._id);

		final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(R.string.dialog_title_groupes);
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.setMultiChoiceItems(c, FavorisGroupesTable.LINKED, GroupeTable.NOM, null);

		final AlertDialog alert = builder.create();
		alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		alert.show();
	}
}

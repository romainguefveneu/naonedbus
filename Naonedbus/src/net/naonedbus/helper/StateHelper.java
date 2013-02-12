package net.naonedbus.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

public class StateHelper {

	private static final String FILENAME = "state";

	private static final String SORT = ":sort";
	private static final String FILTER = ":filter";

	private SharedPreferences mSharedPreferences;

	public StateHelper(Context context) {
		mSharedPreferences = context.getSharedPreferences(FILENAME, 0);
	}

	public int getSortType(Fragment fragment, int defaultType) {
		return mSharedPreferences.getInt(fragment.getClass().getSimpleName() + SORT, defaultType);
	}

	public void setSortType(Fragment fragment, int sortType) {
		mSharedPreferences.edit().putInt(fragment.getClass().getSimpleName() + SORT, sortType).commit();
	}

	public int getFilterType(Fragment fragment, int defaultType) {
		return mSharedPreferences.getInt(fragment.getClass().getSimpleName() + FILTER, defaultType);
	}

	public void setFilterType(Fragment fragment, int filterType) {
		mSharedPreferences.edit().putInt(fragment.getClass().getSimpleName() + FILTER, filterType).commit();
	}

	public int getSens(String codeLigne, int defaultSens) {
		return mSharedPreferences.getInt("sens" + codeLigne, defaultSens);
	}

	public void setSens(String codeLigne, int idSens) {
		mSharedPreferences.edit().putInt("sens" + codeLigne, idSens).commit();
	}

}

package net.naonedbus.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;

public class StateHelper {

	private static final String FILENAME = "state";

	private SharedPreferences mSharedPreferences;

	public StateHelper(Context context) {
		mSharedPreferences = context.getSharedPreferences(FILENAME, 0);
	}

	public int getSortType(Fragment fragment, int defaultType) {
		return mSharedPreferences.getInt(fragment.getClass().getSimpleName(), defaultType);
	}

	public void setSortType(Fragment fragment, int sortType) {
		mSharedPreferences.edit().putInt(fragment.getClass().getSimpleName(), sortType).commit();
	}

	public int getSens(String codeLigne, int defaultSens) {
		return mSharedPreferences.getInt("sens" + codeLigne, defaultSens);
	}

	public void setSens(String codeLigne, int idSens) {
		mSharedPreferences.edit().putInt("sens" + codeLigne, idSens).commit();
	}

}

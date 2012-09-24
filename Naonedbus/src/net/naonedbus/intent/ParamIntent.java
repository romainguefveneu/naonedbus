/**
 *  Copyright (C) 2011 Romain Guefveneu
 *  
 *  This file is part of naonedbus.
 *  
 *  Naonedbus is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Naonedbus is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.naonedbus.intent;

import java.io.Serializable;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;

/**
 * @author romain.guefveneu
 * 
 */

public class ParamIntent extends Intent {

	/**
	 * 
	 */
	public ParamIntent() {
		super();
	}

	/**
	 * @param packageContext
	 * @param cls
	 */
	public ParamIntent(Context packageContext, Class<?> cls) {
		super(packageContext, cls);
	}

	/**
	 * @param o
	 */
	public ParamIntent(Intent o) {
		super(o);
	}

	/**
	 * @param action
	 * @param uri
	 * @param packageContext
	 * @param cls
	 */
	public ParamIntent(String action, Uri uri, Context packageContext, Class<?> cls) {
		super(action, uri, packageContext, cls);
	}

	/**
	 * @param action
	 * @param uri
	 */
	public ParamIntent(String action, Uri uri) {
		super(action, uri);
	}

	/**
	 * @param action
	 */
	public ParamIntent(String action) {
		super(action);
	}

	public Intent putExtra(IIntentParamKey paramName, IIntentParamKey value) {
		return super.putExtra(paramName.toString(), value.ordinal());
	}

	public Intent putExtra(IIntentParamKey paramName, boolean value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, boolean[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, Bundle value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, byte value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, byte[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, char value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, char[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, CharSequence value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, CharSequence[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, double value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, double[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, float value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, float[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, int value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, int[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, long value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, long[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, Parcelable value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, Parcelable[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, short value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, short[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, String value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtra(IIntentParamKey paramName, String[] value) {
		return super.putExtra(paramName.toString(), value);
	}

	public Intent putExtraSerializable(IIntentParamKey paramName, Serializable value) {
		return super.putExtra(paramName.toString(), value);
	}

}

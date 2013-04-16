/**
 * Copyright (C) 2013 Romain Guefveneu.
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
package net.naonedbus.bean.async;

import android.content.ContentResolver;
import android.os.Handler;

/**
 * Classe de données pour les chargements asynchrones.
 * 
 * @author romain
 */
public class AsyncTaskInfo<T> {
	private T tag;
	private Handler handler;
	private ContentResolver contentResolver;

	public AsyncTaskInfo(ContentResolver contentResolver, T tag, Handler handler) {
		this.contentResolver = contentResolver;
		this.tag = tag;
		this.handler = handler;
	}

	public ContentResolver getContentResolver() {
		return contentResolver;
	}

	public T getTag() {
		return tag;
	}

	public Handler getHandler() {
		return handler;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(this.getClass().getSimpleName()).append(";").append(tag.toString())
				.append("]").toString();
	}

}

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

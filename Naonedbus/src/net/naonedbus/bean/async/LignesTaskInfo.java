package net.naonedbus.bean.async;

import android.content.ContentResolver;
import android.os.Handler;

public class LignesTaskInfo extends AsyncTaskInfo<Integer> {

	public LignesTaskInfo(ContentResolver contentResolver, Integer tag, Handler handler) {
		super(contentResolver, tag, handler);
	}

}

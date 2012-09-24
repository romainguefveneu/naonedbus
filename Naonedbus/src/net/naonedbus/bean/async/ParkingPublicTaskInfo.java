package net.naonedbus.bean.async;

import android.content.ContentResolver;
import android.os.Handler;

public class ParkingPublicTaskInfo extends AsyncTaskInfo<Integer> {

	public ParkingPublicTaskInfo(ContentResolver contentResolver, Integer tag, Handler handler) {
		super(contentResolver, tag, handler);
	}

}

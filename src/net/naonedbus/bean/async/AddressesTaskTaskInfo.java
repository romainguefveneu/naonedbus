package net.naonedbus.bean.async;

import android.content.Context;
import android.os.Handler;

public class AddressesTaskTaskInfo extends AsyncTaskInfo<String> {

	public AddressesTaskTaskInfo(final Context context, final String tag, final Handler handler) {
		super(context, tag, handler);
	}
}

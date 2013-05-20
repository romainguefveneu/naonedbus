package net.naonedbus.task;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.naonedbus.BuildConfig;
import net.naonedbus.bean.async.AddressesTaskTaskInfo;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AddressesResolverTask {

	private static final String LOG_TAG = "AddressesResolverTask";
	private static final boolean DBG = BuildConfig.DEBUG;

	private final static double LOWER_LEFT_LATITUDE = 47.081d;
	private final static double LOWER_LEFT_LONGITUDE = -1.843d;
	private final static double UPPER_RIGHT_LATITUDE = 47.346d;
	private final static double UPPER_RIGHT_LONGITUDE = -1.214d;

	private final Context mContext;
	private final ConcurrentLinkedQueue<AddressesTaskTaskInfo> mTasks;
	private final Geocoder mGeocoder;
	private final Object mLock = new Object();

	private Thread mProcessingThread;

	public AddressesResolverTask(final Context context) {
		mContext = context;
		mGeocoder = new Geocoder(context);
		mTasks = new ConcurrentLinkedQueue<AddressesTaskTaskInfo>();
	}

	public AddressesTaskTaskInfo request(final String query, final Handler callback) {
		final AddressesTaskTaskInfo task = new AddressesTaskTaskInfo(mContext, query, callback);

		if (DBG)
			Log.d(LOG_TAG, "request :\t" + task);

		mTasks.clear();
		mTasks.add(task);

		if (mProcessingThread == null || !mProcessingThread.isAlive()) {
			mProcessingThread = new Thread(mProcessRunnable);
			mProcessingThread.start();
		} else if (mProcessingThread.getState().equals(Thread.State.TIMED_WAITING)) {
			synchronized (mLock) {
				mLock.notify();
			}
		}

		return task;
	}

	private List<Address> processQuery(final String query) {
		List<Address> addresses = null;
		try {
			addresses = mGeocoder.getFromLocationName(query, 5, LOWER_LEFT_LATITUDE, LOWER_LEFT_LONGITUDE,
					UPPER_RIGHT_LATITUDE, UPPER_RIGHT_LONGITUDE);
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return addresses;
	}

	private final Runnable mProcessRunnable = new Runnable() {
		@Override
		public void run() {
			AddressesTaskTaskInfo task;
			List<Address> addresses;
			Handler handler;
			Message message;
			String query;
			while ((task = mTasks.poll()) != null) {
				query = task.getTag();
				addresses = processQuery(query);

				handler = task.getHandler();
				message = new Message();
				message.obj = addresses;
				message.arg1 = query.length();
				handler.sendMessage(message);

				if (mTasks.isEmpty()) {
					synchronized (mLock) {
						try {
							mLock.wait(2000);
						} catch (final InterruptedException e) {
						}
					}
				}

			}
		}
	};
}

package net.naonedbus.activity.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import net.naonedbus.bean.Stop;
import net.naonedbus.bean.schedule.Schedule;
import net.naonedbus.manager.impl.ScheduleManager;
import net.naonedbus.manager.impl.StopManager;

import org.joda.time.DateMidnight;
import org.joda.time.MutableDateTime;

import android.content.ContentResolver;
import android.test.ActivityInstrumentationTestCase2;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	public MainActivityTest() {
		super(MainActivity.class);
	}

	public void testTimeAccess() throws InterruptedException {
		final ContentResolver contentResolver = getActivity().getContentResolver();

		final StopManager arretManager = StopManager.getInstance();
		final Stop stop = arretManager.getSingle(contentResolver, "FMIT1");
		assertNotNull(stop);

		final ScheduleManager horaireManager = ScheduleManager.getInstance();
		horaireManager.clearSchedules(contentResolver);

		final int threadCount = 20;
		final CountDownLatch latch = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; i++) {
			final Thread thread = new Thread(new TimeFetcherTask(contentResolver, latch, stop));
			thread.start();
		}
		latch.await();
	}

	class TimeFetcherTask implements Runnable {

		private final Stop mStop;
		private final ContentResolver mContentResolver;
		private final CountDownLatch mCountDownLatch;

		public TimeFetcherTask(final ContentResolver contentResolver, final CountDownLatch latch, final Stop stop) {
			mContentResolver = contentResolver;
			mCountDownLatch = latch;
			mStop = stop;
		}

		@Override
		public void run() {
			final ScheduleManager scheduleManager = ScheduleManager.getInstance();

			final DateMidnight date = new DateMidnight().plusDays((int) Math.round((Math.random() * 7)));
			final MutableDateTime mutableDateTime = date.toMutableDateTime();

			try {
				List<Schedule> schedules = scheduleManager.getSchedules(mContentResolver, mStop, date);
				final int scheduleCount = schedules.size();

				for (int i = 0; i < 20; i++) {
					if (Math.random() > 0.9)
						scheduleManager.clearSchedules(mContentResolver);

					schedules = scheduleManager.getSchedules(mContentResolver, mStop, date);

					for (final Schedule schedule : schedules) {
						mutableDateTime.setTime(schedule.getTimestamp());

						assertEquals(date.getDayOfMonth(), mutableDateTime.getDayOfMonth());
						assertEquals(date.getMonthOfYear(), mutableDateTime.getMonthOfYear());
						assertEquals(date.getYear(), mutableDateTime.getYear());
					}

					assertEquals(scheduleCount, schedules.size());
				}
			} catch (final IOException e) {
				fail(e.getLocalizedMessage());
			}

			mCountDownLatch.countDown();
		}
	}

}

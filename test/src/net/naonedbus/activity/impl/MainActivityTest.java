package net.naonedbus.activity.impl;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import net.naonedbus.bean.Arret;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.manager.impl.ArretManager;
import net.naonedbus.manager.impl.HoraireManager;

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

		final ArretManager arretManager = ArretManager.getInstance();
		final Arret arret = arretManager.getSingle(contentResolver, "FMIT1");
		assertNotNull(arret);

		final HoraireManager horaireManager = HoraireManager.getInstance();
		horaireManager.clearAllHoraires(contentResolver);

		final DateMidnight date = new DateMidnight();

		final int threadCount = 50;
		final CountDownLatch latch = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; i++) {
			final Thread thread = new Thread(new TimeFetcherTask(contentResolver, latch, arret, date));
			thread.start();
		}
		latch.await();
	}

	private static class TimeFetcherTask implements Runnable {

		private final Arret mArret;
		private final DateMidnight mDate;
		private final ContentResolver mContentResolver;
		private final CountDownLatch mCountDownLatch;

		public TimeFetcherTask(final ContentResolver contentResolver, final CountDownLatch latch, final Arret arret,
				final DateMidnight date) {
			mContentResolver = contentResolver;
			mCountDownLatch = latch;
			mArret = arret;
			mDate = date;
		}

		@Override
		public void run() {
			final HoraireManager horaireManager = HoraireManager.getInstance();
			final MutableDateTime mutableDateTime = new MutableDateTime();

			try {
				for (int i = 0; i < 10; i++) {
					final Random random = new Random(System.currentTimeMillis());
					if (random.nextBoolean())
						horaireManager.clearAllHoraires(mContentResolver);

					final List<Horaire> horaires = horaireManager.getHoraires(mContentResolver, mArret, mDate);
					assertTrue(horaires.size() > 0);

					for (final Horaire horaire : horaires) {
						mutableDateTime.setTime(horaire.getTimestamp());

						assertEquals(mutableDateTime.getDayOfMonth(), mDate.getDayOfMonth());
						assertEquals(mutableDateTime.getMonthOfYear(), mDate.getMonthOfYear());
						assertEquals(mutableDateTime.getYear(), mDate.getYear());
					}
				}
			} catch (final IOException e) {
				fail(e.getLocalizedMessage());
			}

			mCountDownLatch.countDown();
		}
	}

}

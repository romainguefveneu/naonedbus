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
package net.naonedbus.manager.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import net.naonedbus.bean.Equipement;
import net.naonedbus.bean.async.ParkingPublicTaskInfo;
import net.naonedbus.bean.parking.pub.ParkingPublic;
import net.naonedbus.manager.Unschedulable;
import net.naonedbus.rest.controller.impl.ParkingPublicsController;

import org.joda.time.DateTime;
import org.json.JSONException;

import android.content.ContentResolver;
import android.os.Handler;
import android.os.Message;

public class ParkingPublicManager implements Unschedulable<ParkingPublicTaskInfo> {

	private static final String LOG_TAG = ParkingPublicManager.class.getSimpleName();

	private static final int CACHE_LIMITE_MINUTES = 15;
	private static ParkingPublicManager sInstance;

	private List<ParkingPublic> mCache;
	private DateTime mDateLimit;

	private Thread mParkingsThread;
	private final Stack<ParkingPublicTaskInfo> mParkingsTasks;
	private final Object mLock = new Object();

	public static synchronized ParkingPublicManager getInstance() {
		if (sInstance == null) {
			sInstance = new ParkingPublicManager();
		}

		return sInstance;
	}

	private ParkingPublicManager() {
		this.mCache = new ArrayList<ParkingPublic>();
		this.mParkingsTasks = new Stack<ParkingPublicTaskInfo>();
	}

	/**
	 * Charger les données et gérer le cache.
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	private void init(final ContentResolver contentResolver) throws IOException, JSONException {
		final DateTime now = new DateTime();

		if (this.mCache.isEmpty() || now.isAfter(this.mDateLimit)) {
			final ParkingPublicsController controller = new ParkingPublicsController();
			this.mCache.clear();
			this.mCache = controller.getAll();
			this.mDateLimit = now.plusMinutes(CACHE_LIMITE_MINUTES);
			fillParkings(contentResolver, this.mCache);
		}
	}

	/**
	 * Récupérer les parkings publics.
	 * 
	 * @return La liste des parkings publics
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<ParkingPublic> getAll(final ContentResolver contentResolver) throws IOException, JSONException {
		init(contentResolver);
		return this.mCache;
	}

	/**
	 * Récupérer un parking public selon son id, si disponible en cache.
	 * 
	 * @param id
	 *            L'id du parking.
	 * @return Le parking s'il est disponible, {@code null} sinon.
	 */
	public ParkingPublic getFromCache(final int id) {
		for (final ParkingPublic parkingPublic : mCache) {
			if (parkingPublic.getId().equals(id)) {
				return parkingPublic;
			}
		}
		return null;
	}

	/**
	 * Ajouter les informations complémentaires des parkings publics contenues
	 * dans l'équipement en base correspondant.
	 * 
	 * @param contentResolver
	 * @param parkingsPublics
	 */
	private void fillParkings(final ContentResolver contentResolver, final List<ParkingPublic> parkingsPublics) {
		final EquipementManager equipementManager = EquipementManager.getInstance();
		final List<Equipement> equipementsParkings = equipementManager.getParkings(contentResolver,
				EquipementManager.SousType.PARKING_PUBLIC);

		for (final ParkingPublic parkingPublic : parkingsPublics) {
			Equipement foundEquipement = null;
			for (final Equipement equipement : equipementsParkings) {
				if (parkingPublic.getId().equals(equipement.getId())) {
					foundEquipement = equipement;
					fillParking(parkingPublic, equipement);
				}
			}
			// Optimisation
			if (foundEquipement != null) {
				equipementsParkings.remove(foundEquipement);
			}
		}

	}

	/**
	 * Compléter les données d'un parking public par celles de l'équipement
	 * correspondant.
	 * 
	 * @param parkingPublic
	 * @param equipement
	 */
	private void fillParking(final ParkingPublic parkingPublic, final Equipement equipement) {
		parkingPublic.setNom(equipement.getNom());
		parkingPublic.setLatitude(equipement.getLatitude());
		parkingPublic.setLongitude(equipement.getLongitude());
		parkingPublic.setAdresse(equipement.getAdresse());
		parkingPublic.setTelephone(equipement.getTelephone());
		parkingPublic.setUrl(equipement.getTelephone());
	}

	/**
	 * Programmer la récupération d'un parking de manière asynchrone.
	 * 
	 * @param contentResolver
	 * @param idParking
	 *            L'id du parking à récupérer.
	 * @param callback
	 *            Un {@code Handler} receptionnant le resultat dans {@code obj}
	 *            sous forme de {@code ParkingPublic} .
	 * @return
	 */
	public ParkingPublicTaskInfo scheduleGetParkingPublic(final ContentResolver contentResolver, final int idParking,
			final Handler callback) {
		final ParkingPublicTaskInfo task = new ParkingPublicTaskInfo(contentResolver, idParking, callback);
		mParkingsTasks.push(task);

		if (mParkingsThread == null || !mParkingsThread.isAlive()) {
			mParkingsThread = new Thread(parkingsLoader);
			mParkingsThread.start();
		} else if (mParkingsThread.getState().equals(Thread.State.TIMED_WAITING)) {
			synchronized (mLock) {
				mLock.notify();
			}
		}

		return task;
	}

	@Override
	public void unschedule(final ParkingPublicTaskInfo task) {
		mParkingsTasks.remove(task);
	}

	/**
	 * Tâche de chargement d'un parking de manière asynchrone.
	 */
	private final Runnable parkingsLoader = new Runnable() {

		@Override
		public void run() {

			final Iterator<ParkingPublicTaskInfo> iterator = mParkingsTasks.iterator();
			ParkingPublic parking;
			ParkingPublicTaskInfo task;
			Handler handler;
			Message message;

			while (iterator.hasNext()) {
				task = mParkingsTasks.pop();

				try {
					init(task.getContentResolver());
					parking = getFromCache(task.getTag());
				} catch (final IOException e) {
					parking = null;
				} catch (final JSONException e) {
					parking = null;
				}

				handler = task.getHandler();
				message = handler.obtainMessage();
				message.obj = parking;
				message.sendToTarget();

				if (mParkingsTasks.isEmpty()) {
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

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

import net.naonedbus.bean.Equipment;
import net.naonedbus.bean.async.PublicParkTaskInfo;
import net.naonedbus.bean.parking.PublicPark;
import net.naonedbus.manager.Unschedulable;
import net.naonedbus.rest.controller.impl.ParkingPublicsController;

import org.joda.time.DateTime;
import org.json.JSONException;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Message;

public class PublicParkManager implements Unschedulable<PublicParkTaskInfo> {

	private static final int CACHE_LIMITE_MINUTES = 15;
	private static PublicParkManager sInstance;

	private List<PublicPark> mCache;
	private DateTime mDateLimit;

	private Thread mParkThread;
	private final Stack<PublicParkTaskInfo> mParkTasks;
	private final Object mLock = new Object();

	public static synchronized PublicParkManager getInstance() {
		if (sInstance == null) {
			sInstance = new PublicParkManager();
		}

		return sInstance;
	}

	private PublicParkManager() {
		mCache = new ArrayList<PublicPark>();
		mParkTasks = new Stack<PublicParkTaskInfo>();
	}

	/**
	 * Charger les données et gérer le cache.
	 * 
	 * @throws IOException
	 * @throws JSONException
	 */
	private void init(final Context context) throws IOException, JSONException {
		final DateTime now = new DateTime();

		if (mCache.isEmpty() || now.isAfter(mDateLimit)) {
			final ParkingPublicsController controller = new ParkingPublicsController();
			mCache.clear();
			mCache = controller.getAll(context.getResources());
			mDateLimit = now.plusMinutes(CACHE_LIMITE_MINUTES);
			fillParkings(context.getContentResolver(), mCache);
		}
	}

	/**
	 * Récupérer les parkings publics.
	 * 
	 * @return La liste des parkings publics
	 * @throws IOException
	 * @throws JSONException
	 */
	public List<PublicPark> getAll(final Context context) throws IOException, JSONException {
		init(context);
		return mCache;
	}

	/**
	 * Récupérer un parking public selon son id, si disponible en cache.
	 * 
	 * @param id
	 *            L'id du parking.
	 * @return Le parking s'il est disponible, {@code null} sinon.
	 */
	public PublicPark getFromCache(final int id) {
		for (final PublicPark parkingPublic : mCache) {
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
	private void fillParkings(final ContentResolver contentResolver, final List<PublicPark> parkingsPublics) {
		final EquipmentManager equipementManager = EquipmentManager.getInstance();
		final List<Equipment> equipementsParkings = equipementManager.getParks(contentResolver,
				EquipmentManager.SubType.PUBLIC_PARK);

		for (final PublicPark parkingPublic : parkingsPublics) {
			Equipment foundEquipement = null;
			for (final Equipment equipment : equipementsParkings) {
				if (parkingPublic.getId().equals(equipment.getId())) {
					foundEquipement = equipment;
					fillParking(parkingPublic, equipment);
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
	 * @param equipment
	 */
	private void fillParking(final PublicPark parkingPublic, final Equipment equipment) {
		parkingPublic.setName(equipment.getName());
		parkingPublic.setLatitude(equipment.getLatitude());
		parkingPublic.setLongitude(equipment.getLongitude());
		parkingPublic.setAdress(equipment.getAddress());
		parkingPublic.setPhone(equipment.getPhone());
		parkingPublic.setUrl(equipment.getPhone());
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
	public PublicParkTaskInfo scheduleGetParkingPublic(final Context context, final int idParking,
			final Handler callback) {
		final PublicParkTaskInfo task = new PublicParkTaskInfo(context, idParking, callback);
		mParkTasks.push(task);

		if (mParkThread == null || !mParkThread.isAlive()) {
			mParkThread = new Thread(parkingsLoader);
			mParkThread.start();
		} else if (mParkThread.getState().equals(Thread.State.TIMED_WAITING)) {
			synchronized (mLock) {
				mLock.notify();
			}
		}

		return task;
	}

	@Override
	public void unschedule(final PublicParkTaskInfo task) {
		mParkTasks.remove(task);
	}

	/**
	 * Tâche de chargement d'un parking de manière asynchrone.
	 */
	private final Runnable parkingsLoader = new Runnable() {

		@Override
		public void run() {

			final Iterator<PublicParkTaskInfo> iterator = mParkTasks.iterator();
			PublicPark parking;
			PublicParkTaskInfo task;
			Handler handler;
			Message message;

			while (iterator.hasNext()) {
				task = mParkTasks.pop();

				try {
					init(task.getContext());
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

				if (mParkTasks.isEmpty()) {
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

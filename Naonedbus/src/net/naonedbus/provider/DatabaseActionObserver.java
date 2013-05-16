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
package net.naonedbus.provider;

import android.os.Handler;

public abstract class DatabaseActionObserver {

	private final Handler mHandler;

	public DatabaseActionObserver(final Handler handler) {
		mHandler = handler;
	}

	/* package */final void dispatchCreate() {
		if (mHandler == null) {
			onCreate();
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					DatabaseActionObserver.this.onCreate();
				}
			});
		}
	}

	/* package */final void dispatchUpgrade(final int oldVersion) {
		if (mHandler == null) {
			onUpgrade(oldVersion);
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					DatabaseActionObserver.this.onUpgrade(oldVersion);
				}
			});
		}
	}

	/* package */final void dispatchUpgradeError() {
		if (mHandler == null) {
			onUpgradeError();
		} else {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					DatabaseActionObserver.this.onUpgradeError();
				}
			});
		}
	}

	/**
	 * Indique que la base est en cour de création.
	 */
	public abstract void onCreate();

	/**
	 * Indique que la base est en cours de mise à jour.
	 */
	public abstract void onUpgrade(int oldVersion);

	/**
	 * Indique qu'une erreur à eu lieu lors de la mise à jour.
	 */
	public abstract void onUpgradeError();

}

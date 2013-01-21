/**
 *  Copyright (C) 2011 Romain Guefveneu
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
package net.naonedbus.appwidget;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.activity.impl.HoraireActivity;
import net.naonedbus.activity.widgetconfigure.WidgetConfigureActivity;
import net.naonedbus.bean.Favori;
import net.naonedbus.bean.NextHoraireTask;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.FavoriManager;
import net.naonedbus.manager.impl.HoraireManager;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.bugsense.trace.BugSenseHandler;

/**
 * @author romain.guefveneu
 * 
 */
public abstract class HoraireWidgetProvider extends AppWidgetProvider {

	private static final String ACTION_APPWIDGET_UPDATE = "net.naonedbus.action.APPWIDGET_UPDATE";
	private static final String ACTION_APPWIDGET_ON_CLICK = "net.naonedbus.action.APPWIDGET_ON_CLICK";
	private static final String LOG_TAG = "HoraireWidgetProvider";

	protected static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
	private static FavoriManager favoriManager;
	private int layoutId;
	private int horaireLimit;
	private static PendingIntent pendingIntent;
	private static long nextTimestamp = Long.MAX_VALUE;

	static {
		HoraireWidgetProvider.favoriManager = FavoriManager.getInstance();
	}

	protected HoraireWidgetProvider(int layoutId, int horaireLimit) {
		this.layoutId = layoutId;
		this.horaireLimit = horaireLimit;
	}

	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		final int count = appWidgetIds.length;

		final Thread update = new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < count; i++) {
					updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
				}
			}
		});
		update.start();
	}

	/**
	 * Programmer le rafraichissement des widgets.
	 * 
	 * @param context
	 * @param timestamp
	 */
	private synchronized void scheduleUpdate(final Context context, final long timestamp) {
		final long now = new DateTime().getMillis();
		final long triggerTimestamp = new DateTime(timestamp).plusMinutes(1).getMillis();

		if (nextTimestamp < now || triggerTimestamp < nextTimestamp) {

			nextTimestamp = triggerTimestamp;

			final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			if (pendingIntent != null) {
				m.cancel(pendingIntent);
				Log.i(LOG_TAG, "\tAnnulation de la précédente alarme.");
			}

			final Intent intent = new Intent(ACTION_APPWIDGET_UPDATE);
			pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			m.set(AlarmManager.RTC, nextTimestamp, pendingIntent);

			Log.i(LOG_TAG, "Programmation de l'alarme à : " + new DateTime(nextTimestamp).toString());
		}

	}

	/**
	 * Update the widget
	 */
	public void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		final RemoteViews views = new RemoteViews(context.getPackageName(), this.layoutId);
		final int idFavori = WidgetConfigureActivity.getFavoriIdFromWidget(context, appWidgetId);
		final Favori favori = favoriManager.getSingle(context.getContentResolver(), idFavori);

		if (favori != null) {
			prepareWidgetView(context, views, favori, appWidgetId);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		} else {
			WidgetConfigureActivity.removeWidgetId(context, appWidgetId);
		}
	}

	/**
	 * Préparer le widget, le mettre à jour si nécessaire
	 */
	protected void prepareWidgetView(Context context, RemoteViews views, Favori favori, Integer appWidgetId) {
		final HoraireManager horaireManager = HoraireManager.getInstance();
		final DateMidnight today = new DateMidnight();

		// Initialisation du widget
		prepareWidgetViewInit(context, views, favori);
		setOnClickListener(context, views, favori);

		try {
			if (horaireManager.isInDB(context.getContentResolver(), favori, today)) {
				// Les horaires sont en cache
				final List<Horaire> horaires = horaireManager.getNextHoraires(context.getContentResolver(), favori,
						today, this.horaireLimit);
				prepareWidgetViewHoraires(context, views, favori, horaires);
			} else {
				// Charger les prochains horaires
				prepareWidgetAndloadHoraires(context, views, favori, appWidgetId);
			}
		} catch (IOException e) {
			BugSenseHandler.sendExceptionMessage("Erreur lors du chargement des horaires", null, e);
		}

	}

	/**
	 * Lancer le chargement des horaires
	 */
	protected void prepareWidgetAndloadHoraires(Context context, RemoteViews views, Favori favori, Integer appWidgetId) {

		final NextHoraireTask horaireTask = new NextHoraireTask();
		horaireTask.setContext(context);
		horaireTask.setArret(favori);
		horaireTask.setId(appWidgetId);
		horaireTask.setLimit(this.horaireLimit);
		horaireTask.setActionCallback(ACTION_APPWIDGET_UPDATE);

		// Lancer le chargement des horaires
		final HoraireManager horaireManager = HoraireManager.getInstance();
		horaireManager.schedule(horaireTask);

		views.setViewVisibility(R.id.loading, View.VISIBLE);
		views.setViewVisibility(R.id.itemTime, View.GONE);
	}

	/**
	 * Préparer le widget avec les éléments fixes (nom, ligne, sens...)
	 */
	protected void prepareWidgetViewInit(Context context, RemoteViews views, Favori favori) {
		views.setTextViewText(R.id.itemSymbole, favori.lettre);

		// Uniquement pour 2.2 et sup
		if (Build.VERSION.SDK_INT > 7) {
			views.setTextColor(R.id.itemSymbole, favori.couleurTexte);
			views.setInt(R.id.itemSymbole, "setBackgroundColor", favori.couleurBackground);
		}

		if (favori.nomFavori == null) {
			views.setTextViewText(R.id.itemTitle, favori.nomArret);
			views.setTextViewText(R.id.itemDescription, "\u2192 " + favori.nomSens);
		} else {
			views.setTextViewText(R.id.itemTitle, favori.nomFavori);
			views.setTextViewText(R.id.itemDescription, favori.nomArret + " \u2192 " + favori.nomSens);
		}

	}

	/**
	 * Set pending intent in remote views
	 * 
	 * @param context
	 * @param views
	 *            Content remote views
	 * @param viewId
	 *            The view listening to click
	 */
	private void setOnClickListener(Context context, RemoteViews views, Favori favori) {
		final Intent intent = new Intent(context, this.getClass());
		intent.setAction(ACTION_APPWIDGET_ON_CLICK);
		intent.putExtra("idFavori", favori._id);
		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, favori._id, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		views.setOnClickPendingIntent(R.id.horaireWidget, pendingIntent);
	}

	/**
	 * Préparer le widget avec les horaires
	 * 
	 * @param views
	 * @param favori
	 */
	protected void prepareWidgetViewHoraires(Context context, RemoteViews views, Favori favori,
			List<Horaire> nextHoraires) {
		final StringBuilder builder = new StringBuilder();

		if (nextHoraires.size() > 0) {
			int count = 0;

			// Programmer si besoin l'alarme
			scheduleUpdate(context, nextHoraires.get(0).getTimestamp());

			for (Horaire horaire : nextHoraires) {
				builder.append(dateFormat.format(horaire.getTimestamp()));
				if (++count < nextHoraires.size()) {
					builder.append(" \u2022 ");
				}
			}

		} else {
			builder.append(context.getString(R.string.msg_aucun_depart_24h));
		}

		views.setTextViewText(R.id.itemTime, builder.toString());
		views.setViewVisibility(R.id.itemTime, View.VISIBLE);
		views.setViewVisibility(R.id.loading, View.GONE);
	}

	/**
	 * Supprimer la configuration du widget
	 */
	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		final int count = appWidgetIds.length;
		for (int i = 0; i < count; i++) {
			WidgetConfigureActivity.removeWidgetId(context, appWidgetIds[i]);
		}
	}

	/**
	 * Gérer le signal de rafraichissement
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		if (ACTION_APPWIDGET_UPDATE.equals(action)) {
			int idExtra = intent.getIntExtra("id", -1);
			int[] ids;

			Log.i(LOG_TAG, "Réception de l'ordre de rafraichissement des widgets.");

			if (idExtra == -1) {
				ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
			} else {
				ids = new int[] { idExtra };
			}

			onUpdate(context, appWidgetManager, ids);
		} else if (HoraireWidgetProvider.ACTION_APPWIDGET_ON_CLICK.equals(action)) {
			int idFavori = intent.getIntExtra("idFavori", -1);
			if (idFavori != -1) {
				final ParamIntent startIntent = new ParamIntent(context, HoraireActivity.class);
				startIntent.putExtra(HoraireActivity.Param.idArret, idFavori);
				startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(startIntent);
			}
		}

		super.onReceive(context, intent);
	}

}

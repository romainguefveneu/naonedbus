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
package net.naonedbus.appwidget;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.naonedbus.BuildConfig;
import net.naonedbus.R;
import net.naonedbus.activity.impl.SchedulesActivity;
import net.naonedbus.activity.impl.MainActivity;
import net.naonedbus.activity.widgetconfigure.WidgetConfigureActivity;
import net.naonedbus.bean.Stop;
import net.naonedbus.bean.StopBookmark;
import net.naonedbus.bean.NextHoraireTask;
import net.naonedbus.bean.schedule.Schedule;
import net.naonedbus.intent.ParamIntent;
import net.naonedbus.manager.impl.StopBookmarkViewManager;
import net.naonedbus.manager.impl.ScheduleManager;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.utils.FormatUtils;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.RemoteViews;
import android.widget.TextView;

/**
 * @author romain.guefveneu
 * 
 */
public abstract class HoraireWidgetProvider extends AppWidgetProvider {

	private static final String LOG_TAG = "HoraireWidgetProvider";
	private static final boolean DBG = BuildConfig.DEBUG;

	private static final String ACTION_APPWIDGET_UPDATE = "net.naonedbus.action.APPWIDGET_UPDATE";
	private static final String ACTION_APPWIDGET_ON_CLICK = "net.naonedbus.action.APPWIDGET_ON_CLICK";

	private static PendingIntent sPendingIntent;
	private static long sNextTimestamp = Long.MAX_VALUE;
	private static Integer sHoraireTextWidth = Integer.MIN_VALUE;
	private static Object sLock = new Object();

	private final int mLayoutId;
	private final int mHoraireLimitRes;
	private int mHoraireLimit = -1;

	protected HoraireWidgetProvider(final int layoutId, final int horaireLimitRes) {
		mLayoutId = layoutId;
		mHoraireLimitRes = horaireLimitRes;
	}

	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
		final int count = appWidgetIds.length;

		if (DBG)
			Log.i(LOG_TAG, Integer.toHexString(hashCode()) + " - " + "onUpdate");

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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onAppWidgetOptionsChanged(final Context context, final AppWidgetManager appWidgetManager,
			final int appWidgetId, final Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
		updateAppWidget(context, appWidgetManager, appWidgetId);
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

		if (sNextTimestamp < now || triggerTimestamp < sNextTimestamp) {
			sNextTimestamp = triggerTimestamp;

			final AlarmManager m = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			if (sPendingIntent != null) {
				m.cancel(sPendingIntent);

				if (DBG)
					Log.i(LOG_TAG, Integer.toHexString(hashCode()) + " - " + "\tAnnulation de la précédente alarme.");
			}

			final Intent intent = new Intent(ACTION_APPWIDGET_UPDATE);
			sPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			m.set(AlarmManager.RTC, sNextTimestamp, sPendingIntent);

			if (DBG)
				Log.i(LOG_TAG, Integer.toHexString(hashCode()) + " - " + "Programmation de l'alarme à : "
						+ new DateTime(sNextTimestamp).toString());
		}

	}

	/**
	 * Update the widget.
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void updateAppWidget(final Context context, final AppWidgetManager appWidgetManager, final int appWidgetId) {
		final RemoteViews views = new RemoteViews(context.getPackageName(), this.mLayoutId);
		final int idFavori = WidgetConfigureActivity.getFavoriIdFromWidget(context, appWidgetId);
		final StopBookmark favori = StopBookmarkViewManager.getInstance().getSingle(context.getContentResolver(), idFavori);

		if (DBG)
			Log.i(LOG_TAG, Integer.toHexString(hashCode()) + " - " + appWidgetId + " - updateAppWidget " + favori);

		// Initialisation du nombre d'horaires
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			final Bundle bundle = appWidgetManager.getAppWidgetOptions(appWidgetId);
			mHoraireLimit = getHorairesCount(context, bundle);
		} else {
			if (mHoraireLimit == -1) {
				mHoraireLimit = context.getResources().getInteger(mHoraireLimitRes);
			}
		}

		if (favori != null) {
			prepareWidgetView(context, views, favori, appWidgetId);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		} else {
			WidgetConfigureActivity.removeWidgetId(context, appWidgetId);
		}
	}

	/**
	 * Préparer le widget, le mettre à jour si nécessaire.
	 */
	protected void prepareWidgetView(final Context context, final RemoteViews views, final StopBookmark favori,
			final Integer appWidgetId) {
		if (DBG)
			Log.d(LOG_TAG, Integer.toHexString(hashCode()) + " - " + appWidgetId + " - prepareWidgetView " + favori);

		final ScheduleManager horaireManager = ScheduleManager.getInstance();
		final DateMidnight today = new DateMidnight();

		// Initialisation du widget
		prepareWidgetViewInit(context, views, favori);
		setOnClickListener(context, views, favori);

		try {
			if (horaireManager.isInDB(context.getContentResolver(), favori, today, mHoraireLimit)) {

				// Les horaires sont en cache
				final List<Schedule> horaires = horaireManager.getNextSchedules(context.getContentResolver(), favori,
						today, mHoraireLimit);
				prepareWidgetViewHoraires(context, views, favori, horaires);

			} else {

				// Charger les prochains horaires
				prepareWidgetAndloadHoraires(context, views, favori, appWidgetId);

			}
		} catch (final IOException e) {
			if (DBG)
				Log.e(LOG_TAG, Integer.toHexString(hashCode()) + " - " + appWidgetId
						+ " - Erreur lors du chargement des horaires", e);
		}
	}

	/**
	 * Lancer le chargement des horaires.
	 */
	protected void prepareWidgetAndloadHoraires(final Context context, final RemoteViews views, final StopBookmark favori,
			final Integer appWidgetId) {
		final NextHoraireTask horaireTask = new NextHoraireTask();
		horaireTask.setContext(context);
		horaireTask.setArret(favori);
		horaireTask.setId(appWidgetId);
		horaireTask.setLimit(mHoraireLimit);
		horaireTask.setActionCallback(ACTION_APPWIDGET_UPDATE);

		// Lancer le chargement des horaires
		final ScheduleManager horaireManager = ScheduleManager.getInstance();
		horaireManager.schedule(horaireTask);

		views.setTextViewText(R.id.itemTime, null);
		views.setViewVisibility(R.id.widgetProgress, View.VISIBLE);
	}

	/**
	 * Préparer le widget avec les éléments fixes (nom, route, direction...)
	 */
	protected void prepareWidgetViewInit(final Context context, final RemoteViews views, final StopBookmark favori) {
		views.setTextViewText(R.id.itemSymbole, favori.getLettre());

		// Uniquement pour 2.2 et sup
		if (Build.VERSION.SDK_INT > 7) {
			views.setTextColor(R.id.itemSymbole, favori.getFrontColor());
			views.setTextColor(R.id.itemTitle, favori.getFrontColor());
			views.setTextColor(R.id.itemDescription, favori.getFrontColor());

			final GradientDrawable gradientDrawable = ColorUtils.getGradiant(favori.getBackColor());
			final Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
			final Canvas canvas = new Canvas(bitmap);
			gradientDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
			gradientDrawable.draw(canvas);
			views.setImageViewBitmap(R.id.widgetHeaderBackground, bitmap);
		}

		if (favori.getBookmarkName() == null) {
			views.setTextViewText(R.id.itemTitle, favori.getName());
			views.setTextViewText(R.id.itemDescription, FormatUtils.formatSens(favori.getDirectionName()));
		} else {
			views.setTextViewText(R.id.itemTitle, favori.getName());
			views.setTextViewText(R.id.itemDescription,
					FormatUtils.formatSens(favori.getName(), favori.getDirectionName()));
		}

		views.setViewVisibility(R.id.widgetLoading, View.GONE);
	}

	/**
	 * Set pending intent in remote views.
	 * 
	 * @param context
	 * @param views
	 *            Content remote views
	 * @param viewId
	 *            The view listening to click
	 */
	private void setOnClickListener(final Context context, final RemoteViews views, final StopBookmark favori) {
		final Intent intent = new Intent(context, this.getClass());
		intent.setAction(ACTION_APPWIDGET_ON_CLICK);
		intent.putExtra("favori", favori);

		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, favori.getId(), intent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		views.setOnClickPendingIntent(R.id.horaireWidget, pendingIntent);
	}

	/**
	 * Préparer le widget avec les horaires.
	 * 
	 * @param views
	 * @param favori
	 */
	protected void prepareWidgetViewHoraires(final Context context, final RemoteViews views, final StopBookmark favori,
			final List<Schedule> nextHoraires) {

		final java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
		CharSequence content = "";

		if (nextHoraires.size() > 0) {

			// Programmer si besoin l'alarme
			scheduleUpdate(context, nextHoraires.get(0).getTimestamp());

			for (int i = 0; i < nextHoraires.size(); i++) {
				final Schedule schedule = nextHoraires.get(i);
				content = TextUtils.concat(content,
						FormatUtils.formatTimeAmPm(context, timeFormat.format(schedule.getTimestamp())));
				if (i < nextHoraires.size() - 1) {
					content = TextUtils.concat(content, " \u2022 ");
				}
			}

		} else {
			content = context.getString(R.string.msg_aucun_depart_24h);
		}

		views.setTextViewText(R.id.itemTime, content);
		views.setViewVisibility(R.id.widgetProgress, View.GONE);
	}

	/**
	 * Supprimer la configuration du widget.
	 */
	@Override
	public void onDeleted(final Context context, final int[] appWidgetIds) {
		if (DBG)
			Log.i(LOG_TAG, Integer.toHexString(hashCode()) + " - " + "onDeleted " + Arrays.toString(appWidgetIds));

		final int count = appWidgetIds.length;
		for (int i = 0; i < count; i++) {
			WidgetConfigureActivity.removeWidgetId(context, appWidgetIds[i]);
		}
	}

	/**
	 * Gérer le signal de rafraichissement.
	 */
	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
		final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

		if (DBG)
			Log.i(LOG_TAG, Integer.toHexString(hashCode()) + " - " + "onReceive " + action);

		final boolean updateWidget = ACTION_APPWIDGET_UPDATE.equals(action)
				|| Intent.ACTION_USER_PRESENT.equals(action);

		if (updateWidget) {

			final int idExtra = intent.getIntExtra("id", -1);
			int[] ids;

			if (DBG)
				Log.i(LOG_TAG, Integer.toHexString(hashCode()) + " - "
						+ "Réception de l'ordre de rafraichissement des widgets.");

			if (idExtra == -1) {
				ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, this.getClass()));
			} else {
				ids = new int[] { idExtra };
			}

			onUpdate(context, appWidgetManager, ids);

		} else if (HoraireWidgetProvider.ACTION_APPWIDGET_ON_CLICK.equals(action)) {

			final Stop stop = intent.getParcelableExtra("favori");
			if (stop != null) {
				final ParamIntent startIntent = new ParamIntent(context, SchedulesActivity.class);
				startIntent.putExtra(SchedulesActivity.PARAM_ARRET, stop);
				startIntent.putExtra(SchedulesActivity.PARAM_FROM_WIDGET, true);
				startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				final TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
				stackBuilder.addParentStack(MainActivity.class);
				stackBuilder.addNextIntentWithParentStack(startIntent);

				stackBuilder.startActivities();
			}
		}

		super.onReceive(context, intent);
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private int getHorairesCount(final Context context, final Bundle bundle) {

		synchronized (sLock) {
			if (sHoraireTextWidth == Integer.MIN_VALUE) {
				final TextView horaireTextView = new TextView(context);
				horaireTextView.setTextAppearance(horaireTextView.getContext(), android.R.style.TextAppearance_Medium);

				final DateTime noon = new DateTime().withHourOfDay(12).withMinuteOfHour(00);
				final java.text.DateFormat timeFormat = DateFormat.getTimeFormat(context);
				horaireTextView.setText(FormatUtils.formatTimeAmPm(context, timeFormat.format(noon.toDate()))
						+ " \u2022  ");
				final int specY = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
				final int specX = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
				horaireTextView.measure(specX, specY);
				sHoraireTextWidth = horaireTextView.getMeasuredWidth();
			}
		}

		final Resources r = context.getResources();
		final int padding = r.getDimensionPixelSize(R.dimen.padding_small);

		final int minWidth = bundle.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
		final int minWidthPixel = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidth,
				r.getDisplayMetrics()))
				- padding;

		return (minWidthPixel / sHoraireTextWidth);
	}

}

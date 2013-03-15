package net.naonedbus.card.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.util.List;

import net.naonedbus.R;
import net.naonedbus.bean.Arret;
import net.naonedbus.bean.horaire.Horaire;
import net.naonedbus.card.Card;
import net.naonedbus.manager.impl.HoraireManager;

import org.joda.time.DateMidnight;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class HoraireCard extends Card {

	private final Arret mArret;
	private final HoraireManager mHoraireManager;
	private final DateFormat mTimeFormat;

	public HoraireCard(final Context context, final Arret arret) {
		super(R.string.card_horaires_title, R.layout.card_horaire);
		mArret = arret;
		mHoraireManager = HoraireManager.getInstance();

		mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
	}

	@Override
	protected void bindView(final Context context, final View view) {
		final TextView horaireNext = (TextView) view.findViewById(R.id.horaireNext);
		setTypefaceRobotoLight(horaireNext);

		try {
			final List<Horaire> horaires = mHoraireManager.getNextHoraires(context.getContentResolver(), mArret,
					new DateMidnight(), 5);

			horaireNext.setText(mTimeFormat.format(horaires.get(0).getDate()));

		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}

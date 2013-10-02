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
package net.naonedbus.bean;

import net.naonedbus.widget.item.SectionItem;
import android.graphics.drawable.Drawable;

public class Favori extends Arret implements SectionItem {

	public static class Builder extends Arret.Builder {
		private String mNomFavori;
		private String mNomSens;
		private int mIdGroupe;
		private String mNomGroupe;
		private Integer mNextHoraire;
		private int mCouleurBackground;
		private int mCouleurTexte;
		private Integer mSection;

		public Builder setIdGroupe(final int idGroupe) {
			mIdGroupe = idGroupe;
			return this;
		}

		public Builder setNomFavori(final String nomFavori) {
			mNomFavori = nomFavori;
			return this;
		}

		public Builder setNomSens(final String nomSens) {
			mNomSens = nomSens;
			return this;
		}

		public Builder setNomGroupe(final String nomGroupe) {
			mNomGroupe = nomGroupe;
			return this;
		}

		public Builder setNextHoraire(final Integer nextHoraire) {
			mNextHoraire = nextHoraire;
			return this;
		}

		public Builder setCouleurBackground(final int couleurBackground) {
			mCouleurBackground = couleurBackground;
			return this;
		}

		public Builder setCouleurTexte(final int couleurTexte) {
			mCouleurTexte = couleurTexte;
			return this;
		}

		public Builder setSection(final Integer section) {
			mSection = section;
			return this;
		}

		@Override
		public Favori build() {
			return new Favori(this);
		}

	}

	private final String mNomSens;
	private final int mIdGroupe;
	private final String mNomGroupe;

	private final Integer mNextHoraire;
	private final int mCouleurBackground;
	private final int mCouleurTexte;

	private Drawable mBackground;
	private String mDelay;

	private String mNomFavori;
	private Integer mSection;

	private Favori(final Builder builder) {
		super(builder);
		mNomFavori = builder.mNomFavori;
		mNomSens = builder.mNomSens;
		mIdGroupe = builder.mIdGroupe;
		mNomGroupe = builder.mNomGroupe;
		mNextHoraire = builder.mNextHoraire;
		mCouleurBackground = builder.mCouleurBackground;
		mCouleurTexte = builder.mCouleurTexte;
		mSection = builder.mSection;
	}

	public String getNomFavori() {
		return mNomFavori;
	}

	public String getNomSens() {
		return mNomSens;
	}

	public int getIdGroupe() {
		return mIdGroupe;
	}

	public String getNomGroupe() {
		return mNomGroupe;
	}

	public Integer getNextHoraire() {
		return mNextHoraire;
	}

	public int getCouleurBackground() {
		return mCouleurBackground;
	}

	public int getCouleurTexte() {
		return mCouleurTexte;
	}

	public Drawable getBackground() {
		return mBackground;
	}

	public String getDelay() {
		return mDelay;
	}

	public void setBackground(final Drawable background) {
		mBackground = background;
	}

	public void setDelay(final String delay) {
		mDelay = delay;
	}

	public void setSection(final Integer section) {
		mSection = section;
	}

	public void setNomFavori(final String nomFavori) {
		mNomFavori = nomFavori;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

}

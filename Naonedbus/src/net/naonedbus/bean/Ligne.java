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
/*
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
package net.naonedbus.bean;

import net.naonedbus.R;
import net.naonedbus.utils.ColorUtils;
import net.naonedbus.widget.item.SectionItem;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class Ligne implements SectionItem, Parcelable {

	public static class Builder {
		private int mId;
		private String mCode;
		private String mLettre;
		private String mDepuis;
		private String mVers;
		private int mCouleurBack;
		private int mCouleurFront;
		private Object mSection;

		public Builder setId(final int id) {
			mId = id;
			return this;
		}

		public Builder setCode(final String code) {
			mCode = code;
			return this;
		}

		public Builder setLettre(final String lettre) {
			mLettre = lettre;
			return this;
		}

		public Builder setDepuis(final String depuis) {
			mDepuis = depuis;
			return this;
		}

		public Builder setVers(final String vers) {
			mVers = vers;
			return this;
		}

		public Builder setCouleurBack(final int couleur) {
			mCouleurBack = couleur;
			return this;
		}

		public Builder setCouleurFront(final int couleur) {
			mCouleurFront = couleur;
			return this;
		}

		public Builder setSection(final Object section) {
			mSection = section;
			return this;
		}

		public Ligne build() {
			return new Ligne(this);
		}
	}

	private final int mId;
	private final String mCode;
	private final String mLettre;
	private final String mDepuis;
	private final String mVers;
	private final String mNom;
	private final int mCouleur;
	private final int mCouleurTexte;

	private Object mSection;
	private Drawable mBackground;

	public static Ligne buildAllLigneItem(final Context context) {
		return new Ligne(context.getString(R.string.target_toutes_lignes),
				context.getString(R.string.target_toutes_lignes_symbole));
	}

	private Ligne(final String name, final String letter) {
		mId = -1;
		mCode = null;
		mLettre = letter;
		mDepuis = null;
		mVers = null;
		mNom = name;
		mCouleur = 0;
		mCouleurTexte = Color.BLACK;
		mSection = null;
	}

	private Ligne(final Builder builder) {
		mId = builder.mId;
		mCode = builder.mCode;
		mLettre = builder.mLettre;
		mDepuis = builder.mDepuis;
		mVers = builder.mVers;
		mNom = mDepuis + " \u2194 " + mVers;
		mCouleur = builder.mCouleurBack;
		mCouleurTexte = builder.mCouleurFront;
		mSection = builder.mSection;
	}

	protected Ligne(final Parcel in) {
		mId = in.readInt();
		mCode = in.readString();
		mLettre = in.readString();
		mDepuis = in.readString();
		mVers = in.readString();
		mNom = in.readString();
		mCouleur = in.readInt();
		mCouleurTexte = in.readInt();
	}

	public int getId() {
		return mId;
	}

	public String getCode() {
		return mCode;
	}

	public String getLettre() {
		return mLettre;
	}

	public String getDepuis() {
		return mDepuis;
	}

	public String getVers() {
		return mVers;
	}

	public String getNom() {
		return mNom;
	}

	public int getCouleur() {
		return mCouleur;
	}

	public int getCouleurTexte() {
		return mCouleurTexte;
	}

	public Drawable getBackground() {
		return mBackground;
	}

	public void setBackground(final Drawable background) {
		mBackground = background;
	}

	public void setSection(final Object section) {
		mSection = section;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof Ligne))
			return false;

		final Ligne autreLigne = (Ligne) o;
		return autreLigne.mId == mId;
	}

	@Override
	public int hashCode() {
		return mId * 31;
	}

	@Override
	public String toString() {
		return new StringBuilder("[").append(mId).append(";").append(mCode).append(";").append(mDepuis).append(";")
				.append(mVers).append("]").toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(mId);
		dest.writeString(mCode);
		dest.writeString(mLettre);
		dest.writeString(mDepuis);
		dest.writeString(mVers);
		dest.writeString(mNom);
		dest.writeInt(mCouleur);
		dest.writeInt(mCouleurTexte);
	}

	public static final Parcelable.Creator<Ligne> CREATOR = new Parcelable.Creator<Ligne>() {
		@Override
		public Ligne createFromParcel(final Parcel in) {
			return new Ligne(in);
		}

		@Override
		public Ligne[] newArray(final int size) {
			return new Ligne[size];
		}
	};
}

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

import net.naonedbus.model.common.ICommentaire;
import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateTime;

import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

public class Commentaire implements ICommentaire, SectionItem, Parcelable {

	private static final long serialVersionUID = -9031229899288954850L;

	private int mId;
	private String mCodeLigne;
	private String mCodeSens;
	private String mCodeArret;
	private String mMessage;
	private String mSource;
	private long mTimestamp;

	private Object mSection;
	private String mDelay;
	private DateTime mDateTime;
	private transient Drawable mBackground;
	private transient Ligne mLigne;
	private transient Sens mSens;
	private transient Arret mArret;

	public Commentaire() {
	}

	@Override
	public Integer getId() {
		return mId;
	}

	@Override
	public void setId(final Integer id) {
		mId = id;
	}

	@Override
	public String getCodeLigne() {
		return mCodeLigne;
	}

	@Override
	public void setCodeLigne(final String codeLigne) {
		mCodeLigne = codeLigne;
	}

	@Override
	public String getCodeSens() {
		return mCodeSens;
	}

	@Override
	public void setCodeSens(final String codeSens) {
		mCodeSens = codeSens;
	}

	@Override
	public String getCodeArret() {
		return mCodeArret;
	}

	@Override
	public void setCodeArret(final String codeArret) {
		mCodeArret = codeArret;
	}

	@Override
	public String getMessage() {
		return mMessage;
	}

	@Override
	public void setMessage(final String message) {
		mMessage = message;
	}

	@Override
	public Long getTimestamp() {
		return mTimestamp;
	}

	@Override
	public void setTimestamp(final Long timestamp) {
		mTimestamp = timestamp;
	}

	@Override
	public void setSource(final String source) {
		mSource = source;
	}

	@Override
	public String getSource() {
		return mSource;
	}

	public void setSection(final Object section) {
		mSection = section;
	}

	@Override
	public Object getSection() {
		return mSection;
	}

	public String getDelay() {
		return mDelay;
	}

	public void setDelay(final String delay) {
		mDelay = delay;
	}

	public DateTime getDateTime() {
		return mDateTime;
	}

	public void setDateTime(final DateTime dateTime) {
		mDateTime = dateTime;
	}

	public Drawable getBackground() {
		return mBackground;
	}

	public void setBackground(final Drawable background) {
		mBackground = background;
	}

	public void setLigne(final Ligne ligne) {
		mLigne = ligne;
	}

	public Ligne getLigne() {
		return mLigne;
	}

	public void setSens(final Sens sens) {
		mSens = sens;
	}

	public Sens getSens() {
		return mSens;
	}

	public void setArret(final Arret arret) {
		mArret = arret;
	}

	public Arret getArret() {
		return mArret;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(mCodeLigne).append(" | ").append(mCodeSens).append(" | ").append(mCodeArret).append(" | ")
				.append(mSource).append(" | ").append(mMessage);
		return builder.toString();
	}

	protected Commentaire(final Parcel in) {
		mId = in.readInt();
		mCodeLigne = in.readString();
		mCodeSens = in.readString();
		mCodeArret = in.readString();
		mMessage = in.readString();
		mSource = in.readString();
		mTimestamp = in.readLong();
		mDelay = in.readString();
		mLigne = in.readParcelable(Ligne.class.getClassLoader());
		mSens = in.readParcelable(Sens.class.getClassLoader());
		mArret = in.readParcelable(Arret.class.getClassLoader());
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(final Parcel dest, final int flags) {
		dest.writeInt(mId);
		dest.writeString(mCodeLigne);
		dest.writeString(mCodeSens);
		dest.writeString(mCodeArret);
		dest.writeString(mMessage);
		dest.writeString(mSource);
		dest.writeLong(mTimestamp);
		dest.writeString(mDelay);
		dest.writeParcelable(mLigne, 0);
		dest.writeParcelable(mSens, 0);
		dest.writeParcelable(mArret, 0);
	}

	public static final Parcelable.Creator<Commentaire> CREATOR = new Parcelable.Creator<Commentaire>() {
		@Override
		public Commentaire createFromParcel(final Parcel in) {
			return new Commentaire(in);
		}

		@Override
		public Commentaire[] newArray(final int size) {
			return new Commentaire[size];
		}
	};
}

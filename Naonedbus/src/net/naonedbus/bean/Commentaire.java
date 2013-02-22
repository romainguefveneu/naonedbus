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
	private Ligne mLigne;
	private Sens mSens;
	private Arret mArret;

	public Commentaire() {
	}

	@Override
	public Integer getId() {
		return mId;
	}

	@Override
	public void setId(Integer id) {
		this.mId = id;
	}

	@Override
	public String getCodeLigne() {
		return mCodeLigne;
	}

	@Override
	public void setCodeLigne(String codeLigne) {
		this.mCodeLigne = codeLigne;
	}

	@Override
	public String getCodeSens() {
		return mCodeSens;
	}

	@Override
	public void setCodeSens(String codeSens) {
		this.mCodeSens = codeSens;
	}

	@Override
	public String getCodeArret() {
		return mCodeArret;
	}

	@Override
	public void setCodeArret(String codeArret) {
		this.mCodeArret = codeArret;
	}

	@Override
	public String getMessage() {
		return mMessage;
	}

	@Override
	public void setMessage(String message) {
		this.mMessage = message;
	}

	@Override
	public Long getTimestamp() {
		return this.mTimestamp;
	}

	@Override
	public void setTimestamp(Long timestamp) {
		this.mTimestamp = timestamp;
	}

	@Override
	public void setSource(String source) {
		this.mSource = source;
	}

	@Override
	public String getSource() {
		return this.mSource;
	}

	public void setSection(Object section) {
		this.mSection = section;
	}

	@Override
	public Object getSection() {
		return this.mSection;
	}

	public String getDelay() {
		return mDelay;
	}

	public void setDelay(String delay) {
		this.mDelay = delay;
	}

	public DateTime getDateTime() {
		return mDateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.mDateTime = dateTime;
	}

	public Drawable getBackground() {
		return mBackground;
	}

	public void setBackground(Drawable background) {
		this.mBackground = background;
	}

	public void setLigne(Ligne ligne) {
		this.mLigne = ligne;
	}

	public Ligne getLigne() {
		return mLigne;
	}

	public void setSens(Sens sens) {
		this.mSens = sens;
	}

	public Sens getSens() {
		return mSens;
	}

	public void setArret(Arret arret) {
		this.mArret = arret;
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

	protected Commentaire(Parcel in) {
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

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
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
		public Commentaire createFromParcel(Parcel in) {
			return new Commentaire(in);
		}

		public Commentaire[] newArray(int size) {
			return new Commentaire[size];
		}
	};
}

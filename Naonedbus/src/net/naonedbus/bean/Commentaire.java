package net.naonedbus.bean;

import net.naonedbus.model.common.ICommentaire;
import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateTime;

import android.graphics.drawable.Drawable;

import com.google.gson.annotations.SerializedName;

public class Commentaire implements ICommentaire, SectionItem {
	private static final long serialVersionUID = -7332209663235356830L;

	@SerializedName("id")
	private Integer mId;
	@SerializedName("codeLigne")
	private String mCodeLigne;
	@SerializedName("codeSens")
	private String mCodeSens;
	@SerializedName("codeArret")
	private String mCodeArret;
	@SerializedName("message")
	private String mMessage;
	@SerializedName("source")
	private String mSource;
	@SerializedName("timestamp")
	private long mTimestamp;

	private Object mSection;
	private String mDelay;
	private DateTime mDateTime;
	private transient Drawable mBackground;
	private transient Ligne mLigne;
	private transient Sens mSens;
	private transient Arret mArret;

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
}

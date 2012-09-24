package net.naonedbus.bean;

import net.naonedbus.model.common.ICommentaire;
import net.naonedbus.widget.item.SectionItem;

import org.joda.time.DateTime;

import android.graphics.drawable.Drawable;

public class Commentaire implements ICommentaire, SectionItem {
	private static final long serialVersionUID = -7332209663235356830L;
	private Integer id;
	private String codeLigne;
	private String codeSens;
	private String codeArret;
	private String message;
	private String source;
	private long timestamp;
	private Object section;

	private String delay;
	private DateTime dateTime;
	private transient Drawable background;
	private Ligne ligne;
	private Sens sens;
	private Arret arret;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public String getCodeLigne() {
		return codeLigne;
	}

	@Override
	public void setCodeLigne(String codeLigne) {
		this.codeLigne = codeLigne;
	}

	@Override
	public String getCodeSens() {
		return codeSens;
	}

	@Override
	public void setCodeSens(String codeSens) {
		this.codeSens = codeSens;
	}

	@Override
	public String getCodeArret() {
		return codeArret;
	}

	@Override
	public void setCodeArret(String codeArret) {
		this.codeArret = codeArret;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public Long getTimestamp() {
		return this.timestamp;
	}

	@Override
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public String getSource() {
		return this.source;
	}

	public void setSection(Object section) {
		this.section = section;
	}

	@Override
	public Object getSection() {
		return this.section;
	}

	public String getDelay() {
		return delay;
	}

	public void setDelay(String delay) {
		this.delay = delay;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Drawable getBackground() {
		return background;
	}

	public void setBackground(Drawable background) {
		this.background = background;
	}

	public void setLigne(Ligne ligne) {
		this.ligne = ligne;
	}

	public Ligne getLigne() {
		return ligne;
	}

	public void setSens(Sens sens) {
		this.sens = sens;
	}

	public Sens getSens() {
		return sens;
	}

	public void setArret(Arret arret) {
		this.arret = arret;
	}

	public Arret getArret() {
		return arret;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(codeLigne).append(" | ").append(codeSens).append(" | ").append(codeArret).append(" | ")
				.append(source).append(" | ").append(message);
		return builder.toString();
	}
}

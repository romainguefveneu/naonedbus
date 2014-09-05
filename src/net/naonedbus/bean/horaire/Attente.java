package net.naonedbus.bean.horaire;

public class Attente {
	private String mCodeLigne;
	private String mCodeSens;
	private String mCodeArret;
	private String mTemps;

	public String getCodeLigne() {
		return mCodeLigne;
	}

	public void setCodeLigne(String codeLigne) {
		mCodeLigne = codeLigne;
	}

	public String getCodeSens() {
		return mCodeSens;
	}

	public void setCodeSens(String codeSens) {
		mCodeSens = codeSens;
	}

	public String getCodeArret() {
		return mCodeArret;
	}

	public void setCodeArret(String codeArret) {
		mCodeArret = codeArret;
	}

	public String getTemps() {
		return mTemps;
	}

	public void setTemps(String temps) {
		mTemps = temps;
	}
}

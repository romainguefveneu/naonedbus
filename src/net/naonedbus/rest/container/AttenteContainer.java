package net.naonedbus.rest.container;

public class AttenteContainer {
	public int sens;
	public String temps;
	public LigneNode ligne;
	public ArretNode arret;

	public static class LigneNode {
		public String numLigne;
	}

	public static class ArretNode {
		public String codeArret;
	}
}

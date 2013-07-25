package cz.mzk.k4.tools.domain;

public enum Knihovna {

	MZK("MZK"), 
	NKP("NKP");

	private String text;

	private Knihovna(String text) {
		this.text = text;
	}

	public String getText() {
		return this.text;
	}

	public static Knihovna fromString(String text) {
		if (text != null) {
			for (Knihovna b : Knihovna.values()) {
				if (text.equalsIgnoreCase(b.text)) {
					return b;
				}
			}
		}
		return null;
	}

}

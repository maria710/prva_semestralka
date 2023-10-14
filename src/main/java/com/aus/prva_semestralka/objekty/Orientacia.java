package com.aus.prva_semestralka.objekty;

public enum Orientacia {

	SEVEROZAPAD("SZ"),
	SEVEROVYCHOD("SV"),
	JUHOVYCHOD("JV"),
	JUHOZAPAD("JZ");

	Orientacia(String orientacia) {
	}

	//	public static Orientacia getOrientacia(String svetovaStranaSirka, String svetovaStranaDlzka) {
//		return switch (svetovaStranaSirka + svetovaStranaDlzka) {
//			case "SZ" -> Orientacia.SEVEROZAPAD;
//			case "SV" -> Orientacia.SEVEROVYCHOD;
//			case "JV" -> Orientacia.JUHOVYCHOD;
//			case "JZ" -> Orientacia.JUHOZAPAD;
//			default -> null;
//		};
//	}
	public static Orientacia getOrientacia(String orientacia) {
		return switch (orientacia) {
			case "SZ" -> Orientacia.SEVEROZAPAD;
			case "SV" -> Orientacia.SEVEROVYCHOD;
			case "JV" -> Orientacia.JUHOVYCHOD;
			case "JZ" -> Orientacia.JUHOZAPAD;
			default -> null;
		};
	}
}

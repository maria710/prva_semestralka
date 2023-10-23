package com.aus.prva_semestralka.objekty;

public enum OrientaciaEnum {

	SEVEROZAPAD("SZ"),
	SEVEROVYCHOD("SV"),
	JUHOVYCHOD("JV"),
	JUHOZAPAD("JZ");

	OrientaciaEnum(String orientacia) {
	}

	public static OrientaciaEnum getOrientacia(String orientacia) {
		return switch (orientacia) {
			case "SZ" -> OrientaciaEnum.SEVEROZAPAD;
			case "SV" -> OrientaciaEnum.SEVEROVYCHOD;
			case "JV" -> OrientaciaEnum.JUHOVYCHOD;
			case "JZ" -> OrientaciaEnum.JUHOZAPAD;
			default -> null;
		};
	}
}

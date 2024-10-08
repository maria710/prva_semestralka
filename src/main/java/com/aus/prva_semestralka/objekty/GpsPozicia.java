package com.aus.prva_semestralka.objekty;

public class GpsPozicia {
	private final String sirka; // vychod/zapad
	private final String dlzka; // sever/juh
	private final Double X;
	private final Double Y;
	private final OrientaciaEnum orientaciaEnum;

	public GpsPozicia(String sirka, String dlzka, Double X, Double Y) {
		this.sirka = sirka;
		this.dlzka = dlzka;
		this.X = X;
		this.Y = Y;
		this.orientaciaEnum = OrientaciaEnum.getOrientacia(sirka.trim() + dlzka.trim());
	}

	public Double getX() {
		return this.X;
	}

	public Double getY() {
		return this.Y;
	}

	public String getSirka() { return this.sirka; }

	public String getVyska() { return this.dlzka; }
}

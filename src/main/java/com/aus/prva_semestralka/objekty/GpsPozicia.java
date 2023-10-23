package com.aus.prva_semestralka.objekty;

public class GpsPozicia {
	private String sirka; // vychod/zapad
	private String dlzka; // sever/juh
	private Double X;
	private Double Y;
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
}

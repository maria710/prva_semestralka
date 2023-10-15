package com.aus.prva_semestralka.objekty;

public class GpsPozicia {
	private char sirka; // vychod/zapad
	private char dlzka; // sever/juh
	private Double X;
	private Double Y;
	private Orientacia orientacia;

	public GpsPozicia(char sirka, char dlzka, Double X, Double Y) {
		this.sirka = sirka;
		this.dlzka = dlzka;
		this.X = X;
		this.Y = Y;
		this.orientacia = Orientacia.getOrientacia(String.valueOf(sirka).trim() + String.valueOf(dlzka).trim());
	}

	public Double getX() {
		return this.X;
	}

	public Double getY() {
		return this.Y;
	}
}

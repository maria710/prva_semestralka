package com.aus.prva_semestralka.objekty;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Nehnutelnost implements IPozemok {

	private Integer supisneCislo;
	private String popis;

	private List<Parcela> parcely;
	private Ohranicenie gpsPozicie;

	public Nehnutelnost(Integer i, String popis, Ohranicenie gpsPozicia1Parcela) {
		this.supisneCislo = i;
		this.popis = popis;
		this.gpsPozicie = gpsPozicia1Parcela;
		this.parcely = new ArrayList<>();
	}

	public List<Parcela> getParcely() {
		return this.parcely;
	}

	@Override
	public Ohranicenie getGpsSuradnice() {
		return this.gpsPozicie;
	}

	@Override
	public String toString() {
		return "Supisne cislo: " + supisneCislo + ", popis: " + popis + ", gps pozicie: " + gpsPozicie + ", parcely: " + parcely;
	}
}

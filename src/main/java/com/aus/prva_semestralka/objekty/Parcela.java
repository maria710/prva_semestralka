package com.aus.prva_semestralka.objekty;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class Parcela implements IPozemok {

	private Integer supisneCislo;
	private String popis;
	private List<Nehnutelnost> nehnutelnosti;
	private Ohranicenie gpsPozicie;

	public Parcela(Integer i, String popis, Ohranicenie gpsPozicia1Parcela) {
		this.supisneCislo = i;
		this.popis = popis;
		this.gpsPozicie = gpsPozicia1Parcela;
		this.nehnutelnosti = new ArrayList<>();
	}

	@Override
	public Ohranicenie getGpsSuradnice() {
		return this.gpsPozicie;
	}

	public List<Nehnutelnost> getNehnutelnosti() {
		return this.nehnutelnosti;
	}

	@Override
	public String toString() {
		return "Supisne cislo: " + supisneCislo + ", popis: " + popis + ", gps pozicie: " + gpsPozicie;
	}

}

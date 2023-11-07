package com.aus.prva_semestralka.objekty;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		StringBuilder sb = new StringBuilder("Supisne cislo: ")
				.append(supisneCislo)
				.append(", popis: ")
				.append(popis)
				.append(", gps pozicie: ")
				.append(gpsPozicie)
				.append(", nehnutelnosti: ");

		nehnutelnosti.forEach(parcela -> sb.append(parcela.toStringZoznam()));

		return sb.toString();
	}

	@Override
	public Integer getSupisneCislo() {
		return this.supisneCislo;
	}

	@Override
	public String getPopis() {
		return this.popis;
	}

	@Override
	public void setPopis(String popis) {
		this.popis = popis;
	}

	public String toStringZoznam() {
		return "Supisne cislo: " + supisneCislo + ", popis: " + popis + ", gps pozicie: " + gpsPozicie;
	}

	@Override
	public Integer getPrimarnyKluc() {
		return this.supisneCislo;
	}

	@Override
	public Ohranicenie getSekundarnyKluc() {
		return this.gpsPozicie;
	}

	@Override
	public void setData(IData data) {
		if (data instanceof Parcela parcela) {
			this.supisneCislo = parcela.supisneCislo;
			this.popis = parcela.popis;
			this.gpsPozicie = parcela.gpsPozicie;
			this.nehnutelnosti = parcela.nehnutelnosti;
		}
	}

	@Override
	public boolean equals(IData o) {
		return Objects.equals(o.getPrimarnyKluc(), this.supisneCislo) && gpsPozicie.equalsOhranicenie(o.getSekundarnyKluc());
	}
}

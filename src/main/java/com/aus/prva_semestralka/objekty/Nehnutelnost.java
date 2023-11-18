package com.aus.prva_semestralka.objekty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
		StringBuilder sb = new StringBuilder("Supisne cislo: ")
				.append(supisneCislo)
				.append(", popis: ")
				.append(popis)
				.append(", gps pozicie: ")
				.append(gpsPozicie)
				.append(", parcely: ");

		parcely.forEach(parcela -> sb.append(parcela.toStringZoznam()));

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
	public void setData(IData<Integer> data) {
		if (data instanceof Nehnutelnost nehnutelnost) {
			this.supisneCislo = nehnutelnost.supisneCislo;
			this.popis = nehnutelnost.popis;
			this.gpsPozicie = nehnutelnost.gpsPozicie;
			this.parcely = nehnutelnost.parcely;
		}
	}

	@Override
	public boolean equals(IData<Integer> o) {
		return Objects.equals(o.getPrimarnyKluc(), this.supisneCislo) && gpsPozicie.equalsOhranicenie(o.getSekundarnyKluc());
	}
}

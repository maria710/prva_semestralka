package com.aus.prva_semestralka.objekty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;


@RequiredArgsConstructor
public class Nehnutelnost implements IPozemok {


	private Integer supisneCislo;
	private String popis;

	private List<Parcela> parcely;
	private List<GpsPozicia> gpsPozicie;

	public Nehnutelnost(Integer i, String popis, List<GpsPozicia> gpsPozicia1Parcela) {
		this.supisneCislo = i;
		this.popis = popis;
		this.gpsPozicie = gpsPozicia1Parcela;
	}

	public void pridajParcelu(Parcela parcela) {
		parcely.add(parcela);
	}

	@Override
	public Integer getSupisneCislo() {
		return this.supisneCislo;
	}

	@Override
	public String getPopis() {
		return popis;
	}

	@Override
	public List<GpsPozicia> getGpsSuradnice() {
		return this.gpsPozicie;
	}
}

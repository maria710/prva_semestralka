package com.aus.prva_semestralka.objekty;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@RequiredArgsConstructor
public class Parcela implements IPozemok {


	private Integer supisneCislo;

	private String popis;

	@Getter
	private List<Nehnutelnost> nehnutelnosti;
	private List<GpsPozicia> gpsPozicie;

	public Parcela(Integer i, String popis, List<GpsPozicia> gpsPozicia1Parcela) {
		this.supisneCislo = i;
		this.popis = popis;
		this.gpsPozicie = gpsPozicia1Parcela;
		this.nehnutelnosti = new ArrayList<>();
	}

	@Override
	public List<GpsPozicia> getGpsSuradnice() {
		return this.gpsPozicie;
	}

	@Override
	public Integer getSupisneCislo() {
		return this.supisneCislo;
	}

	@Override
	public String getPopis() {
		return this.popis;
	}

}

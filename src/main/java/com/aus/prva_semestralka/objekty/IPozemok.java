package com.aus.prva_semestralka.objekty;

public interface IPozemok {

	Ohranicenie getGpsSuradnice();
	String toString();

	Integer getSupisneCislo();

	String getPopis();

	void setPopis(String popis);

	void setSupisneCislo(Integer supisneCislo);
}

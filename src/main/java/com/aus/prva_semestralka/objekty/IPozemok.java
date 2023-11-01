package com.aus.prva_semestralka.objekty;

public interface IPozemok extends IData {

	Ohranicenie getGpsSuradnice();
	String toString();
	Integer getSupisneCislo();
	String getPopis();
	void setPopis(String popis);
}

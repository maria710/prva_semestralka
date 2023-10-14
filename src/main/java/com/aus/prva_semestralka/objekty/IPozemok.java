package com.aus.prva_semestralka.objekty;

import java.util.List;

public interface IPozemok {

	Integer getSupisneCislo();

	String getPopis();
	List<GpsPozicia> getGpsSuradnice();
}

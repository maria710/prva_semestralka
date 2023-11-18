package com.aus.prva_semestralka.objekty;

public interface IData<T> {

	Comparable<T> getPrimarnyKluc();
	Ohranicenie getSekundarnyKluc();
	void setData(IData<T> data);
	boolean equals(IData<T> o);
}

package com.aus.prva_semestralka.objekty;

public class GeneratorKlucov {

	private int kluc = 0;

	public int getKluc() {
		return kluc++;
	}
}

package com.aus.prva_semestralka.generatory;

public class GeneratorKlucov {

	private static int kluc = 0;

	public static int getKluc() {
		return kluc++;
	}

	public static void setKluc(int kluc) {
		GeneratorKlucov.kluc = kluc;
	}
}

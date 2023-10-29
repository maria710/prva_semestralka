package com.aus.prva_semestralka.objekty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Generator {

	private final Random random = new Random();
	private final GeneratorKlucov generatorKlucov = new GeneratorKlucov();

	public IPozemok vygenerujPozemok(int sirka, int dlzka) {
		return new Nehnutelnost(generatorKlucov.getKluc(), "Nehnutelnost", getRandomOhranicenie(sirka, dlzka));
	}

	public List<IPozemok> vygenerujPozemky(int pocet, int sirka, int dlzka) {
		ArrayList<IPozemok> pozemky = new ArrayList<>();
		for (int i = 0; i < pocet; i++) {
			pozemky.add(vygenerujPozemok(sirka, dlzka));
		}
		return pozemky;
	}

	public Ohranicenie getRandomOhranicenie(int sirka, int dlzka) {
		double suradnicaX1 = vygenerujVOhraniceni(0, sirka);
		double suradnicaY1 = vygenerujVOhraniceni(0, dlzka);
		double suradnicaX2 = vygenerujVOhraniceni(suradnicaX1, sirka);
		double suradnicaY2 = vygenerujVOhraniceni(suradnicaY1, dlzka);

		return new Ohranicenie(new GpsPozicia("S", "Z", suradnicaX1, suradnicaY1), new GpsPozicia("J", "V", suradnicaX2, suradnicaY2));
	}

	private double vygenerujVOhraniceni(double min, double max) {
		return random.nextDouble() * (max - min) + min;
	}

}

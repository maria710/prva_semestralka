package com.aus.prva_semestralka.objekty;

import java.util.ArrayList;
import java.util.List;

public class Ohranicenie {

	private GpsPozicia suradnicaPravyHorny;
	private GpsPozicia suradnicaLavyDolny;

	public Ohranicenie(GpsPozicia suradnicaLavyDolny, GpsPozicia suradnicaPravyHorny) {
		this.suradnicaLavyDolny = suradnicaLavyDolny;
		this.suradnicaPravyHorny = suradnicaPravyHorny;
	}

	public GpsPozicia getSuradnicaPravyHorny() {
		return this.suradnicaPravyHorny;
	}

	public GpsPozicia getSuradnicaLavyDolny() {
		return this.suradnicaLavyDolny;
	}

	public boolean zmestiSaDovnutra(Ohranicenie porovnavaneOhranicenie) {

		double suradnicaX1 = suradnicaLavyDolny.getX();
		double suradnicaX2 = suradnicaPravyHorny.getX();
		double suradnicaY1 = suradnicaLavyDolny.getY();
		double suradnicaY2 = suradnicaPravyHorny.getY();

		double suradnicaNodeX1 = porovnavaneOhranicenie.suradnicaLavyDolny.getX();
		double suradnicaNodeX2 = porovnavaneOhranicenie.suradnicaPravyHorny.getX();
		double suradnicaNodeY1 = porovnavaneOhranicenie.suradnicaLavyDolny.getY();
		double suradnicaNodeY2 = porovnavaneOhranicenie.suradnicaPravyHorny.getY();

		return suradnicaX1 <= suradnicaNodeX1 && suradnicaX2 >= suradnicaNodeX2 && suradnicaY1 <= suradnicaNodeY1 && suradnicaY2 >= suradnicaNodeY2;
	}

	public List<Double> rozdelNaPolovicu() {
		double midX = (suradnicaPravyHorny.getX() + suradnicaLavyDolny.getX()) / 2;
		double midY = (suradnicaPravyHorny.getY() + suradnicaLavyDolny.getY()) / 2;
		return List.of(midX, midY);
	}

	public List<Ohranicenie> rozdel() {
		List<Ohranicenie> listOhraniceni = new ArrayList<>();

		double minX = Math.min(suradnicaPravyHorny.getX(), suradnicaLavyDolny.getX());
		double maxX = Math.max(suradnicaPravyHorny.getX(), suradnicaLavyDolny.getX());
		double minY = Math.min(suradnicaPravyHorny.getY(), suradnicaLavyDolny.getY());
		double maxY = Math.max(suradnicaPravyHorny.getY(), suradnicaLavyDolny.getY());

		var stredneHodnoty = rozdelNaPolovicu();
		double midX = stredneHodnoty.get(0);
		double midY = stredneHodnoty.get(1);

		listOhraniceni.add(new Ohranicenie(new GpsPozicia("S", "Z", minX, midY), new GpsPozicia("S", "Z", midX, maxY)));
		listOhraniceni.add(new Ohranicenie(new GpsPozicia("S", "V", midX, midY), new GpsPozicia("S", "V", maxX, maxY)));
		listOhraniceni.add(new Ohranicenie(new GpsPozicia("J", "V", midX, minY), new GpsPozicia("J", "V", maxX, midY)));
		listOhraniceni.add(new Ohranicenie(new GpsPozicia("J", "Z", minX, minY), new GpsPozicia("J", "Z", midX, midY)));

		return listOhraniceni;
	}

	@Override
	public String toString() { // TODO prepisat - lavy dolny ide prvy
		return "(" + suradnicaPravyHorny.getX() + " ; " + suradnicaPravyHorny.getY() + ")" +
				"(" + suradnicaLavyDolny.getX() + " ; " + suradnicaLavyDolny.getY() + ")";

	}
}

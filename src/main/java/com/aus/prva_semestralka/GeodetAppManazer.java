package com.aus.prva_semestralka;

import java.util.List;

import com.aus.prva_semestralka.objekty.IPozemok;
import com.aus.prva_semestralka.objekty.Nehnutelnost;
import com.aus.prva_semestralka.objekty.Parcela;
import com.aus.prva_semestralka.struktury.QuadTree;

public class GeodetAppManazer {

	QuadTree nehnutelnosti;
	QuadTree parcely;

	public void vytvorStromy(int maxHlbka, int sirka, int dlzka) {
		nehnutelnosti = new QuadTree(maxHlbka, sirka, dlzka);
		parcely = new QuadTree(maxHlbka, sirka, dlzka);
	}

	public boolean pridaj(IPozemok pozemok) {

		if (pozemok == null) {
			return false;
		}

		var pozemokPridany = false;

		//		if (pozemok instanceof Nehnutelnost) {
		//			pozemokPridany = nehnutelnosti.pridaj(pozemok);
		//			pridajZavislostiNaPozemkoch(pozemok, nehnutelnosti);
		//		} else {
		//			pozemokPridany = parcely.pridaj(pozemok);
		//			pridajZavislostiNaPozemkoch(pozemok, parcely);
		//		}

		// pred pridanim skontrolujem ci sa prelinaju

		//		QuadTree quadTree = new QuadTree(30, 100, 100);
		//
		//		GpsPozicia gpsPozicia1Parcela = new GpsPozicia('S', 'V', 24.00, 49.00);
		//		GpsPozicia gpsPozicia2Parcela = new GpsPozicia('S', 'V', 30.00, 60.00);
		//		Parcela parcela = new Parcela(123, "zahrada", List.of(gpsPozicia1Parcela, gpsPozicia2Parcela));
		//		quadTree.pridaj(parcela);
		//
		//		GpsPozicia gpsPozicia1Nehnutelnost = new GpsPozicia('S', 'V', 23.00, 59.00);
		//		GpsPozicia gpsPozicia2Nehnutelnost = new GpsPozicia('S', 'V', 29.00, 54.00);
		//		Nehnutelnost nehnutelnost = new Nehnutelnost(123, "dom", List.of(gpsPozicia1Nehnutelnost, gpsPozicia2Nehnutelnost));
		//		quadTree.pridaj(nehnutelnost);
		//
		//		GpsPozicia gpsPozicia1Parcela2 = new GpsPozicia('S', 'V', 4.00, 60.00);
		//		GpsPozicia gpsPozicia2Parcela2 = new GpsPozicia('S', 'V', 70.00, 80.00);
		//		Parcela parcela2 = new Parcela(123, "zahrada", List.of(gpsPozicia1Parcela2, gpsPozicia2Parcela2));
		//		quadTree.pridaj(parcela2);
		//
		//		GpsPozicia gpsPozicia1Parcela3 = new GpsPozicia('S', 'V', 40.00, 43.00);
		//		GpsPozicia gpsPozicia2Parcela3 = new GpsPozicia('S', 'V', 43.00, 46.00);
		//		Parcela parcela3 = new Parcela(123, "zahrada", List.of(gpsPozicia1Parcela3, gpsPozicia2Parcela3));
		//		quadTree.pridaj(parcela3);
		//
		//		GpsPozicia gpsPozicia1Parcela4 = new GpsPozicia('S', 'V', 41.00, 44.00);
		//		GpsPozicia gpsPozicia2Parcela4 = new GpsPozicia('S', 'V', 44.00, 47.00);
		//		Parcela parcela4 = new Parcela(123, "zahrada", List.of(gpsPozicia1Parcela4, gpsPozicia2Parcela4));
		//		quadTree.pridaj(parcela4);

		return pozemokPridany;
	}

	public boolean prelinajuSaPozemky(IPozemok nehnutelnost, IPozemok parcela) {
		// check if the two rectangles are intersecting
		var suradnica1 = nehnutelnost.getGpsSuradnice().get(0);
		var suradnica2 = nehnutelnost.getGpsSuradnice().get(1);

		var suradnica3 = parcela.getGpsSuradnice().get(0);
		var suradnica4 = parcela.getGpsSuradnice().get(1);

		double suradnicaX1 = suradnica1.getX();
		double suradnicaX2 = suradnica2.getX();
		double suradnicaY1 = suradnica1.getY();
		double suradnicaY2 = suradnica2.getY();

		double suradnicaX3 = suradnica3.getX();
		double suradnicaX4 = suradnica4.getX();
		double suradnicaY3 = suradnica3.getY();
		double suradnicaY4 = suradnica4.getY();

		// skontrolujeme ci sa prekryvaju na xovej osi
		if (suradnicaX2 < suradnicaX3 || suradnicaX4 < suradnicaX1) {
			return false;
		}

		// skontrolujeme ci sa prekryvaju na osi y
		if (suradnicaY1 < suradnicaY4 || suradnicaY3 < suradnicaY2) {
			return false;
		}

		return true;
	}

	public boolean pridajNehnutelnost(Nehnutelnost nehnutelnost) {

		List<IPozemok> parcelyZoznam = parcely.getAllPozemky();
		pridajZavislostiNaPozemkoch(nehnutelnost, parcelyZoznam);
		return nehnutelnosti.pridaj(nehnutelnost);
	}

	public boolean pridajParcelu(Parcela parcela) {

		List<IPozemok> nehnutelnostiZoznam = nehnutelnosti.getAllPozemky();
		pridajZavislostiNaPozemkoch(parcela, nehnutelnostiZoznam);
		return parcely.pridaj(parcela);
	}

	public boolean vymazNehnutelnost(Nehnutelnost nehnutelnost) {
		return nehnutelnosti.deletePozemok(nehnutelnost);
	}

	public boolean vymazParcelu(Parcela parcela) {
		return parcely.deletePozemok(parcela);
	}

	private void pridajZavislostiNaPozemkoch(IPozemok pozemok, List<IPozemok> zavislosti) {
		for (IPozemok pozemok1 : zavislosti) {
			if (!prelinajuSaPozemky(pozemok1, pozemok)) {
				continue;
			}
			if (pozemok instanceof Nehnutelnost && pozemok1 instanceof Parcela) {
				((Parcela) pozemok1).getNehnutelnosti().add((Nehnutelnost) pozemok);
				((Nehnutelnost) pozemok).getParcely().add((Parcela) pozemok1);
			}
			if (pozemok instanceof Parcela && pozemok1 instanceof Nehnutelnost) {
				((Parcela) pozemok).getNehnutelnosti().add((Nehnutelnost) pozemok1);
				((Nehnutelnost) pozemok1).getParcely().add((Parcela) pozemok);
			}
		}
	}

	public List<IPozemok> getNehnutelnosti() {
		return nehnutelnosti.getAllPozemky();
	}

	public List<IPozemok> getParcely() {
		return parcely.getAllPozemky();
	}
}

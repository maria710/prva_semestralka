package com.aus.prva_semestralka.struktury;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import com.aus.prva_semestralka.generatory.Generator2;
import com.aus.prva_semestralka.objekty.IPozemok;

public class QuadTreeProfilerTest {

	private QuadTree<Integer> quadTree;
	private ArrayList<IPozemok> pozemky;
	private ArrayList<IPozemok> pozemkyNaPridanieAVymazanie;
	private final Generator2 generator = new Generator2();
	private final int MAX_POCET_POZEMKOV = 20000;
	private final int MAX_HLBKA = 50;
	private final int SIRKA = 1000;
	private final int DLZKA = 1000;

	@BeforeEach
	void setUp() {
		quadTree = new QuadTree<>(MAX_HLBKA, SIRKA, DLZKA);
		pozemky = new ArrayList<>(MAX_POCET_POZEMKOV);
		pozemkyNaPridanieAVymazanie = new ArrayList<>(3000);
		pozemky.addAll(generator.vygenerujPozemky(MAX_POCET_POZEMKOV, SIRKA, DLZKA, false));
		pozemkyNaPridanieAVymazanie.addAll(generator.vygenerujPozemky(8000, SIRKA, DLZKA, false));
		for (IPozemok pozemok : pozemky) {
			quadTree.pridaj(pozemok);
		}
	}

	@Test
	void pridajADelete() {
		for (IPozemok pozemok : pozemkyNaPridanieAVymazanie) {
			quadTree.pridaj(pozemok);
		}

		for (IPozemok pozemok : pozemkyNaPridanieAVymazanie) {
			quadTree.deleteData(pozemok);
		}

		quadTree = quadTree.optimalizuj();

		for (IPozemok pozemok : pozemkyNaPridanieAVymazanie) {
			quadTree.pridaj(pozemok);
		}

		for (IPozemok pozemok : pozemkyNaPridanieAVymazanie) {
			quadTree.deleteData(pozemok);
		}
	}
}

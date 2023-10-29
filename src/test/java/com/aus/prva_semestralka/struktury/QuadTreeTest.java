package com.aus.prva_semestralka.struktury;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.aus.prva_semestralka.fileManazer.Exporter;
import com.aus.prva_semestralka.objekty.Generator;
import com.aus.prva_semestralka.objekty.IPozemok;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuadTreeTest {

	private QuadTree quadTree;
	private ArrayList<IPozemok> pozemky;
	private final Generator generator = new Generator();
	private final int MAX_POCET_POZEMKOV = 50;
	private final int MAX_HLBKA = 10;
	private final int SIRKA = 100;
	private final int DLZKA = 100;

	private final Logger logger = Logger.getLogger(QuadTreeTest.class.getName());


	@BeforeEach
	void setUp() {
		quadTree = new QuadTree(MAX_HLBKA, SIRKA, DLZKA);
		pozemky = new ArrayList<>();
		pozemky.addAll(generator.vygenerujPozemky(MAX_POCET_POZEMKOV, SIRKA, DLZKA));
	}

	@Test
	void pridajTest() {
		for (IPozemok pozemok : pozemky) {
			logger.log (Level.INFO, "Pridavam pozemok: " + pozemok);
			assertTrue(quadTree.pridaj(pozemok));
		}
		Exporter.exportToCSV(pozemky, "quadTreeGenerovany.csv");
		assertEquals(MAX_POCET_POZEMKOV, quadTree.getAllPozemky().size());
		assertEquals(MAX_POCET_POZEMKOV, quadTree.getPocetPozemkov());
	}

	@Test
	void deleteTest() {
		for (IPozemok pozemok : pozemky) {
			quadTree.pridaj(pozemok);
		}
		for (IPozemok pozemok : pozemky) {
			logger.log (Level.INFO, "Vymazavam pozemok: " + pozemok);
			assertTrue(quadTree.deletePozemok(pozemok));
		}
		assertEquals(0, quadTree.getAllPozemky().size());
	}

	@Test
	void findTest() {
		var ohranicenie = generator.getRandomOhranicenie(SIRKA, DLZKA);
		var spadajuDoOhranicenia = new ArrayList<IPozemok>();
		for (IPozemok pozemok : pozemky) {
			quadTree.pridaj(pozemok);
			if (ohranicenie.zmestiSaDovnutra(pozemok.getGpsSuradnice())) {
				spadajuDoOhranicenia.add(pozemok);
			}
		}
		var najdeneMetodouFind = new ArrayList<>(quadTree.findWithin(ohranicenie).getPozemky());
		var obsahujuToTieIstePozemky = najdeneMetodouFind.containsAll(spadajuDoOhranicenia) && spadajuDoOhranicenia.containsAll(najdeneMetodouFind);
		assertTrue(obsahujuToTieIstePozemky);
		assertEquals(spadajuDoOhranicenia.size(), najdeneMetodouFind.size());
	}
}
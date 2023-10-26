package com.aus.prva_semestralka.struktury;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import com.aus.prva_semestralka.objekty.Generator;
import com.aus.prva_semestralka.objekty.IPozemok;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuadTreeTest {

	private QuadTree quadTree;
	private ArrayList<IPozemok> pozemky;
	private final Generator generator = new Generator();
	private final int MAX_POCET_POZEMKOV = 100;
	private final int MAX_HLBKA = 15;
	private final int SIRKA = 100;
	private final int DLZKA = 100;


	@BeforeEach
	void setUp() {
		quadTree = new QuadTree(MAX_HLBKA, SIRKA, DLZKA);
		pozemky = new ArrayList<>();
		pozemky.addAll(generator.vygenerujPozemky(MAX_POCET_POZEMKOV, SIRKA, DLZKA));
	}

	@Test
	void pridaj() {
		for (IPozemok pozemok : pozemky) {
			assertTrue(quadTree.pridaj(pozemok));
		}
		assertEquals(MAX_POCET_POZEMKOV, quadTree.getAllPozemky().size());
	}

	@Test
	void findWithin() {
	}

	@Test
	void deletePozemok() {
	}
}
package com.aus.prva_semestralka.struktury;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.aus.prva_semestralka.generatory.Generator;
import com.aus.prva_semestralka.generatory.Generator2;
import com.aus.prva_semestralka.objekty.IPozemok;
import com.aus.prva_semestralka.objekty.IRecord;
import com.aus.prva_semestralka.objekty.Nehnutelnost;
import com.aus.prva_semestralka.objekty.Parcela;

import static org.junit.jupiter.api.Assertions.*;

class DynamickeHashovanieTest {

	DynamickeHashovanie<Parcela> hashovanie = new DynamickeHashovanie<>(Parcela.class, 3,  "subor.bin");
	private ArrayList<IRecord> pozemky;
	private final Generator generator = new Generator();
	private final int BLOKOVACI_FAKTOR = 2;

	private final Logger logger = Logger.getLogger(QuadTreeTest.class.getName());

	DynamickeHashovanieTest() throws FileNotFoundException {
	}

	@BeforeEach
	void setUp() {
		pozemky = new ArrayList<>();
		pozemky.addAll(generator.vygenerujPozemky(10000, 100, 100, true));
	}

	@Test
	void insert() {
		for (IRecord pozemok: pozemky) {
			assertTrue(hashovanie.insert((Parcela) pozemok));
			hashovanie.print();
			System.out.println("**********************************************************************************************");
		}
	}

	@Test
	void find() {
		for (IRecord pozemok: pozemky) {
			assertNotNull(hashovanie.najdiZaznam(pozemok));
		}
	}
}
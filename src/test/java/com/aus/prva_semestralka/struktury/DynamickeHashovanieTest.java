package com.aus.prva_semestralka.struktury;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.aus.prva_semestralka.generatory.Generator;
import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IRecord;
import com.aus.prva_semestralka.objekty.Ohranicenie;
import com.aus.prva_semestralka.objekty.Parcela;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamickeHashovanieTest {

	DynamickeHashovanie<Parcela> hashovanie = new DynamickeHashovanie<>(Parcela.class, 2,  "subor.bin");
	private ArrayList<IRecord> pozemky;
	private final Generator generator = new Generator();

	DynamickeHashovanieTest() throws FileNotFoundException {
	}

	@BeforeEach
	void setUp() {
		pozemky = new ArrayList<>();
		pozemky.addAll(generator.vygenerujPozemky(1000, 100, 100, true));
	}

	@Test
	void insert() {
		for (IRecord pozemok: pozemky) {
			assertTrue(hashovanie.insert((Parcela) pozemok));
//			hashovanie.print();
//			System.out.println("**********************************************************************************************");
		}

		Parcela parcela = new Parcela(99930, "testovacia parcela velka",
									  new Ohranicenie(new GpsPozicia("S", "V", 0.0, 0.0), new GpsPozicia("J", "Z", 100.0, 100.0)));
		assertTrue(hashovanie.insert(parcela));
		assertNotEquals(parcela, (hashovanie.najdiZaznam(parcela))); // parcela insertnuta ma len 15 znakov, parcela najdena ma 25 znakov, uz sa nerovnaju
	}

	@Test
	void find() {
		for (IRecord pozemok: pozemky) {
			hashovanie.insert((Parcela) pozemok);
		}
		for (IRecord pozemok: pozemky) {
			assertNotNull(hashovanie.najdiZaznam(pozemok));
		}

		// create new parcela and try to find it
		Parcela parcela = new Parcela(99930, "test", null);
		assertNull(hashovanie.najdiZaznam(parcela));
	}

	@AfterEach
	public void tearDown() throws Exception {
		hashovanie.close();
	}
}
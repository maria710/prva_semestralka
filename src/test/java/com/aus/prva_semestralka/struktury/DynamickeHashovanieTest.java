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

	DynamickeHashovanie<Parcela> hashovanie = new DynamickeHashovanie<>(Parcela.class, 2,  "subor.bin", "preplnovaciSubor.bin", 4);
	private ArrayList<IRecord> pozemky;
	private final Generator generator = new Generator();

	DynamickeHashovanieTest() throws FileNotFoundException {
	}

	@BeforeEach
	void setUp() {
		hashovanie.clear();
		pozemky = new ArrayList<>();
		pozemky.addAll(generator.vygenerujPozemky(10, 100, 100, true));
	}

	@Test
	void insert() {
		for (IRecord pozemok: pozemky) {
			System.out.println("Pridana parcela: " + ((Parcela) pozemok).getSupisneCislo());
			assertTrue(hashovanie.insert((Parcela) pozemok));

		}
		System.out.println(hashovanie.toStringHlavny());
		System.out.println("**********************************************************************************************");
		System.out.println(hashovanie.toStringPreplnovaci());

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
			if (hashovanie.najdiZaznam(pozemok) == null) {
				System.out.println("Parcela: " + ((Parcela) pozemok).getSupisneCislo() + " nebola najdena");
			}
			assertNotNull(hashovanie.najdiZaznam(pozemok));
		}

		Parcela parcela = new Parcela(99930, "test", null);
		assertNull(hashovanie.najdiZaznam(parcela));
	}

	@AfterEach
	public void tearDown() throws Exception {
		hashovanie.close();
	}
}
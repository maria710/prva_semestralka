package com.aus.prva_semestralka.struktury;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.aus.prva_semestralka.generatory.Generator;
import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IRecord;
import com.aus.prva_semestralka.objekty.Ohranicenie;
import com.aus.prva_semestralka.objekty.Parcela;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamickeHashovanieTest {

	DynamickeHashovanie<Parcela> hashovanie = new DynamickeHashovanie<>(Parcela.class, 2,  "subor.bin", "preplnovaciSubor.bin", 3);
	private ArrayList<IRecord> pozemky;
	private final Generator generator = new Generator();

	DynamickeHashovanieTest() throws FileNotFoundException {
	}

	@BeforeEach
	void setUp() {
		hashovanie.clear();
		pozemky = new ArrayList<>();
		pozemky.addAll(generator.vygenerujPozemky(500, 100, 100, true));
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
			if (hashovanie.najdiZaznam((Parcela) pozemok) == null) {
				System.out.println("Parcela: " + ((Parcela) pozemok).getSupisneCislo() + " nebola najdena");
			}
			assertNotNull(hashovanie.najdiZaznam((Parcela) pozemok));
		}

		Parcela parcela = new Parcela(99930, "test", null);
		assertNull(hashovanie.najdiZaznam(parcela));
	}

	@Test
	void delete() {
		for (IRecord pozemok: pozemky) {
			hashovanie.insert((Parcela) pozemok);
		}
		for (IRecord pozemok: pozemky) {
			assertTrue(hashovanie.delete((Parcela) pozemok));
		}
		for (IRecord pozemok: pozemky) {
			assertNull(hashovanie.najdiZaznam((Parcela) pozemok));
		}
	}

	@Test
	void deleteStriasanie() {
		for (IRecord pozemok: pozemky) {
			hashovanie.insert((Parcela) pozemok);
		}
		assertTrue(hashovanie.delete((Parcela) pozemky.get(2)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(10)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(0)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(1)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(3)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(4)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(5)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(6)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(7)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(8)));
		assertTrue(hashovanie.delete((Parcela) pozemky.get(9)));

		assertNull(hashovanie.najdiZaznam((Parcela) pozemky.get(2)));

		assertNotNull(hashovanie.najdiZaznam((Parcela) pozemky.get(11)));
		assertNotNull(hashovanie.najdiZaznam((Parcela) pozemky.get(12)));
		assertNotNull(hashovanie.najdiZaznam((Parcela) pozemky.get(13)));
		assertNotNull(hashovanie.najdiZaznam((Parcela) pozemky.get(14)));

	}

	@AfterEach
	public void tearDown() throws Exception {
		hashovanie.close();
	}
}
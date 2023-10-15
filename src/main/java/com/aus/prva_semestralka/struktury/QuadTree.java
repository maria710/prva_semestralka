package com.aus.prva_semestralka.struktury;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Objects;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IPozemok;

@Data
@RequiredArgsConstructor
public class QuadTree {

	private QTNode root;
	public static Integer maxHlbka;
	private Integer sirka;
	private Integer dlzka;

	public QuadTree(Integer maxHlbka, Integer sirka, Integer dlzka) {
		QuadTree.maxHlbka = maxHlbka;
		this.sirka = sirka;
		this.dlzka = dlzka;
		var prvaSuradnicaKorena = new GpsPozicia('S', 'Z',  0.0, 0.0);
		var druhaSuradnicaKorena = new GpsPozicia('S', 'Z', sirka.doubleValue(), dlzka.doubleValue());
		this.root = new QTNode(1, List.of(prvaSuradnicaKorena, druhaSuradnicaKorena), 1);
		root.rozdel();
	}

	public boolean pridaj(IPozemok pozemok) {
		// ak je pozemok velmi velky alebo neexistuje, tak ho nepridavame
		if (pozemok == null || !root.zmestiSa(pozemok)) {
			return false;
		}
		return root.pridajPozemok(pozemok);
	}

	public void vypisStrom() {
		// root.vypis();
	}

	// vyhladanie nehnutelnosti podla gps pozicie
	// vyhladanie parcely podla gps pozicie
	// vyhladanie vsetkych nehnutelnosti podla dvoch zadanych gps pozicii - tieto definuju obdlznik
	// pridanie nehnutelnosti
	// pridanie parcely
}

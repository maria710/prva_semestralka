package com.aus.prva_semestralka.struktury;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.aus.prva_semestralka.fileManazer.BlokManazer;
import com.aus.prva_semestralka.fileManazer.FileManazer;
import com.aus.prva_semestralka.objekty.Blok;
import com.aus.prva_semestralka.objekty.IRecord;

public class DynamickeHashovanie<T extends IRecord> {

	TrieNode<T> root;
	private final int blokovaciFaktor; // velkost bloku
	private final int blokovaciFaktorPreplnovaci; // velkost bloku pre preplnovaci subor
	private final FileManazer fileManazer;
	private final FileManazer fileManazerPreplnovaci;
	private int currentBitIndex = -1;
	private final int pocetBitovVHash = 3;

	private final BlokManazer<T> blokManazer;
	private final BlokManazer<T> blokManazerPreplnovaci;


	public DynamickeHashovanie(Class<T> classType, int blokovaciFaktor, String path, String pathPreplnovaci, int blokovaciFaktorPreplnovaci) throws FileNotFoundException {
		root = new TrieNodeExterny<>(null, 0);
		this.blokovaciFaktor = blokovaciFaktor;
		this.fileManazer = new FileManazer(path);
		this.fileManazerPreplnovaci = new FileManazer(pathPreplnovaci);
		this.blokovaciFaktorPreplnovaci = blokovaciFaktorPreplnovaci;

		this.blokManazer = new BlokManazer<>(classType, fileManazer, blokovaciFaktor);
		this.blokManazerPreplnovaci = new BlokManazer<>(classType, fileManazerPreplnovaci, blokovaciFaktorPreplnovaci);
	}

	public IRecord najdiZaznam(T record) {
		BitSet bitSet = getHash(record);
		TrieNodeExterny<T> externalNode = najdiExternyNode(bitSet);

		int indexBlokuNode = externalNode.getIndexBloku();
		if (indexBlokuNode == -1) {
			return null;
		}
		Blok<T> blok = blokManazer.citajBlokZoSuboru(indexBlokuNode);

		while (true) {
			var zaznam = blok.najdiZaznam(record);
			if (zaznam != null) {
				return zaznam;
			}
			if (blok.getNasledovnik() == -1) {
				return null;
			}
			blok = blokManazerPreplnovaci.citajBlokZoSuboru(blok.getNasledovnik());
		}
	}

	public boolean insert(T record) {

		if (najdiZaznam(record) != null) { // zaznamy v strome musia byt unikatne
			return false;
		}

		// aplikujem hashovaciu funkciu
		BitSet bitSet = getHash(record);

		TrieNodeExterny<T> currentNodeExterny = najdiExternyNode(bitSet);
		int indexBlokuNode = currentNodeExterny.getIndexBloku();


		if (currentNodeExterny.getIndexBloku() == 0) {
			System.out.println("Insertujem do node s blokom 0 ");
		}

		// mam externy vrchol
		if (indexBlokuNode == -1) { // ak este nemame blok
			blokManazer.zapisDoNovehoBloku(record, currentNodeExterny);
			return true;
		}

		// ak uz je vytvoreny blok, moze byt plny alebo nie
		var blok = blokManazer.citajBlokZoSuboru(indexBlokuNode);
		if (blok.getAktualnyPocetRecordov() != blokovaciFaktor) {
			blok.pridaj(record);
			currentNodeExterny.zvysPocetRecordov();
			blokManazer.zapisBlokDoSubor(blok, indexBlokuNode);
			return true;
		}

		if (currentBitIndex >= pocetBitovVHash) { // skontrolujeme ci sme nepresiahli pocet bitov
			currentNodeExterny.zvysPocetRecordov();
			return ulozDoPreplnovaciehoSuboru(record, blok, currentNodeExterny);
		}

		// ak je blok plny
		return rozdelNodeAZapis(record, currentNodeExterny, blok, indexBlokuNode);
	}

	public boolean delete(T record) {

		if (najdiZaznam(record) == null) { // musime skontrolovat ci tam uz je, ak nie nema vyznam mazat
			return false;
		}

		BitSet bitSet = getHash(record);
		TrieNodeExterny<T> externalNode = najdiExternyNode(bitSet);

		if (externalNode.getIndexBloku() == 0) {
			System.out.println("Deletujem v node s blokom 0");
		}

		int indexBlokuNode = externalNode.getIndexBloku();
		if (indexBlokuNode == -1) {
			return false;
		}

		BlokManazer<T> blokManazerCurrent = this.blokManazer;
		Blok<T> blok = blokManazer.citajBlokZoSuboru(indexBlokuNode);

		while (true) {
			var vymazalSa = blok.vymazRecord(record);
			if (vymazalSa) {
				externalNode.znizPocetRecordov();
				blokManazerCurrent.zapisBlokDoSubor(blok, indexBlokuNode);
				Blok<T> blokNaStrasenie = dajBlokNaStriasanie(externalNode);
				if (blokNaStrasenie != null) {
					blokManazerPreplnovaci.dealokujBlok(blokNaStrasenie.getIndex());
					externalNode.znizPocetBlokovVZretazeni();
				}
//				// dealokujeme blok ak je prazdny a nema nasledovnika
//				if (blok.getAktualnyPocetRecordov() == 0 && blok.getNasledovnik() == - 1) {
//					externalNode.setIndexBloku(-1);
//					blokManazer.dealokujBlok(indexBlokuNode);
//				}

				var zruseny = zrusInternyNodeAkSaDa((TrieNodeInterny<T>) externalNode.getParent());

				while(zruseny != null) {
					zruseny = zrusInternyNodeAkSaDa((TrieNodeInterny<T>) zruseny.getParent());
				}

				return true;
			}
			if (blok.getNasledovnik() == -1) {
				return false;
			}
			blokManazerCurrent = blokManazerPreplnovaci;
			indexBlokuNode = blok.getNasledovnik();
			blok = blokManazerPreplnovaci.citajBlokZoSuboru(indexBlokuNode);
		}
	}

	public boolean edit(T record) {

		var najdeny = najdiZaznam(record);
		if (najdeny == null) { // musime skontrolovat ci tam uz je, ak nie nema vyznam mazat
			return false;
		}

		najdeny.upravParametre(record);
		return false;
	}


	private Blok<T> dajBlokNaStriasanie(TrieNodeExterny<T> nodeExterny) {
		if (nodeExterny.getPocetRecordov() == 0 || nodeExterny.getPocetBlokovVZretazeni() == 0) {
			return null;
		}

		if (mozemeZiskatVolnyBlok(nodeExterny)) {

			ArrayList<Blok<T>> blokyVZretazeni = new ArrayList<>();
			ArrayList<T> zaznamy = new ArrayList<>();

			int indexBloku = nodeExterny.getIndexBloku();
			Blok<T> hlavnyBlok = blokManazer.citajBlokZoSuboru(indexBloku);

			var dalsi = hlavnyBlok.getNasledovnik();

			while (dalsi != -1) {
				var dalsiBlok = blokManazerPreplnovaci.citajBlokZoSuboru(dalsi);
				blokyVZretazeni.add(dalsiBlok);
				zaznamy.addAll(dalsiBlok.getRecords());
				dalsiBlok.clearRecords();
				dalsi = dalsiBlok.getNasledovnik();
			}
			int pocetDoHlavneho = blokovaciFaktor - hlavnyBlok.getAktualnyPocetRecordov();
			for (int i = pocetDoHlavneho;  i > 0;  i--) { // najskor naplnime blok v hlavnom subor
				if (!zaznamy.isEmpty()) {
					hlavnyBlok.pridaj(zaznamy.remove(0));
				}
			}
			blokManazer.zapisBlokDoSubor(hlavnyBlok, indexBloku);

			while (!zaznamy.isEmpty()) { // naplnime bloky v zretazeni, jeden musi ostat prazdny
				for (int i = 0; i < blokyVZretazeni.size() - 1; i++) {
					for (int j = 0; j < blokovaciFaktorPreplnovaci; j++) {
						if (!zaznamy.isEmpty()) {
							blokyVZretazeni.get(i).pridaj(zaznamy.remove(0));
						}
					}
					blokManazerPreplnovaci.zapisBlokDoSubor(blokyVZretazeni.get(i), blokyVZretazeni.get(i).getIndex());
				}
			}
			if (blokyVZretazeni.size() > 1) {
				blokyVZretazeni.get(blokyVZretazeni.size() - 2).setNasledovnik(-1);
				blokManazerPreplnovaci.zapisBlokDoSubor(blokyVZretazeni.get(blokyVZretazeni.size() - 2), blokyVZretazeni.get(blokyVZretazeni.size() - 2).getIndex());
			} else {
				hlavnyBlok.setNasledovnik(-1);
				blokManazer.zapisBlokDoSubor(hlavnyBlok, indexBloku);
			}
		    return blokyVZretazeni.get(blokyVZretazeni.size() - 1); // vratime posledny blok v zretazeni
		} else {
			return null;
		}
	}

	private boolean mozemeZiskatVolnyBlok(TrieNodeExterny<T> externalNode) {
		int celkovaKapacita = blokovaciFaktor + (blokovaciFaktorPreplnovaci * externalNode.getPocetBlokovVZretazeni());
		int pocetRecordov = externalNode.getPocetRecordov();

		// ak je celkovy pocet recordov mensi ako celkova kapacita minus velkost bloku pre preplnovaci subor, mozeme ziskat volny blok
		return pocetRecordov <= celkovaKapacita - blokovaciFaktorPreplnovaci;
	}

	private boolean rozdelNodeAZapis(T record, TrieNodeExterny<T> currentNodeExterny, Blok<T> blok, int indexBlokuNode) {

		while (true) {
			if (currentBitIndex >= pocetBitovVHash) { // skontrolujeme ci sme nepresiahli pocet bitov
				currentNodeExterny.zvysPocetRecordov();
				return ulozDoPreplnovaciehoSuboru(record, blok, currentNodeExterny);
			}

			TrieNodeInterny<T> newTrieNodeInterny = vytvorNovyInternyNode(currentNodeExterny); // z externeho sa stal interny lebo sa rozdeli
			List<T> records = new ArrayList<>(blok.getRecords());

			int indexBlokuPravy = blokManazer.alokujBlok();
			Blok<T> blokPravy = blokManazer.citajBlokZoSuboru(indexBlokuPravy);
			blok.clear();

			((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).setIndexBloku(indexBlokuNode);
			((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).setIndexBloku(indexBlokuPravy);

			boolean padloDoLava = false;
			boolean padloDoPrava = false;

			for (T iRecord : records) { // presunieme vsetky zaznamy do novych blokov
				BitSet bitset = getHash(iRecord);
				if (bitset.get(currentBitIndex)) {
					padloDoPrava = true;
					blokPravy.pridaj(iRecord);
					((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).zvysPocetRecordov();
				} else {
					padloDoLava = true;
					blok.pridaj(iRecord);
					((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).zvysPocetRecordov();
				}
			}

			// ak sa roztriedili do oboch tak mozeme pridat vkladany zaznam -> vytvorili sme miesto
			if (skusVlozitZaznam(record, blok, blokPravy, newTrieNodeInterny)) {
				blokManazer.zapisBlokDoSubor(blok, indexBlokuNode);
				blokManazer.zapisBlokDoSubor(blokPravy, indexBlokuPravy);
				return true;
			}

			// dealokujeme nepouzity blok
			if (padloDoPrava) {
				currentNodeExterny = (TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn();
				blokManazer.zapisBlokDoSubor(blokPravy, indexBlokuPravy);
				blok = blokPravy;
				blok.setIndex(indexBlokuPravy);
			} else {
				((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).setIndexBloku(-1);
				blokManazer.dealokujBlok(indexBlokuPravy);
			}
			if (padloDoLava) {
				currentNodeExterny = (TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn();
				blok.setIndex(indexBlokuNode);
				blokManazer.zapisBlokDoSubor(blok, indexBlokuNode);
			} else {
				((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).setIndexBloku(-1);
				blokManazer.dealokujBlok(indexBlokuNode);
				indexBlokuNode = indexBlokuPravy;
			}
			currentBitIndex++;
		}
	}

	private boolean skusVlozitZaznam(T record, Blok<T> blok, Blok<T> blokPravy, TrieNodeInterny<T> newTrieNodeInterny) {
		BitSet bitset = getHash(record);
		if (bitset.get(currentBitIndex)) {
			if (blokPravy.getAktualnyPocetRecordov() != blokovaciFaktor) {
				blokPravy.pridaj(record);
				((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).zvysPocetRecordov();
				return true;
			}
		} else {
			if (blok.getAktualnyPocetRecordov() != blokovaciFaktor) {
				blok.pridaj(record);
				((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).zvysPocetRecordov();
				return true;
			}
		}
		return false;
	}

	private boolean ulozDoPreplnovaciehoSuboru(T record, Blok<T> blokVHlavnomSubore, TrieNodeExterny<T> currentNodeExterny) {
		int index = blokVHlavnomSubore.getNasledovnik();
		if (index == -1) { // robim len pre hlavny subor
			index = blokManazerPreplnovaci.alokujBlok();
			var blokVPreplnovacomSubore = blokManazerPreplnovaci.citajBlokZoSuboru(index);
			blokVHlavnomSubore.setNasledovnik(blokVPreplnovacomSubore.getIndex());
			blokVPreplnovacomSubore.setPredchodca(blokVHlavnomSubore.getIndex());
			currentNodeExterny.zvysPocetBlokovVZretazeni();
			blokManazer.zapisBlokDoSubor(blokVHlavnomSubore, blokVHlavnomSubore.getIndex());
			blokManazerPreplnovaci.zapisBlokDoSubor(blokVPreplnovacomSubore, blokVPreplnovacomSubore.getIndex());
		}

		while (true) {
			var blok = blokManazerPreplnovaci.citajBlokZoSuboru(index);
			if (blok.getAktualnyPocetRecordov() != blokovaciFaktorPreplnovaci) { // ak sa zmesti do nasledovnika
				blok.pridaj(record);
				blokManazerPreplnovaci.zapisBlokDoSubor(blok, index);
				return true;
			}
			index = blok.getNasledovnik();
			if (index == -1) {
				index = blokManazerPreplnovaci.alokujBlok();
				blok.setNasledovnik(index);
				currentNodeExterny.zvysPocetBlokovVZretazeni();
				blokManazerPreplnovaci.zapisBlokDoSubor(blok, blok.getIndex());
			}
		}
	}

	private TrieNodeExterny<T> najdiExternyNode(BitSet bitSet) {
		currentBitIndex = 0;

		TrieNode<T> currentNode = root;
		while (currentNode instanceof TrieNodeInterny) {
			currentNode = bitSet.get(currentBitIndex) ? ((TrieNodeInterny<T>) currentNode).getPravySyn() : ((TrieNodeInterny<T>) currentNode).getLavySyn();
			currentBitIndex++;
		}

		return (TrieNodeExterny<T>) currentNode;
	}

	private TrieNodeInterny<T> vytvorNovyInternyNode(TrieNodeExterny<T> currentNodeExterny) {
		TrieNodeInterny<T> newTrieNodeInterny = new TrieNodeInterny<>(currentNodeExterny.getParent());
		TrieNodeInterny<T> parent = (TrieNodeInterny<T>) currentNodeExterny.getParent();

		if (parent == null) {
			root = newTrieNodeInterny;
		} else if (parent.getLavySyn() == currentNodeExterny) {
			parent.setLavySyn(newTrieNodeInterny);
		} else {
			parent.setPravySyn(newTrieNodeInterny);
		}

		return newTrieNodeInterny;
	}

	private TrieNodeExterny<T> zrusInternyNodeAkSaDa(TrieNodeInterny<T> trieNodeInterny) {
		if (trieNodeInterny == null || trieNodeInterny.getLavySyn() instanceof TrieNodeInterny<T> || trieNodeInterny.getPravySyn() instanceof TrieNodeInterny<T>) {
			return null;
		}

		TrieNodeInterny<T> parent = (TrieNodeInterny<T>) trieNodeInterny.getParent();

		TrieNodeExterny<T> lavySynInterneho = (TrieNodeExterny<T>) trieNodeInterny.getLavySyn();
		TrieNodeExterny<T> pravySynInterneho = (TrieNodeExterny<T>) trieNodeInterny.getPravySyn();

		if (lavySynInterneho.getPocetRecordov() + pravySynInterneho.getPocetRecordov() > blokovaciFaktor) { // ak sa nezmesia, koncime
			return null;
		}

		TrieNodeExterny<T> nodeNaVymenu = lavySynInterneho.getIndexBloku() != -1 ? lavySynInterneho : pravySynInterneho;
		TrieNodeExterny<T> nodeNaVyhodenie = lavySynInterneho.getIndexBloku() == -1 ? lavySynInterneho : pravySynInterneho;

		Blok<T> blokNaVymenu = blokManazer.citajBlokZoSuboru(nodeNaVymenu.getIndexBloku());
		Blok<T> blokNaVyhodenie = nodeNaVyhodenie.getIndexBloku() == -1 ? null : blokManazer.citajBlokZoSuboru(nodeNaVyhodenie.getIndexBloku());

		if (blokNaVymenu.getNasledovnik() != -1 || (blokNaVyhodenie != null && blokNaVyhodenie.getNasledovnik() != -1)) {
			return null;
		}

		if (blokNaVyhodenie != null) { // ak v druhom bloku nieco bolo, presunieme
			for (T zaznam : blokNaVyhodenie.getRecords()) {
				blokNaVymenu.pridaj(zaznam);
				nodeNaVymenu.zvysPocetRecordov();
			}
			blokManazer.dealokujBlok(blokNaVyhodenie.getIndex());
		}
		blokManazer.zapisBlokDoSubor(blokNaVymenu, blokNaVymenu.getIndex());

		if (parent == null) { // ak sme v roote
			nodeNaVymenu.setParent(null);
			root = nodeNaVymenu;
			return nodeNaVymenu;
		}

		nodeNaVymenu.setParent(parent);

		if (parent.getLavySyn() == trieNodeInterny) {
			parent.setLavySyn(nodeNaVymenu);
		} else {
			parent.setPravySyn(nodeNaVymenu);
		}

		return nodeNaVymenu;
	}


	public String toStringPreplnovaci() {
		return toStringSubor(blokManazerPreplnovaci);
	}

	public String toStringHlavny() {
		return toStringSubor(blokManazer);
	}

	public String toStringSubor(BlokManazer<T> blokManazer) {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 0; i < blokManazer.getVelkostSuboru(); i++) {
			Blok<T> blok = blokManazer.citajBlokZoSuboru(i);
			stringBuilder.append(blok.print(i));
		}
		return stringBuilder.toString();
	}

	public void clear() {
		fileManazer.clear();
		fileManazerPreplnovaci.clear();
	}

	private BitSet getHash(IRecord record) {
		return record.getHash(pocetBitovVHash); // tu budem prenastavovat pocet bitov
	}

	public void close() throws Exception {
		this.fileManazer.close();
	}}

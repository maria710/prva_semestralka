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
	private int prvyVolnyBlokIndex;
	private final FileManazer fileManazer;
	private int currentBitIndex = -1;
	private final Class<T> classType;

	private final int blokovaciFaktorPreplnovaci; // velkost bloku pre preplnovaci subor
	private int prvyVolnyBlokPreplnovaciIndex;
	private int pocetBlokovPreplnovaci;
	private final FileManazer fileManazerPreplnovaci;

	private final int pocetBitovVHash = 2;


	private final BlokManazer<T> blokManazer;
	private final BlokManazer<T> blokManazerPreplnovaci;


	public DynamickeHashovanie(Class<T> classType, int blokovaciFaktor, String path, String pathPreplnovaci, int blokovaciFaktorPreplnovaci)
			throws FileNotFoundException {
		root = new TrieNodeExterny<>(null, 0);
		prvyVolnyBlokIndex = -1;
		this.classType = classType;
		this.blokovaciFaktor = blokovaciFaktor;
		this.fileManazer = new FileManazer(path);
		this.fileManazerPreplnovaci = new FileManazer(pathPreplnovaci);
		this.blokovaciFaktorPreplnovaci = blokovaciFaktorPreplnovaci;
		this.prvyVolnyBlokPreplnovaciIndex = -1;

		this.blokManazer = new BlokManazer<>(classType, fileManazer, blokovaciFaktor, prvyVolnyBlokIndex);
		this.blokManazerPreplnovaci = new BlokManazer<>(classType, fileManazerPreplnovaci, blokovaciFaktorPreplnovaci, prvyVolnyBlokPreplnovaciIndex);
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

		// mam externy vrchol
		if (indexBlokuNode == -1) { // ak este nemame blok
			zapisDoNovehoBloku(record, currentNodeExterny);
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

		int indexBlokuNode = externalNode.getIndexBloku();
		if (indexBlokuNode == -1) {
			return false;
		}

		BlokManazer<T> blokManazerCurrent = this.blokManazer;
		Blok<T> blok = blokManazer.citajBlokZoSuboru(indexBlokuNode);

		while (true) {
			var vyzalSa = blok.vymazRecord(record);
			if (vyzalSa) {
				externalNode.znizPocetRecordov();
				blokManazerCurrent.zapisBlokDoSubor(blok, indexBlokuNode);
				Blok<T> blokNaStrasenie = dajBlokNaStriasanie(externalNode);
				if (blokNaStrasenie != null) {
					dealokujBlokVPreplnovacom(blokNaStrasenie.getIndex());
					externalNode.znizPocetBlokovVZretazeni();
				}
				// dealokujeme blok ak je prazdny a nema nasledovnika
				if (blok.getAktualnyPocetRecordov() == 0 && blok.getNasledovnik() == - 1) {
					externalNode.setIndexBloku(-1);
					dealokujBlok(indexBlokuNode);
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

	private void dealokujBlokVPreplnovacom(int index) {
		Blok<T> blok = blokManazerPreplnovaci.citajBlokZoSuboru(index);
		blok.clear();
		blok.setNasledovnik(prvyVolnyBlokPreplnovaciIndex);

		if (prvyVolnyBlokPreplnovaciIndex != -1) {
			Blok<T> prvyVolnyBlok = blokManazerPreplnovaci.citajBlokZoSuboru(prvyVolnyBlokPreplnovaciIndex);
			prvyVolnyBlok.setPredchodca(index);
			blokManazerPreplnovaci.zapisBlokDoSubor(prvyVolnyBlok, prvyVolnyBlokPreplnovaciIndex);
		} else {
			prvyVolnyBlokPreplnovaciIndex = index;
		}

		pocetBlokovPreplnovaci--;
		blokManazerPreplnovaci.zapisBlokDoSubor(blok, index);

		int pocetBlokovNaOdstranenie = 0;
		int pocetBlokovVSubore = (int) this.fileManazerPreplnovaci.getFileSize() / blok.getSize(blokovaciFaktorPreplnovaci);
		if (index == pocetBlokovVSubore - 1) {
			for (int i = pocetBlokovVSubore - 1; i >= 0; i--) {
				Blok<T> blokNaOdstranenie = blokManazerPreplnovaci.citajBlokZoSuboru(i);
				if (blokNaOdstranenie.getNasledovnik() != -1) {
					// nastav nasledovnika predchodcovi
					Blok<T> nasledovnik = blokManazerPreplnovaci.citajBlokZoSuboru(blokNaOdstranenie.getNasledovnik());
					nasledovnik.setPredchodca(blokNaOdstranenie.getPredchodca());
					blokManazerPreplnovaci.zapisBlokDoSubor(nasledovnik, blokNaOdstranenie.getNasledovnik());
				}
				if (blokNaOdstranenie.getPredchodca() != -1) {
					Blok<T> predchodca = blokManazerPreplnovaci.citajBlokZoSuboru(blokNaOdstranenie.getPredchodca());
					predchodca.setNasledovnik(blokNaOdstranenie.getNasledovnik());
					blokManazerPreplnovaci.zapisBlokDoSubor(predchodca, blokNaOdstranenie.getPredchodca());
				}

				if (blokNaOdstranenie.getIndex() == prvyVolnyBlokIndex) {
					prvyVolnyBlokIndex = blokNaOdstranenie.getNasledovnik();
				}
				if (blokNaOdstranenie.getAktualnyPocetRecordov() == 0) {
					if (i == prvyVolnyBlokPreplnovaciIndex) {
						prvyVolnyBlokPreplnovaciIndex = - 1;
					}
					pocetBlokovNaOdstranenie++;
				} else {
					break;
				}
			}
			this.fileManazerPreplnovaci.skratSubor((pocetBlokovVSubore - pocetBlokovNaOdstranenie) * blok.getSize(blokovaciFaktorPreplnovaci));
		}
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

	public boolean edit() {
		return false;
	}

	private BitSet getHash(IRecord record) {
		return record.getHash(pocetBitovVHash); // tu budem prenastavovat pocet bitov
	}

	public void close() throws Exception {
		this.fileManazer.close();
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

			if (padloDoLava && padloDoPrava) { // ak sa roztriedili do oboch tak mozeme pridat vkladany zaznam -> vytvorili sme miesto
				BitSet bitset = getHash(record);
				if (bitset.get(currentBitIndex)) {
					blokPravy.pridaj(record);
					((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).zvysPocetRecordov();
				} else {
					blok.pridaj(record);
					((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).zvysPocetRecordov();
				}
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
				dealokujBlok(indexBlokuPravy);
			}
			if (padloDoLava) {
				currentNodeExterny = (TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn();
				blok.setIndex(indexBlokuNode);
				blokManazer.zapisBlokDoSubor(blok, indexBlokuNode);
			} else {
				((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).setIndexBloku(-1);
				dealokujBlok(indexBlokuNode);
				indexBlokuNode = indexBlokuPravy;
			}
			currentBitIndex++;
		}
	}

	private boolean ulozDoPreplnovaciehoSuboru(T record, Blok<T> blokVHlavnomSubore, TrieNodeExterny<T> currentNodeExterny) {
		int index = blokVHlavnomSubore.getNasledovnik();
		if (index == -1) { // robim len pre hlavny subor
			index = blokManazerPreplnovaci.alokujBlok();
			blokVHlavnomSubore.setNasledovnik(index);
			currentNodeExterny.zvysPocetBlokovVZretazeni();
			blokManazer.zapisBlokDoSubor(blokVHlavnomSubore, blokVHlavnomSubore.getIndex());
		}

		while (true) {
			var blok = blokManazerPreplnovaci.citajBlokZoSuboru(index);
			if (blok.getAktualnyPocetRecordov() != blokovaciFaktorPreplnovaci) {
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

	private void zapisDoNovehoBloku(T record, TrieNodeExterny<T> currentNodeExterny) {
		int indexBloku = blokManazer.alokujBlok();
		var blok = blokManazer.citajBlokZoSuboru(indexBloku);
		blok.pridaj(record);
		currentNodeExterny.setIndexBloku(indexBloku);
		currentNodeExterny.zvysPocetRecordov();
		blokManazer.zapisBlokDoSubor(blok, indexBloku);
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

	private void dealokujBlok(int indexBloku) {
		Blok<T> blok = blokManazer.citajBlokZoSuboru(indexBloku);
		blok.clear();
		blok.setNasledovnik(prvyVolnyBlokIndex);

		if (prvyVolnyBlokIndex != -1) {
			Blok<T> prvyVolnyBlok = blokManazer.citajBlokZoSuboru(prvyVolnyBlokIndex);
			prvyVolnyBlok.setPredchodca(indexBloku);
			blokManazer.zapisBlokDoSubor(prvyVolnyBlok, prvyVolnyBlokIndex);
		} else {
			prvyVolnyBlokIndex = indexBloku;
		}

		blokManazer.zapisBlokDoSubor(blok, indexBloku);

		int pocetBlokovNaOdstranenie = 0;
		var pocetBlokovVSubore = (int) fileManazer.getFileSize()/blok.getSize(blokovaciFaktor);
		if (indexBloku == pocetBlokovVSubore) {
			for (int i = pocetBlokovVSubore - 1; i >= 0; i--) {
				Blok<T> blokNaOdstranenie = blokManazer.citajBlokZoSuboru(i);
				if (blokNaOdstranenie.getNasledovnik() != -1) {
					// nastav nasledovnika predchodcovi
					Blok<T> nasledovnik = blokManazer.citajBlokZoSuboru(blokNaOdstranenie.getNasledovnik());
					nasledovnik.setPredchodca(blokNaOdstranenie.getPredchodca());
					blokManazer.zapisBlokDoSubor(nasledovnik, blokNaOdstranenie.getNasledovnik());
				}
				if (blokNaOdstranenie.getPredchodca() != -1) {
					Blok<T> predchodca = blokManazer.citajBlokZoSuboru(blokNaOdstranenie.getPredchodca());
					predchodca.setNasledovnik(blokNaOdstranenie.getNasledovnik());
					blokManazer.zapisBlokDoSubor(predchodca, blokNaOdstranenie.getPredchodca());
				}

				if (blokNaOdstranenie.getIndex() == prvyVolnyBlokIndex) {
					prvyVolnyBlokIndex = blokNaOdstranenie.getNasledovnik();
				}

				if (blokNaOdstranenie.getAktualnyPocetRecordov() == 0) {
					pocetBlokovNaOdstranenie++;
				} else {
					break;
				}
			}
			this.fileManazer.skratSubor((pocetBlokovVSubore + 1 - pocetBlokovNaOdstranenie) * blok.getSize(blokovaciFaktor));
		}
	}

	public String toStringPreplnovaci() {

		StringBuilder stringBuilder = new StringBuilder();
		Blok<T> blok = new Blok<>(classType);
		for (int i = 0; i < this.fileManazerPreplnovaci.getFileSize() / blok.getSize(blokovaciFaktorPreplnovaci); i++) {

			Blok<T> blok2 = blokManazerPreplnovaci.citajBlokZoSuboru(i);
			stringBuilder.append(blok2.printPreplnovaci(i));
		}
		return stringBuilder.toString();
	}

	public String toStringHlavny() {

		StringBuilder stringBuilder = new StringBuilder();
		Blok<T> blok = new Blok<>(classType);
		for (int i = 0; i < this.fileManazer.getFileSize() / blok.getSize(blokovaciFaktor); i++) {

			Blok<T> blok2 = blokManazer.citajBlokZoSuboru(i);
			stringBuilder.append(blok2.print(i));
		}
		return stringBuilder.toString();
	}

	public void clear() {
		fileManazer.clear();
		fileManazerPreplnovaci.clear();
	}
}

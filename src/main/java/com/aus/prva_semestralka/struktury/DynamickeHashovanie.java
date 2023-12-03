package com.aus.prva_semestralka.struktury;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.aus.prva_semestralka.fileManazer.FileManazer;
import com.aus.prva_semestralka.objekty.Blok;
import com.aus.prva_semestralka.objekty.IRecord;

public class DynamickeHashovanie<T extends IRecord> {

	TrieNode<T> root;
	private final int blokovaciFaktor; // velkost bloku
	private int prvyVolnyBlokIndex;
	private int pocetBlokov;
	private final FileManazer fileManazer;
	private int currentBitIndex = -1;
	private final Class<T> classType;

	private final int blokovaciFaktorPreplnovaci; // velkost bloku pre preplnovaci subor
	private int prvyVolnyBlokPreplnovaciIndex;
	private int pocetBlokovPreplnovaci;
	private final FileManazer fileManazerPreplnovaci;

	private final int pocetBitovVHash = 2;

	public DynamickeHashovanie(Class<T> classType, int blokovaciFaktor, String path, String pathPreplnovaci, int blokovaciFaktorPreplnovaci)
			throws FileNotFoundException {
		root = new TrieNodeExterny<>(null, 0);
		prvyVolnyBlokIndex = -1;
		this.classType = classType;
		this.blokovaciFaktor = blokovaciFaktor;
		pocetBlokov = 0;
		this.fileManazer = new FileManazer(path);
		this.fileManazerPreplnovaci = new FileManazer(pathPreplnovaci);
		this.blokovaciFaktorPreplnovaci = blokovaciFaktorPreplnovaci;
		this.prvyVolnyBlokPreplnovaciIndex = -1;
	}

	public IRecord najdiZaznam(T record) {
		BitSet bitSet = getHash(record);
		TrieNodeExterny<T> externalNode = najdiExternyNode(bitSet);

		int indexBlokuNode = externalNode.getIndexBloku();
		if (indexBlokuNode == -1) {
			return null;
		}
		Blok<T> blok = citajBlokZoSuboru(fileManazer, indexBlokuNode, blokovaciFaktor);

		while (true) {
			var zaznam = blok.najdiZaznam(record);
			if (zaznam != null) {
				return zaznam;
			}
			if (blok.getNasledovnik() == -1) {
				return null;
			}
			blok = citajBlokZoSuboru(fileManazerPreplnovaci, blok.getNasledovnik(), blokovaciFaktorPreplnovaci);
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
		var blok = citajBlokZoSuboru(fileManazer, indexBlokuNode, blokovaciFaktor);
		blok.setIndex(indexBlokuNode);
		if (blok.getAktualnyPocetRecordov() != blokovaciFaktor) {
			blok.pridaj(record);
			currentNodeExterny.zvysPocetRecordov();
			zapisBlokDoSubor(fileManazer, blok, indexBlokuNode, blokovaciFaktor);
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

		FileManazer fileManazer = this.fileManazer;
		int blokovaciFaktor = this.blokovaciFaktor;
		Blok<T> blokHlavny = citajBlokZoSuboru(fileManazer, indexBlokuNode, blokovaciFaktor);
		Blok<T> blok = blokHlavny;

		while (true) {
			var vyzalSa = blok.vymazRecord(record);
			if (vyzalSa) {
				externalNode.znizPocetRecordov();
				zapisBlokDoSubor(fileManazer, blok, indexBlokuNode, blokovaciFaktor);
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
			fileManazer = fileManazerPreplnovaci;
			blokovaciFaktor = blokovaciFaktorPreplnovaci;
			indexBlokuNode = blok.getNasledovnik();
			blok = citajBlokZoSuboru(fileManazer, indexBlokuNode, blokovaciFaktor);
		}
	}

	private void dealokujBlokVPreplnovacom(int index) {
		Blok<T> blok = citajBlokZoSuboru(fileManazerPreplnovaci, index, blokovaciFaktorPreplnovaci);
		blok.clear();
		blok.setNasledovnik(prvyVolnyBlokPreplnovaciIndex);

		if (prvyVolnyBlokPreplnovaciIndex != -1) {
			Blok<T> prvyVolnyBlok = citajBlokZoSuboru(fileManazerPreplnovaci, prvyVolnyBlokPreplnovaciIndex, blokovaciFaktorPreplnovaci);
			prvyVolnyBlok.setPredchodca(index);
			zapisBlokDoSubor(fileManazerPreplnovaci, prvyVolnyBlok, prvyVolnyBlokPreplnovaciIndex, blokovaciFaktorPreplnovaci);
		} else {
			prvyVolnyBlokPreplnovaciIndex = index;
		}

		pocetBlokovPreplnovaci--;
		zapisBlokDoSubor(fileManazerPreplnovaci, blok, index, blokovaciFaktorPreplnovaci);

		int pocetBlokovNaOdstranenie = 0;
		int pocetBlokovVSubore = (int) this.fileManazerPreplnovaci.getFileSize() / blok.getSize(blokovaciFaktorPreplnovaci);
		if (index == pocetBlokovVSubore - 1) {
			for (int i = pocetBlokovVSubore - 1; i >= 0; i--) {
				Blok<T> blokNaOdstranenie = citajBlokZoSuboru(fileManazerPreplnovaci, i, blokovaciFaktorPreplnovaci);
				if (blokNaOdstranenie.getNasledovnik() != -1) {
					// nastav nasledovnika predchodcovi
					Blok<T> nasledovnik = citajBlokZoSuboru(fileManazerPreplnovaci, blokNaOdstranenie.getNasledovnik(), blokovaciFaktorPreplnovaci);
					nasledovnik.setPredchodca(blokNaOdstranenie.getPredchodca());
					zapisBlokDoSubor(fileManazerPreplnovaci, nasledovnik, blokNaOdstranenie.getNasledovnik(), blokovaciFaktorPreplnovaci);
				}
				if (blokNaOdstranenie.getPredchodca() != -1) {
					Blok<T> predchodca = citajBlokZoSuboru(fileManazerPreplnovaci, blokNaOdstranenie.getPredchodca(), blokovaciFaktorPreplnovaci);
					predchodca.setNasledovnik(blokNaOdstranenie.getNasledovnik());
					zapisBlokDoSubor(fileManazerPreplnovaci, predchodca, blokNaOdstranenie.getPredchodca(), blokovaciFaktorPreplnovaci);
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
			Blok<T> hlavnyBlok = citajBlokZoSuboru(fileManazer, indexBloku, blokovaciFaktor);
			hlavnyBlok.setIndex(indexBloku);

			var dalsi = hlavnyBlok.getNasledovnik();

			while (dalsi != -1) {
				var dalsiBlok = citajBlokZoSuboru(fileManazerPreplnovaci, dalsi, blokovaciFaktorPreplnovaci);
				dalsiBlok.setIndex(dalsi);
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
			zapisBlokDoSubor(fileManazer, hlavnyBlok, indexBloku, blokovaciFaktor);

			while (!zaznamy.isEmpty()) { // naplnime bloky v zretazeni, jeden musi ostat prazdny
				for (int i = 0; i < blokyVZretazeni.size() - 1; i++) {
					for (int j = 0; j < blokovaciFaktorPreplnovaci; j++) {
						if (!zaznamy.isEmpty()) {
							blokyVZretazeni.get(i).pridaj(zaznamy.remove(0));
						}
					}
					zapisBlokDoSubor(fileManazerPreplnovaci, blokyVZretazeni.get(i), blokyVZretazeni.get(i).getIndex(), blokovaciFaktorPreplnovaci);
				}
			}
			if (blokyVZretazeni.size() > 1) {
				blokyVZretazeni.get(blokyVZretazeni.size() - 2).setNasledovnik(-1);
				zapisBlokDoSubor(fileManazerPreplnovaci, blokyVZretazeni.get(blokyVZretazeni.size() - 2), blokyVZretazeni.get(blokyVZretazeni.size() - 2).getIndex(), blokovaciFaktorPreplnovaci);
			} else {
				hlavnyBlok.setNasledovnik(-1);
				zapisBlokDoSubor(fileManazer, hlavnyBlok, indexBloku, blokovaciFaktor);
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

	private void zapisBlokDoSubor(FileManazer fileManazer, Blok<T> blok, int indexBloku, int blokovaciFaktor) {
		byte[] data = blok.toByteArray(blokovaciFaktor);
		fileManazer.write(data, indexBloku * blok.getSize(blokovaciFaktor));
	}

	private Blok<T> citajBlokZoSuboru(FileManazer fileManazer, int indexBloku, int blokovaciFaktor) {
		Blok<T> blok = new Blok<>(classType);
		byte[] data = fileManazer.read(blok.getSize(blokovaciFaktor), indexBloku * blok.getSize(blokovaciFaktor));
		var blokReturn = blok.fromByteArray(data);
		blokReturn.setIndex(indexBloku);
		return blokReturn;
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

			int indexBlokuPravy = alokujBlok();
			Blok<T> blokPravy = citajBlokZoSuboru(fileManazer, indexBlokuPravy, blokovaciFaktor);
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
				zapisBlokDoSubor(fileManazer, blok, indexBlokuNode, blokovaciFaktor);
				zapisBlokDoSubor(fileManazer, blokPravy, indexBlokuPravy, blokovaciFaktor);
				return true;
			}

			// dealokujeme nepouzity blok
			if (padloDoPrava) {
				currentNodeExterny = (TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn();
				zapisBlokDoSubor(fileManazer, blokPravy, indexBlokuPravy, blokovaciFaktor);
				blok = blokPravy;
				blok.setIndex(indexBlokuPravy);
			} else {
				((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).setIndexBloku(-1);
				dealokujBlok(indexBlokuPravy);
			}
			if (padloDoLava) {
				currentNodeExterny = (TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn();
				blok.setIndex(indexBlokuNode);
				zapisBlokDoSubor(fileManazer, blok, indexBlokuNode, blokovaciFaktor);
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
			index = alokujIndexVPreplnujucomSubore();
			blokVHlavnomSubore.setNasledovnik(index);
			currentNodeExterny.zvysPocetBlokovVZretazeni();
			zapisBlokDoSubor(fileManazer, blokVHlavnomSubore, blokVHlavnomSubore.getIndex(), blokovaciFaktor);
		}

		while (true) {
			var blok = citajBlokZoSuboru(fileManazerPreplnovaci, index, blokovaciFaktorPreplnovaci);
			if (blok.getAktualnyPocetRecordov() != blokovaciFaktorPreplnovaci) {
				blok.pridaj(record);
				zapisBlokDoSubor(fileManazerPreplnovaci, blok, index, blokovaciFaktorPreplnovaci);
				return true;
			}
			index = blok.getNasledovnik();
			if (index == -1) {
				index = alokujIndexVPreplnujucomSubore();
				blok.setNasledovnik(index);
				currentNodeExterny.zvysPocetBlokovVZretazeni();
				zapisBlokDoSubor(fileManazerPreplnovaci, blok, blok.getIndex(), blokovaciFaktorPreplnovaci);
			}
		}
	}

	private int alokujIndexVPreplnujucomSubore() {
		int index;
		if (prvyVolnyBlokPreplnovaciIndex == -1) { // ak nemam ziadny volny blok alokujem na konci
			Blok<T> blok = new Blok<>(classType);
			index = pocetBlokovPreplnovaci;
			blok.setNasledovnik(-1);
			blok.setPredchodca(-1);
			blok.setNasledovnik(-1);
			zapisBlokDoSubor(fileManazerPreplnovaci, blok, index, blokovaciFaktorPreplnovaci); // musime zapisay
			pocetBlokovPreplnovaci++;
			return pocetBlokovPreplnovaci - 1;
		}

		var prvyVolnyBlok = citajBlokZoSuboru(fileManazerPreplnovaci, prvyVolnyBlokPreplnovaciIndex, blokovaciFaktorPreplnovaci);
		int indexNasledovnika = prvyVolnyBlok.getNasledovnik();
		if (indexNasledovnika != -1) {
			var nasledovnik = citajBlokZoSuboru(fileManazerPreplnovaci, indexNasledovnika, blokovaciFaktorPreplnovaci);
			nasledovnik.setPredchodca(-1);
			zapisBlokDoSubor(fileManazerPreplnovaci, nasledovnik, indexNasledovnika, blokovaciFaktorPreplnovaci);
		}

		prvyVolnyBlok.setNasledovnik(-1);
		prvyVolnyBlok.setPredchodca(-1);

		zapisBlokDoSubor(fileManazerPreplnovaci, prvyVolnyBlok, prvyVolnyBlokPreplnovaciIndex, blokovaciFaktorPreplnovaci);

		index = prvyVolnyBlokPreplnovaciIndex;
		prvyVolnyBlokPreplnovaciIndex = indexNasledovnika;
		pocetBlokovPreplnovaci++;
		return index;
	}

	private void zapisDoNovehoBloku(T record, TrieNodeExterny<T> currentNodeExterny) {
		int indexBloku = alokujBlok();
		var blok = citajBlokZoSuboru(fileManazer, indexBloku, blokovaciFaktor);
		blok.pridaj(record);
		currentNodeExterny.setIndexBloku(indexBloku);
		currentNodeExterny.zvysPocetRecordov();
		zapisBlokDoSubor(fileManazer, blok, indexBloku, blokovaciFaktor);
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

	private int alokujBlok() {

		if (prvyVolnyBlokIndex == 7) {
			System.out.println("break");
		}

		int index;
		if (prvyVolnyBlokIndex == -1) { // ak nemam ziadny volny blok alokujem na konci
			Blok<T> blok = new Blok<>(classType);
			index = pocetBlokov; // mam 5 blokov od 0..4, ziadny volny tak priradim index 5 novemu bloku
			blok.setNasledovnik(-1);
			blok.setPredchodca(-1);
			pocetBlokov++;
			blok.setIndex(index);

			zapisBlokDoSubor(fileManazer, blok, index, blokovaciFaktor); // musime zapisat
			return index;
		}

		// najdem index prveho volneho bloku a vratim ho, do neho zapiseme "novy" blok
		var prvyVolnyBlok = citajBlokZoSuboru(fileManazer, prvyVolnyBlokIndex, blokovaciFaktor);
		int indexNasledovnika = prvyVolnyBlok.getNasledovnik();
		if (indexNasledovnika != -1) {
			var nasledovnik = citajBlokZoSuboru(fileManazer, indexNasledovnika, blokovaciFaktor);
			nasledovnik.setPredchodca(-1);
			zapisBlokDoSubor(fileManazer, nasledovnik, indexNasledovnika, blokovaciFaktor);
		}

		prvyVolnyBlok.setNasledovnik(-1);
		prvyVolnyBlok.setPredchodca(-1);
		zapisBlokDoSubor(fileManazer, prvyVolnyBlok, prvyVolnyBlokIndex, blokovaciFaktor);

		index = prvyVolnyBlokIndex;
		prvyVolnyBlokIndex = indexNasledovnika;
		pocetBlokov++;

		return index;
	}

	private void dealokujBlok(int indexBloku) {
		Blok<T> blok = citajBlokZoSuboru(fileManazer, indexBloku, blokovaciFaktor);
		blok.clear();
		blok.setNasledovnik(prvyVolnyBlokIndex);

		if (prvyVolnyBlokIndex != -1) {
			Blok<T> prvyVolnyBlok = citajBlokZoSuboru(fileManazer, prvyVolnyBlokIndex, blokovaciFaktor);
			prvyVolnyBlok.setPredchodca(indexBloku);
			zapisBlokDoSubor(fileManazer, prvyVolnyBlok, prvyVolnyBlokIndex, blokovaciFaktor);
		} else {
			prvyVolnyBlokIndex = indexBloku;
		}

		pocetBlokov--;
		zapisBlokDoSubor(fileManazer, blok, indexBloku, blokovaciFaktor);

		int pocetBlokovNaOdstranenie = 0;
		var pocetBlokovVSubore = (int) fileManazer.getFileSize()/blok.getSize(blokovaciFaktor);
		if (indexBloku == pocetBlokovVSubore) {
			for (int i = pocetBlokovVSubore - 1; i >= 0; i--) {
				Blok<T> blokNaOdstranenie = citajBlokZoSuboru(fileManazer, i, blokovaciFaktor);
				if (blokNaOdstranenie.getNasledovnik() != -1) {
					// nastav nasledovnika predchodcovi
					Blok<T> nasledovnik = citajBlokZoSuboru(fileManazer, blokNaOdstranenie.getNasledovnik(), blokovaciFaktor);
					nasledovnik.setPredchodca(blokNaOdstranenie.getPredchodca());
					zapisBlokDoSubor(fileManazer, nasledovnik, blokNaOdstranenie.getNasledovnik(), blokovaciFaktor);
				}
				if (blokNaOdstranenie.getPredchodca() != -1) {
					Blok<T> predchodca = citajBlokZoSuboru(fileManazer, blokNaOdstranenie.getPredchodca(), blokovaciFaktor);
					predchodca.setNasledovnik(blokNaOdstranenie.getNasledovnik());
					zapisBlokDoSubor(fileManazer, predchodca, blokNaOdstranenie.getPredchodca(), blokovaciFaktor);
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

			Blok<T> blok2 = citajBlokZoSuboru(fileManazerPreplnovaci, i, blokovaciFaktorPreplnovaci);
			stringBuilder.append(blok2.printPreplnovaci(i));
		}
		return stringBuilder.toString();
	}

	public String toStringHlavny() {

		StringBuilder stringBuilder = new StringBuilder();
		Blok<T> blok = new Blok<>(classType);
		for (int i = 0; i < this.fileManazer.getFileSize() / blok.getSize(blokovaciFaktor); i++) {

			Blok<T> blok2 = citajBlokZoSuboru(fileManazer, i, blokovaciFaktor);
			stringBuilder.append(blok2.print(i));
		}
		return stringBuilder.toString();
	}

	public void clear() {
		fileManazer.clear();
		fileManazerPreplnovaci.clear();
	}
}

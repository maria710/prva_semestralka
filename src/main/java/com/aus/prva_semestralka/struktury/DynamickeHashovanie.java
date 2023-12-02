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

	public DynamickeHashovanie(Class<T> classType, int blokovaciFaktor, String path, String pathPreplnovaci, int blokovaciFaktorPreplnovaci) throws FileNotFoundException {
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

	public IRecord najdiZaznam(IRecord record) {
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

		if (currentBitIndex > pocetBitovVHash) { // skontrolujeme ci sme nepresiahli pocet bitov
			currentNodeExterny.zvysPocetRecordov();
			return ulozDoPreplnovaciehoSuboru(record, blok);
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

		Blok<T> blok = citajBlokZoSuboru(fileManazer, indexBlokuNode, blokovaciFaktor);

		while (true) {
			var vyzalSa = blok.vymazRecord(record);
			if (vyzalSa) {
				zapisBlokDoSubor(fileManazer, blok, indexBlokuNode, blokovaciFaktor);
				return true;
			}
			if (blok.getNasledovnik() == -1) {
				return false;
			}
			blok = citajBlokZoSuboru(fileManazerPreplnovaci, blok.getNasledovnik(), blokovaciFaktorPreplnovaci);
		}
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
		return blok.fromByteArray(data);
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
				return ulozDoPreplnovaciehoSuboru(record, blok);
			}

			TrieNodeInterny<T> newTrieNodeInterny = vytvorNovyInternyNode(currentNodeExterny); // z externeho sa stal interny lebo sa rozdeli
			List<T> records = new ArrayList<>(blok.getRecords());

			int indexBlokuPravy = alokujBlok();
			Blok<T> blokPravy =  citajBlokZoSuboru(fileManazer, indexBlokuPravy, blokovaciFaktor);
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

	private boolean ulozDoPreplnovaciehoSuboru(T record, Blok<T> blokVHlavnomSubore) {

		int index = blokVHlavnomSubore.getNasledovnik();
		if (index == -1) { // robim len pre hlavny subor
			index = alokujIndexVPreplnujucomSubore();
			blokVHlavnomSubore.setNasledovnik(index);
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

		if (prvyVolnyBlokIndex != - 1) {
			Blok<T> prvyVolnyBlok = citajBlokZoSuboru(fileManazer, prvyVolnyBlokIndex, blokovaciFaktor);
			prvyVolnyBlok.setPredchodca(indexBloku);
			zapisBlokDoSubor(fileManazer, prvyVolnyBlok, prvyVolnyBlokIndex, blokovaciFaktor);
		} else {
			prvyVolnyBlokIndex = indexBloku;
		}

		pocetBlokov--;
		zapisBlokDoSubor(fileManazer, blok, indexBloku, blokovaciFaktor);

		int pocetBlokovNaOdstranenie = 0;
		if (indexBloku == pocetBlokov) {
			for (int i = pocetBlokov - 1; i >= 0; i--) {
				Blok<T> blokNaOdstranenie = citajBlokZoSuboru(fileManazer, i, blokovaciFaktor);
				if (blokNaOdstranenie.getAktualnyPocetRecordov() == 0) {
					pocetBlokovNaOdstranenie++;
				} else {
					break;
				}
			}
			this.fileManazer.skratSubor((pocetBlokov - pocetBlokovNaOdstranenie) * blok.getSize(blokovaciFaktor));
		}
	}

	public void print() {
		Blok<T> blok = new Blok<>(classType);
		for (int i = 0; i < this.fileManazer.getFileSize()/blok.getSize(blokovaciFaktor); i++) {

			if (i == 9) {
				System.out.println("break");
			}
			Blok<T> blok2 = citajBlokZoSuboru(fileManazer, i, blokovaciFaktor);
			blok2.print(i);
		}
	}

	public void printPreplnovaciSubor() {
		Blok<T> blok = new Blok<>(classType);
		for (int i = 0; i < this.fileManazerPreplnovaci.getFileSize()/blok.getSize(blokovaciFaktorPreplnovaci); i++) {

			if (i == 8) {
				System.out.println("break");
			}
			Blok<T> blok2 = citajBlokZoSuboru(fileManazerPreplnovaci, i, blokovaciFaktorPreplnovaci);
			blok2.printPreplnovaci(i);
		}
	}

	public String toStringPreplnovaci() {

		StringBuilder stringBuilder = new StringBuilder();
		Blok<T> blok = new Blok<>(classType);
		for (int i = 0; i < this.fileManazerPreplnovaci.getFileSize()/blok.getSize(blokovaciFaktorPreplnovaci); i++) {

			Blok<T> blok2 = citajBlokZoSuboru(fileManazerPreplnovaci, i, blokovaciFaktorPreplnovaci);
			stringBuilder.append(blok2.printPreplnovaci(i));
		}
		return stringBuilder.toString();
	}

	public String toStringHlavny() {

		StringBuilder stringBuilder = new StringBuilder();
		Blok<T> blok = new Blok<>(classType);
		for (int i = 0; i < this.fileManazer.getFileSize()/blok.getSize(blokovaciFaktor); i++) {

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

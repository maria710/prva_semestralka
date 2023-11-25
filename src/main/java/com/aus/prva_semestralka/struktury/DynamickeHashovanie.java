package com.aus.prva_semestralka.struktury;

import java.io.FileNotFoundException;
import java.util.BitSet;
import java.util.List;

import com.aus.prva_semestralka.fileManazer.FileManazer;
import com.aus.prva_semestralka.objekty.Blok;
import com.aus.prva_semestralka.objekty.IRecord;

public class DynamickeHashovanie<T extends IRecord> {

	TrieNode<T> root;
	private final int blokovaciFaktor; // velkost bloku
	private Blok<T> prvyVolnyBlok;
	private int pocetBlokov;
	private final Class<T> classType;
	private final FileManazer fileManazer;

	public DynamickeHashovanie(Class<T> classType, int blokovaciFaktor, String path) throws FileNotFoundException {
		root = new TrieNodeExterny<>(null, 0);
		prvyVolnyBlok = null;
		this.classType = classType;
		this.blokovaciFaktor = blokovaciFaktor;
		pocetBlokov = 0;
		this.fileManazer = new FileManazer(path);
	}

	public IRecord najdiZaznam(IRecord record) {
		BitSet bitSet = getHash(record);
		int bitIndex = -1;
		int maxBitIndex = bitSet.length() - 1;

		TrieNode<T> currentNode = root;
		while (maxBitIndex != bitIndex || currentNode instanceof TrieNodeInterny) {
			bitIndex++;
			if(bitSet.get(bitIndex)) {
				currentNode = ((TrieNodeInterny<T>) currentNode).getPravySyn();
			} else {
				currentNode = ((TrieNodeInterny<T>) currentNode).getLavySyn();
			}
		}

		TrieNodeExterny<T> currentNodeExterny = (TrieNodeExterny<T>) currentNode;
		int indexBlokuNode = currentNodeExterny.getIndexBloku();
		Blok<T> blok = citajBlokZoSuboru(indexBlokuNode);

		return blok == null ? null : blok.findRecord(record);
	}

	public boolean insert(T record) {
		// aplikujem hashovaciu funkciu na primarny kluc
		BitSet bitSet = getHash(record);
		int bitIndex = -1;
		int maxBitIndex = bitSet.length() - 1;

		TrieNode<T> currentNode = root;
		while (currentNode instanceof TrieNodeInterny) {
			bitIndex++;
			if(bitSet.get(bitIndex)) {
				currentNode = ((TrieNodeInterny<T>) currentNode).getPravySyn();
			} else {
				currentNode = ((TrieNodeInterny<T>) currentNode).getLavySyn();
			}
		}

		// mam externy vrchol
		TrieNodeExterny<T> currentNodeExterny = (TrieNodeExterny<T>) currentNode;
		int indexBlokuNode = currentNodeExterny.getIndexBloku();

		if (indexBlokuNode == -1) { // ak este nemame blok
			int indexBloku = alokujBlok();
			var blok = citajBlokZoSuboru(indexBloku);
			blok.pridaj(record);
			currentNodeExterny.setIndexBloku(indexBloku);
			zapisBlokDoSubor(blok, indexBloku);
			return true;
		}

		// ak uz je vytvoreny blok, moze byt plny alebo nie
		var blok = citajBlokZoSuboru(indexBlokuNode);
		if (blok.getAktualnyPocetRecordov() != blokovaciFaktor) {
			blok.pridaj(record);
			zapisBlokDoSubor(blok, indexBlokuNode);
			return true;
		}

		// ak je blok plny
		while (true) {
			TrieNodeInterny<T> newTrieNodeInterny = new TrieNodeInterny<>(currentNodeExterny.getParent()); // z externeho sa stal interny lebo sa rozdeli
			int indexBlokuLavy = alokujBlok();
			int indexBlokuPravy = alokujBlok();
			Blok<T> blokLavy = citajBlokZoSuboru(indexBlokuLavy);
			Blok<T> blokPravy = citajBlokZoSuboru(indexBlokuPravy);

			((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).setIndexBloku(indexBlokuLavy);
			((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).setIndexBloku(indexBlokuPravy);

			List<T> records = blok.getRecords();
			bitIndex++;
			boolean padloDoLava = false;
			boolean padloDoPrava = false;

			for (T iRecord : records) { // presunieme vsetky zaznamy do novych blokov
				BitSet bitset = getHash(iRecord);
				if (bitset.get(bitIndex)) {
					padloDoPrava = true;
					blokPravy.pridaj(iRecord);
				} else {
					padloDoLava = true;
					blokLavy.pridaj(iRecord);
				}
			}

			if (padloDoLava && padloDoPrava) { // ak sa roztriedili do oboch tak mozeme pridat vkladany zaznam -> vytvorili sme miesto
				BitSet bitset = getHash(record);
				if (bitset.get(bitIndex)) {
					blokPravy.pridaj(record);
					zapisBlokDoSubor(blokPravy, indexBlokuLavy);
				} else {
					blokLavy.pridaj(record);
					zapisBlokDoSubor(blokLavy, indexBlokuLavy);
				}
				return true;
			}

			// dealokujeme nepouzity blok
			if (!padloDoPrava) {
				((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).setIndexBloku(-1);
			}
			if (!padloDoLava) {
				((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).setIndexBloku(-1);
			}

			if (bitIndex >= maxBitIndex) {
				// tu sa bude ukladat do preplnovacieho suboru
				return false;
			}
			// pokracujeme v cykle
			if(bitSet.get(bitIndex)) {
				currentNodeExterny = (TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn();
			} else {
				currentNodeExterny = (TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn();
			}
		}
	}

	private int alokujBlok() {
		int index = 0;
		if (prvyVolnyBlok == null) { // ak nemam ziadny volny blok alokujem na konci
			Blok<T> blok = new Blok<>();
			index = pocetBlokov; // mam 5 blokov od 0..4, ziadny volny tak priradim index 5 novemu bloku
			blok.setNasledovnik(index);
			blok.setPredchodca(-1);
			zapisBlokDoSubor(blok, index);
			pocetBlokov++;
			return pocetBlokov - 1;
		}

		// najdem index prveho volneho bloku a vratim ho, do neho zapiseme "novy" blok
		var currentBlok = citajBlokZoSuboru(index); // zaciname citat od prveho bloku
		while (currentBlok != null && currentBlok != prvyVolnyBlok) {
			index++;
			currentBlok = citajBlokZoSuboru(index);
		}
		return index;
	}

	private void dealokujBlok(int indexBloku) {
		Blok<T> blok = citajBlokZoSuboru(indexBloku);
		blok.setIndex(indexBloku);
		prvyVolnyBlok.setPredchodca(indexBloku);
		blok.setNasledovnik(prvyVolnyBlok.getIndex());
		blok.setPredchodca(-1);
		prvyVolnyBlok = blok;
	}

	public boolean delete() {
		return false;
	}

	public boolean edit() {
		return false;
	}

	private void zapisBlokDoSubor(Blok<T> blok, int indexBloku) {
		byte[] data = blok.toByteArray();
		this.fileManazer.write(data, indexBloku);
	}

	private Blok<T> citajBlokZoSuboru(int indexBloku) {
		Blok<T> blok = new Blok<>(classType);
		byte[] data = this.fileManazer.read(blok.getSize(blokovaciFaktor), indexBloku);
		return blok.fromByteArray(data);
	}

	private BitSet getHash(IRecord record) {
		return record.getHash(0); // tu budem prenastavovat pocet bitov
	}
}

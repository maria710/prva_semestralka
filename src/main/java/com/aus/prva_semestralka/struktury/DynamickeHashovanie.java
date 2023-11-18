package com.aus.prva_semestralka.struktury;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.aus.prva_semestralka.objekty.Blok;
import com.aus.prva_semestralka.objekty.IRecord;

public class DynamickeHashovanie<T> {

	TrieNode<T> root;

	public static final int BLOKOVACI_FAKTOR = 10; // velkost bloku

	private final ArrayList<Blok<T>> bloky;

	public DynamickeHashovanie() {
		root = new TrieNodeExterny<>(null, 0);
		bloky = new ArrayList<>();
	}

	public boolean insert(IRecord<T> record) {
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
			bloky.get(indexBloku).pridajRecord(record);
			currentNodeExterny.setIndexBloku(indexBloku);
			return true;
		}

		// ak uz je vytvoreny blok, moze byt plny alebo nie
		var blok = bloky.get(indexBlokuNode);
		if (blok.getSize() != BLOKOVACI_FAKTOR) {
			blok.pridajRecord(record);
			return true;
		}

		// ak je blok plny
		while (true) {
			TrieNodeInterny<T> newTrieNodeInterny = new TrieNodeInterny<>(currentNodeExterny.getParent()); // z externeho sa stale interny lebo sa rozdeli
			int indexBlokuLavy = alokujBlok();
			int indexBlokuPravy = alokujBlok();

			((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).setIndexBloku(indexBlokuLavy);
			((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).setIndexBloku(indexBlokuPravy);

			List<IRecord<T>> records = blok.getRecords();
			bitIndex++;
			boolean padloDoLava = false;
			boolean padloDoPrava = false;

			for (IRecord<T> iRecord : records) { // presunieme vsetky zaznamy do novych blokov
				BitSet bitset = getHash(iRecord);
				if (bitset.get(bitIndex)) {
					padloDoPrava = true;
					bloky.get(indexBlokuPravy).pridajRecord(iRecord);
				} else {
					padloDoLava = true;
					bloky.get(indexBlokuLavy).pridajRecord(iRecord);
				}
			}

			if (padloDoLava && padloDoPrava) { // ak sa roztriedili do oboch tak mozeme pridat vkladany zaznam
				BitSet bitset = getHash(record);
				if (bitset.get(bitIndex)) {
					bloky.get(indexBlokuPravy).pridajRecord(record);
				} else {
					bloky.get(indexBlokuLavy).pridajRecord(record);
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
		Blok<T> blok = new Blok<>();
		bloky.add(blok);
		return bloky.size() - 1;
	}

	public boolean delete() {
		return false;
	}

	public boolean find() {
		return false;
	}

	public boolean edit() {
		return false;
	}

	public BitSet getHash(IRecord<T> record) {
		return record.getHash();
	}
}

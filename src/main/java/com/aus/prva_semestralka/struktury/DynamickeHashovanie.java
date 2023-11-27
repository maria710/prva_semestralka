package com.aus.prva_semestralka.struktury;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Stack;

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
	private int currentBitIndex = -1;

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
		TrieNodeExterny<T> externalNode = najdiExternyNode(bitSet);

		int indexBlokuNode = externalNode.getIndexBloku();
		Blok<T> blok = citajBlokZoSuboru(indexBlokuNode);

		return blok == null ? null : blok.najdiZaznam(record, Integer.SIZE);
	}

	public boolean insert(T record) {
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
		var blok = citajBlokZoSuboru(indexBlokuNode);
		if (blok.getAktualnyPocetRecordov() != blokovaciFaktor) {
			blok.pridaj(record);
			zapisBlokDoSubor(blok, indexBlokuNode);
			return true;
		}

		// ak je blok plny
		return rozdelNodeAZapis(record, currentNodeExterny, blok, indexBlokuNode, bitSet);
	}

	public boolean delete() {
		return false;
	}

	public boolean edit() {
		return false;
	}

	private void zapisBlokDoSubor(Blok<T> blok, int indexBloku) {
		byte[] data = blok.toByteArray(blokovaciFaktor);
		this.fileManazer.write(data, indexBloku * blok.getSize(blokovaciFaktor));
	}

	private Blok<T> citajBlokZoSuboru(int indexBloku) {
		Blok<T> blok = new Blok<>(classType);
		byte[] data = this.fileManazer.read(blok.getSize(blokovaciFaktor), indexBloku * blok.getSize(blokovaciFaktor));
		return blok.fromByteArray(data);
	}

	private BitSet getHash(IRecord record) {
		return record.getHash(Integer.SIZE); // tu budem prenastavovat pocet bitov
	}

	public void close() throws Exception {
		this.fileManazer.close();
	}

	public void print() {

		Stack<TrieNode<T>> stack = new Stack<>();
		TrieNode<T> current = root;

		while (current != null || !stack.isEmpty()) {
			while (current instanceof TrieNodeInterny) {
				stack.push(current);
				current = ((TrieNodeInterny<T>) current).getLavySyn();
			}

			// mame externy vrchol
			if (current instanceof TrieNodeExterny<T> externy) {
				citajBlokZoSuboru(externy.getIndexBloku()).print(externy.getIndexBloku());
			}

			if (stack.isEmpty()) {
				break;
			} else {
				current = ((TrieNodeInterny<T>) stack.pop()).getPravySyn(); // spracujeme praveho syn
			}
		}
	}

	private TrieNodeExterny<T> najdiExternyNode(BitSet bitSet) {
		currentBitIndex = -1;
		int maxcurrentBitIndex = bitSet.length() - 1;

		TrieNode<T> currentNode = root;
		while (maxcurrentBitIndex != currentBitIndex && currentNode instanceof TrieNodeInterny) {
			currentBitIndex++;
			currentNode = bitSet.get(currentBitIndex) ? ((TrieNodeInterny<T>) currentNode).getPravySyn() : ((TrieNodeInterny<T>) currentNode).getLavySyn();
		}

		return (TrieNodeExterny<T>) currentNode;
	}

	private boolean rozdelNodeAZapis(T record, TrieNodeExterny<T> currentNodeExterny, Blok<T> blok, int indexBlokuNode, BitSet bitSet) {
		while (true) {
			TrieNodeInterny<T> newTrieNodeInterny = vytvorNovyInternyNode(currentNodeExterny); // z externeho sa stal interny lebo sa rozdeli
			List<T> records = new ArrayList<>(blok.getRecords());

			int indexBlokuPravy = alokujBlok();
			Blok<T> blokPravy = citajBlokZoSuboru(indexBlokuPravy);
			blok.clear();

			((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).setIndexBloku(indexBlokuNode);
			((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).setIndexBloku(indexBlokuPravy);

			currentBitIndex++;
			boolean padloDoLava = false;
			boolean padloDoPrava = false;

			for (T iRecord : records) { // presunieme vsetky zaznamy do novych blokov
				BitSet bitset = getHash(iRecord);
				if (bitset.get(currentBitIndex)) {
					padloDoPrava = true;
					blokPravy.pridaj(iRecord);
					((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).zvysPocetRecordov();
				} else {
					padloDoLava = true;
					blok.pridaj(iRecord);
					((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).zvysPocetRecordov();
				}
			}

			if (padloDoLava && padloDoPrava) { // ak sa roztriedili do oboch tak mozeme pridat vkladany zaznam -> vytvorili sme miesto
				BitSet bitset = getHash(record);
				if (bitset.get(currentBitIndex)) {
					blokPravy.pridaj(record);
					((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).zvysPocetRecordov();
				} else {
					blok.pridaj(record);
					((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).zvysPocetRecordov();
				}
				zapisBlokDoSubor(blok, indexBlokuNode);
				zapisBlokDoSubor(blokPravy, indexBlokuPravy);
				return true;
			}

			// dealokujeme nepouzity blok
			if (!padloDoPrava) {
				((TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn()).setIndexBloku(-1);
			}
			if (!padloDoLava) {
				((TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn()).setIndexBloku(-1);
			}

			// pokracujeme v cykle
			if(bitSet.get(currentBitIndex)) {
				currentNodeExterny = (TrieNodeExterny<T>) newTrieNodeInterny.getPravySyn();
			} else {
				currentNodeExterny = (TrieNodeExterny<T>) newTrieNodeInterny.getLavySyn();
			}
		}
	}

	private void zapisDoNovehoBloku(T record, TrieNodeExterny<T> currentNodeExterny) {
		int indexBloku = alokujBlok();
		var blok = citajBlokZoSuboru(indexBloku);
		blok.pridaj(record);
		currentNodeExterny.setIndexBloku(indexBloku);
		zapisBlokDoSubor(blok, indexBloku);
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
		int index = 0;
		if (prvyVolnyBlok == null) { // ak nemam ziadny volny blok alokujem na konci
			Blok<T> blok = new Blok<>(classType);
			index = pocetBlokov; // mam 5 blokov od 0..4, ziadny volny tak priradim index 5 novemu bloku
			blok.setNasledovnik(-1);
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
		prvyVolnyBlok.setPredchodca(indexBloku);
		//blok.setNasledovnik(dajIndexBloku(prvyVolnyBlok));
		blok.setPredchodca(-1);
		prvyVolnyBlok = blok;
	}
}

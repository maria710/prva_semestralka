package com.aus.prva_semestralka.struktury;

public class TrieNodeExterny<T> extends TrieNode<T> {

	private int indexBloku;
	private int pocetRecordov; // kolko zaznamov je na adrese (v bloku)

	public TrieNodeExterny(TrieNode<T> parent, int pocetRecordov) {
		super(parent);
		this.indexBloku = -1;
		this.pocetRecordov = pocetRecordov;
	}

	public int getIndexBloku() {
		return indexBloku;
	}

	public void setIndexBloku(int indexBloku) {
		this.indexBloku = indexBloku;
	}

	public int getPocetRecordov() {
		return pocetRecordov;
	}

	public void setPocetRecordov(int pocetRecordov) {
		this.pocetRecordov = pocetRecordov;
	}

	public void zvysPocetRecordov() {
		this.pocetRecordov++;
	}
}

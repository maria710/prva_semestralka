package com.aus.prva_semestralka.struktury;

public class TrieNodeExterny<T> extends TrieNode<T> {

	private int indexBloku;
	private int pocetRecordov; // kolko zaznamov je na adrese (v bloku)
	private int pocetBlokovVZretazeni = 0;

	public TrieNodeExterny(TrieNode<T> parent, int pocetRecordov) {
		super(parent);
		this.indexBloku = -1;
		this.pocetRecordov = pocetRecordov;
	}

	public TrieNodeExterny(int indexBloku, int pocetRecordov, int pocetBlokovVZretazeni) {
		super(null);
		this.indexBloku = indexBloku;
		this.pocetRecordov = pocetRecordov;
		this.pocetBlokovVZretazeni = pocetBlokovVZretazeni;
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

	public void zvysPocetRecordov() {
		this.pocetRecordov++;
	}

	public void znizPocetRecordov() {
		this.pocetRecordov--;
	}

	public int getPocetBlokovVZretazeni() {
		return pocetBlokovVZretazeni;
	}

	public void zvysPocetBlokovVZretazeni() {
		this.pocetBlokovVZretazeni++;
	}

	public void znizPocetBlokovVZretazeni() {
		this.pocetBlokovVZretazeni--;
	}
}

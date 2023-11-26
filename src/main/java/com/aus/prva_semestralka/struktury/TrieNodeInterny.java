package com.aus.prva_semestralka.struktury;

public class TrieNodeInterny<T> extends TrieNode<T> {

	TrieNode<T> lavySyn;
	TrieNode<T> pravySyn;

	public TrieNodeInterny() {
		super(null);
	}

	public TrieNodeInterny(TrieNode<T> parent) {
		super(parent);
		this.lavySyn = new TrieNodeExterny<>(parent, 0);
		this.pravySyn = new TrieNodeExterny<>(parent, 0);
	}

	public TrieNode<T> getLavySyn() {
		return lavySyn;
	}

	public void setLavySyn(TrieNode<T> lavySyn) {
		this.lavySyn = lavySyn;
	}

	public TrieNode<T> getPravySyn() {
		return pravySyn;
	}

	public void setPravySyn(TrieNode<T> pravySyn) {
		this.pravySyn = pravySyn;
	}

}

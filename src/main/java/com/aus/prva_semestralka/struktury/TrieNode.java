package com.aus.prva_semestralka.struktury;

public class TrieNode<T> {

	protected TrieNode<T> parent;

	public TrieNode(TrieNode<T> parent) {
		this.parent = parent;
	}

	public TrieNode<T> getParent() {
		return parent;
	}

	public void setParent(TrieNode<T> parent) {
		this.parent = parent;
	}
}

package com.aus.prva_semestralka.objekty;

import java.util.BitSet;

import com.aus.prva_semestralka.struktury.TrieNodeExterny;

public class TrieNodeMap<T> {

	public TrieNodeMap(BitSet key, TrieNodeExterny<T> node) {
		this.key = key;
		this.node = node;
	}
	BitSet key; // postupnost bitov v trie
	TrieNodeExterny<T> node;

	public BitSet getKey() {
		return key;
	}

	public TrieNodeExterny getNode() {
		return node;
	}
}

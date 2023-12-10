package com.aus.prva_semestralka.objekty;

import java.util.BitSet;
import java.util.List;

import com.aus.prva_semestralka.struktury.TrieNode;
import com.aus.prva_semestralka.struktury.TrieNodeExterny;

public class TrieNodeMap<T> {

	private String key; // postupnost bitov v trie
	private TrieNode<T> node;


	public TrieNodeMap() {
	}

	public TrieNodeMap(String key, TrieNode<T> node) {
		this.key = key;
		this.node = node;
	}

	public String getKey() {
		return key;
	}

	public TrieNode<T> getNode() {
		return node;
	}

	public void setKey(String key) {
		this.key = key;
	}
}

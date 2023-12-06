package com.aus.prva_semestralka.struktury;

public class TrieNodeInterny<T> extends TrieNode<T> {

	TrieNode<T> lavySyn;
	TrieNode<T> pravySyn;

	public TrieNodeInterny() {
		super(null);
	}

	public TrieNodeInterny(TrieNode<T> parent) {
		super(parent);
		this.lavySyn = new TrieNodeExterny<>(this, 0);
		this.pravySyn = new TrieNodeExterny<>(this, 0);
	}

	public TrieNode<T> getLavySyn() {
		return lavySyn;
	}

	public void setLavySyn(TrieNode<T> lavySyn) {
		this.lavySyn = lavySyn;
		// If the new node is not an external node, update its parent to this node
		if (!(lavySyn instanceof TrieNodeExterny)) {
			lavySyn.setParent(this);
		}
	}

	public TrieNode<T> getPravySyn() {
		return pravySyn;
	}

	public void setPravySyn(TrieNode<T> pravySyn) {
		this.pravySyn = pravySyn;
		// Similar update for the right child
		if (!(pravySyn instanceof TrieNodeExterny)) {
			pravySyn.setParent(this);
		}
	}

}

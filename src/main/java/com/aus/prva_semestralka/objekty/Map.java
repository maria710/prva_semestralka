package com.aus.prva_semestralka.objekty;

import java.util.ArrayList;
import java.util.List;

import com.aus.prva_semestralka.struktury.QTNode;

public class Map {

	private QTNode root;
	private List<IPozemok> pozemky;

	public Map(QTNode root, List<IPozemok> pozemky) {
		this.root = root;
		this.pozemky = pozemky;
	}

	public static Map of(QTNode currentNode, List<IPozemok> result) {
		return new Map(currentNode, result);
	}

	public QTNode getRoot() {
		return root;
	}

	public List<IPozemok> getPozemky() {
		return pozemky;
	}
}

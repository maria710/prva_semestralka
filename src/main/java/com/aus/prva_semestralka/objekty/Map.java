package com.aus.prva_semestralka.objekty;

import java.util.List;

import com.aus.prva_semestralka.struktury.QTNode;

public class Map {

	private QTNode node;
	private List<IData> data;

	public Map(QTNode node, List<IData> data) {
		this.node = node;
		this.data = data;
	}

	public static Map of(QTNode currentNode, List<IData> result) {
		return new Map(currentNode, result);
	}

	public QTNode getNode() {
		return node;
	}

	public List<IData> getData() {
		return data;
	}
}

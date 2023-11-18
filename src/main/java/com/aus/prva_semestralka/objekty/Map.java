package com.aus.prva_semestralka.objekty;

import java.util.List;

import com.aus.prva_semestralka.struktury.QTNode;

public class Map<T> {

	private final QTNode<T> node;
	private final List<IData<T>> data;

	public Map(QTNode<T> node, List<IData<T>> data) {
		this.node = node;
		this.data = data;
	}

	public Map<T> of(QTNode<T> currentNode, List<IData<T>> result) {
		return new Map<T>(currentNode, result);
	}

	public QTNode<T> getNode() {
		return node;
	}

	public List<IData<T>> getData() {
		return data;
	}
}

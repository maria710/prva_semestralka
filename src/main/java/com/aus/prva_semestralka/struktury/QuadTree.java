package com.aus.prva_semestralka.struktury;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IPozemok;

@Data
@RequiredArgsConstructor
public class QuadTree {

	private QTNode root;
	public static Integer maxHlbka;
	private Integer sirka;
	private Integer dlzka;

	public QuadTree(Integer maxHlbka, Integer sirka, Integer dlzka) {
		QuadTree.maxHlbka = maxHlbka;
		this.sirka = sirka;
		this.dlzka = dlzka;
		var prvaSuradnicaKorena = new GpsPozicia("S", "Z", 0.0, 0.0);
		var druhaSuradnicaKorena = new GpsPozicia("S", "Z", sirka.doubleValue(), dlzka.doubleValue());
		this.root = new QTNode(1, List.of(prvaSuradnicaKorena, druhaSuradnicaKorena), maxHlbka);
		root.rozdel();
	}

	public boolean pridaj(IPozemok pozemok) {
		// ak je pozemok velmi velky alebo neexistuje, tak ho nepridavame
		if (pozemok == null || !root.zmestiSa(pozemok)) {
			return false;
		}
		return root.pridajPozemok(pozemok);
	}

	public List<IPozemok> getAllPozemky() {
		return getAllPozemkyFromNode(root);
	}

	public List<IPozemok> getAllPozemkyFromNode(QTNode node) {
		if (node == null) {
			return Collections.emptyList();
		}

		List<IPozemok> pozemky = new ArrayList<>(4 ^ root.getHlbka());
		LinkedList<QTNode> nodeNaSpracovanie = new LinkedList<>();

		nodeNaSpracovanie.add(node);

		while (!nodeNaSpracovanie.isEmpty()) {
			QTNode currentNode = nodeNaSpracovanie.poll();

			// pridanie pozemkov z aktualneho node
			pozemky.addAll(currentNode.getPozemkySPrekrocenouHlbkou());
			pozemky.addAll(currentNode.getPozemky());

			if (currentNode.isJeList() && currentNode.getPozemok_data() != null) {
				pozemky.add(currentNode.getPozemok_data());
			} else {
				// pridanie synov do zoznamu na spracovanie
				nodeNaSpracovanie.addAll(currentNode.getSynovia());
			}
		}

		return pozemky;
	}

	public List<IPozemok> findWithin(GpsPozicia suradnica1, GpsPozicia suradnica2) {
		var currentNode = root;
		var nodeNaSpracovanie = new LinkedList<QTNode>();

		nodeNaSpracovanie.add(currentNode);

		while (currentNode != null && currentNode.zmestiSa(suradnica1, suradnica2)) {
			for (QTNode syn : currentNode.getSynovia()) {
				if (syn.zmestiSa(suradnica1, suradnica2)) {
					nodeNaSpracovanie.add(syn);
				}
			}
			currentNode = nodeNaSpracovanie.poll();
		}

		if (currentNode == null) {
			return Collections.emptyList();
		}

		return getAllPozemkyFromNode(currentNode);
	}

	public boolean deletePozemok(IPozemok pozemok) {
		var currentNode = root;

		while (true) {
			// ak je current node list a ma pozemok, tak ho vymazeme
			if (currentNode.isJeList()) {
				if (currentNode.getPozemok_data() != null && currentNode.getPozemok_data().equals(pozemok)) {
					currentNode.setPozemok_data(null);
					return true;
				} else {
					return currentNode.getPozemkySPrekrocenouHlbkou().remove(pozemok);
				}
			}

			// ak ma synov, tak vymazeme pozemok z aktualneho node
			if (currentNode.getPozemky().contains(pozemok)) {
				return currentNode.getPozemky().remove(pozemok);
			}
			if (currentNode.getPozemkySPrekrocenouHlbkou().contains(pozemok)) {
				return currentNode.getPozemkySPrekrocenouHlbkou().remove(pozemok);
			}
			// ak sa pozemok nenachadza v aktualnom node, tak sa posunieme na dalsi node
			for (QTNode syn : currentNode.getSynovia()) {
				if (syn.zmestiSa(pozemok)) {
					currentNode = syn;
				}
			}
		}
	}
}

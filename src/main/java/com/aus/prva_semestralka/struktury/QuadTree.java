package com.aus.prva_semestralka.struktury;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IData;
import com.aus.prva_semestralka.objekty.Map;
import com.aus.prva_semestralka.objekty.Ohranicenie;

@Data
@RequiredArgsConstructor
public class QuadTree {

	private QTNode root;
	public static Integer maxHlbka;
	private Integer sirka;
	private Integer dlzka;
	private Integer size = 0;

	public QuadTree(Integer maxHlbka, Integer sirka, Integer dlzka) {
		QuadTree.maxHlbka = maxHlbka;
		this.sirka = sirka;
		this.dlzka = dlzka;
		var prvaSuradnicaKorena = new GpsPozicia("S", "Z", 0.0, 0.0);
		var druhaSuradnicaKorena = new GpsPozicia("S", "Z", sirka.doubleValue(), dlzka.doubleValue());
		var ohranicenie = new Ohranicenie(prvaSuradnicaKorena, druhaSuradnicaKorena);
		this.root = new QTNode(1, ohranicenie, 0);
		root.rozdel();
	}

	public int getSize() {
		return size;
	}

	public int getMaxHlbka() { return maxHlbka; }

	public boolean pridaj(IData data) {
		return pridajDoNode(data, root);
	}

	public boolean pridajDoNode(IData data, QTNode node) {
		if (data == null || !node.zmestiSa(data)) { // ak je sekundarny kluc velmi velky alebo data neexistuju, tak ho nepridavame
			return false;
		}

		var currentNode = node;
		while (!currentNode.jeList()) {
			var indexSyna = currentNode.getKvadrantPreData(data);
			if (indexSyna != -1) {
				currentNode = currentNode.getSynovia().get(indexSyna - 1);
			} else {
				currentNode.getData().add(data); // presli sme vsetkych synov currentNode ale ani do jedneho sa nam nezmesti, tak ho pridame do tohto
				size++;
				return true;
			}
		}

		if (Objects.equals(currentNode.getHlbka(), maxHlbka)) { // nemozeme prekrocit sme maximalnu hlbku
			currentNode.getDataSPrekrocenouHlbkou().add(data);
			size++;
			return true;
		}

		if (currentNode.getDataListu() == null) { // current node je list - nema synov
			currentNode.setDataListu(data);
			size++;
			return true;
		} else {
			// ak uz obsahuje nejake data, potom ideme delit node
			currentNode.rozdel();
			var jePriradenyKvadrat = currentNode.zaradDoKvadratu(currentNode.getDataListu()); // pridame data, ktore uz bol v node
			var suZaradeneDataNaVkladanie = currentNode.zaradDoKvadratu(data); // pridame data, ktore chceme vlozit
			if (jePriradenyKvadrat) {
				currentNode.setDataListu(null);
				size--;
			}
			if (suZaradeneDataNaVkladanie) {
				size++;
			}
			if (jePriradenyKvadrat) {
				size++;
			}
			return jePriradenyKvadrat && suZaradeneDataNaVkladanie;
		}
	}

	public List<IData> getAllData() {
		return getAllDataFromNode(root);
	}

	public List<IData> getAllDataFromNode(QTNode node) {
		if (node == null) {
			return Collections.emptyList();
		}

		List<IData> dataList = new ArrayList<>(4 ^ root.getHlbka());
		LinkedList<QTNode> nodeNaSpracovanie = new LinkedList<>();

		nodeNaSpracovanie.add(node);

		while (!nodeNaSpracovanie.isEmpty()) {
			QTNode currentNode = nodeNaSpracovanie.poll();

			// pridanie dat z aktualneho node
			dataList.addAll(currentNode.getDataSPrekrocenouHlbkou());
			dataList.addAll(currentNode.getData());

			if (currentNode.jeList() && currentNode.getDataListu() != null) {
				dataList.add(currentNode.getDataListu());
			} else {
				// pridanie synov do zoznamu na spracovanie
				nodeNaSpracovanie.addAll(currentNode.getSynovia());
			}
		}

		return dataList;
	}

	public Map findWithin(Ohranicenie ohranicenie) {
		return findWithinFromNode(ohranicenie, root);
	}

	public Map findWithinFromNode(Ohranicenie ohranicenie, QTNode node) {
		if (node == null) {
			node = root;
		}

		var currentNode = node;
		ArrayList<IData> result = new ArrayList<>();

		while (true) {
			if (currentNode.getSynovia().isEmpty()) {
				result.addAll(currentNode.getData());
				result.addAll(currentNode.getDataSPrekrocenouHlbkou());
				if (currentNode.getDataListu() != null) {
					result.add(currentNode.getDataListu());
				}
				return Map.of(currentNode, result);
			}
			int index = currentNode.getKvadrantPreOhranicenie(ohranicenie);
			if (index == -1) {
				var najdeneData = getAllDataFromNode(currentNode);
				for (IData data : najdeneData) {
					if (ohranicenie.zmestiSaDovnutra(data.getSekundarnyKluc())) {
						result.add(data);
					}
				}
				return Map.of(currentNode, result);
			} else {
				var syn = currentNode.getSynovia().get(index - 1);
				if (syn == null) {
					return Map.of(null, Collections.emptyList());
				}
				currentNode = syn;
			}
		}
	}

	public boolean deleteData(IData data) {
		return deleteDataFromNode(data, root);
	}

	public boolean deleteDataFromNode(IData data, QTNode node) {
		if (node == null) {
			node = root;
		}
		var currentNode = node;
		List<QTNode> parentNodes = new LinkedList<>();

		while (true) {
			// ak je current node list a ma data, tak ho vymazeme
			var result = false;
			if (currentNode.jeList()) {
				if (currentNode.getDataListu() != null && currentNode.getDataListu().equals(data)) { // vlastne equals
					currentNode.setDataListu(null);
					result = true;
				} else {
					result = currentNode.getDataSPrekrocenouHlbkou().remove(data);
				}
				vymazPraznychSynov(parentNodes, result);
				if (result) {
					size--;
				}
				return result; // koncime uplne aj ked sme v liste nic nenasli
			}

			// ak ma synov, tak vymazeme data z aktualneho node
			if (currentNode.getData().contains(data)) {
				result = currentNode.getData().remove(data);
			}

			if (result) { // nemozeme vratit negativny vysledok, mozno sa najde este v dalsich synoch
				vymazPraznychSynov(parentNodes, result);
				size--;
				return true;
			}
			if (currentNode.getDataSPrekrocenouHlbkou().contains(data)) {
				result = currentNode.getDataSPrekrocenouHlbkou().remove(data);
			}

			if (result) { // nemozeme vratit negativny vysledok, mozno sa najde este v dalsich synoch
				vymazPraznychSynov(parentNodes, result);
				size--;
				return true;
			}

			// ak sa data nenachadzaju v aktualnom node, tak sa posunieme na dalsi node
			var indexSyna = currentNode.getKvadrantPreData(data);
			if (indexSyna == -1) {
				return false;
			}
			parentNodes.add(0, currentNode);
			currentNode = currentNode.getSynovia().get(indexSyna - 1);
		}
	}

	private void vymazPraznychSynov(List<QTNode> parentNodes, boolean result) {
		if (result && !parentNodes.isEmpty()) {
			for (QTNode parentNode : parentNodes) {
				var dajuSaZmazatSynovia = parentNode.dajuSaZmazatSynovia();
				if (!dajuSaZmazatSynovia) {
					break;
				}
				if (parentNode.getData().isEmpty() && parentNode.getDataSPrekrocenouHlbkou().isEmpty() && parentNode.getDataListu() == null) {
					parentNode.getSynovia().clear();
					parentNode.zmenJeList(true);
				} else {
					break;
				}
			}
		}
	}

	public boolean uprav(IData povodneData, IData data, QTNode node) {
		var najdene = findWithinFromNode(povodneData.getSekundarnyKluc(), node).getData();
		if (najdene.isEmpty()) {
			return false;
		}
		for (IData iData : najdene) {
			if (iData.getPrimarnyKluc().equals(povodneData.getPrimarnyKluc())) {
				iData.setData(data);
				return true;
			}
		}
		return false;
	}

	public boolean zmenHlbku(Integer hlbka) {
		if (hlbka < 1) {
			return false;
		}

		if (hlbka == root.getHlbka()) {
			return true;
		}
		if (hlbka < root.getHlbka()) {
			Predicate<QTNode> predicate = node -> node.getHlbka() > hlbka;
			var nodes = getNodesPodlaPredikatu(predicate);
			maxHlbka = hlbka;

			for (QTNode node : nodes) {
				ArrayList<IData> dataNaVlozenie = new ArrayList<>();
				dataNaVlozenie.add(node.getDataListu());
				dataNaVlozenie.addAll(node.getData());
				dataNaVlozenie.addAll(node.getDataSPrekrocenouHlbkou());

				for (IData data : dataNaVlozenie) {
					deleteData(data); // vymazeme data aby sme nemali duplicity
					pridaj(data);
				}
			}
			return true;
		}
		if (hlbka > maxHlbka) {
			Predicate<QTNode> predicate = node -> node.getHlbka() == maxHlbka && !node.getDataSPrekrocenouHlbkou().isEmpty();
			var nodes = getNodesPodlaPredikatu(predicate);
			maxHlbka = hlbka;

			for (QTNode node : nodes) { // pre kazdy node s datami s prekrocenou hlbkou
				// pre kazde data s prekrocenou hlbkou sa pokusime znovu vlozit do stromu, vsetky sa musia odstranit!
				for (IData data : node.getDataSPrekrocenouHlbkou()) {
					if (pridajDoNode(data, node)) { // nehladame kde sa zmesti, lebo uz vieme ze sa zmesti len do current node alebo nizsie
						node.getDataSPrekrocenouHlbkou().remove(data);
						size--; // ked ostranime zo zoznamu, musime znizit pocet v zozname dat aby sme nepridali duplikat
					}
				}
			}
			return true;
		}
		return false;
	}

	public List<QTNode> getNodesPodlaPredikatu(Predicate<QTNode> predicate) {
		List<QTNode> nodes = new ArrayList<>(4 ^ root.getHlbka());
		LinkedList<QTNode> nodeNaSpracovanie = new LinkedList<>();

		nodeNaSpracovanie.add(root);

		while (!nodeNaSpracovanie.isEmpty()) {
			QTNode currentNode = nodeNaSpracovanie.poll();
			if (predicate.test(currentNode)) {
				nodes.add(currentNode);
			}
			nodeNaSpracovanie.addAll(currentNode.getSynovia());
		}
		return nodes;
	}
}

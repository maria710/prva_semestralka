package com.aus.prva_semestralka.struktury;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Predicate;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IData;
import com.aus.prva_semestralka.objekty.Map;
import com.aus.prva_semestralka.objekty.Ohranicenie;

import static com.aus.prva_semestralka.objekty.Threshold.VELKOST_DATA_THRESHOLD;
import static com.aus.prva_semestralka.objekty.Threshold.jePrekrocenyBalanceFactorThreshold;
import static com.aus.prva_semestralka.objekty.Threshold.jePrekrocenyVelkostDataThreshold;

public class QuadTree<T> {

	private final QTNode<T> root;
	public static Integer maxHlbka;
	private final Integer sirka;
	private final Integer dlzka;
	private Integer size = 0;

	public QuadTree(Integer maxHlbka, Integer sirka, Integer dlzka) {
		QuadTree.maxHlbka = maxHlbka;
		this.sirka = sirka;
		this.dlzka = dlzka;
		var prvaSuradnicaKorena = new GpsPozicia("S", "Z", 0.0, 0.0);
		var druhaSuradnicaKorena = new GpsPozicia("S", "Z", sirka.doubleValue(), dlzka.doubleValue());
		var ohranicenie = new Ohranicenie(prvaSuradnicaKorena, druhaSuradnicaKorena);
		this.root = new QTNode<>(ohranicenie, 0);
		root.rozdel();
	}

	public int getSize() {
		return size;
	}

	public int getSirka() {
		return sirka;
	}

	public int getDlzka() {
		return dlzka;
	}

	public void setMaxHlbka(Integer maxHlbka) {
		QuadTree.maxHlbka = maxHlbka;
	}

	public boolean pridaj(IData<T> data) {
		return pridajDoNode(data, root);
	}

	public boolean pridajDoNode(IData<T> data, QTNode<T> node) {
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

	public List<IData<T>> getAllData() {
		return getAllDataFromNode(root);
	}

	public List<IData<T>> getAllDataFromNode(QTNode<T> node) {
		if (node == null) {
			return Collections.emptyList();
		}

		List<IData<T>> dataList = new ArrayList<>(4 ^ root.getHlbka());
		LinkedList<QTNode<T>> nodeNaSpracovanie = new LinkedList<>();

		nodeNaSpracovanie.add(node);

		while (!nodeNaSpracovanie.isEmpty()) {
			QTNode<T> currentNode = nodeNaSpracovanie.poll();

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

	public Map<T> findWithin(Ohranicenie ohranicenie) {
		return findWithinFromNode(ohranicenie, root);
	}

	public Map<T> findWithinFromNode(Ohranicenie ohranicenie, QTNode<T> node) {
		if (node == null) {
			node = root;
		}

		var currentNode = node;
		ArrayList<IData<T>> result = new ArrayList<>();

		while (true) {
			if (currentNode.getSynovia().isEmpty()) {
				result.addAll(currentNode.getData());
				result.addAll(currentNode.getDataSPrekrocenouHlbkou());
				if (currentNode.getDataListu() != null) {
					result.add(currentNode.getDataListu());
				}
				return new Map<>(currentNode, result);
			}
			int index = currentNode.getKvadrantPreOhranicenie(ohranicenie);
			if (index == -1) {
				var najdeneData = getAllDataFromNode(currentNode);
				for (IData<T> data : najdeneData) {
					if (ohranicenie.zmestiSaDovnutra(data.getSekundarnyKluc())) {
						result.add(data);
					}
				}
				return new Map<>(currentNode, result);
			} else {
				var syn = currentNode.getSynovia().get(index - 1);
				if (syn == null) {
					return new Map<>(null, Collections.emptyList());
				}
				currentNode = syn;
			}
		}
	}

	public boolean deleteData(IData<T> data) {
		return deleteDataFromNode(data, root);
	}

	public boolean deleteDataFromNode(IData<T> data, QTNode<T> node) {
		if (node == null) {
			node = root;
		}
		var currentNode = node;
		List<QTNode<T>> parentNodes = new LinkedList<>();

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
			for (int i = 0; i < currentNode.getData().size(); i++) {
				if (currentNode.getData().get(i).getPrimarnyKluc().equals(data.getPrimarnyKluc())) {
					currentNode.getData().remove(i);
					result = true;
					break;
				}
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

	private void vymazPraznychSynov(List<QTNode<T>> parentNodes, boolean result) {
		if (result && !parentNodes.isEmpty()) {
			for (QTNode<T> parentNode : parentNodes) {
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

	public boolean uprav(IData<T> povodneData, IData<T> data, QTNode<T> node) {
		List<IData<T>> najdene = findWithinFromNode(povodneData.getSekundarnyKluc(), node).getData();
		if (najdene.isEmpty()) {
			return false;
		}
		for (IData<T> iData : najdene) {
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
			Predicate<QTNode<T>> predicate = node -> node.getHlbka() > hlbka;
			var nodes = getNodesPodlaPredikatu(predicate);
			maxHlbka = hlbka;

			for (QTNode<T> node : nodes) {
				ArrayList<IData<T>> dataNaVlozenie = new ArrayList<>();
				dataNaVlozenie.add(node.getDataListu());
				dataNaVlozenie.addAll(node.getData());
				dataNaVlozenie.addAll(node.getDataSPrekrocenouHlbkou());

				for (IData<T> data : dataNaVlozenie) {
					deleteData(data); // vymazeme data aby sme nemali duplicity
					pridaj(data);
				}
			}
			return true;
		}
		if (hlbka > maxHlbka) {
			Predicate<QTNode<T>> predicate = node -> node.getHlbka() == maxHlbka && !node.getDataSPrekrocenouHlbkou().isEmpty();
			var nodes = getNodesPodlaPredikatu(predicate);
			maxHlbka = hlbka;

			for (QTNode<T> node : nodes) { // pre kazdy node s datami s prekrocenou hlbkou
				// pre kazde data s prekrocenou hlbkou sa pokusime znovu vlozit do stromu, vsetky sa musia odstranit!
				for (IData<T> data : node.getDataSPrekrocenouHlbkou()) {
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

	public List<QTNode<T>> getNodesPodlaPredikatu(Predicate<QTNode<T>> predicate) {
		List<QTNode<T>> nodes = new ArrayList<>(4 ^ root.getHlbka());
		LinkedList<QTNode<T>> nodeNaSpracovanie = new LinkedList<>();

		nodeNaSpracovanie.add(root);

		while (!nodeNaSpracovanie.isEmpty()) {
			QTNode<T> currentNode = nodeNaSpracovanie.poll();
			if (predicate.test(currentNode)) {
				nodes.add(currentNode);
			}
			nodeNaSpracovanie.addAll(currentNode.getSynovia());
		}
		return nodes;
	}

	public QuadTree<T> optimalizuj() {
		// urci percento prvkov zo stromu podla ktorych budeme vytvarat novy strom
		List<IData<T>> dataZRoot = root.getData();
		List<IData<T>> vsetkyData = getAllData();

		var pocet = (int) (dataZRoot.size() * 0.1); // chceme 10 percent napr

		List<IData<T>> najvacsieData = new ArrayList<>(pocet);
		List<Double> najvacsieObsahy = dataZRoot.stream().map(data -> data.getSekundarnyKluc().getObsahOhranicenia()).sorted(Comparator.reverseOrder())
												.toList();
		List<Double> prveNajvacsieObsahy = najvacsieObsahy.subList(0, pocet);

		for (IData<T> data : dataZRoot) {
			if (prveNajvacsieObsahy.contains(data.getSekundarnyKluc().getObsahOhranicenia()) && najvacsieData.size() < pocet) {
				najvacsieData.add(data);
			}
		}
		// uz mame nase najvacie data, teraz podla nich vytvorime novu sirku a dlzku stromu
		var optimalizovanyStrom = getNovyQuadTree(pocet, najvacsieData);
		for (IData<T> data : vsetkyData) {
			optimalizovanyStrom.pridaj(data);
		}
		optimalizovanyStrom.setMaxHlbka(getMaxHlbka(root) + 1);
		return optimalizovanyStrom;
	}

	public int getMaxHlbka(QTNode<T> node) {
		var maxHlbka = 0;
		LinkedList<QTNode<T>> nodeNaSpracovanie = new LinkedList<>();

		nodeNaSpracovanie.add(node);

		while (!nodeNaSpracovanie.isEmpty()) {
			QTNode<T> currentNode = nodeNaSpracovanie.poll();
			if (maxHlbka < currentNode.getHlbka()) {
				maxHlbka = currentNode.getHlbka();
			}
			nodeNaSpracovanie.addAll(currentNode.getSynovia());
		}
		return maxHlbka;
	}

	private QuadTree<T> getNovyQuadTree(int pocet, List<IData<T>> najvacsieData) {
		var sirka = 0;
		var dlzka = 0;
		for (int i = 0; i < pocet; i++) {
			sirka = Math.max(sirka, najvacsieData.get(i).getSekundarnyKluc().getSuradnicaPravyHorny().getX().intValue() * 3);
			dlzka = Math.max(dlzka, najvacsieData.get(i).getSekundarnyKluc().getSuradnicaPravyHorny().getY().intValue() * 3);
		}

		// maxHlbku nastavime zatial na MAXINTEGER, nechame nech sa nastavia az na posledny mozny node
		// ziskame si najhlbsiu hlbku + 1, to bude nova maxHlbka
		maxHlbka = Integer.MAX_VALUE;
		return new QuadTree<>(maxHlbka, sirka, dlzka);
	}

	public Double getZdravie() {
		vypocitajBalanceFaktorKazdehoNode(root);

		LinkedList<QTNode<T>> nodeNaSpracovanie = new LinkedList<>();
		var pocetNodesNesplnajucichObeKriteria = 0;
		var pocetNodesSplnajucichKriteriumBalanceFactor = 0;
		var pocetNesplnajucichkriteriaDlzkyZoznamu = 0;
		var pocetNodes = 0;

		nodeNaSpracovanie.add(root);
		pocetNodes++;

		while (!nodeNaSpracovanie.isEmpty()) {
			QTNode<T> currentNode = nodeNaSpracovanie.poll();
			if (!currentNode.getSynovia().isEmpty()) {
				pocetNodes += currentNode.getSynovia().size();
				nodeNaSpracovanie.addAll(currentNode.getSynovia());
			}

			if (jePrekrocenyBalanceFactorThreshold(currentNode.getBalanceFactor())) {
				pocetNodesSplnajucichKriteriumBalanceFactor++;
			}
			if (jePrekrocenyVelkostDataThreshold(currentNode.getData().size())) {
				pocetNesplnajucichkriteriaDlzkyZoznamu++;
				if (currentNode.getData().size() > (size / 2)) { // ak nam nejaky zoznam v node obsahuje viac ako polovicu dat, tak musime zdravie velmi znizit
					pocetNesplnajucichkriteriaDlzkyZoznamu += VELKOST_DATA_THRESHOLD;
				}
			}
			if (jePrekrocenyBalanceFactorThreshold(currentNode.getBalanceFactor()) && jePrekrocenyVelkostDataThreshold(currentNode.getData().size())) {
				pocetNodesNesplnajucichObeKriteria++;
			}
		}
		// vypocitame si zdravie podla vzorca, mame 2 mnoziny a jeden prienik, potrebujeme vypocitat zjednotenie tychto mnozim ako K1 + K2 - OBE
		return 100 - ((((pocetNesplnajucichkriteriaDlzkyZoznamu + pocetNodesSplnajucichKriteriumBalanceFactor - pocetNodesNesplnajucichObeKriteria) /
				(double) pocetNodes)) * 100);
	}

	private void vypocitajBalanceFaktorKazdehoNode(QTNode<T> node) {
		Stack<QTNode<T>> stack = new Stack<>();
		QTNode<T> currentNode = node;
		LinkedList<QTNode<T>> spracovaneNodes = new LinkedList<>();

		while (currentNode != null || !stack.isEmpty()) {
			if (currentNode != null) {
				stack.push(currentNode);
				if (currentNode.getSynovia().isEmpty()) {
					currentNode = null;
				} else {
					currentNode = currentNode.getSynovia().get(0); // ak current node nie je null, vyskusame pridat jeho synov na spracovanie
				}
			} else {
				currentNode = stack.peek();

				// nemozeme spracovat node ak nie su spracovany vsetci jeho synovia
				boolean mozeSaSpracovatCurrentNode = true;
				if (!currentNode.getSynovia().isEmpty()) {
					for (int i = 0; i < 4; i++) {
						// ak synovia nie su null, musime pozriet ci sme ich uz spracovali, najskor spracujeme synov, potom seba
						if (!spracovaneNodes.contains(currentNode.getSynovia().get(i))) {
							// If a child exists and hasn't been processed, we can't process this node yet
							mozeSaSpracovatCurrentNode = false;
							currentNode = currentNode.getSynovia().get(i);
							break;
						}
					}
				}

				if (mozeSaSpracovatCurrentNode) {
					int maxHeight = 0; // chcem tam jedna aby sa mi dobre pocital balance factor
					if (!currentNode.getSynovia().isEmpty()) {
						for (int i = 0; i < 4; i++) {
							maxHeight = Math.max(maxHeight, currentNode.getSynovia().get(i).getHlbkaOdSpodu());
							spracovaneNodes.remove(
									currentNode.getSynovia().get(i)); // nasli sme uz vsetky synov, tak ich mozeme vymazat, skrati sa nam vyhladavanie
						}
					}
					currentNode.setHlbkaOdSpodu(maxHeight + 1);
					currentNode.setBalanceFactor(getBalanceFactor(currentNode));
					stack.pop();
					spracovaneNodes.addLast(currentNode);
					currentNode = null;
				}
			}
		}
	}

	private int getBalanceFactor(QTNode<T> node) {
		if (node.getSynovia().isEmpty()) {
			return 0;
		}

		var maxHlbka = 0; // najdeme si najdlhsiu vysku podstromu
		var minHlbka = Integer.MAX_VALUE; // najdeme si najkratsiu vysku podstromu

		for (int i = 0; i < 4; i++) {
			int vyskaSyna = node.getSynovia().get(i).getHlbkaOdSpodu();
			maxHlbka = Math.max(maxHlbka, vyskaSyna);
			minHlbka = Math.min(minHlbka, vyskaSyna);
		}
		return maxHlbka - minHlbka;
	}
}

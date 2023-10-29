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
import com.aus.prva_semestralka.objekty.IPozemok;
import com.aus.prva_semestralka.objekty.Map;
import com.aus.prva_semestralka.objekty.Ohranicenie;

@Data
@RequiredArgsConstructor
public class QuadTree {

	private QTNode root;
	public static Integer maxHlbka;
	private Integer sirka;
	private Integer dlzka;
	private Integer pocetPozemkov = 0;

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

	public int getPocetPozemkov() {
		return pocetPozemkov;
	}

	public int getMaxHlbka() { return maxHlbka; }

	public boolean pridaj(IPozemok pozemok) {
		return pridajDoNode(pozemok, root);
	}

	public boolean pridajDoNode(IPozemok pozemok, QTNode node) {
		if (pozemok == null || !node.zmestiSa(pozemok)) { // ak je pozemok velmi velky alebo neexistuje, tak ho nepridavame
			return false;
		}

		var currentNode = node;
		while (!currentNode.jeList()) {
			var indexSyna = currentNode.getKvadrantPrePozemok(pozemok);
			if (indexSyna != -1) {
				currentNode = currentNode.getSynovia().get(indexSyna - 1);
			} else {
				currentNode.getPozemky().add(pozemok); // presli sme vsetkych synov currentNode ale ani do jedneho sa nam nezmesti, tak ho pridame do tohto
				pocetPozemkov++;
				return true;
			}
		}

		if (Objects.equals(currentNode.getHlbka(), maxHlbka)) { // nemozeme prekrocit sme maximalnu hlbku
			currentNode.getPozemkySPrekrocenouHlbkou().add(pozemok);
			pocetPozemkov++;
			return true;
		}

		if (currentNode.getPozemok_data() == null) { // current node je list - nema synov
			currentNode.setPozemok_data(pozemok);
			pocetPozemkov++;
			return true;
		} else {
			// ak uz obsahuje nejake data, potom ideme delit node
			currentNode.rozdel();
			var jePriradenyKvadrat = currentNode.zaradPozemokDoKvadratu(currentNode.getPozemok_data()); // pridame pozemok, ktory uz bol v node
			var jeZaradenyPozemokNaVkladanie = currentNode.zaradPozemokDoKvadratu(pozemok); // pridame pozemok, ktory chceme vlozit
			if (jePriradenyKvadrat) {
				currentNode.setPozemok_data(null);
				pocetPozemkov--;
			}
			if (jeZaradenyPozemokNaVkladanie) {
				pocetPozemkov++;
			}
			if (jePriradenyKvadrat) {
				pocetPozemkov++;
			}
			return jePriradenyKvadrat && jeZaradenyPozemokNaVkladanie;
		}
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

			if (currentNode.jeList() && currentNode.getPozemok_data() != null) {
				pozemky.add(currentNode.getPozemok_data());
			} else {
				// pridanie synov do zoznamu na spracovanie
				nodeNaSpracovanie.addAll(currentNode.getSynovia());
			}
		}

		return pozemky;
	}

	public Map findWithin(Ohranicenie ohranicenie) {
		return findWithinFromNode(ohranicenie, root);
	}

	public Map findWithinFromNode(Ohranicenie ohranicenie, QTNode node) {
		if (node == null) {
			node = root;
		}

		var currentNode = node;
		ArrayList<IPozemok> result = new ArrayList<>();

		while (true) {
			if (currentNode.getSynovia().isEmpty()) {
				result.addAll(currentNode.getPozemky());
				result.addAll(currentNode.getPozemkySPrekrocenouHlbkou());
				if (currentNode.getPozemok_data() != null) {
					result.add(currentNode.getPozemok_data());
				}
				return Map.of(currentNode, result);
			}
			int index = currentNode.getKvadrantPreOhranicenie(ohranicenie);
			if (index == -1) {
				var najdenePozemky = getAllPozemkyFromNode(currentNode);
				for (IPozemok pozemok : najdenePozemky) {
					if (ohranicenie.zmestiSaDovnutra(pozemok.getGpsSuradnice())) {
						result.add(pozemok);
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

	public boolean deletePozemok(IPozemok pozemok) {
		return deletePozemokFromNode(pozemok, root);
	}

	public boolean deletePozemokFromNode(IPozemok pozemok, QTNode node) {
		if (node == null) {
			node = root;
		}
		var currentNode = node;
		List<QTNode> parentNodes = new LinkedList<>();

		while (true) {
			// ak je current node list a ma pozemok, tak ho vymazeme
			var result = false;
			if (currentNode.jeList()) {
				if (currentNode.getPozemok_data() != null && currentNode.getPozemok_data().equals(pozemok)) {
					currentNode.setPozemok_data(null);
					result = true;
				} else {
					result = currentNode.getPozemkySPrekrocenouHlbkou().remove(pozemok);
				}
				vymazPraznychSynov(parentNodes, result);
				if (result) {
					pocetPozemkov--;
				}
				return result; // koncime uplne aj ked sme v liste nic nenasli
			}

			// ak ma synov, tak vymazeme pozemok z aktualneho node
			if (currentNode.getPozemky().contains(pozemok)) {
				result = currentNode.getPozemky().remove(pozemok);
			}

			if (result) { // nemozeme vratit negativny vysledok, mozno sa najde este v dalsich synoch
				vymazPraznychSynov(parentNodes, result);
				pocetPozemkov--;
				return true;
			}
			if (currentNode.getPozemkySPrekrocenouHlbkou().contains(pozemok)) {
				result = currentNode.getPozemkySPrekrocenouHlbkou().remove(pozemok);
			}

			if (result) { // nemozeme vratit negativny vysledok, mozno sa najde este v dalsich synoch
				vymazPraznychSynov(parentNodes, result);
				pocetPozemkov--;
				return true;
			}

			// ak sa pozemok nenachadza v aktualnom node, tak sa posunieme na dalsi node
			var indexSyna = currentNode.getKvadrantPrePozemok(pozemok);
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
				if (parentNode.getPozemky().isEmpty() && parentNode.getPozemkySPrekrocenouHlbkou().isEmpty() && parentNode.getPozemok_data() == null) {
					parentNode.getSynovia().clear();
					parentNode.zmenJeList(true);
				} else {
					break;
				}
			}
		}
	}

	public boolean uprav(IPozemok povodnyPozemok, IPozemok pozemok, QTNode node) {
		var najdene = findWithinFromNode(povodnyPozemok.getGpsSuradnice(), node).getPozemky();
		if (najdene.isEmpty()) {
			return false;
		}
		for (IPozemok pozemok1 : najdene) {
			if (pozemok1.getSupisneCislo().equals(povodnyPozemok.getSupisneCislo())) {
				pozemok1.setPopis(pozemok.getPopis());
				pozemok1.setSupisneCislo(pozemok.getSupisneCislo());
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
				ArrayList<IPozemok> pozemkyNaVlozenie = new ArrayList<>();
				pozemkyNaVlozenie.add(node.getPozemok_data());
				pozemkyNaVlozenie.addAll(node.getPozemky());
				pozemkyNaVlozenie.addAll(node.getPozemkySPrekrocenouHlbkou());

				for (IPozemok pozemok : pozemkyNaVlozenie) {
					deletePozemok(pozemok); // vymazeme pozemok aby sme nemali duplicity
					pridaj(pozemok);
				}
			}
			return true;
		}
		if (hlbka > maxHlbka) {
			Predicate<QTNode> predicate = node -> node.getHlbka() == maxHlbka && !node.getPozemkySPrekrocenouHlbkou().isEmpty();
			var nodes = getNodesPodlaPredikatu(predicate);
			maxHlbka = hlbka;

			for (QTNode node : nodes) { // pre kazdy node s pozemkami s prekrocenou hlbkou
				// pre kazdy pozemok s prekrocenou hlbkou sa pokusime znovu vlozit do stromu, vsetky sa musia odstranit!
				for (IPozemok pozemok : node.getPozemkySPrekrocenouHlbkou()) {
					if (pridajDoNode(pozemok, node)) { // nehladame kde sa zmeti, lebo uz vieme ze sa zmesti len do current node alebo nizsie
						node.getPozemkySPrekrocenouHlbkou().remove(pozemok);
						pocetPozemkov--; // ked ostranime zo zoznamu, musime znizit pocet pozemkov aby sme nepridali duplikat
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

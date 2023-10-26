package com.aus.prva_semestralka.struktury;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IPozemok;
import com.aus.prva_semestralka.objekty.Ohranicenie;

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
		var ohranicenie = new Ohranicenie(prvaSuradnicaKorena, druhaSuradnicaKorena);
		this.root = new QTNode(1, ohranicenie, 0);
		root.rozdel();
	}

	public boolean pridaj(IPozemok pozemok) {
		if (pozemok == null || !root.zmestiSa(pozemok)) { // ak je pozemok velmi velky alebo neexistuje, tak ho nepridavame
			return false;
		}

		var currentNode = root;
		while (!currentNode.jeList()) {
			var indexSyna = currentNode.getKvadrantPrePozemok(pozemok);
			if (indexSyna != -1) {
				currentNode = currentNode.getSynovia().get(indexSyna - 1);
			} else {
				currentNode.getPozemky().add(pozemok); // presli sme vsetkych synov currentNode ale ani do jedneho sa nam nezmesti, tak ho pridame do tohto
				return true;
			}
		}

		if (Objects.equals(currentNode.getHlbka(), maxHlbka)) { // nemozeme prekrocit sme maximalnu hlbku
			currentNode.getPozemkySPrekrocenouHlbkou().add(pozemok);
			return true;
		}

		if (currentNode.getPozemok_data() == null) { // current node je list - nema synov
			currentNode.setPozemok_data(pozemok);
			return true;
		} else {
			// ak uz obsahuje nejake data, potom ideme delit node
			currentNode.rozdel();
			var jePriradenyKvadrat = currentNode.zaradPozemokDoKvadratu(currentNode.getPozemok_data()); // pridame pozemok, ktory uz bol v node
			if (jePriradenyKvadrat) {
				currentNode.setPozemok_data(null);
			}
			var jeZaradenyPozemokNaVkladanie = currentNode.zaradPozemokDoKvadratu(pozemok); // pridame pozemok, ktory chceme vlozit
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

	public List<IPozemok> findWithin(Ohranicenie ohranicenie) {
		var currentNode = root;
		ArrayList<IPozemok> result = new ArrayList<>();

		while (true) {
			if (currentNode.getSynovia() == null) {
				result.addAll(currentNode.getPozemky());
				result.addAll(currentNode.getPozemkySPrekrocenouHlbkou());
				if (currentNode.getPozemok_data() != null) {
					result.add(currentNode.getPozemok_data());
				}
				return result;
			}
			int index = currentNode.getKvadrantPreOhranicenie(ohranicenie);
			if (index == -1) {
				var najdenePozemky = getAllPozemkyFromNode(currentNode);
				for (IPozemok pozemok : najdenePozemky) {
					if (ohranicenie.zmestiSaDovnutra(pozemok.getGpsSuradnice())) {
						result.add(pozemok);
					}
				}
				return result;
			} else {
				var syn = currentNode.getSynovia().get(index - 1);
				if (syn == null) {
					return Collections.emptyList();
				}
				currentNode = syn;
			}
		}
	}

	public boolean deletePozemok(IPozemok pozemok) {
		var currentNode = root;
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
				return result; // koncime uplne aj ked sme v liste nic nenasli
			}

			// ak ma synov, tak vymazeme pozemok z aktualneho node
			if (currentNode.getPozemky().contains(pozemok)) {
				result = currentNode.getPozemky().remove(pozemok);
			}
			if (currentNode.getPozemkySPrekrocenouHlbkou().contains(pozemok)) {
				result = currentNode.getPozemkySPrekrocenouHlbkou().remove(pozemok);
			}
			vymazPraznychSynov(parentNodes, result);
			if (result) { // nemozeme vratit negativny vysledok, mozno sa najde este v dalsich synoch
				return true;
			}

			// ak sa pozemok nenachadza v aktualnom node, tak sa posunieme na dalsi node
			for (QTNode syn : currentNode.getSynovia()) {
				if (syn.zmestiSa(pozemok)) {
					parentNodes.add(0, currentNode);
					currentNode = syn;
				}
			}
		}
	}

	private void vymazPraznychSynov(List<QTNode> parentNodes, boolean result) {
		if (result && !parentNodes.isEmpty()) {
			for (QTNode parentNode : parentNodes) {
				var dajuSaZmazatSynovia = parentNode.dajuSaZmazatSynovia();
				if (!dajuSaZmazatSynovia) {
					break;
				}
				parentNode.getSynovia().clear();
				parentNode.zmenJeList(true);
			}
		}
	}

	public boolean uprav(IPozemok povodnyPozemok, IPozemok pozemok) {
		var najdene = findWithin(povodnyPozemok.getGpsSuradnice());
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
}

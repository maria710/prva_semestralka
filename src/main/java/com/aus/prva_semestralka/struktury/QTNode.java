package com.aus.prva_semestralka.struktury;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IPozemok;

@Data
@RequiredArgsConstructor
public class QTNode {

	private Integer primarnyKluc;
	private List<GpsPozicia> sekundarnyKluc;
	private List<QTNode> synovia;
	private List<IPozemok> pozemky;
	private List<IPozemok> pozemkySPrekrocenouHlbkou;
	private boolean jeList;
	private Integer hlbka;
	private final Integer MAX_POCET_SYNOV = 4;

	public QTNode(Integer primarnyKluc, List<GpsPozicia> sekundarnyKluc, Integer hlbka) {
		this.primarnyKluc = primarnyKluc;
		this.sekundarnyKluc = sekundarnyKluc;
		this.synovia = new ArrayList<>(4);
		this.pozemky = new ArrayList<>(10);
		this.pozemkySPrekrocenouHlbkou = new ArrayList<>(20);
		this.jeList = true;
		this.hlbka = hlbka;
	}

	public boolean compareKeys() {
		return true;
	}

	public QTNode getSeverozapadnySyn() {
		return synovia.get(0);
	}

	public QTNode getSeverovychodnySyn() {
		return synovia.get(1);
	}

	public QTNode getJuhozapadnySyn() {
		return synovia.get(2);
	}

	public QTNode getJuhovychodnySyn() {
		return synovia.get(3);
	}

	public void setSeverozapadnySyn(QTNode syn) {
		this.jeList = false;
		synovia.add(0, syn);
	}

	public void setSeverovychodnySyn(QTNode syn) {
		this.jeList = false;
		synovia.add(1, syn);
	}

	public void setJuhovychodnySyn(QTNode syn) {
		this.jeList = false;
		synovia.add(2, syn);
	}

	public void setJuhozapadnySyn(QTNode syn) {
		this.jeList = false;
		synovia.add(3, syn);
	}



	public boolean pridajPozemok(IPozemok pozemok) {

		QTNode currentNode = this; // Start with the current node as the root

		// Traverse from the root to a leaf node
		while (!currentNode.jeList) {
			boolean propertyInserted = false;
			for (QTNode syn : currentNode.synovia) {
				if (syn.zmestiSa(pozemok)) {
					currentNode = syn; // Move to the child node
					propertyInserted = true;
					break;
				}
			}

			// If the property couldn't be inserted into any child node we will insert it into the overflow list
			if (!propertyInserted) {
				currentNode.pozemky.add(pozemok);
				return true;
			}
		}

		// The current node is now a leaf node; insert the property
		if (currentNode.zmestiSa(pozemok)) {
			currentNode.rozdel();
			for (QTNode syn : currentNode.synovia) {
				if (syn.zmestiSa(pozemok)) {
					syn.getPozemky().add(pozemok);
					return true;
				}
			}
			currentNode.pozemky.add(pozemok);
			return true;
		} else {
			// The property doesn't fit in this leaf node, so it should be added to the overflow list
			currentNode.pozemkySPrekrocenouHlbkou.add(pozemok);
			return true;
		}

//		if (zmestiSaDoNode(pozemok)) {
//			if (isJeList()) {
//				rozdel();
//			}
			// podmienka ci uz neni taky pozemok v node - pridame ho do listu pozemkov
			// ak je v node uz pozemok, ktory ma rovnake suradnice ako pridavany pozemok, tak ho pridame do listu pozemkov

//			var currentNodeIndex = 0;
//			var currentNode = synovia.get(currentNodeIndex);
//			var currentSynovia = synovia;
//
//			while (currentNode != null) {
//				if (currentNode.zmestiSaDoKvadratu(pozemok, currentNode)) {
//					currentSynovia = currentNode.getSynovia();
//					if (currentSynovia != null) {
//						currentNode.rozdel();
//						currentNodeIndex = 0;
//						currentNode = currentSynovia.get(currentNodeIndex);
//					} else {
//						currentNode.getPozemky().add(pozemok);
//						currentNode = null;
//					}
//				} else {
//					currentNodeIndex++;
//					currentNode = currentSynovia.get(currentNodeIndex);
//				}
//				if (currentNodeIndex == 4) {
//					currentNode.getPozemky().add(pozemok);
//					currentNode = null;
//				}
//			}
	}

	public void rozdel() {

		double suradnicaX1 = sekundarnyKluc.get(0).getX(); // 0
		double suradnicaX2 = sekundarnyKluc.get(1).getX(); // 100
		double suradnicaY1 = sekundarnyKluc.get(0).getY(); // 0
		double suradnicaY2 = sekundarnyKluc.get(1).getY(); // 100

		double minX = Math.min(suradnicaX1, suradnicaX2); // 0
		double maxX = Math.max(suradnicaX1, suradnicaX2); // 100
		double minY = Math.min(suradnicaY1, suradnicaY2); // 0
		double maxY = Math.max(suradnicaY1, suradnicaY2); // 100

		double midX = (suradnicaX1 + suradnicaX2) / 2; // 50
		double midY = (suradnicaY1 + suradnicaY2) / 2; // 50

		// 1 kvadrant
		setSeverozapadnySyn(new QTNode(primarnyKluc + 1,
									   List.of(new GpsPozicia('S', 'Z', minX, midY), new GpsPozicia('S', 'Z', midX, maxY)), hlbka + 1));
		// 2 kvadrant
		setSeverovychodnySyn(new QTNode(primarnyKluc + 2,
										List.of(new GpsPozicia('S', 'V', midX, midY), new GpsPozicia('S', 'V', maxX, maxY)), hlbka + 1));
		// 3 kvadrant
		setJuhovychodnySyn(new QTNode(primarnyKluc + 3,
									  List.of(new GpsPozicia('J', 'V', midX, minY), new GpsPozicia('J', 'V', maxX, midY)), hlbka + 1));
		// 4 kvadrant
		setJuhozapadnySyn(new QTNode(primarnyKluc + 4,
									 List.of(new GpsPozicia('J', 'Z', minX, minY), new GpsPozicia('J', 'Z', midX, midY)), hlbka + 1));
	}

	public boolean zmestiSa(IPozemok pozemok) {

		var suradnica1 = pozemok.getGpsSuradnice().get(0);
		var suradnica2 = pozemok.getGpsSuradnice().get(1);

		double suradnicaX1 = suradnica1.getX();
		double suradnicaX2 = suradnica2.getX();
		double suradnicaY1 = suradnica1.getY();
		double suradnicaY2 = suradnica2.getY();

		double suradnicaNodeX1 = sekundarnyKluc.get(0).getX();
		double suradnicaNodeX2 = sekundarnyKluc.get(1).getX();
		double suradnicaNodeY1 = sekundarnyKluc.get(0).getY();
		double suradnicaNodeY2 = sekundarnyKluc.get(1).getY();

		return suradnicaX1 >= suradnicaNodeX1 && suradnicaX2 <= suradnicaNodeX2 && suradnicaY1 >= suradnicaNodeY1 && suradnicaY2 <= suradnicaNodeY2;
	}

//	public boolean zmestiSaDoKvadratu(IPozemok pozemok, QTNode kvadrat) {
//
//		if (!isJeList()) {
//			var suradnica1 = pozemok.getGpsSuradnice().get(0);
//			var suradnica2 = pozemok.getGpsSuradnice().get(1);
//
//			double suradnicaX1 = suradnica1.getX();
//			double suradnicaX2 = suradnica2.getX();
//			double suradnicaY1 = suradnica1.getY();
//			double suradnicaY2 = suradnica2.getY();
//
//			double suradnicaKvadratX1 = kvadrat.getSekundarnyKluc().get(0).getX();
//			double suradnicaKvadratX2 = kvadrat.getSekundarnyKluc().get(1).getX();
//			double suradnicaKvadratY1 = kvadrat.getSekundarnyKluc().get(0).getY();
//			double suradnicaKvadratY2 = kvadrat.getSekundarnyKluc().get(1).getY();
//
//			return suradnicaX1 >= suradnicaKvadratX1 && suradnicaX2 <= suradnicaKvadratX2 && suradnicaY1 >= suradnicaKvadratY1 && suradnicaY2 <= suradnicaKvadratY2;
//		}
//		return false;
//	}
}

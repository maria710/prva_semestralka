package com.aus.prva_semestralka.struktury;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.aus.prva_semestralka.objekty.GpsPozicia;
import com.aus.prva_semestralka.objekty.IPozemok;

@Data
@RequiredArgsConstructor
public class QTNode {

	private Integer primarnyKluc;
	private List<GpsPozicia> sekundarnyKluc;
	private List<QTNode> synovia;
	private IPozemok pozemok_data;
	private List<IPozemok> pozemky;
	private List<IPozemok> pozemkySPrekrocenouHlbkou;
	private boolean jeList;
	private Integer hlbka;
	private int pocetSynov = 0;
	private final Integer MAX_POCET_SYNOV = 4;

	public QTNode(Integer primarnyKluc, List<GpsPozicia> sekundarnyKluc, Integer hlbka) {
		this.primarnyKluc = primarnyKluc;
		this.sekundarnyKluc = sekundarnyKluc;
		this.synovia = new ArrayList<>(MAX_POCET_SYNOV);
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

	public IPozemok getPozemok_data() {
		return pozemok_data;
	}

	public void setPozemok_data(IPozemok pozemok_data) {
		this.jeList = true;
		this.pozemok_data = pozemok_data;
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

		while (!currentNode.jeList) {
			boolean posunNaDalsiehoSyna = false;
			// prejdeme vsetkych synov a najdeme toho, do ktoreho sa zmesti pozemok
			for (QTNode syn : currentNode.synovia) {
				if (syn.zmestiSa(pozemok)) {
					currentNode = syn; // posunieme sa na syna do ktoreho sa zmesti pozemok
					posunNaDalsiehoSyna = true;
					break;
				}
			}
			// presli sme vsetkych synov currentNode ale ani do jedneho sa nam nezmesti, tak ho pridame do tohto
			if (!posunNaDalsiehoSyna) {
				currentNode.pozemky.add(pozemok);
				return true;
			}
		}
		// current node je list - nema synov
		if (currentNode.pozemok_data == null) {
			currentNode.pozemok_data = pozemok;
		} else {
			// ak uz obsahuje nejake data, potom ideme delit node
			if (Objects.equals(currentNode.hlbka, QuadTree.maxHlbka)) {
				currentNode.pozemkySPrekrocenouHlbkou.add(pozemok);
				return true;
			}
			currentNode.rozdel();
			// najskor zaradime pozemok do kvadrantu
			var jePriradenyKvadrat = zaradPozemokDoKvadratu(currentNode.pozemok_data, currentNode.synovia);
			if (!jePriradenyKvadrat) {
				currentNode.pozemky.add(currentNode.pozemok_data);
				currentNode.pozemok_data = null;
			}
			// zaradime novy pozemok do kvadrantu
			var jeZaradenyAjPozemokNaVkladanie = zaradPozemokDoKvadratu(pozemok, currentNode.synovia);
			if (!jeZaradenyAjPozemokNaVkladanie) {
				currentNode.pozemky.add(pozemok);
			}
		}
		return true;
	}

	private boolean zaradPozemokDoKvadratu(IPozemok pozemok_data, List<QTNode> synovia) {
		// prechadzame novo vytvorene pozemky a hladame ten, ktory sa zmesti do kvadrantu
		for (QTNode syn : synovia) {
			// ak sa zmesti
			if (syn.zmestiSa(pozemok_data)) {
				// potom musime este zistit ci je prazdny
				// ak je prazdny tak ho pridame na miesto pozemku
				if (syn.getPozemok_data() == null) {
					syn.setPozemok_data(pozemok_data);
					return true;
				} else {
					var pozemokSyna = syn.getPozemok_data();
					// inak musime rozdelit syna,
					// tento scenar sa moze stat ak prvy pozemok ktory tento node mal ale nemal synov pridame napr do tretieho
					// kvadratu a potom pridame dalsi pozemok, ktory sa ma zmestit do toho isteho

					// while oba sa zmestia do toho isteho kvadrantu
					while (true) {
						boolean zmestiaSaObaja = false;
						boolean zmestilSaPrvy = false;
						boolean zmestilSaDruhy = false;
						if (Objects.equals(syn.hlbka, QuadTree.maxHlbka)) {
							syn.pozemkySPrekrocenouHlbkou.add(pozemok_data);
							syn.pozemkySPrekrocenouHlbkou.add(pozemokSyna);
							return true;
						}
						syn.rozdel();
						for (QTNode syn2 : syn.synovia) {
							if (syn2.zmestiSa(pozemok_data) && syn2.zmestiSa(pozemokSyna)) {
								syn = syn2;
								zmestiaSaObaja = true;
								break;
							} else if (syn2.zmestiSa(pozemok_data)) {
								zmestilSaPrvy = true;
								syn2.pozemok_data = pozemok_data;
								break;
							} else if (syn2.zmestiSa(pozemokSyna)) {
								zmestilSaDruhy = true;
								syn2.pozemok_data = pozemokSyna;
								break;
							}
						}
						// ak sa zmestia obaja, tak pokracujeme v cykle
						// ak sa nezmesti ani jeden, tak pridame oba pozemky do pozemkov aktualneho s
						if (!zmestiaSaObaja) {
							if (!zmestilSaPrvy) {
								syn.pozemky.add(pozemok_data);
							}
							if (!zmestilSaDruhy) {
								syn.pozemky.add(pozemokSyna);
							}
							break;
						}
					}
				}
				return true;
			}
		}
		return false;
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
									   List.of(new GpsPozicia("S", "Z", minX, midY), new GpsPozicia("S", "Z", midX, maxY)), hlbka + 1));
		// 2 kvadrant
		setSeverovychodnySyn(new QTNode(primarnyKluc + 2,
										List.of(new GpsPozicia("S", "V", midX, midY), new GpsPozicia("S", "V", maxX, maxY)), hlbka + 1));
		// 3 kvadrant
		setJuhovychodnySyn(new QTNode(primarnyKluc + 3,
									  List.of(new GpsPozicia("J", "V", midX, minY), new GpsPozicia("J", "V", maxX, midY)), hlbka + 1));
		// 4 kvadrant
		setJuhozapadnySyn(new QTNode(primarnyKluc + 4,
									 List.of(new GpsPozicia("J", "Z", minX, minY), new GpsPozicia("J", "Z", midX, midY)), hlbka + 1));
	}

	public boolean zmestiSa(IPozemok pozemok) {

		var suradnica1 = pozemok.getGpsSuradnice().get(0);
		var suradnica2 = pozemok.getGpsSuradnice().get(1);

		return zmestiSa(suradnica1, suradnica2);
	}

	public boolean zmestiSa(GpsPozicia suradnica1, GpsPozicia suradnica2) {

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

}

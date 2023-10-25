package com.aus.prva_semestralka.struktury;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.aus.prva_semestralka.objekty.IPozemok;
import com.aus.prva_semestralka.objekty.Ohranicenie;

@Data
@RequiredArgsConstructor
public class QTNode {

	private Integer primarnyKluc;
	private Ohranicenie ohranicenie;
	private List<QTNode> synovia;
	private IPozemok pozemok_data;
	private List<IPozemok> pozemky;
	private List<IPozemok> pozemkySPrekrocenouHlbkou;
	private boolean jeList;
	private Integer hlbka;

	public QTNode(Integer primarnyKluc, Ohranicenie ohranicenie, Integer hlbka) {
		this.primarnyKluc = primarnyKluc;
		this.ohranicenie = ohranicenie;
		this.synovia = new ArrayList<>(4);
		this.pozemky = new ArrayList<>(10);
		this.pozemkySPrekrocenouHlbkou = new ArrayList<>(20);
		this.jeList = true;
		this.hlbka = hlbka;
	}

	public QTNode getSeverozapadnySyn() { return synovia.get(0); }

	public QTNode getSeverovychodnySyn() { return synovia.get(1); }

	public QTNode getJuhovychodnySyn() { return synovia.get(2); }

	public QTNode getJuhozapadnySyn() { return synovia.get(3); }

	public IPozemok getPozemok_data() {	return pozemok_data; }

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

	public boolean zaradPozemokDoKvadratu(IPozemok pozemok_data) {
		int indexSyna = getKvadrantPrePozemok(pozemok_data);
		if (indexSyna == -1) {
			pozemky.add(pozemok_data);
			return false;
		}
		var syn = synovia.get(indexSyna - 1);

		// ak je prazdny tak ho pridame na miesto pozemku
		if (syn.getPozemok_data() == null && syn.isJeList()) {
			syn.setPozemok_data(pozemok_data);
			return true;
		}

		// ak nie je prazdny tak ho pridame do zoznamu pozemkov, rozdelime
		var pozemokSyna = syn.getPozemok_data();
		while (true) {
			if (Objects.equals(syn.hlbka, QuadTree.maxHlbka)) {
				syn.pozemkySPrekrocenouHlbkou.add(pozemok_data);
				syn.pozemkySPrekrocenouHlbkou.add(pozemokSyna);
				return true;
			}
			syn.rozdel();
			int indexSynaPrePozemokSyna = syn.getKvadrantPrePozemok(pozemokSyna);
			int indexSynaPrePozemokVkladany = syn.getKvadrantPrePozemok(pozemok_data);

			if (indexSynaPrePozemokSyna == indexSynaPrePozemokVkladany && indexSynaPrePozemokSyna != -1) {
				syn = syn.synovia.get(indexSynaPrePozemokSyna - 1);
			} else {
				// ak sa nezmestia do synov tak ich pridame do pozemkov
				if (indexSynaPrePozemokSyna == -1) {
					syn.getPozemky().add(pozemokSyna);
				}
				if (indexSynaPrePozemokVkladany == -1) {
					syn.getPozemky().add(pozemok_data);
				}
				if (indexSynaPrePozemokSyna != -1 && indexSynaPrePozemokVkladany != -1) {
					syn.synovia.get(indexSynaPrePozemokSyna - 1).pozemok_data = pozemokSyna;
					syn.synovia.get(indexSynaPrePozemokVkladany -1).pozemok_data = pozemok_data;
				}
				return true;
			}
		}
	}

	public void rozdel() {
		var ohranicenia = ohranicenie.rozdel();

		setSeverozapadnySyn(new QTNode(primarnyKluc + 1, ohranicenia.get(0), hlbka + 1));
		setSeverovychodnySyn(new QTNode(primarnyKluc + 2, ohranicenia.get(1), hlbka + 1));
		setJuhovychodnySyn(new QTNode(primarnyKluc + 3, ohranicenia.get(2), hlbka + 1));
		setJuhozapadnySyn(new QTNode(primarnyKluc + 4, ohranicenia.get(3), hlbka + 1));
	}

	public boolean zmestiSa(IPozemok pozemok) {
		return ohranicenie.zmestiSaDovnutra(pozemok.getGpsSuradnice());
	}

	public int getKvadrantPrePozemok(IPozemok pozemok) {
		if (getSeverozapadnySyn().zmestiSa(pozemok)) {
			return 1;
		}
		if (getSeverovychodnySyn().zmestiSa(pozemok)) {
			return 2;
		}
		if (getJuhovychodnySyn().zmestiSa(pozemok)) {
			return 3;
		}
		if (getJuhozapadnySyn().zmestiSa(pozemok)) {
			return 4;
		}
		return -1;
	}

	public int getKvadrantPreOhranicenie(Ohranicenie ohranicenie) {
		if (getSeverozapadnySyn().ohranicenie.zmestiSaDovnutra(ohranicenie)) {
			return 1;
		}
		if (getSeverovychodnySyn().ohranicenie.zmestiSaDovnutra(ohranicenie)) {
			return 2;
		}
		if (getJuhovychodnySyn().ohranicenie.zmestiSaDovnutra(ohranicenie)) {
			return 3;
		}
		if (getJuhozapadnySyn().ohranicenie.zmestiSaDovnutra(ohranicenie)) {
			return 4;
		}
		return -1;
	}

}

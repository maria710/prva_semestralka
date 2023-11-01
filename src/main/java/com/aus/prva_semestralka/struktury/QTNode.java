package com.aus.prva_semestralka.struktury;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.aus.prva_semestralka.objekty.IData;
import com.aus.prva_semestralka.objekty.Ohranicenie;

import static com.aus.prva_semestralka.GeodetAppManazer.generatorKlucov;

@Data
@RequiredArgsConstructor
public class QTNode {

	private Integer primarnyKluc;
	private Ohranicenie ohranicenie;
	private List<QTNode> synovia;
	private IData dataListu;
	private List<IData> data;
	private List<IData> dataSPrekrocenouHlbkou;
	private boolean jeList;
	private Integer hlbka;

	public QTNode(Integer primarnyKluc, Ohranicenie ohranicenie, Integer hlbka) {
		this.primarnyKluc = primarnyKluc;
		this.ohranicenie = ohranicenie;
		this.synovia = new ArrayList<>(4);
		this.data = new ArrayList<>(10);
		this.dataSPrekrocenouHlbkou = new ArrayList<>(20);
		this.jeList = true;
		this.hlbka = hlbka;
	}

	public QTNode getSeverozapadnySyn() {
		return synovia.get(0);
	}

	public QTNode getSeverovychodnySyn() {
		return synovia.get(1);
	}

	public QTNode getJuhovychodnySyn() {
		return synovia.get(2);
	}

	public QTNode getJuhozapadnySyn() {
		return synovia.get(3);
	}

	public IData getDataListu() {
		return dataListu;
	}

	public boolean jeList() {
		return jeList;
	}

	public int getHlbka() {
		return hlbka;
	}

	public List<QTNode> getSynovia() {
		return synovia;
	}

	public List<IData> getData() {
		return data;
	}

	public List<IData> getDataSPrekrocenouHlbkou() {
		return dataSPrekrocenouHlbkou;
	}

	public void setDataListu(IData dataListu) {
		this.dataListu = dataListu;
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

	public boolean zaradDoKvadratu(IData data) {
		int indexSyna = getKvadrantPreData(data);
		if (indexSyna == -1) {
			this.data.add(data);
			return true;
		}
		var syn = synovia.get(indexSyna - 1);

		// ak je prazdny tak ho pridame na miesto pre data listu
		if (syn.getDataListu() == null && syn.jeList()) {
			syn.setDataListu(data);
			return true;
		}

		// ak nie je prazdny tak ho pridame do zoznamu dat, rozdelime
		var dataSyna = syn.getDataListu();
		while (true) {
			if (Objects.equals(syn.hlbka, QuadTree.maxHlbka)) {
				syn.dataSPrekrocenouHlbkou.add(data);
				syn.dataSPrekrocenouHlbkou.add(dataSyna);
				return true;
			}
			syn.rozdel();
			int indexSynaPreDataSyna = syn.getKvadrantPreData(dataSyna);
			int indexSynaPreDataVkladane = syn.getKvadrantPreData(data);

			if (indexSynaPreDataSyna == indexSynaPreDataVkladane && indexSynaPreDataSyna != -1) {
				syn = syn.synovia.get(indexSynaPreDataSyna - 1);
			} else {
				// ak sa nezmestia do synov tak ich pridame do zoznamu dat
				if (indexSynaPreDataSyna == -1) {
					syn.getData().add(dataSyna);
				} else {
					syn.synovia.get(indexSynaPreDataSyna - 1).dataListu = dataSyna;
					syn.setDataListu(null);
				}
				if (indexSynaPreDataVkladane == -1) {
					syn.getData().add(data);
				} else {
					syn.synovia.get(indexSynaPreDataVkladane - 1).dataListu = data;
				}
				return true;
			}
		}
	}

	public void rozdel() {
		var ohranicenia = ohranicenie.rozdel();

		setSeverozapadnySyn(new QTNode(generatorKlucov.getKluc(), ohranicenia.get(0), hlbka + 1));
		setSeverovychodnySyn(new QTNode(generatorKlucov.getKluc(), ohranicenia.get(1), hlbka + 1));
		setJuhovychodnySyn(new QTNode(generatorKlucov.getKluc(), ohranicenia.get(2), hlbka + 1));
		setJuhozapadnySyn(new QTNode(generatorKlucov.getKluc(), ohranicenia.get(3), hlbka + 1));
	}

	public boolean zmestiSa(IData data) {
		return ohranicenie.zmestiSaDovnutra(data.getSekundarnyKluc());
	}

	public int getKvadrantPreData(IData data) {
		if (getSeverozapadnySyn().zmestiSa(data)) {
			return 1;
		}
		if (getSeverovychodnySyn().zmestiSa(data)) {
			return 2;
		}
		if (getJuhovychodnySyn().zmestiSa(data)) {
			return 3;
		}
		if (getJuhozapadnySyn().zmestiSa(data)) {
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

	public boolean mozeSaVymazat() {
		return dataListu == null && data.isEmpty() && dataSPrekrocenouHlbkou.isEmpty() && synovia.isEmpty() && jeList;
	}

	public boolean dajuSaZmazatSynovia() {
		for (QTNode syn : synovia) {
			if (!syn.mozeSaVymazat()) {
				return false;
			}
		}
		return true;
	}

	public void zmenJeList(boolean b) {
		this.jeList = b;
	}
}

package com.aus.prva_semestralka.objekty;

import java.util.ArrayList;
import java.util.List;

public class Blok<T> {

	private final ArrayList<IRecord<T>> records;
	private int index;
	private int adresa;
	private int aktualnyPocetRecordov;

	public Blok() {
		records = new ArrayList<>();
	}

	public void pridajRecord(IRecord<T> IRecord) {
		aktualnyPocetRecordov++;
		records.add(IRecord);
		// zapis do suboru

	}

	public void vymazRecord(IRecord<T> IRecord) {
		records.remove(IRecord);
	}

	public void vymazRecord(int index) {
		records.remove(index);
	}

	public IRecord<T> getRecord(int index) {
		return records.get(index);
	}

	public int getSize() {
		return aktualnyPocetRecordov;
	}

	Byte[] toByteArray() {
		return null;
	}

	public void fromByteArray(Byte[] data) {

	}

	public void citajBlock() {

	}

	public void zapisRecordDoSuboru(IRecord<T> record) {
		// otvor binarny subor
		// zapis record do suboru na miesto indexu bloku

	}

	public List<IRecord<T>> getRecords() {
		return records;
	}
}

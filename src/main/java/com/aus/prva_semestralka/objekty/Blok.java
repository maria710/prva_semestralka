package com.aus.prva_semestralka.objekty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Blok<T extends IRecord> {

	private ArrayList<T> records;
	private int predchodca;
	private int nasledovnik;
	private int aktualnyPocetRecordov;
	private int index;
	private Class<T> classType;

	public Blok() {
		records = new ArrayList<>();
	}

	public Blok(int aktualnyPocetRecordov, ArrayList<T> records) {
		this.records = records;
		this.aktualnyPocetRecordov = aktualnyPocetRecordov;
	}

	public Blok(Class<T> classType) {
		this.classType = classType;
	}

	public Blok(int aktualnyPocetRecordov, ArrayList<T> records, int predchodca, int nasledovnik) {
		this.records = records;
		this.aktualnyPocetRecordov = aktualnyPocetRecordov;
		this.predchodca = predchodca;
		this.nasledovnik = nasledovnik;
	}

	public void pridaj(T data) {
		aktualnyPocetRecordov++;
		records.add(data);
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}

	public void vymazRecord(T IRecord) {
		records.remove(IRecord);
	}

	public int getAktualnyPocetRecordov() {
		return this.aktualnyPocetRecordov;
	}

	public void vymazRecord(int index) {
		records.remove(index);
	}

	public T getRecord(int index) {
		return records.get(index);
	}

	public List<T> getRecords() {
		return records;
	}

	public int getSize(int blokovaciFaktor) {

		int recordSize;
		try {
			// vytvor novu instanciu IRecord
			IRecord novaIntancia = classType.getDeclaredConstructor().newInstance().dajObjekt();
			recordSize = novaIntancia.getSize();
		} catch (Exception e) {
			throw new RuntimeException("Chyba pri vytrvarani novej instancie " + classType.getName(), e);
		}

		return 4 * Integer.SIZE + blokovaciFaktor * recordSize;
	}

	public int getPredchodca() {
		return predchodca;
	}

	public int getNasledovnik() {
		return nasledovnik;
	}

	public void setPredchodca(int predchodca) {
		this.predchodca = predchodca;
	}

	public void setNasledovnik(int nasledovnik) {
		this.nasledovnik = nasledovnik;
	}

	public byte[] toByteArray() {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

			objectOutputStream.writeInt(aktualnyPocetRecordov);
			objectOutputStream.writeInt(predchodca);
			objectOutputStream.writeInt(nasledovnik);

			for (int i = 0; i < aktualnyPocetRecordov; i++) {
				objectOutputStream.write(records.get(i).toByteArray());
			}
			return byteArrayOutputStream.toByteArray();

		} catch (IOException e) {
			throw new RuntimeException("Chyba pri serializacii objektu: " + this + " do pola bajtov. ERROR:" + e.getMessage());
		}
	}

	public Blok<T> fromByteArray(byte[] data) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			 ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

			int aktualnyPocetRecordov = objectInputStream.readInt();
			int predchodca = objectInputStream.readInt();
			int nasledovnik = objectInputStream.readInt();
			ArrayList<T> records = new ArrayList<>();

			// vytvor novu instanciu IRecord
			IRecord novaInstancia = classType.getDeclaredConstructor().newInstance().dajObjekt();

			for (int i = 0; i < aktualnyPocetRecordov; i++) {
				byte[] recordData = new byte[novaInstancia.getSize()];
				IRecord record = novaInstancia.fromByteArray(recordData);
				records.add((T) record);
			}

			return new Blok<>(aktualnyPocetRecordov, records, predchodca, nasledovnik);

		} catch (IOException e) {
			throw new RuntimeException("Error deserializing Blok from byte array: " + e.getMessage());
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public IRecord findRecord(IRecord record) {
		for (IRecord r : records) {
			if (r.equals(record)) {
				return r;
			}
		}

		return null;
	}
}

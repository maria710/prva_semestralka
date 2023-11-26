package com.aus.prva_semestralka.objekty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class Blok<T extends IRecord> {

	private final ArrayList<T> records;
	private int predchodca;
	private int nasledovnik;
	private int aktualnyPocetRecordov;
	private int index;
	private Class<T> classType;

	public Blok() {
		records = new ArrayList<>();
	}

	public Blok(Class<T> classType) {
		this.classType = classType;
		records = new ArrayList<>();
	}

	public Blok(int aktualnyPocetRecordov, ArrayList<T> records, int predchodca, int nasledovnik, Class<T> classType) {
		this.records = records;
		this.aktualnyPocetRecordov = aktualnyPocetRecordov;
		this.predchodca = predchodca;
		this.nasledovnik = nasledovnik;
		this.classType = classType;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
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

	public List<T> getRecords() {
		return records;
	}

	public int getAktualnyPocetRecordov() {
		return this.aktualnyPocetRecordov;
	}

	public void pridaj(T data) {
		aktualnyPocetRecordov++;
		records.add(data);
	}

	public IRecord najdiZaznam(IRecord record, int pocetBitov) {
		for (IRecord r : records) {
			if (r.equals(record, pocetBitov)) {
				return r;
			}
		}
		return null;
	}

	public void vymazRecord(T IRecord) {
		records.remove(IRecord);
	}

	public int getSize(int blokovaciFaktor) {
		int recordSize;
		try {
			T novaIntancia = classType.getDeclaredConstructor().newInstance();
			recordSize = novaIntancia.getSize();
		} catch (Exception e) {
			throw new RuntimeException("Chyba pri vytrvarani novej instancie " + classType.getName(), e);
		}

		return ((3 * Integer.SIZE) / 8) + blokovaciFaktor * recordSize + (blokovaciFaktor * Integer.SIZE / 8);
	}

	public byte[] toByteArray(int blokovaciFaktor) {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 DataOutputStream objectOutputStream = new DataOutputStream(byteArrayOutputStream)) {

			objectOutputStream.writeInt(aktualnyPocetRecordov);
			objectOutputStream.writeInt(predchodca);
			objectOutputStream.writeInt(nasledovnik);

			for (int i = 0; i < blokovaciFaktor; i++) {
				if (i < aktualnyPocetRecordov) {
					objectOutputStream.writeInt(1); // mame zaznam
					objectOutputStream.write(records.get(i).toByteArray());
				} else {
					objectOutputStream.writeInt(-1); // nemame ziadny zaznam, doplnime prazdne miesto
					IRecord novaIntancia = classType.getDeclaredConstructor().newInstance().dajObjekt();
					objectOutputStream.write(new byte[novaIntancia.getSize()]);
				}
			}

			return byteArrayOutputStream.toByteArray();

		} catch (IOException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Chyba pri serializacii objektu: " + this + " do pola bajtov. ERROR:" + e.getMessage());
		}
	}

	public Blok<T> fromByteArray(byte[] data) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			 DataInputStream objectInputStream = new DataInputStream(byteArrayInputStream)) {

			int aktualnyPocetRecordov = objectInputStream.readInt();
			int predchodca = objectInputStream.readInt();
			int nasledovnik = objectInputStream.readInt();

			ArrayList<T> records = new ArrayList<>();
			T novaInstancia = classType.getDeclaredConstructor().newInstance();

			for (int i = 0; i < aktualnyPocetRecordov; i++) {
				int zaznamFlag = objectInputStream.readInt();
				if (zaznamFlag == 1) {
					byte[] recordData = new byte[novaInstancia.getSize()];
					objectInputStream.readFully(recordData);
					IRecord record = novaInstancia.fromByteArray(recordData);
					records.add((T) record);
				} else {
					objectInputStream.skipBytes(novaInstancia.getSize());
				}
			}
			return new Blok<>(aktualnyPocetRecordov, records, predchodca, nasledovnik, classType);

		} catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			throw new RuntimeException("Error deserializing Blok from byte array: " + e.getMessage());
		}
	}

	public void print(int index) {
		System.out.print("--------------------------------------------------------------------------------------- \n");
		System.out.println("Blok: " + index);
		System.out.println("Aktualny pocet recordov: " + aktualnyPocetRecordov);
		System.out.println("Predchodca: " + predchodca);
		System.out.println("Nasledovnik: " + nasledovnik);
		System.out.println("Zaznamy:");
		for (T record : records) {
			System.out.println(record.toString() + "\n");
		}
	}

	public void clear() {
		records.clear();
		aktualnyPocetRecordov = 0;
		predchodca = -1;
		nasledovnik = -1;
	}
}

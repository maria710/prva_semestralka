package com.aus.prva_semestralka.objekty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
		records = new ArrayList<>();
	}

	public Blok(int aktualnyPocetRecordov, ArrayList<T> records, int predchodca, int nasledovnik, Class<T> classType) {
		this.records = records;
		this.aktualnyPocetRecordov = aktualnyPocetRecordov;
		this.predchodca = predchodca;
		this.nasledovnik = nasledovnik;
		this.classType = classType;
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

		return ((3 * Integer.SIZE) / 8) + blokovaciFaktor * recordSize + (blokovaciFaktor * Integer.SIZE / 8); // blokovaci faktor * 1 = boolean hodnoty ci je zaznam pritomny
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
					objectOutputStream.writeInt(-1); // nemame ziadny zaznam
					IRecord novaIntancia = classType.getDeclaredConstructor().newInstance().dajObjekt();
					objectOutputStream.write(new byte[novaIntancia.getSize()]);
				}
			}

			return byteArrayOutputStream.toByteArray();

		} catch (IOException e) {
			throw new RuntimeException("Chyba pri serializacii objektu: " + this + " do pola bajtov. ERROR:" + e.getMessage());
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}

	public Blok<T> fromByteArray(byte[] data) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			 DataInputStream objectInputStream = new DataInputStream(byteArrayInputStream)) {

			int aktualnyPocetRecordov = objectInputStream.readInt();
			int predchodca = objectInputStream.readInt();
			int nasledovnik = objectInputStream.readInt();

			ArrayList<T> records = new ArrayList<>();

			for (int i = 0; i < aktualnyPocetRecordov; i++) {
				int mameZaznam = objectInputStream.readInt();
				if (mameZaznam == 1) {
					// Deserialize the record
					byte[] recordData = new byte[classType.getDeclaredConstructor().newInstance().dajObjekt().getSize()];
					objectInputStream.readFully(recordData);
					IRecord record = classType.getDeclaredConstructor().newInstance().dajObjekt().fromByteArray(recordData);
					records.add((T) record);
				} else {
					// Skip the placeholder bytes
					IRecord novaInstancia = classType.getDeclaredConstructor().newInstance().dajObjekt();
					objectInputStream.skipBytes(novaInstancia.getSize());
				}
			}

			return new Blok<>(aktualnyPocetRecordov, records, predchodca, nasledovnik, classType);

		} catch (IOException e) {
			throw new RuntimeException("Error deserializing Blok from byte array: " + e.getMessage());
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}


	private boolean isAllZeroes(byte[] data) {
		for (byte b : data) {
			if (b != 0) {
				return false;
			}
		}
		return true;
	}


	public IRecord findRecord(IRecord record) {
		for (IRecord r : records) {
			if (r.equals(record)) {
				return r;
			}
		}

		return null;
	}

	public void print(int index) {
		System.out.print("--------------------------------------------------------------------------------------- \n");
		System.out.println("Blok: " + index + "\n");
		System.out.println("Aktualny pocet recordov: " + aktualnyPocetRecordov + "\n");
		System.out.println("Predchodca: " + predchodca + "\n");
		System.out.println("Nasledovnik: " + nasledovnik + "\n");
		System.out.println("Zaznamy:");
		for (T record : records) {
			System.out.println(record.toString() + "\n");
		}
		System.out.print("--------------------------------------------------------------------------------------- \n");
	}

	public void clear() {
		records.clear();
		aktualnyPocetRecordov = 0;
		predchodca = -1;
		nasledovnik = -1;
	}
}

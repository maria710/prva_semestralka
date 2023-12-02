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
	private Class<T> classType;
	private int index;

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

	public IRecord najdiZaznam(IRecord record) {
		for (IRecord r : records) {
			if (r.equals(record)) {
				return r;
			}
		}
		return null;
	}

	public boolean vymazRecord(T IRecord) {
		for (int i = 0; i < records.size(); i++) {
			if (records.get(i).equals(IRecord)) {
				records.remove(i);
				aktualnyPocetRecordov--;
				return true;
			}
		}
		return false;
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
			 DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {

			dataOutputStream.writeInt(aktualnyPocetRecordov);
			dataOutputStream.writeInt(predchodca);
			dataOutputStream.writeInt(nasledovnik);

			for (int i = 0; i < blokovaciFaktor; i++) {
				if (i < aktualnyPocetRecordov) {
					dataOutputStream.writeInt(1); // mame zaznam
					dataOutputStream.write(records.get(i).toByteArray());
				} else {
					dataOutputStream.writeInt(-1); // nemame ziadny zaznam, doplnime prazdne miesto
					T novaIntancia = classType.getDeclaredConstructor().newInstance();
					dataOutputStream.write(new byte[novaIntancia.getSize()]);
				}
			}

			return byteArrayOutputStream.toByteArray();

		} catch (IOException | NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException("Chyba pri serializacii triedy Blok:" + e.getMessage());
		}
	}

	public Blok<T> fromByteArray(byte[] data) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			 DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)) {

			int aktualnyPocetRecordov = dataInputStream.readInt();
			int predchodca = dataInputStream.readInt();
			int nasledovnik = dataInputStream.readInt();

			ArrayList<T> records = new ArrayList<>();
			T novaInstancia = classType.getDeclaredConstructor().newInstance();

			for (int i = 0; i < aktualnyPocetRecordov; i++) {
				int zaznamFlag = dataInputStream.readInt();
				if (zaznamFlag == 1) {
					byte[] recordData = new byte[novaInstancia.getSize()];
					dataInputStream.readFully(recordData);
					IRecord record = novaInstancia.fromByteArray(recordData);
					records.add((T) record);
				} else {
					dataInputStream.skipBytes(novaInstancia.getSize());
				}
			}
			return new Blok<>(aktualnyPocetRecordov, records, predchodca, nasledovnik, classType);

		} catch (IOException | InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
			throw new RuntimeException("Chyba pri deserializaci triedy Blok: " + e.getMessage());
		}
	}

	public String print(int index) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("--------------------------------------------------------------------------------------- \n");
		stringBuilder.append("Blok: ").append(index).append("\n");
		stringBuilder.append("Aktualny pocet recordov: ").append(aktualnyPocetRecordov).append("\n");
		stringBuilder.append("Predchodca: ").append(predchodca).append("\n");
		stringBuilder.append("Nasledovnik: ").append(nasledovnik).append("\n");
		stringBuilder.append("Zaznamy: \n");
		for (T record : records) {
			stringBuilder.append(record.toString()).append("\n");
		}

		return stringBuilder.toString();
	}

	public void clear() {
		records.clear();
		aktualnyPocetRecordov = 0;
		predchodca = -1;
		nasledovnik = -1;
		index = -1;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public String printPreplnovaci(int indexPreplnovaciehoBloku) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("PREPLNUJUCI BLOK").append("\n");
		stringBuilder.append(print(indexPreplnovaciehoBloku));

		return stringBuilder.toString();
	}
}

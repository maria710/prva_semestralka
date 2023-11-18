package com.aus.prva_semestralka.objekty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

public class Parcela implements IPozemok {

	private Integer supisneCislo;
	private String popis;
	private List<Nehnutelnost> nehnutelnosti;
	private Ohranicenie gpsPozicie;

	public Parcela(Integer i, String popis, Ohranicenie gpsPozicia1Parcela) {
		this.supisneCislo = i;
		this.popis = popis;
		this.gpsPozicie = gpsPozicia1Parcela;
		this.nehnutelnosti = new ArrayList<>();
	}

	@Override
	public Ohranicenie getGpsSuradnice() {
		return this.gpsPozicie;
	}

	public List<Nehnutelnost> getNehnutelnosti() {
		return this.nehnutelnosti;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Supisne cislo: ")
				.append(supisneCislo)
				.append(", popis: ")
				.append(popis)
				.append(", gps pozicie: ")
				.append(gpsPozicie)
				.append(", nehnutelnosti: ");

		nehnutelnosti.forEach(parcela -> sb.append(parcela.toStringZoznam()));

		return sb.toString();
	}

	@Override
	public Integer getSupisneCislo() {
		return this.supisneCislo;
	}

	@Override
	public String getPopis() {
		return this.popis;
	}

	@Override
	public void setPopis(String popis) {
		this.popis = popis;
	}

	public String toStringZoznam() {
		return "Supisne cislo: " + supisneCislo + ", popis: " + popis + ", gps pozicie: " + gpsPozicie;
	}

	@Override
	public Integer getPrimarnyKluc() {
		return this.supisneCislo;
	}

	@Override
	public Ohranicenie getSekundarnyKluc() {
		return this.gpsPozicie;
	}

	@Override
	public void setData(IData<Integer> data) {
		if (data instanceof Parcela parcela) {
			this.supisneCislo = parcela.supisneCislo;
			this.popis = parcela.popis;
			this.gpsPozicie = parcela.gpsPozicie;
			this.nehnutelnosti = parcela.nehnutelnosti;
		}
	}

	@Override
	public boolean equals(IData<Integer> o) {
		return Objects.equals(o.getPrimarnyKluc(), this.supisneCislo) && gpsPozicie.equalsOhranicenie(o.getSekundarnyKluc());
	}

	@Override
	public boolean equals(IRecord<Integer> o) {
		return o.getHash().equals(this.getHash());
	}

	@Override
	public BitSet getHash() {
		int hash = 17 * (31 + 3 * this.supisneCislo);

		BitSet bitSet = new BitSet(Integer.SIZE);
		for (int i = 0; i < Integer.SIZE; i++) {
			bitSet.set(i, (hash & (1 << i)) != 0);
		}

		return bitSet;
	}

	@Override
	public int getSize() {
		return toByteArray().length; // kolko bajtov sa bude do suboru zapisovat
	}

	@Override
	public Byte[] toByteArray() {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

			// Serialize the object
			objectOutputStream.writeObject(this);

			// Convert the byte array to Byte[]
			byte[] byteArray = byteArrayOutputStream.toByteArray();
			Byte[] byteArrayWrapper = new Byte[byteArray.length];
			for (int i = 0; i < byteArray.length; i++) {
				byteArrayWrapper[i] = byteArray[i];
			}

			return byteArrayWrapper;

		} catch (IOException e) {
			e.printStackTrace(); // Handle the exception appropriately
			return null;
		}
	}

	@Override
	public List<IRecord<Integer>> fromByteArray(Byte[] data) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(toPrimitiveByteArray(data));
			 ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {

			// Deserialize the object
			Object object = objectInputStream.readObject();

			if (object instanceof Parcela) {
				List<Parcela> parcelaList = new ArrayList<>();
				parcelaList.add((Parcela) object);
				return null;
				// return parcelaList;
			} else {
				// Handle the case where the deserialized object is not a Parcela
				return null;
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace(); // Handle the exception appropriately
			return null;
		}
	}

	private byte[] toPrimitiveByteArray(Byte[] byteArrayWrapper) {
		byte[] byteArray = new byte[byteArrayWrapper.length];
		for (int i = 0; i < byteArrayWrapper.length; i++) {
			byteArray[i] = byteArrayWrapper[i];
		}
		return byteArray;
	}
}

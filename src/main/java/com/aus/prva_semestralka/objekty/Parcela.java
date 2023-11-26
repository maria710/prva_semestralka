package com.aus.prva_semestralka.objekty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

public class Parcela implements IPozemok, Serializable, IRecord {

	private Integer supisneCislo;
	private String popis;
	private List<Nehnutelnost> nehnutelnosti;
	private Ohranicenie gpsPozicie;
	@Serial
	private static final long serialVersionUID = 1L;

	public Parcela() {
		this.nehnutelnosti = new ArrayList<>();
	}

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
	public boolean equals(IRecord o) {
		return o.getHash(0).equals(this.getHash(0));
	}

	@Override
	public BitSet getHash(int pocetBitov) {
		int hash = 17 * (31 + 3 * this.supisneCislo);

		BitSet bitSet = new BitSet(Integer.SIZE);
		for (int i = 0; i < pocetBitov; i++) {
			bitSet.set(i, (hash & (1 << i)) != 0);
		}

		return bitSet;
	}

	@Override
	public int getSize() {
		return ((Double.SIZE * 4) / 8) + (Integer.SIZE / 8) + (Properties.POCET_PLATNYCH_ZNAKOV * (Character.SIZE / 8)); // kolko bajtov sa bude do suboru zapisovat
	}

	@Override
	public byte[] toByteArray() {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 DataOutputStream objectOutputStream = new DataOutputStream(byteArrayOutputStream)) {

			String popisNaSerializaciu = convertujString(popis);

			objectOutputStream.writeInt(supisneCislo);
			objectOutputStream.writeChars(popisNaSerializaciu); // zapise string ako pole charov, 1 char su 2 bajty - kvoli UTF-16 Character.SIZE=16
			objectOutputStream.writeDouble(gpsPozicie.getSuradnicaLavyDolny().getX());
			objectOutputStream.writeDouble(gpsPozicie.getSuradnicaLavyDolny().getY());
			objectOutputStream.writeDouble(gpsPozicie.getSuradnicaPravyHorny().getX());
			objectOutputStream.writeDouble(gpsPozicie.getSuradnicaPravyHorny().getY());
			return byteArrayOutputStream.toByteArray();

		} catch (IOException e) {
			throw new RuntimeException("Chyba pri serializacii objektu: " + this + " do pola bajtov. ERROR:" + e.getMessage());
		}
	}

	@Override
	public IRecord fromByteArray(byte[] data) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			 DataInputStream objectInputStream = new DataInputStream(byteArrayInputStream)) {

			Integer supisneCislo = objectInputStream.readInt();
			StringBuilder popisBuilder = new StringBuilder(Properties.POCET_PLATNYCH_ZNAKOV);
			for (int i = 0; i < Properties.POCET_PLATNYCH_ZNAKOV; i++) {
				popisBuilder.append(objectInputStream.readChar());
			}
			String popis = popisBuilder.toString().trim();

			double x1 = objectInputStream.readDouble();
			double y1 = objectInputStream.readDouble();
			double x2 = objectInputStream.readDouble();
			double y2 = objectInputStream.readDouble();

			return new Parcela(supisneCislo, popis, new Ohranicenie(new GpsPozicia("S", "V", x1, y1), new GpsPozicia("S", "V" , x2, y2)));

		} catch (IOException e) {
			throw new RuntimeException("Chyba pri deserializacii objektu z pola bajtov. ERROR:" + e.getMessage());
		}
	}

	@Override
	public Parcela dajObjekt() {
		return new Parcela();
	}

	private String convertujString(String povodnyPopis) {
		if (povodnyPopis.length() > Properties.POCET_PLATNYCH_ZNAKOV) {
			return povodnyPopis.substring(0, Properties.POCET_PLATNYCH_ZNAKOV);
		} else {
			return String.format("%-" + Properties.POCET_PLATNYCH_ZNAKOV + "s", povodnyPopis); // pridame medzery na koniec
		}
	}
}

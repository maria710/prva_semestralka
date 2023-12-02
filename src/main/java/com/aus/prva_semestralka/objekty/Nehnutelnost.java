package com.aus.prva_semestralka.objekty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

public class Nehnutelnost implements IPozemok {

	private Integer supisneCislo;
	private String popis;

	private List<Parcela> parcely;
	private Ohranicenie gpsPozicie;

	public Nehnutelnost() {
		this.parcely = new ArrayList<>();
	}

	public Nehnutelnost(Integer i, String popis, Ohranicenie gpsPozicia1Parcela) {
		this.supisneCislo = i;
		this.popis = popis;
		this.gpsPozicie = gpsPozicia1Parcela;
		this.parcely = new ArrayList<>();
	}

	public List<Parcela> getParcely() {
		return this.parcely;
	}

	@Override
	public Ohranicenie getGpsSuradnice() {
		return this.gpsPozicie;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Supisne cislo: ")
				.append(supisneCislo)
				.append(", popis: ")
				.append(popis)
				.append(", gps pozicie: ")
				.append(gpsPozicie)
				.append(", parcely: ");

		parcely.forEach(parcela -> sb.append(parcela.toStringZoznam()));

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
		if (data instanceof Nehnutelnost nehnutelnost) {
			this.supisneCislo = nehnutelnost.supisneCislo;
			this.popis = nehnutelnost.popis;
			this.gpsPozicie = nehnutelnost.gpsPozicie;
			this.parcely = nehnutelnost.parcely;
		}
	}

	@Override
	public boolean equals(IData<Integer> o) {
		return Objects.equals(o.getPrimarnyKluc(), this.supisneCislo) && gpsPozicie.equalsOhranicenie(o.getSekundarnyKluc());
	}

	@Override
	public boolean equals(IRecord o) {
		return Objects.equals(this.supisneCislo, ((Nehnutelnost) o).supisneCislo);
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
		return (Double.SIZE * 4) / 8 + Integer.SIZE / 8 + Properties.POCET_PLATNYCH_ZNAKOV * (Character.SIZE / 8); // kolko bajtov sa bude do suboru zapisovat
	}

	@Override
	public byte[] toByteArray() {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {

			String popisNaSerializaciu = convertujString(popis);

			dataOutputStream.writeInt(supisneCislo);
			dataOutputStream.writeChars(popisNaSerializaciu); // zapise string ako pole charov, 1 char su 2 bajty - kvoli UTF-16 Character.SIZE=16
			dataOutputStream.writeDouble(gpsPozicie.getSuradnicaLavyDolny().getX());
			dataOutputStream.writeDouble(gpsPozicie.getSuradnicaLavyDolny().getY());
			dataOutputStream.writeDouble(gpsPozicie.getSuradnicaPravyHorny().getX());
			dataOutputStream.writeDouble(gpsPozicie.getSuradnicaPravyHorny().getY());
			return byteArrayOutputStream.toByteArray();

		} catch (IOException e) {
			throw new RuntimeException("Chyba pri serializacii triedy Nehnutelnost:" + e.getMessage());
		}
	}

	@Override
	public IRecord fromByteArray(byte[] data) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			 DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)) {

			Integer supisneCislo = dataInputStream.readInt();

			StringBuilder popisBuilder = new StringBuilder(Properties.POCET_PLATNYCH_ZNAKOV);
			for (int i = 0; i < Properties.POCET_PLATNYCH_ZNAKOV; i++) {
				popisBuilder.append(dataInputStream.readChar());
			}
			String popis = popisBuilder.toString().trim();

			double x1 = dataInputStream.readDouble();
			double y1 = dataInputStream.readDouble();
			double x2 = dataInputStream.readDouble();
			double y2 = dataInputStream.readDouble();

			return new Parcela(supisneCislo, popis, new Ohranicenie(new GpsPozicia("S", "V", x1, y1), new GpsPozicia("S", "V" , x2, y2)));

		} catch (IOException e) {
			throw new RuntimeException("Chyba pri deserializacii triedy Nehnutelnost:" + e.getMessage());
		}
	}

	private String convertujString(String povodnyPopis) {
		if (povodnyPopis.length() > Properties.POCET_PLATNYCH_ZNAKOV) {
			return povodnyPopis.substring(0, Properties.POCET_PLATNYCH_ZNAKOV);
		} else {
			return String.format("%-" + Properties.POCET_PLATNYCH_ZNAKOV + "s", povodnyPopis); // pridame medzery na koniec
		}
	}
}

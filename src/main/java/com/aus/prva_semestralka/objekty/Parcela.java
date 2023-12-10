package com.aus.prva_semestralka.objekty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;

public class Parcela implements IPozemok, Serializable, IRecord {

	private Integer identifikacneCislo;
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

	public Parcela(Integer id, Ohranicenie gpsPozicia1Parcela) {
		this.identifikacneCislo = id;
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

	public void setNehnutelnosti(List<Nehnutelnost> nehnutelnosti) {
		this.nehnutelnosti = nehnutelnosti;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Supisne cislo: ")
				.append(supisneCislo)
				.append(", identifikacne cislo: ")
				.append(identifikacneCislo)
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
		return "Identifikacne cislo:" + identifikacneCislo + ", Supisne cislo: " + supisneCislo + ", popis: " + popis + ", gps pozicie: " + gpsPozicie;
	}

	@Override
	public Integer getPrimarnyKluc() {
		return this.identifikacneCislo;
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
	public void upravParametre(IRecord record) {
		if (record instanceof Parcela parcela) {
			this.supisneCislo = parcela.supisneCislo;
			this.popis = parcela.popis;
			this.gpsPozicie = parcela.gpsPozicie;
			this.nehnutelnosti = parcela.nehnutelnosti;
		}
	}

	@Override
	public boolean equals(IData<Integer> o) {
		return Objects.equals(o.getPrimarnyKluc(), this.identifikacneCislo) && gpsPozicie.equalsOhranicenie(o.getSekundarnyKluc());
	}

	@Override
	public boolean equals(IRecord o) {
		return Objects.equals(this.identifikacneCislo, ((Parcela) o).identifikacneCislo);
	}

	@Override
	public Integer getIdetifikacneCislo() {
		return this.identifikacneCislo;
	}

	public void setIdentifikacneCislo(Integer identifikacneCislo) {
		this.identifikacneCislo = identifikacneCislo;
	}

	@Override
	public BitSet getHash(int pocetBitov) {
		int hash = 17 * (31 + 3 * this.identifikacneCislo);

		BitSet bitSet = new BitSet(pocetBitov);
		for (int i = 0; i < pocetBitov; i++) {
			bitSet.set(i, (hash & (1 << i)) != 0);
		}

		return bitSet;
	}

	@Override
	public int getSize() {
		return ((Double.SIZE * 4) / 8) + (Integer.SIZE / 8) + (Properties.POCET_PLATNYCH_ZNAKOV * (Character.SIZE / 8)) + (5 * (Integer.SIZE / 8)); // kolko bajtov sa bude do suboru zapisovat
	}

	@Override
	public byte[] toByteArray() {
		try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			 DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream)) {

			String popisNaSerializaciu = convertujString(popis);

			dataOutputStream.writeInt(identifikacneCislo);
			dataOutputStream.writeChars(popisNaSerializaciu); // zapise string ako pole charov, 1 char su 2 bajty - kvoli UTF-16 Character.SIZE=16
			dataOutputStream.writeDouble(gpsPozicie.getSuradnicaLavyDolny().getX());
			dataOutputStream.writeDouble(gpsPozicie.getSuradnicaLavyDolny().getY());
			dataOutputStream.writeDouble(gpsPozicie.getSuradnicaPravyHorny().getX());
			dataOutputStream.writeDouble(gpsPozicie.getSuradnicaPravyHorny().getY());

			for (int i = 0; i < 5; i++) {
				if (i < nehnutelnosti.size()) {
					dataOutputStream.writeInt(nehnutelnosti.get(i).getIdetifikacneCislo());
				} else {
					dataOutputStream.writeInt(-1);
				}
			}
			return byteArrayOutputStream.toByteArray();

		} catch (IOException e) {
			throw new RuntimeException("Chyba pri serializacii triedy Parcela:" + e.getMessage());
		}
	}

	@Override
	public IRecord fromByteArray(byte[] data) {
		try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			 DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream)) {

			Integer identifikacneCislo = dataInputStream.readInt();
			StringBuilder popisBuilder = new StringBuilder(Properties.POCET_PLATNYCH_ZNAKOV);
			for (int i = 0; i < Properties.POCET_PLATNYCH_ZNAKOV; i++) {
				popisBuilder.append(dataInputStream.readChar());
			}
			String popis = popisBuilder.toString().trim();

			double x1 = dataInputStream.readDouble();
			double y1 = dataInputStream.readDouble();
			double x2 = dataInputStream.readDouble();
			double y2 = dataInputStream.readDouble();


			ArrayList<Nehnutelnost> nehnutelnosti = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				int identifikacneCisloNehnutelnosti = dataInputStream.readInt();
				if (identifikacneCisloNehnutelnosti != -1) {
					pridajNehnutelnost(identifikacneCisloNehnutelnosti, nehnutelnosti);
				}
			}

			var parcela = new Parcela(null, popis, new Ohranicenie(new GpsPozicia("S", "V", x1, y1), new GpsPozicia("S", "V" , x2, y2)));
			parcela.setNehnutelnosti(nehnutelnosti);
			parcela.setIdentifikacneCislo(identifikacneCislo);
			return parcela;

		} catch (IOException e) {
			throw new RuntimeException("Chyba pri deserializacii triedy Parcela" + e.getMessage());
		}
	}

	private void pridajNehnutelnost(int identifikacneCisloNehnutelnosti, ArrayList<Nehnutelnost> nehnutelnosti) {
		var nehnutelnost = new Nehnutelnost(null, null, null);
		nehnutelnost.setIdentifikacneCislo(identifikacneCisloNehnutelnosti);
		nehnutelnosti.add(nehnutelnost);
	}

	private String convertujString(String povodnyPopis) {
		if (povodnyPopis.length() > Properties.POCET_PLATNYCH_ZNAKOV) {
			return povodnyPopis.substring(0, Properties.POCET_PLATNYCH_ZNAKOV);
		} else {
			return String.format("%-" + Properties.POCET_PLATNYCH_ZNAKOV + "s", povodnyPopis); // pridame medzery na koniec
		}
	}
}

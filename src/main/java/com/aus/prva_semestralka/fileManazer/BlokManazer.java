package com.aus.prva_semestralka.fileManazer;

import com.aus.prva_semestralka.objekty.Blok;
import com.aus.prva_semestralka.objekty.IRecord;

public class BlokManazer<T extends IRecord> {

	private final Class<T> classType;
	private final FileManazer fileManazer;
	private final int blokovaciFaktor;
	private int prvyVolnyBlokIndex;
	private int pocetBlokov = 0;

	public BlokManazer(Class<T> classType, FileManazer fileManazer, int blokovaciFaktor, int prvyVolnyBlokIndex) {
		this.classType = classType;
		this.fileManazer = fileManazer;
		this.blokovaciFaktor = blokovaciFaktor;
		this.prvyVolnyBlokIndex = prvyVolnyBlokIndex;
	}

	public Blok<T> citajBlokZoSuboru(int indexBloku) {
		Blok<T> blok = new Blok<>(classType);
		byte[] data = fileManazer.read(blok.getSize(blokovaciFaktor), indexBloku * blok.getSize(blokovaciFaktor));
		var blokReturn = blok.fromByteArray(data);
		blokReturn.setIndex(indexBloku);
		return blokReturn;
	}

	public void zapisBlokDoSubor(Blok<T> blok, int indexBloku) {
		byte[] data = blok.toByteArray(blokovaciFaktor);
		fileManazer.write(data, indexBloku * blok.getSize(blokovaciFaktor));
	}

	public int alokujBlok() {

		int index;
		if (prvyVolnyBlokIndex == -1) { // ak nemam ziadny volny blok alokujem na konci
			Blok<T> blok = new Blok<>(classType);
			index = pocetBlokov; // mam 5 blokov od 0..4, ziadny volny tak priradim index 5 novemu bloku
			blok.setNasledovnik(-1);
			blok.setPredchodca(-1);
			pocetBlokov++;
			blok.setIndex(index);

			zapisBlokDoSubor(blok, index); // musime zapisat
			return index;
		}

		// najdem index prveho volneho bloku a vratim ho, do neho zapiseme "novy" blok
		var prvyVolnyBlok = citajBlokZoSuboru(prvyVolnyBlokIndex);
		int indexNasledovnika = prvyVolnyBlok.getNasledovnik();
		if (indexNasledovnika != -1) {
			var nasledovnik = citajBlokZoSuboru(indexNasledovnika);
			nasledovnik.setPredchodca(-1);
			zapisBlokDoSubor(nasledovnik, indexNasledovnika);
		}

		prvyVolnyBlok.setNasledovnik(-1);
		prvyVolnyBlok.setPredchodca(-1);
		zapisBlokDoSubor(prvyVolnyBlok, prvyVolnyBlokIndex);

		index = prvyVolnyBlokIndex;
		prvyVolnyBlokIndex = indexNasledovnika;
		pocetBlokov++;

		return index;
	}
}

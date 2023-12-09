package com.aus.prva_semestralka.fileManazer;

import com.aus.prva_semestralka.objekty.Blok;
import com.aus.prva_semestralka.objekty.IRecord;
import com.aus.prva_semestralka.struktury.TrieNodeExterny;

public class BlokManazer<T extends IRecord> {

	private final Class<T> classType;
	private final FileManazer fileManazer;
	private final int blokovaciFaktor;
	private int prvyVolnyBlokIndex;
	private int pocetBlokov = 0;

	public BlokManazer(Class<T> classType, FileManazer fileManazer, int blokovaciFaktor) {
		this.classType = classType;
		this.fileManazer = fileManazer;
		this.blokovaciFaktor = blokovaciFaktor;
		this.prvyVolnyBlokIndex = - 1;
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

	public void dealokujBlok(int indexBloku) {
		Blok<T> blok = citajBlokZoSuboru(indexBloku);
		blok.clear();
		blok.setPredchodca(-1);

		if (prvyVolnyBlokIndex != -1) {
			Blok<T> prvyVolnyBlok = citajBlokZoSuboru(prvyVolnyBlokIndex);
			prvyVolnyBlok.setPredchodca(indexBloku);
			blok.setNasledovnik(prvyVolnyBlokIndex);
			zapisBlokDoSubor(prvyVolnyBlok, prvyVolnyBlokIndex);
		}
		prvyVolnyBlokIndex = indexBloku;


		zapisBlokDoSubor(blok, indexBloku);

		int pocetBlokovNaOdstranenie = 0;
		var pocetBlokovVSubore = (int) fileManazer.getFileSize()/blok.getSize(blokovaciFaktor);
		if (indexBloku == pocetBlokovVSubore - 1) {
			for (int i = pocetBlokovVSubore - 1; i >= 0; i--) {
				Blok<T> blokNaOdstranenie = citajBlokZoSuboru(i);

				if (blokNaOdstranenie.getAktualnyPocetRecordov() == 0) {
					if (blokNaOdstranenie.getNasledovnik() != -1) {
						// nastav nasledovnika predchodcovi
						Blok<T> nasledovnik = citajBlokZoSuboru(blokNaOdstranenie.getNasledovnik());
						nasledovnik.setPredchodca(blokNaOdstranenie.getPredchodca());
						zapisBlokDoSubor(nasledovnik, blokNaOdstranenie.getNasledovnik());
					}
					if (blokNaOdstranenie.getPredchodca() != -1) {
						Blok<T> predchodca = citajBlokZoSuboru(blokNaOdstranenie.getPredchodca());
						predchodca.setNasledovnik(blokNaOdstranenie.getNasledovnik());
						zapisBlokDoSubor(predchodca, blokNaOdstranenie.getPredchodca());
					}
					if (blokNaOdstranenie.getIndex() == prvyVolnyBlokIndex) {
						prvyVolnyBlokIndex = blokNaOdstranenie.getNasledovnik();
					}
					pocetBlokovNaOdstranenie++;
				} else {
					break;
				}
			}
			this.fileManazer.skratSubor((pocetBlokovVSubore - pocetBlokovNaOdstranenie) * blok.getSize(blokovaciFaktor));
		}
		pocetBlokov--;
	}

	public void zapisDoNovehoBloku(T record, TrieNodeExterny<T> currentNodeExterny) {
		int indexBloku = alokujBlok();
		var blok = citajBlokZoSuboru(indexBloku);
		blok.pridaj(record);
		currentNodeExterny.setIndexBloku(indexBloku);
		currentNodeExterny.zvysPocetRecordov();
		zapisBlokDoSubor(blok, indexBloku);
	}

	public int getVelkostSuboru() {
		Blok<T> blok = new Blok<>(classType);
		return (int) (this.fileManazer.getFileSize() / blok.getSize(blokovaciFaktor));
	}

}

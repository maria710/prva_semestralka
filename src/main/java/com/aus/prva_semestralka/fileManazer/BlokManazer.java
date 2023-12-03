package com.aus.prva_semestralka.fileManazer;

import com.aus.prva_semestralka.objekty.Blok;
import com.aus.prva_semestralka.objekty.IRecord;

public class BlokManazer<T extends IRecord> {

	private final Class<T> classType;
	private final FileManazer fileManazer;
	private final int blokovaciFaktor;

	public BlokManazer(Class<T> classType, FileManazer fileManazer, int blokovaciFaktor) {
		this.classType = classType;
		this.fileManazer = fileManazer;
		this.blokovaciFaktor = blokovaciFaktor;
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
}

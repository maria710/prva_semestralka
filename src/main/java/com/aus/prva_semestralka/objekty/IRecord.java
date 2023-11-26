package com.aus.prva_semestralka.objekty;

import java.util.BitSet;

public interface IRecord {

	boolean equals(IRecord o,  int pocetBitov);
	BitSet getHash(int pocetBitov);
	int getSize();
	byte[] toByteArray();
	IRecord fromByteArray(byte[] data);
	IRecord dajObjekt();
}

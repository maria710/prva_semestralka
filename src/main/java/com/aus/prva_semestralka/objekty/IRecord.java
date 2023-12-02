package com.aus.prva_semestralka.objekty;

import java.util.BitSet;

public interface IRecord {

	boolean equals(IRecord o);
	BitSet getHash(int pocetBitov);
	int getSize();
	byte[] toByteArray();
	IRecord fromByteArray(byte[] data);
}

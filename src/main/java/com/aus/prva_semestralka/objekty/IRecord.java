package com.aus.prva_semestralka.objekty;

import java.util.BitSet;
import java.util.List;

public interface IRecord<T> {

	boolean equals(IRecord<T> o);
	BitSet getHash();
	int getSize();
	Byte[] toByteArray();
	List<IRecord<T>> fromByteArray(Byte[] data);

}

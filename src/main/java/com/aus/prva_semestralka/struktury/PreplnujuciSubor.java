package com.aus.prva_semestralka.struktury;

import java.io.File;

import com.aus.prva_semestralka.fileManazer.FileManazer;
import com.aus.prva_semestralka.objekty.Blok;
import com.aus.prva_semestralka.objekty.IRecord;

public class PreplnujuciSubor<T extends IRecord> {


	private FileManazer fileManazer;
	private Blok<T> prvyVolnyBlok;
	private int pocetBlokov;
//	private final Class<T> classType;



}

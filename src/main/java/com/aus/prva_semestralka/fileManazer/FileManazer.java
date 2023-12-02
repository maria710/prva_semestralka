package com.aus.prva_semestralka.fileManazer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileManazer {

	private final RandomAccessFile file;
	private final Logger logger = Logger.getLogger(FileManazer.class.getName());

	public FileManazer(String path) throws FileNotFoundException {
		this.file = new RandomAccessFile(path, "rw");
	}

	public void close() throws Exception {
		this.file.close();
	}

	public void write(byte[] data, int offset) {
		try {
			file.seek(offset); // presunieme sa na pozadovanu poziciu
			file.write(data);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Nepodarilo sa zapisat do suboru: " + offset/data.length);
		}
	}

	public void write(byte[] data) throws Exception {
		this.file.write(data);
	}

	public byte[] read(int size, int offset) {
		try {
			file.seek(offset); // presunieme sa na pozadovanu poziciu

			byte[] buffer = new byte[size];
			int pocetPrecitanychBytov = file.read(buffer);

			if (pocetPrecitanychBytov != -1) { // -1 znamena EOF
				return buffer;
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Nepodarilo sa citat zo suboru na index: " + offset/size);
		}

		return null;
	}

	public void clear() {
		try {
			file.setLength(0);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Nepodarilo sa vycistit subor: " + Arrays.toString(e.getStackTrace()));
		}
	}

	public long getFileSize() {
		try {
			return file.length();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Nepodarilo sa ziskat velkost suboru: " + Arrays.toString(e.getStackTrace()));
		}
		return 0;
	}

	public void skratSubor(int i) {
		try {
			file.setLength(i);
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Nepodarilo sa skratit subor: " + Arrays.toString(e.getStackTrace()));
		}
	}
}

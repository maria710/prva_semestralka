package com.aus.prva_semestralka.fileManazer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.aus.prva_semestralka.objekty.Nehnutelnost;
import com.aus.prva_semestralka.objekty.Parcela;
import com.aus.prva_semestralka.struktury.DynamickeHashovanie;
import com.aus.prva_semestralka.struktury.TrieNode;
import com.aus.prva_semestralka.struktury.TrieNodeExterny;
import com.aus.prva_semestralka.struktury.TrieNodeInterny;

public class ImporterSubor {

	public DynamickeHashovanie<Parcela> importDynamickeHashovanieParcela(String path) {
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;
			reader.readLine();

			line = reader.readLine();
			String[] data = line.split(";");
			int blokovaciFaktor = Integer.parseInt(data[0]);
			int blokovaciFaktorPreplnovaci = Integer.parseInt(data[1]);
			String pathHlavny = data[2];
			String pathPreplnovaci = data[3];
			int pocetBitov = Integer.parseInt(data[4]);

			DynamickeHashovanie<Parcela> dynamickeHashovanie = new DynamickeHashovanie<>(Parcela.class, blokovaciFaktor, pathHlavny, pathPreplnovaci, blokovaciFaktorPreplnovaci, pocetBitov);

			while ((line = reader.readLine()) != null) {
				data = line.split(";");
				String key = data[0];
				int indexBloku = Integer.parseInt(data[1]);
				int pocetRecordov = Integer.parseInt(data[2]);
				int pocetBlokovVZretazeni = Integer.parseInt(data[3]);

				TrieNodeExterny<Parcela> trieNodeExterny = new TrieNodeExterny<>(indexBloku, pocetRecordov, pocetBlokovVZretazeni);
				TrieNode<Parcela> currentNode = dynamickeHashovanie.getRoot();
				for (int i = 0; i < key.length(); i++) {
					if ( key.length() - 1 == i && currentNode instanceof TrieNodeInterny<Parcela> interny) {
						if (key.charAt(i) == '0') {
							interny.setLavySyn(trieNodeExterny);
							trieNodeExterny.setParent(interny);
						} else {
							interny.setPravySyn(trieNodeExterny);
							trieNodeExterny.setParent(interny);
						}
					} else if (currentNode instanceof TrieNodeExterny<Parcela> externy){
						currentNode = dynamickeHashovanie.vytvorNovyInternyNode(externy);
					}
				}
			}
			return dynamickeHashovanie;
		} catch (IOException e) {
			throw new RuntimeException("Chyba pri citani zo suboru: " + path);
		}

	}

	public DynamickeHashovanie<Nehnutelnost> importDynamickeHashovanieNehnutelnosti(String path) {
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;
			reader.readLine();

			line = reader.readLine();
			String[] data = line.split(";");
			int blokovaciFaktor = Integer.parseInt(data[0]);
			int blokovaciFaktorPreplnovaci = Integer.parseInt(data[1]);
			String pathHlavny = data[2];
			String pathPreplnovaci = data[3];
			int pocetBitov = Integer.parseInt(data[4]);

			DynamickeHashovanie<Nehnutelnost> dynamickeHashovanie = new DynamickeHashovanie<>(Nehnutelnost.class, blokovaciFaktor, pathHlavny, pathPreplnovaci, blokovaciFaktorPreplnovaci, pocetBitov);

			while ((line = reader.readLine()) != null) {
				data = line.split(";");
				String key = data[0];
				int indexBloku = Integer.parseInt(data[1]);
				int pocetRecordov = Integer.parseInt(data[2]);
				int pocetBlokovVZretazeni = Integer.parseInt(data[3]);

				TrieNodeExterny<Nehnutelnost> trieNodeExterny = new TrieNodeExterny<>(indexBloku, pocetRecordov, pocetBlokovVZretazeni);
				TrieNode<Nehnutelnost> currentNode = dynamickeHashovanie.getRoot();
				for (int i = 0; i <= key.length(); i++) {
					if ( key.length() == i) {
						TrieNodeInterny<Nehnutelnost> parent = (TrieNodeInterny<Nehnutelnost>) currentNode.getParent();
						if (key.charAt(i - 1) == '0') {
							parent.setLavySyn(trieNodeExterny);
							trieNodeExterny.setParent(parent);
							break;
						} else {
							parent.setPravySyn(trieNodeExterny);
							trieNodeExterny.setParent(parent);
							break;
						}
					} else if (currentNode instanceof TrieNodeExterny<Nehnutelnost> externy){
						currentNode = dynamickeHashovanie.vytvorNovyInternyNode(externy);
					}
					if (key.charAt(i) == '0') {
						currentNode = ((TrieNodeInterny<Nehnutelnost>) currentNode).getLavySyn();
					} else {
						currentNode = ((TrieNodeInterny<Nehnutelnost>) currentNode).getPravySyn();
					}
				}
			}
			return dynamickeHashovanie;
		} catch (IOException e) {
			throw new RuntimeException("Chyba pri citani zo suboru: " + path);
		}

	}
}
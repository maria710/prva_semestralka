package com.aus.prva_semestralka.fileManazer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.aus.prva_semestralka.objekty.TrieNodeMap;

public class ExporterSubor<T> {

	public void exportDynamickeHashovanie(int blokovaciFaktor, int blokovaciFaktorPreplnovaci, String pathHlavny, String pathPreplnovaci, String path,
												 int pocetBitov, List<TrieNodeMap<T>> trieNodes) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
			writer.write("BFHlavny;BFPreplnovaci;CestaHlavny;CestaPreplnujuci;PocetBitov\n"); // prvy riadok v exceli

			writer.write(blokovaciFaktor + ";" + blokovaciFaktorPreplnovaci + ";" + pathHlavny + ";" + pathPreplnovaci + ";" + pocetBitov + "\n");

			trieNodes.forEach(node -> {
				try {
					for (int i = 0; i < pocetBitov; i++) {
						if (node.getKey().get(i)) {
							writer.write("1;");
						} else {
							writer.write("0;");
						}

					}
					writer.write("\n");
					writer.write(node.getNode().getIndexBloku() + ";" + node.getNode().getPocetRecordov() + ";" + node.getNode().getPocetBlokovVZretazeni() + "\n");
				} catch (IOException e) {
					throw new RuntimeException("Chyba pri zapisovani do suboru: " + path);
				}
			});


		} catch (IOException e) {
			throw new RuntimeException("Chyba pri zapisovani do suboru: " + path);
		}
	}
}

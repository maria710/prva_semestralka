package com.aus.prva_semestralka.fileManazer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.aus.prva_semestralka.objekty.TrieNodeMap;
import com.aus.prva_semestralka.struktury.TrieNodeExterny;

public class ExporterSubor<T> {

	public void exportDynamickeHashovanie(int blokovaciFaktor, int blokovaciFaktorPreplnovaci, String pathHlavny, String pathPreplnovaci, String path,
												 int pocetBitov, List<TrieNodeMap<T>> trieNodes) {

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
			writer.write("BFHlavny;BFPreplnovaci;CestaHlavny;CestaPreplnujuci;PocetBitov\n"); // prvy riadok v exceli

			writer.write(blokovaciFaktor + ";" + blokovaciFaktorPreplnovaci + ";" + "C:\\Users\\mkuruczova\\projects\\aus2\\prva_semestralka\\" + pathHlavny + ";"
								 + "C:\\Users\\mkuruczova\\projects\\aus2\\prva_semestralka\\" + pathPreplnovaci + ";" + pocetBitov + "\n");

			trieNodes.forEach(node -> {
				if (node.getNode() instanceof TrieNodeExterny<T> externy) {
					try {
						writer.write(node.getKey() + ";");
						writer.write(externy.getIndexBloku() + ";" + externy.getPocetRecordov() + ";" + externy.getPocetBlokovVZretazeni() + "\n");
					} catch (IOException e) {
						throw new RuntimeException("Chyba pri zapisovani do suboru: " + path);
					}
				}
			});


		} catch (IOException e) {
			throw new RuntimeException("Chyba pri zapisovani do suboru: " + path);
		}
	}
}

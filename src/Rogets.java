package entitysimilarity;

import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import ca.site.elkb.Head;
import ca.site.elkb.Paragraph;
import ca.site.elkb.RogetELKB;

public class Rogets {
	
	RogetELKB rogets;
	
	public Rogets() {
		rogets = new RogetELKB();
	}
	
	public Set<String> getSynonyms(String word) {
		TreeSet<String> synonyms = new TreeSet<String>();
		ArrayList<int[]> entries = rogets.index.getEntryListNumerical(word);
		for(int[] entry : entries){
			Head h = rogets.text.getHead(entry[4]);
			String partOfSpeech = rogets.index.convertToPOS(entry[5]);
			if(partOfSpeech.equals("N.")){
				Paragraph p = h.getPara(entry[6]-1, partOfSpeech);
				for (String synonym : p.getAllWordList()) {
					synonyms.add(synonym.trim().toLowerCase());
				}
			}
		}
		return synonyms;
	}
	
	public int getSimilarity(String word1, String word2){
		ArrayList<int[]> list1 = rogets.index.getEntryListNumerical(word1);
		ArrayList<int[]> list2 = rogets.index.getEntryListNumerical(word2);
		if(list1.size() == 0 || list2.size() == 0){
			return 0;
		}
		int best = 0;
		for (int i = 0; i < list1.size(); i++) {
			int[] entry1 = list1.get(i);
			for (int j = 0; j < list2.size(); j++) {
				int[] entry2 = list2.get(j);
				int diff = 16;
				for (int k = 0; k < 8; k++) {
					if (entry1[k] != entry2[k]){
						if(2*k < diff){
							diff = 2*k;
						}
					}
				}
				if(best < diff){
					best = diff;
				}
			}
		}
		return best;
	}
}

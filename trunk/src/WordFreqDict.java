package entitysimilarity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.tartarus.snowball.ext.porterStemmer;

public class WordFreqDict {
	private HashMap<String, Long> wordFreqDict;
	
	public WordFreqDict(String fileName, StopwordDict stopwordDict, Integer minFreq) {
		wordFreqDict = new HashMap<String, Long>();
		String dictFileStr = FileUtil.ReadFileContent(fileName);
		porterStemmer stemmer = new porterStemmer();
		for (String line : dictFileStr.split("\n")) {
			String[] parts = line.split(":");
			Long freq = Long.parseLong(parts[1].trim());
			if (freq < minFreq) {
				break;
			}
			String wordStr = parts[0].trim();
			String[] wordParts = wordStr.split(" ");
			String actualWord = "";
			for (int i = 0; i < wordParts.length; i++) {
				String[] curPart = wordParts[i].trim().toLowerCase().split("/");

				if (curPart[0].trim().length() == 0) {
					continue;
				}
				actualWord = actualWord + curPart[0].trim().toLowerCase();
				if (i < wordParts.length - 1) {
					actualWord += " ";
				}
			}
			actualWord = actualWord.trim().toLowerCase();
			stemmer.setCurrent(actualWord);
			stemmer.stem();
			String stemmed = stemmer.getCurrent();
			if (stopwordDict.isStopWord(stemmed)) {
				continue;
			}
			Long oldFreq = wordFreqDict.get(stemmed);
			if (oldFreq != null) {
				wordFreqDict.put(actualWord, oldFreq + freq);
			} else {
				wordFreqDict.put(actualWord, freq);
			}
		}
	}
	
	// Filter from the passed set, the ones that does not occur in the corpus. 
	public void filterWords(Set<String> words) {
		List<String> absent = new ArrayList<String>();
		for (String word : words) {
			word = word.toLowerCase().replace("_", " ");
			if (!wordFreqDict.containsKey(word)) {
				absent.add(word);
			}
		}
		
		// Remove the words not in the corpus.
		for (String word : absent) {
			words.remove(word);
		}
	}
	
	// Return the log of the frequency.
	public double getFrequency(String word) {
		Long freq = wordFreqDict.get(word.toLowerCase().replace("_", " "));
		if (freq != null) {
			return Math.log((double)freq);
		}
		return 0.0;
	}
	
	// Membership testing.
	public boolean hasWord(String word) {
		return wordFreqDict.containsKey(word.toLowerCase().replace("_", " "));
	}
}
package entitysimilarity;

import java.util.HashSet;

import org.tartarus.snowball.ext.porterStemmer;

public class StopwordDict {
	private HashSet<String> stopwordSet;
	private porterStemmer stemmer;
	
	public StopwordDict(String fileName) {
		stopwordSet = new HashSet<String>();
		stemmer = new porterStemmer();
		String stopwordStrings = FileUtil.ReadFileContent(fileName);
		for (String stopword : stopwordStrings.split("\n")) {
			stemmer.setCurrent(stopword);
			stemmer.stem();
			stopwordSet.add(stemmer.getCurrent());
		}
	}
	
	public boolean isStopWord(String word) {
		stemmer.setCurrent(word);
		stemmer.stem();
		String stemmed = stemmer.getCurrent();
		return stopwordSet.contains(stemmed);
	}
}

package entitysimilarity;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import edu.sussex.nlp.jws.Lin;

import net.didion.jwnl.JWNLException;

public class ThesaurusDict {
	HashMap<String, Set<String>> thesaurus;
	WordnetHelper wnHelper;
	
	public ThesaurusDict(String fileName) throws FileNotFoundException, JWNLException {
		thesaurus = new HashMap<String, Set<String>>();
		wnHelper = WordnetHelper.getHelper(); 
		String thesaurusStr = FileUtil.ReadFileContent(fileName);
		String[] lines = thesaurusStr.split("\n");
		for (String line : lines) {
			String[] words = line.split(",");
			String parent = words[0].toLowerCase();
			Set<String> synonyms = thesaurus.get(parent);
			if (synonyms == null) {
				synonyms = new TreeSet<String>();
			}
			for (int idx = 1; idx < words.length; ++idx) {
				String word = words[idx].trim().toLowerCase().replace("_", " ");
				if (word.length() == 0) {
					continue;
				}
				synonyms.add(word);
			}
			thesaurus.put(parent, synonyms);
		}
	}
	
	public Set<String> getSynonyms(String word) throws FileNotFoundException, JWNLException {
		Set<String> retSyns = new TreeSet<String>();
		Set<String> syns = thesaurus.get(word.toLowerCase().replace("_", " "));
		if (syns == null) {
			return retSyns;
		}
		retSyns.addAll(wnHelper.getSimilarWords(word, syns, WordnetTopicDumper.SIM_THRESHOLD));
		return retSyns;
	}
}
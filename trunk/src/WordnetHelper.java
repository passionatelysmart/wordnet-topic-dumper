package entitysimilarity;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.Pointer;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.Dictionary;
import edu.sussex.nlp.jws.JWS;
import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Path;

public class WordnetHelper {
	private static WordnetHelper wnHelper;
	private JWS wnSim;
	private Dictionary dictionary;
	private PointerUtils ptUtils;
	private WordFreqDict wfDict;
	
	// Factory method to get a wordnet helper instance.
	public static WordnetHelper getHelper() throws FileNotFoundException, JWNLException {
		if (wnHelper == null) {
			wnHelper = new WordnetHelper();
		}
		return wnHelper;
	}
	
	// Private constructor.
	private WordnetHelper() throws FileNotFoundException, JWNLException {
		String wordnetDir = "C:/Users/rohith/Downloads/WordNet-3.0";
		wnSim = new JWS(wordnetDir, "3.0");
		JWNL.initialize(new FileInputStream(
				"C:\\Users\\rohith\\Downloads\\jwnl14-rc2\\jwnl14-rc2\\config\\file_properties.xml"));
		dictionary = Dictionary.getInstance();
		ptUtils = PointerUtils.getInstance();
	}
	
	public void setWordFreqDict(WordFreqDict wfDict) {
		this.wfDict = wfDict;
	}
	
	// Return the similarity root object.
	public JWS getSim() {
		return wnSim;
	}
	
	// Return the senseId of a particular word.
	public int getSenseIdx(Word word) throws JWNLException {
		IndexWord indexWord = Dictionary.getInstance().getIndexWord(
				word.getPOS(), word.getLemma());
		Synset[] synsets = indexWord.getSenses();
		int i = 1;
		for (Synset synset : synsets) {
			if (synset == word.getSynset()) {
				return i;
			} else {
				++i;
			}
		}
		return i;
	}

	// Returns the number of senses for a given word.
	public int getNumSenses(Word word) throws JWNLException {
		IndexWord indexWord = Dictionary.getInstance().getIndexWord(
				word.getPOS(), word.getLemma());
		if (indexWord == null) {
			return 0;
		}
		return indexWord.getSenseCount();
	}
	
	// Returns the number of senses for a given word and pos.
	public int getNumSenses(String word, POS pos) throws JWNLException {
		IndexWord indexWord = Dictionary.getInstance().getIndexWord(
				pos, word);
		if (indexWord == null) {
			return 0;
		}
		return indexWord.getSenseCount();
	}
	
	// Return words from PointerTargetNodeList
	public Set<String> getWordsFromPtrNodeList(PointerTargetNodeList list) throws JWNLException {
		Set<String> words = new TreeSet<String>();
		if (list == null) {
			return words;
		}
		for (int i = 0; i < list.size(); ++i) {
			PointerTargetNode pt = (PointerTargetNode) list.get(i);
			if (pt != null) {
				Word word = pt.getWord();
				if (word != null && wfDict.hasWord(word.getLemma())) {
					if (getNumSenses(word) > WordnetTopicDumper.SENSE_THRESHOLD) {
						continue;
					}
					words.add(word.getLemma());
				}
			}
		}
		return words;
	}
	
	// Given a set of synWords, get the domain category words from them.
	public Set<String> getWordsFromDomainType(Set<ComparableWord> synWords, PointerType type) throws JWNLException {
		Set<String> words = new TreeSet<String>();
		for (ComparableWord curWord : synWords) {
			Synset synset = curWord.getSynset();
			Pointer[] pointerArr = synset.getPointers(type);
			for (Pointer pointer : pointerArr) {
				Synset curSet = pointer.getTargetSynset();
				Word[] allWords = curSet.getWords();
				for (Word word : allWords) {
					if (getNumSenses(word) > WordnetTopicDumper.SENSE_THRESHOLD) {
						continue;
					}
					if (wfDict == null || wfDict.hasWord(word.getLemma())) {
						words.add(word.getLemma());
					}
				}
			}
		}
		return words;
	}
	
	// Given a synset, return all associated words to this synset.
	public Set<String> getAssociatedWords(Synset synset)
			throws JWNLException {
		Set<String> words = new TreeSet<String>();
		PointerUtils ptUtils = PointerUtils.getInstance();
		// Get all synonyms.
		Word[] allWords = synset.getWords();
		for (Word word : allWords) {
			if (getNumSenses(word) > WordnetTopicDumper.SENSE_THRESHOLD) {
				continue;
			}
			if (wfDict == null || wfDict.hasWord(word.getLemma())) {
				words.add(word.getLemma());
			}
		}
		
		// Also sees.
		words.addAll(getWordsFromPtrNodeList(ptUtils.getAlsoSees(synset)));
		// Get synonyms.
		words.addAll(getWordsFromPtrNodeList(ptUtils.getSynonyms(synset)));
		return words;
	}
	
	// Get all synsets for a particular word from wordnet.
	public Synset[] getSynsets(Word word) throws JWNLException {
		IndexWord indexWord = dictionary.getIndexWord(word.getPOS(),
				word.getLemma());
		return indexWord.getSenses();
	}
	
	// Get the sysnset with the given string, sense idx and POS
	public Synset getSynset(String word, POS pos, int senseIdx) throws JWNLException {
		IndexWord indexWord = dictionary.getIndexWord(pos, word);
		return indexWord.getSense(senseIdx);
	}
	
	// Get all synsets for a particular word from wordnet.
	@SuppressWarnings("unchecked")
	public List<Synset> getSynsets(String word) throws JWNLException {
		List<Synset> synsets = new ArrayList<Synset>();
		IndexWord indexWord = dictionary.getIndexWord(POS.NOUN, word);
		if (indexWord == null) {
			return synsets;
		}
		for (Synset synset : indexWord.getSenses()) {
			synsets.add(synset);
		}
		return synsets;
	}
	
	// Get the hyponym synsets of a given synset.
	public List<Synset> getHyponyms(Synset synset) throws JWNLException {
		PointerTargetNodeList hyponyms = ptUtils.getDirectHyponyms(synset);
		List<Synset> synsets = new ArrayList<Synset>();
		for (int i = 0; i < hyponyms.size(); ++i) {
			PointerTargetNode pt = (PointerTargetNode) hyponyms.get(i);
			Synset hyponym = pt.getSynset();
			if (hyponym != null) {
				synsets.add(hyponym);
			}
		}
		return synsets;
	}
	
	private class WordScore implements Comparable<Object> {
		private String word;
		private double score;
		
		public WordScore(String word, double score) {
			super();
			this.word = word;
			this.score = score;
		}

		public String getWord() {
			return word;
		}

		public double getScore() {
			return score;
		}

		@Override
		public int compareTo(Object rhs) {
			WordScore rhsWordScore = (WordScore)rhs;
			if (score == rhsWordScore.score) {
				return word.compareTo(rhsWordScore.getWord());
			} else if (score < rhsWordScore.score) {
				return 1;
			}
			return -1;
		}
	}
	
	// Get similar words from a set of words, given a word.
	public Set<String> getSimilarWords(String word, Set<String> wordSet, double threshold) throws JWNLException {
		Set<String> similarWords = new TreeSet<String>();
		Set<WordScore> wordScoreSet = new TreeSet<WordScore>();
		Path path = wnHelper.getSim().getPath();
		for (String curWord : wordSet) {
			double score = path.max(word, curWord, "n");
			wordScoreSet.add(new WordScore(curWord, score));
		}
		
		for (WordScore ws : wordScoreSet) {
			if (ws.getScore() < threshold) {
				break;
			}
			String curWord = ws.getWord();
			boolean hasWord = true;
			if (wfDict != null) {
				hasWord = wfDict.hasWord(curWord);
			}

			if (hasWord) {
				similarWords.add(curWord);
			}
		}
		return similarWords;
	}
	
	// Get similarity between two words.
	public Double getSimilarity(String word1, String word2) {
		Lin lin = wnSim.getLin();
		return lin.max(word1.toLowerCase().replace("_", " "), 
				word2.toLowerCase().replace("_", " "), "n");
	}
}
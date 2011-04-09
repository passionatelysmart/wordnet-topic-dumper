package entitysimilarity;

import java.awt.peer.SystemTrayPeer;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.sussex.nlp.jws.Lin;
import edu.sussex.nlp.jws.Path;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

public class WordnetTopicDumper {
	public static Double SIM_THRESHOLD = 0.25;
	public static Integer SENSE_THRESHOLD = 2;
	private WordnetHelper wnHelper;
	private WordFreqDict wfDict;
	private ThesaurusDict thesaurus;

	public WordnetTopicDumper(WordnetHelper wnHelper, WordFreqDict wfDict,
			ThesaurusDict thesaurus) {
		super();
		this.wnHelper = wnHelper;
		this.wfDict = wfDict;
		this.thesaurus = thesaurus;
	}

	// Get all synonyms expandable from the given synset.
	public void getAllSynwords(Synset synset, Set<ComparableWord> synWords)
			throws JWNLException {
		Word[] words = synset.getWords();
		List<ComparableWord> newWords = new ArrayList<ComparableWord>();
		for (Word word : words) {
			ComparableWord cw = new ComparableWord(word);
			if (!synWords.contains(cw)) {
				newWords.add(cw);
			}
		}
		synWords.addAll(newWords);
		for (ComparableWord word : newWords) {
			Synset[] synsets = wnHelper.getSynsets(word.getWord());
			for (Synset newSynset : synsets) {
				for (Word newWord : newSynset.getWords()) {
					if (wnHelper.getNumSenses(newWord) > WordnetTopicDumper.SENSE_THRESHOLD) {
						continue;
					}
					synWords.add(new ComparableWord(newWord));
				}
			}
		}
		return;
	}

	// Get all strings under a given synset.
	public void getAllStrings(Synset synset, Set<String> words)
			throws JWNLException {
		Set<ComparableWord> synWords = new TreeSet<ComparableWord>();
		for (Word word : synset.getWords()) {
			if (wnHelper.getNumSenses(word) > WordnetTopicDumper.SENSE_THRESHOLD) {
				continue;
			}
			synWords.add(new ComparableWord(word));
			words.add(word.getLemma());
		}
		words.addAll(wnHelper.getWordsFromDomainType(synWords,
				PointerType.CATEGORY_MEMBER));
		words.addAll(wnHelper.getAssociatedWords(synset));

		List<Synset> hyponyms = wnHelper.getHyponyms(synset);
		for (Synset hyponym : hyponyms) {
			getAllStrings(hyponym, words);
		}
	}

	// Traverse the wordnet tree creating the topics.
	public void getHyponyms(Synset synset, String topicStr)
			throws JWNLException, FileNotFoundException {
		boolean isLeaf = false;
		String currentWord = synset.getWord(0).getLemma();
		String newTopicStr = topicStr + "/" + currentWord;
		List<Synset> hyponyms = wnHelper.getHyponyms(synset);

		Set<ComparableWord> synWords = new TreeSet<ComparableWord>();
		getAllSynwords(synset, synWords);
		
		Set<String> associatedWords = wnHelper.getWordsFromDomainType(synWords,
				PointerType.CATEGORY_MEMBER);
		if (associatedWords.size() >= 0) {
			isLeaf = true;
			
			Set<ComparableWord> tmpSynwords = new TreeSet<ComparableWord>();
			tmpSynwords.addAll(synWords);
			
			// Other synonyms.
			for (ComparableWord tmpSynword : tmpSynwords) {
				String tmpSynwordStr = tmpSynword.getWord().getLemma();
				Set<String> synonyms = thesaurus.getSynonyms(tmpSynwordStr);
				
				for (String synonym : synonyms) {
					List<Synset> otherSyns = wnHelper.getSynsets(synonym);
					
					for (Synset otherSyn : otherSyns) {
						getAllStrings(otherSyn, associatedWords);
						/*
						for (Word otherSynWord : otherSyn.getWords()) {
							synWords.add(new ComparableWord(otherSynWord));associatedWords.add(otherSynWord.getLemma());
						}
						*/
					}
				}
			}
		}

		for (ComparableWord newWord : synWords) {
			getAllStrings(newWord.getSynset(), associatedWords);
		}

		// Filter the words not in the corpus.
		if (wfDict != null) {
			wfDict.filterWords(associatedWords);
		}

		if (associatedWords.size() < 5) {
			isLeaf = false;
		}

		if (isLeaf) {
			String allWords = new String();
			for (String word : associatedWords) {
				//double simScore = wnHelper.getSimilarity(currentWord, word);
				//if (simScore >= 0.1) {
					double freq = 1.0;
					if (wfDict != null) {
						freq = wfDict.getFrequency(word);
					}
					if (freq > 0.0) {
						allWords = allWords + word.toLowerCase().replace("_", " ") + "~"
							+  freq /* * simScore*/ + ",";
					}
				//}
			}
			if (allWords.length() > 0) {
				System.out.println(newTopicStr + ":" + allWords);
			}
		}

		for (Synset hyponym : hyponyms) {
			getHyponyms(hyponym, newTopicStr);
		}
	}

	// Dump all topics under this synset.
	public void DumpTopics(Synset synset) throws JWNLException, FileNotFoundException {
		getHyponyms(synset, "");
	}

	public static void main(String[] args) throws FileNotFoundException,
			JWNLException {
		
		
		System.err.println("Loading Stopwords.");
		StopwordDict swDict = new StopwordDict(args[3]);
		System.err.println("Loading Word frequencies.");
		WordFreqDict wfDict = new WordFreqDict(args[2], swDict, 100);
		//WordFreqDict wfDict = null;
		System.err.println("Initializing Wordnet.");
		WordnetHelper wnHelper = WordnetHelper.getHelper();
		wnHelper.setWordFreqDict(wfDict);
		
		
		System.err.println("Loading Thesaurus.");
		ThesaurusDict thesaurus = new ThesaurusDict(args[4]);
	
		/*
		Set<String> syns = thesaurus.getSynonyms("computer_science");
		for (String syn : syns) {
			System.out.println(syn);
		}
		*/
		
		Synset synset = wnHelper.getSynset(args[0], POS.NOUN, Integer.parseInt(args[1]));
		WordnetTopicDumper topicDumper = new WordnetTopicDumper(wnHelper,
				wfDict, thesaurus);

		System.err.println("Starting dump.");
		topicDumper.DumpTopics(synset);
		
	}
}
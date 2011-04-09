package entitysimilarity;

import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;

@SuppressWarnings("rawtypes")
public class ComparableWord implements Comparable {
	private Word word;

	public ComparableWord(Word word) {
		this.word = word;
	}

	public Word getWord() {
		return this.word;
	}

	public Synset getSynset() {
		return this.word.getSynset();
	}

	@Override
	public int compareTo(Object rhs) {
		ComparableWord rhsWord = (ComparableWord) rhs;
		return this.word.getLemma().compareTo(rhsWord.getWord().getLemma());
	}
}

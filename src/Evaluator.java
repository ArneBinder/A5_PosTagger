/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 21.01.13
 * Time: 21:44
 * To change this template use File | Settings | File Templates.
 */
public class Evaluator {
	int fp;
	int fn;
	int tp;
	int tn;

	public double getFMeasure(Corpus correctCorpus, Corpus testCorpus) {
		fp = 0;
		fn = 0;
		tp = 0;
		tn = 0;
		assert correctCorpus.size() == testCorpus.size() : "Corpus size does not match.";
		for (int i = 0; i < correctCorpus.size(); i++) {
			checkSentences(correctCorpus.getSentence(i), testCorpus.getSentence(i));
		}
		double precision = ((double) tp) / (double) (tp + fp);
		double recall = ((double) tp) / (double) (tp + fn);
		return 2 * precision * recall / (precision + recall);
	}

	private void checkSentences(Sentence correctSentence, Sentence testSentence) {
		assert correctSentence.length() == testSentence.length() : "Sentence length does not match.";

		for (int i = 0; i < correctSentence.length(); i++) {
			if (correctSentence.getTag(i) == testSentence.getTag(i))
				tp++;
			else {
				//TODO: count FN and FP and TN!
			}
		}

	}

}

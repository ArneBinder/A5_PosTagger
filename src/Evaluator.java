import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 21.01.13
 * Time: 21:44
 * To change this template use File | Settings | File Templates.
 */

public class Evaluator {
	//int fp;
	//int fn;
	//int tp;
	int f;
	int t;
	//int tn;



	// TODO: check this!
	public double getFMeasure(Corpus correctCorpus, Corpus testCorpus) {
		//fp = 0;
		//fn = 0;
		//tp = 0;
		f = 0;
		t = 0;
		//tn = 0;
		assert correctCorpus.size() == testCorpus.size() : "Corpus size does not match.";
		for (int i = 0; i < correctCorpus.size(); i++) {
			checkSentences(correctCorpus.getSentence(i), testCorpus.getSentence(i), i, correctCorpus, testCorpus);
		}
		//double precision = ((double) tp) / (double) (tp + fp);
		//double recall = ((double) tp) / (double) (tp + fn);
		//return 2 * precision * recall / (precision + recall);
		//return (double) tp / (double) (tp + fn + fp);
		return (double) t / (double) (t + f);
	}

	public double getSimpleFMeasure(Corpus correctCorpus, Corpus testCorpus) {
		//fp = 0;
		//fn = 0;
		//tp = 0;
		f = 0;
		t = 0;
		//tn = 0;
		//assert correctCorpus.size() == testCorpus.size() : "Corpus size does not match.";
		for (int i = 0; i < testCorpus.size(); i++) {
			checkSentences(correctCorpus.getSentence(i), testCorpus.getSentence(i), i, correctCorpus, testCorpus);
		}
		//double precision = ((double) tp) / (double) (tp + fp);
		//double recall = ((double) tp) / (double) (tp + fn);
		//return 2 * precision * recall / (precision + recall);
		//return (double) tp / (double) (tp + fn + fp);
		return ((double) t) / ((double) (t + f));
	}

	private void checkSentences(Sentence correctSentence, Sentence testSentence, int sentencID, Corpus correctCorpus, Corpus testCorpus) {
		assert correctSentence.length() == testSentence.length() : "Sentence length does not match.";

		for (int i = 0; i < correctSentence.length(); i++) {
			if (correctSentence.getTag(i) == testSentence.getTag(i))
				t++;
			else {
				f++;
				//System.out.println(sentencID +",\t"+i+": \t"+ correctCorpus.getTagSet().tagToString(correctSentence.getTag(i))+"\t"+correctCorpus.getTagSet().tagToString(testSentence.getTag(i)));
				//System.out.println(correctSentence);
				//System.out.println(testSentence);

				}
		}

	}


	public static void main(String[] args) {
		final int partitionCount = 10;
		long startTime = System.currentTimeMillis();
		TagSet tagSet = new TagSet("");
		FeatureExtractor featureExtractor = new FeatureExtractor();
		Corpus corpus = new Corpus(tagSet);
		int i = 0;
		for (String fileName : Helper.getFileList("brown_learn")) {
			//System.out.println(brown_learn);
			corpus.addContentFromFile("brown_learn\\" + fileName, featureExtractor);
			//if(i>100)
			//	break;
			i++;
		}

		long corpusCreated = System.currentTimeMillis();
		System.out.println("corpus created after " + (corpusCreated - startTime) + "ms");
		System.out.println("total size: " + corpus.size() + " sentences.");
		//corpus.writeContentToFile("outTest");
		System.out.println("tagSet.size(): " + tagSet.size());

		System.out.println("construct partition...");
		corpus.constructPartition(partitionCount);

		double fSum = 0;
		List<Double> fMeasures = new ArrayList<Double>();
		for (int j = 0; j < partitionCount; j++) {
			Corpus trainCorpus = corpus.getTrainCorpus(j);
			Corpus evalCorpus = corpus.getEvaluationCorpus(j);

			HMM hmm = new HMM(trainCorpus, 1, tagSet);
			System.out.println("HMM initialized.");
			System.out.println("start training...");
			long startTagging = System.currentTimeMillis();
			hmm.setCorpus(evalCorpus);
			Corpus taggedEvalCorpus = hmm.tag();
			long hmmTagged = System.currentTimeMillis();
			System.out.println("tagging done. " + (hmmTagged - startTagging) + "ms");
			Evaluator evaluator = new Evaluator();
			double fMeasure = evaluator.getFMeasure(corpus.getEvaluationCorpus(j), taggedEvalCorpus);
			fSum += fMeasure;
			System.out.println("F-Measure: " + fMeasure);
		}
		Collections.sort(fMeasures);
		System.out.println(fMeasures);
		double median = fMeasures.get((partitionCount / 2));
		double mean = fSum / partitionCount;
		System.out.println("mean: "+mean);
		System.out.println("median: "+median);

	}

}

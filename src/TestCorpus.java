import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.HashSet;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 24.01.13
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
public class TestCorpus {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		TagSet tagSet = new TagSet("");
		Corpus corpus = new Corpus(tagSet);
		int i = 0;
		for (String fileName : Helper.getFileList("brown_learn")) {
			//System.out.println(brown_learn);
			corpus.addContentFromFile("brown_learn\\" + fileName);
			if(corpus.size() > 40000)
				break;
			//if(i>100)
			//	break;
			i++;
		}

		long corpusCreated = System.currentTimeMillis();
		System.out.println("corpus created after " + (corpusCreated - startTime) + "ms");
		System.out.println("total size: " + corpus.size() + " sentences.");
		//corpus.writeContentToFile("outTest");
		System.out.println("tagSet.size(): "+tagSet.size());


		System.out.println(tagSet);
		TagSet tagSet1 = new TagSet(tagSet.toString());
		System.out.println(tagSet1);


		System.out.println("construct partition...");
		corpus.constructPartition(10);
		Corpus trainCorpus = corpus.getTrainCorpus(9);
		Corpus evalCorpus = corpus.getEvaluationCorpus(9);

		System.out.println("start training...");
		HMM hmm = new HMM(trainCorpus, 2, tagSet);
		System.out.println("HMM initialized.");
		//System.out.println(corpus.t);
		//System.out.println("blub");
		hmm.train();
		long hmmTrained = System.currentTimeMillis();
		System.out.println("hmm trained after " + (hmmTrained - corpusCreated) + "ms");
		hmm.writeModelToFile("model");
		System.out.println("model written to file \"model\".");
		System.out.println("start tagging...");
		hmm.setCorpus(evalCorpus);
		Corpus taggedEvalCorpus = hmm.tag();
		long hmmTagged = System.currentTimeMillis();
		System.out.println("tagging done. " + (hmmTagged - hmmTrained) + "ms");
		Evaluator evaluator = new Evaluator();
		System.out.println("F-Measure: " + evaluator.getFMeasure(corpus.getEvaluationCorpus(9), taggedEvalCorpus));
		//System.out.println("".split("/").length);


	}
}

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
		final String directoryName = "brown_learn";
		TagSet tagSet = new TagSet("");
		Corpus corpus = new Corpus(tagSet);

		for (String fileName : Helper.getFileList(directoryName)) {
			//System.out.println(brown_learn);
			corpus.addContentFromFile(directoryName+"\\" + fileName);
			if(corpus.size() > 45000)
				break;
		}

		long corpusCreated = System.currentTimeMillis();
		System.out.println("corpus created after " + (corpusCreated - startTime) + "ms");
		System.out.println("total size: " + corpus.size() + " sentences.");
		//corpus.writeContentToFile("outTest");
		System.out.println("tagSet.size(): "+tagSet.size());
		System.out.println(tagSet);
		//TagSet t = new TagSet(tagSet.toString());
		//System.out.println(t);

		//System.out.println(tagSet);
		//TagSet tagSet1 = new TagSet(tagSet.toString());
		//System.out.println(tagSet1);


		System.out.println("construct partition...");
		corpus.constructPartition(10);
		Corpus trainCorpus = corpus.getTrainCorpus(9);
		Corpus evalCorpus = corpus.getEvaluationCorpus(9);

		System.out.println("start training...");
		HMM hmm = new HMM(trainCorpus, 1, tagSet);
		System.out.println("HMM initialized.");
		//System.out.println(corpus.t);
		//System.out.println("blub");
		hmm.train();
		long hmmTrained = System.currentTimeMillis();
		System.out.println("hmm trained after " + (hmmTrained - corpusCreated) + "ms");
		hmm.writeModelToFile("model");
		System.out.println("model written to file \"model\".");
		hmm.printTransitionProbs();
		System.out.println();
        /*
		HMM hmm = new HMM("model");
		evalCorpus.writeContentToFile("evalCorpus");
		hmm.setCorpus(evalCorpus);
		Corpus taggedCorpus = hmm.tag();
		taggedCorpus.writeContentToFile("taggedCorpus");
		Evaluator evaluator = new Evaluator();
		System.out.println("FMeasure: "+evaluator.getSimpleFMeasure(evalCorpus, taggedCorpus));
		*/

		//hmm.printEmissionProbs(4);
		/*System.out.println("start tagging...");
		hmm.setCorpus(evalCorpus);
		Corpus taggedEvalCorpus = hmm.tag();
		long hmmTagged = System.currentTimeMillis();
		System.out.println("tagging done. " + (hmmTagged - hmmTrained) + "ms");
		Evaluator evaluator = new Evaluator();
		System.out.println("F-Measure: " + evaluator.getFMeasure(corpus.getEvaluationCorpus(9), taggedEvalCorpus));
		//System.out.println("".split("/").length);
        */

	}
}

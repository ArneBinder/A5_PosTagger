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
		FeatureExtractor featureExtractor = new FeatureExtractor();
		Corpus corpus = new Corpus(tagSet);

		int i = 0;
		System.out.println("fiels to read: "+Helper.getFileList(directoryName).length);
		for (String fileName : Helper.getFileList(directoryName)) {
			//System.out.println(brown_learn);
			System.out.println(i);
			corpus.addContentFromFile(directoryName + "\\" + fileName, featureExtractor);
			if(corpus.size() > 45000)
				break;
			if(i > 97)
				break;
			i++;
		}
		System.out.println("write featureValues to file...");
		featureExtractor.writeFeatureValuesToFile("featureValues");
		System.out.println("done.");
		FeatureExtractor f2 = new FeatureExtractor("featureValues");

		for (int featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
			BidiMap<String, Integer> a = featureExtractor.getFeatureValues(featureIndex);
			BidiMap<String, Integer> b = f2.getFeatureValues(featureIndex);
			System.out.println(a.size()+" "+b.size());
			for (int j = 0; j < a.size(); j++) {
				if(a.getKey(j).equals(b.getKey(j)))
					System.out.println(a.getKey(j)+"!="+b.getKey(j));
			}
		}

		/*long corpusCreated = System.currentTimeMillis();
		System.out.println("corpus created after " + (corpusCreated - startTime) + "ms");
		System.out.println("total size: " + corpus.size() + " sentences.");
		//corpus.writeContentToFile("outTest");
		System.out.println("tagSet.size(): "+tagSet.size());
		System.out.println(tagSet);

		System.out.println("start training...");
		HMM hmm = new HMM(corpus, 1, tagSet);
		System.out.println("HMM initialized.");
		//System.out.println(corpus.t);
		//System.out.println("blub");
		hmm.train();
		long hmmTrained = System.currentTimeMillis();
		System.out.println("hmm trained after " + (hmmTrained - corpusCreated) + "ms");
		hmm.writeModelToFile("model");
		System.out.println("model written to file \"model\".");
		//TagSet t = new TagSet(tagSet.toString());
		//System.out.println(t);

         */
		//System.out.println(tagSet);
		//TagSet tagSet1 = new TagSet(tagSet.toString());
		//System.out.println(tagSet1);


		/*System.out.println("construct partition...");
		corpus.constructPartition(10);
		Corpus trainCorpus = corpus.getTrainCorpus(9);
		Corpus evalCorpus = corpus.getEvaluationCorpus(9);

		for (int featureIndes = 0; featureIndes < FeatureExtractor.featureSize; featureIndes++) {
			char[] a = trainCorpus.getFeatureExtractor().getFeatureValues()[featureIndes];
			char[] b = evalCorpus.getFeatureExtractor().getFeatureValues()[featureIndes];
			System.out.println(a.length+" \t"+b.length);
			for (int valueIndex = 0; valueIndex < a.length; valueIndex++) {
				if(a[valueIndex]!=b[valueIndex])
					System.out.println(a[valueIndex]+"!="+b[valueIndex]);
			}
		}
		*/
		System.out.println("done");

		/*
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
        */

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

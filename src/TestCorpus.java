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
		/*FeatureExtractor featureExtractor = new FeatureExtractor();
		Corpus corpus = new Corpus(tagSet);

		int i = 0;
		System.out.println("fiels to read: " + Helper.getFileList(directoryName).length);
		for (String fileName : Helper.getFileList(directoryName)) {
			//System.out.println(brown_learn);
			System.out.println(i+":\t"+corpus.size()+"\t"+featureExtractor.getFeatureValues().size());
			corpus.addContentFromFile(directoryName + "\\" + fileName, featureExtractor);
			if (corpus.size() > 45000)
				break;
			if (i > 300)
			 	break;
			i++;
		}


		//for (int j = 0; j < FeatureExtractor.featureSize; j++) {
		//	System.out.println(featureExtractor.getFeatureValues(j).size());
		//}
		System.out.println("write featureValues to file...");
		featureExtractor.writeFeatureValuesToFile("featureValues", true);
		//featureExtractor.resetFeatureValues();
		System.out.println("done.");
		long corpusCreated = System.currentTimeMillis();
          */
		/*
		FeatureExtractor f2 = new FeatureExtractor("featureValues");
		//for (int featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
			HashMap<String, Integer> a = featureExtractor.getFeatureValues();
			HashMap<String, Integer> b = f2.getFeatureValues();
			System.out.println(a.size()+" "+b.size());
			for (Map.Entry<String, Integer> entry : a.entrySet()) {
				if(!b.get(entry.getKey()).equals(entry.getValue()))
					System.out.println(b.get(entry.getKey())+"!="+entry.getValue());
			}
		//}
        */

		/*
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


		//System.out.println("construct partition...");
		//corpus.constructPartition(10);
		for (int i = 0; i < 10; i++) {

			FeatureExtractor featureExtractor2 = new FeatureExtractor();
			FeatureExtractor featureExtractor3 = new FeatureExtractor();
			//Corpus trainCorpus = corpus.getTrainCorpus(9);
			Corpus evalCorpus = new Corpus(tagSet);
			evalCorpus.addContentFromFile("evalCorpus"+i, featureExtractor2);
			Corpus taggedCorpus = new Corpus(tagSet);
			taggedCorpus.addContentFromFile("taggedCorpus"+i, featureExtractor3);

			Evaluator evaluator = new Evaluator();
			System.out.println("FMeasure: "+evaluator.getSimpleFMeasure(evalCorpus, taggedCorpus));
		}

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
		//hmm.printTransitionProbs();
		System.out.println();
        */
		                                                               /*
		System.out.println("read model from file...");
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

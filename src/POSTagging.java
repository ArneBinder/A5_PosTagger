import java.io.File;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 31.01.13
 * Time: 17:34
 * To change this template use File | Settings | File Templates.
 */
public class POSTagging {
	public static void main(String[] args) {
		if(args.length!=2){
			System.out.println("wrong arguments. [learn|annotate] <directory_name>");
			return;
		}
		final String modelName = "model";
		final String featureValuesFile = "featureValues";
		String directory_name = args[1];
		long startTime = System.currentTimeMillis();

		if(args[0].equals("learn")){
			TagSet tagSet = new TagSet("");
			FeatureExtractor featureExtractor = new FeatureExtractor();
			Corpus corpus = new Corpus(tagSet);
			for (String fileName : Helper.getFileList(directory_name)) {
				corpus.addContentFromFile(directory_name+File.separator + fileName, featureExtractor);
			}
			int featureValueCount = featureExtractor.getFeatureValueCount();
			featureExtractor.writeFeatureValuesToFile(featureValuesFile, true);
			long corpusCreated = System.currentTimeMillis();
			System.out.println("corpus created in " + (corpusCreated - startTime) + "ms");
			System.out.println("total size: " + corpus.size() + " sentences.");

			HMM hmm = new HMM(corpus, tagSet, featureValueCount);
			System.out.println("HMM initialized.");
			System.out.println("start training...");
			hmm.train();
			long hmmTrained = System.currentTimeMillis();
			System.out.println("training finished in " + (hmmTrained - corpusCreated) + "ms");
			System.out.println("write model to file \""+modelName+"\"...");
			hmm.writeModelToFile(modelName);
			System.out.println("done.");
		}else if(args[0].equals("annotate")){
			System.out.println("read model from file \""+modelName+"\"...");
			FeatureExtractor featureExtractor = new FeatureExtractor(featureValuesFile);
			HMM hmm = new HMM(modelName);

			System.out.println(hmm.getTagSet());
			//hmm.printEmissionProbs(0);
			//hmm.printTransitionProbs();
			for (String fileName : Helper.getFileList(directory_name)) {
				Corpus corpus = new Corpus(hmm.getTagSet());
				System.out.println("read sentences from file \""+directory_name+File.separator+fileName+"\"...");
				corpus.addContentFromFile(directory_name + File.separator + fileName, featureExtractor);
				hmm.setCorpus(corpus);
				System.out.println("tag sentences...");
				Corpus taggedCorpus = hmm.tag();
				Evaluator evaluator = new Evaluator();
				System.out.println("FMeasure: "+evaluator.getFMeasure(corpus, taggedCorpus));
				taggedCorpus.writeContentToFile(directory_name + File.separator + fileName+".pos");
				System.out.println("output written to \""+directory_name + File.separator + fileName+".pos");
			}
			System.out.println("done.");
		} else
			System.out.println("wrong arguments. [learn|annotate] <directory_name>");
	}
}

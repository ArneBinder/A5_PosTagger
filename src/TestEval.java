/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 03.02.13
 * Time: 03:39
 * To change this template use File | Settings | File Templates.
 */
public class TestEval {
	public static void main(String[] args) {
		TagSet tagSet = new TagSet("");
		FeatureExtractor featureExtractor = new FeatureExtractor();
		Corpus evalCorpus = new Corpus(tagSet);
		evalCorpus.addContentFromFile("evalCorpus", featureExtractor);
		Corpus taggedCorpus = new Corpus(tagSet);
		taggedCorpus.addContentFromFile("taggedCorpus", featureExtractor);

		System.out.println(evalCorpus.size());
		System.out.println(taggedCorpus.size());
		Evaluator evaluator = new Evaluator();
		System.out.println(evaluator.getSimpleFMeasure(evalCorpus, taggedCorpus));
	}
}

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 03.02.13
 * Time: 03:39
 * To change this template use File | Settings | File Templates.
 */
public class TestEval {
	public static void main(String[] args) {
		Corpus evalCorpus = new Corpus(new TagSet(""));
		evalCorpus.addContentFromFile("evalCorpus");
		System.out.println(evalCorpus.size());
		Corpus taggedCorpus = new Corpus(new TagSet(""));
		taggedCorpus.addContentFromFile("taggedCorpus");
		System.out.println(taggedCorpus.size());
		Evaluator evaluator = new Evaluator();
		System.out.println(evaluator.getSimpleFMeasure(evalCorpus, taggedCorpus));
	}
}

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 24.01.13
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
public class TestCorpus {
	public static void main(String[] args) {
		TagSet tagSet = new TagSet("");
		Corpus corpus = new Corpus(tagSet);
		corpus.addContentFromFile("brown_learn\\ca01");
		corpus.addContentFromFile("brown_learn\\ca02");
		corpus.addContentFromFile("brown_learn\\ca03");
		corpus.addContentFromFile("brown_learn\\ca04");
		corpus.addContentFromFile("brown_learn\\ca05");
		corpus.addContentFromFile("brown_learn\\ca06");
		corpus.addContentFromFile("brown_learn\\ca07");


		corpus.writeContentToFile("outTest");
		System.out.println("tagSet.size(): "+tagSet.size());
		HMM hmm = new HMM(corpus, 2, tagSet);
		//corpus.constructPartition(10);
		hmm.train();
		//System.out.println("".split("/").length);
		System.out.println();
		System.out.println(tagSet);
		TagSet tagSet1 = new TagSet(tagSet.toString());
		System.out.println(tagSet1);
	}
}

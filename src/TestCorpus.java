/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 24.01.13
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
public class TestCorpus {
	public static void main(String[] args) {
		Corpus corpus = new Corpus();
		corpus.addContentFromFile("brown_learn\\ca01");
		corpus.addContentFromFile("brown_learn\\ca02");
		corpus.addContentFromFile("brown_learn\\ca03");
		corpus.addContentFromFile("brown_learn\\ca04");
		corpus.addContentFromFile("brown_learn\\ca05");
		corpus.addContentFromFile("brown_learn\\ca06");
		corpus.addContentFromFile("brown_learn\\ca07");


		corpus.writeContentToFile("outTest");
		HMM hmm = new HMM();
		//corpus.constructPartition(10);
		hmm.train(corpus, 1);
		//System.out.println("".split("/").length);
	}
}

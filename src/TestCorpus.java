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
		int i =0;
		for (String fileName : Helper.getFileList("brown_learn")) {
			//System.out.println(brown_learn);
			corpus.addContentFromFile("brown_learn\\"+fileName);
			if(i>100)
				break;
			i++;
		}

		/*
		corpus.addContentFromFile("brown_learn\\ca01");
		corpus.addContentFromFile("brown_learn\\ca02");
		corpus.addContentFromFile("brown_learn\\ca03");
		corpus.addContentFromFile("brown_learn\\ca04");
		corpus.addContentFromFile("brown_learn\\ca05");
		corpus.addContentFromFile("brown_learn\\ca06");
		corpus.addContentFromFile("brown_learn\\ca07");
		*/
		long corpusCreated = System.currentTimeMillis();
		System.out.println("corpus created after "+(corpusCreated-startTime) +"ms");
		//corpus.writeContentToFile("outTest");
		//System.out.println("tagSet.size(): "+tagSet.size());
		HMM hmm = new HMM(corpus, 2, tagSet);
		//corpus.constructPartition(10);
		hmm.train();
		long hmmTrained = System.currentTimeMillis();
		System.out.println("hmm trained after "+(hmmTrained-corpusCreated) +"ms");
		//System.out.println("".split("/").length);
		System.out.println();
		System.out.println(tagSet);
		TagSet tagSet1 = new TagSet(tagSet.toString());
		System.out.println(tagSet1);
	}
}

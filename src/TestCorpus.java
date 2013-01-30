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

		// HASH-PAIR-CHECK
		String s1 = "abc";
		String s2 = "bc";
		s2 = "a"+s2;
		byte b1 = 5;
		byte b2 = 5;
		Pair<Byte, String> t1 = new Pair<Byte, String>(b1,s1);
		Pair<Byte, String> t2 = new Pair<Byte, String>(b2,s2);
		HashSet<Pair<Byte, String>> hs = new HashSet<Pair<Byte, String>>();
		hs.add(new Pair<Byte, String>(b1,s1));
		hs.add(new Pair<Byte, String>(b2,s2));
		System.out.println(hs.size());
		//System.out.println(t1.equals(t2));
		Multiset<Pair<Byte, String>> mset = HashMultiset.create();
		mset.add(t1);
		mset.add(t2);

		for (Multiset.Entry<Pair<Byte, String>> entry : mset.entrySet()) {
			System.out.println(entry.getElement()+": "+entry.getCount());
		}

		HashSet<String> hs2 = new HashSet<String>();
		hs2.add(s1);
		hs2.add(s2);
		System.out.println(hs2.size());

		System.out.println("start training...");


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

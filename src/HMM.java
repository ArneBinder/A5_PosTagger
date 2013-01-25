import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 21.01.13
 * Time: 21:30
 * To change this template use File | Settings | File Templates.
 */
public class HMM {
	//////// Features //////////
	/**
	 * - startProbs
	 * - transitionProbs
	 * - emissionProbs
	 */
	//HashMap<Long, Integer> transitionCounts;
	Multiset<Long> transitionCounts;
	int totalTransitions;
	// emissionCounts;
	int totalEmissions;

	//int gramCount;

	//////// Methods ///////////

	/**
	 * read model (from file))
	 */

	/**
	 * write model (to file)
	 */
	public void train(Corpus corpus, int gramCount, TagSet tagSet) {
		assert gramCount <= Helper.maxGramCount : "max qGram size is "+Helper.maxGramCount+".";
		transitionCounts = HashMultiset.create();//new HashMap<Long, Integer>();//(gramCount)
		totalTransitions = 0;
		totalEmissions = 0;

		long tagGram = 0;
		for (Sentence sentence : corpus.getContent()) {
			for (int i = 0; i < sentence.length(); i++) {
				totalTransitions++;

				//System.out.println(Helper.tagGramToString(sentence.getPrevTagsCoded(i,3)));
				tagGram &= Helper.gramMask.get(gramCount);
				tagGram <<= tagSet.tagBoundBitCount;
				tagGram += sentence.getTag(i);
				System.out.println(tagSet.tagGramToString(tagGram) + ": " + tagSet.tagGramToString(sentence.getTag(i)));
				transitionCounts.add(tagGram);
				//if (!transitionCounts.containsKey(tagGram))
				//	transitionCounts.put(tagGram, 1);
				//else
				//	transitionCounts.put(tagGram, transitionCounts.get(tagGram)+1);

				////
				// TODO: Features des aktuellen Wortes extrahieren & (gramTag --> Features) zaehlen
			}
			tagGram = 0;
			System.out.println();
		}

		////DEBUG & STATS
		System.out.println();
		for (Multiset.Entry<Long> entry : transitionCounts.entrySet()) {
			System.out.println(tagSet.tagGramToString(entry.getElement())+": "+entry.getCount());
		}
		//for (Map.Entry<Long, Integer> entry : transitionCounts.entrySet()) {
		//	System.out.println(tagSet.tagGramToString(entry.getKey())+": "+entry.getValue());
		//}
		System.out.println();
		System.out.println("totalTransitions: "+totalTransitions);
		System.out.println("discriminative Transitions: "+ transitionCounts.elementSet().size());

		float mean = (float)totalTransitions/(float) transitionCounts.elementSet().size();
		System.out.println("mean: "+mean);
		//////////////////
	}


}

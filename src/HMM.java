import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
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
	Multiset<ImmutableMap<Long,String>>[] emissionCounts;
	//saves how often a
	Multiset<Long> totalEmissions;

	//int gramCount;

	//////// Methods ///////////

	/**
	 * read model (from file))
	 */

	/**
	 * write model (to file)
	 */
	public void train(Corpus corpus, int gramCount, TagSet tagSet) {
		assert gramCount <= Helper.maxGramCount : "given gram count = "+gramCount+" is bigger than max gram count = "+Helper.maxGramCount+".";
		transitionCounts = HashMultiset.create();//new HashMap<Long, Integer>();//(gramCount)
		totalTransitions = 0;

		//totalEmissions = 0;

		long tagGram = 0;
		for (Sentence sentence : corpus.getContent()) {
			for (int i = 0; i < sentence.length(); i++) {
				totalTransitions++;

				//System.out.println(Helper.tagGramToString(sentence.getPrevTagsCoded(i,3)));
				tagGram &= Helper.gramMask.get(gramCount);
				// --> auch zaehlen fuer normierung der emissionsCounts
				// totalEmissions.add(tagGram)

				tagGram <<= tagSet.tagBoundBitCount;
				tagGram += sentence.getTag(i);
				System.out.println(tagSet.tagGramToString(tagGram) + ": " + tagSet.tagGramToString(sentence.getTag(i)));
				transitionCounts.add(tagGram);





				////
				// Features des aktuellen Wortes extrahieren & (gramTag --> Features) zaehlen
				FeatureExtractor featureExtractor = new FeatureExtractor();
				FeatureVector featureVector = featureExtractor.getFeatures(sentence, i);
				for (int j = 0; j < FeatureVector.size; j++){
					emissionCounts[j].add(ImmutableMap.of(tagGram, featureVector.features[j]));
				}
			}



			tagGram = 0;
			System.out.println();
		}

		//// normieren
		// emissionCounts durch transition counts


		////DEBUG & STATS
		System.out.println();
		for (Multiset.Entry<Long> entry : transitionCounts.entrySet()) {
			System.out.println(tagSet.tagGramToString(entry.getElement())+": "+entry.getCount());
		}

		System.out.println();
		System.out.println("totalTransitions: "+totalTransitions);
		System.out.println("discriminative Transitions: " + transitionCounts.elementSet().size());

		float mean = (float)totalTransitions/(float) transitionCounts.elementSet().size();
		System.out.println("mean: "+mean);
		//////////////////
	}


}

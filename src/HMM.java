import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import java.util.AbstractMap;
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

	Multiset<Long> transitionCounts;
	Multiset<Long> totalTransitions;

	int allTransitionCount;
	// saves for every feature (-->Array) how often a posGram(Long) emits a specific value (String)
	Multiset<Map.Entry<Long,String>>[] emissionCounts;
	//saves how often a posGram total occurs
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
		totalTransitions = HashMultiset.create();
		totalEmissions = HashMultiset.create();
		emissionCounts = new Multiset[FeatureVector.size];
		for (int i = 0; i < emissionCounts.length; i++) {
			emissionCounts[i] = HashMultiset.create();
		}
		//totalEmissions = 0;
		allTransitionCount = 0;
		long tagGram;
		for (Sentence sentence : corpus.getContent()) {
			tagGram = 0;
			for (int i = 0; i < sentence.length(); i++) {
				allTransitionCount++;
				totalTransitions.add(tagGram);
				tagGram <<= tagSet.tagBoundBitCount;
				tagGram += sentence.getTag(i);
				transitionCounts.add(tagGram);

				//System.out.println(Helper.tagGramToString(sentence.getPrevTagsCoded(i,3)));
				tagGram &= Helper.gramMask.get(gramCount);
				// --> auch zaehlen fuer normierung der emissionsCounts
			 	totalEmissions.add(tagGram);

				System.out.println(tagSet.tagGramToString(tagGram) + ": " + tagSet.tagGramToString(sentence.getTag(i)));

				////
				// Features des aktuellen Wortes extrahieren & (gramTag --> Features) zaehlen
				FeatureExtractor featureExtractor = new FeatureExtractor();
				FeatureVector featureVector = featureExtractor.getFeatures(sentence, i);
				for (int j = 0; j < FeatureVector.size; j++){
					//TODO: check, if Pair works correct
					emissionCounts[j].add(new Pair<Long, String>(tagGram, featureVector.features[j]));
				}
			}

			System.out.println();
		}

		//// normieren //////////
		// emissionCounts by totalEmissions
		for (byte i = 0; i < FeatureVector.size; i++) {
			for (Multiset.Entry<Map.Entry<Long, String>> entry : emissionCounts[i].entrySet()) {
				Long posGram = entry.getElement().getKey();
				double logProb = Math.log(entry.getCount()) - Math.log(totalEmissions.count(posGram));
				//int featureIndex = i;

				// --> array aus posTagGram, featureIndex, featureValue, logProb
			}
		}

		// transitionCounts by totalTransitions
		for (Multiset.Entry<Long> entry : transitionCounts.entrySet()) {
			// reconstruct source posGram
			Long posGram = (entry.getElement() >> TagSet.tagBoundBitCount);
			double logProb = Math.log(entry.getCount()) - Math.log(totalTransitions.count(posGram));
			// --> array aus fromPosTagGram, toPosTagGram, logProb

		}


		////////////////////////


		////DEBUG & STATS
		System.out.println();
		for (Multiset.Entry<Long> entry : transitionCounts.entrySet()) {
			System.out.println(tagSet.tagGramToString(entry.getElement())+": "+entry.getCount());
		}

		System.out.println();
		System.out.println("allTransitionCount: "+allTransitionCount);
		System.out.println("discriminative Transitions: " + transitionCounts.elementSet().size());

		float mean = (float)allTransitionCount/(float) transitionCounts.elementSet().size();
		System.out.println("mean: "+mean);
		//////////////////
	}


}

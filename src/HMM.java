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
	HashMap<Long, Double> transitionProbs;

	Corpus corpus;
	int gramCount;
	TagSet tagSet;

	//////// Methods ///////////

	HMM(Corpus corpus, int gramCount, TagSet tagSet) {
		assert gramCount <= Helper.maxGramCount : "given gram count = " + gramCount + " is bigger than max gram count = " + Helper.maxGramCount + ".";
		this.corpus = corpus;
		this.gramCount = gramCount;
		this.tagSet = tagSet;
	}

	/**
	 * read model (from file))
	 */

	/**
	 * write model (to file)
	 */
	public void train() {
		Multiset<Long> transitionCounts = HashMultiset.create();
		Multiset<Long> totalTransitions = HashMultiset.create();
		Multiset<Map.Entry<Long, String>>[] emissionCounts = new Multiset[FeatureVector.size]; // saves for every feature (-->Array) how often a posGram(Long) emits a specific value (String)
		Multiset<Long> totalEmissions = HashMultiset.create(); //saves how often a posGram occurs in total
		int allTransitionCount; //STATS
		for (int i = 0; i < emissionCounts.length; i++) {
			emissionCounts[i] = HashMultiset.create();
		}
		allTransitionCount = 0;
		long tagGram;
		for (Sentence sentence : corpus.getContent()) {
			tagGram = 0;
			for (int i = 0; i < sentence.length(); i++) {
				allTransitionCount++;  //STATS
				totalTransitions.add(tagGram);
				tagGram <<= tagSet.tagBoundBitCount;
				tagGram += sentence.getTag(i);
				transitionCounts.add(tagGram);

				//System.out.println(Helper.tagGramToString(sentence.getPrevTagsCoded(i,3)));
				tagGram &= Helper.gramMask.get(gramCount);
				// --> auch zaehlen fuer normierung der emissionsCounts
				totalEmissions.add(tagGram);

				//System.out.println(tagSet.tagGramToString(tagGram) + ": " + tagSet.tagGramToString(sentence.getTag(i)));

				////
				// Features des aktuellen Wortes extrahieren & (gramTag --> Features) zaehlen
				FeatureExtractor featureExtractor = new FeatureExtractor();
				FeatureVector featureVector = featureExtractor.getFeatures(sentence, i);
				for (int j = 0; j < FeatureVector.size; j++) {
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
			transitionProbs.put(entry.getElement(), logProb);

		}
		////////////////////////


		////DEBUG & STATS
		System.out.println();
		for (Multiset.Entry<Long> entry : transitionCounts.entrySet()) {
			System.out.println(tagSet.tagGramToString(entry.getElement()) + ": " + entry.getCount());
		}

		System.out.println();
		System.out.println("allTransitionCount: " + allTransitionCount);
		System.out.println("discriminative Transitions: " + transitionCounts.elementSet().size());

		float mean = (float) allTransitionCount / (float) transitionCounts.elementSet().size();
		System.out.println("mean: " + mean);
		//////////////////
	}

	public Corpus tag() {
		Corpus taggedCorpus = new Corpus(tagSet);
		for (int i = 0; i < corpus.size(); i++) {
			taggedCorpus.setSentence(tagSentence(corpus.getSentence(i)), i);
		}
		return taggedCorpus;
	}

	private Sentence tagSentence(Sentence sentence) {
		FeatureExtractor featureExtractor = new FeatureExtractor();
		double[][] pathProbs = new double[sentence.length() + 1][tagSet.size()];
		// set initial transition probabilities
		for (byte i = 0; i < tagSet.size(); i++) {
			pathProbs[0][i] = getTransitionProb(0, i);
		}

		byte[] bestTags = new byte[sentence.length()];
		long tagGramCoded = 0;

		// for all words do...
		for (int currentWordIndex = 0; currentWordIndex < sentence.length(); currentWordIndex++) {
			FeatureVector featureVector = featureExtractor.getFeatures(sentence, currentWordIndex);
			// for all possible tagGrams (sources) do...
			for (byte prevTagIndex7 = 0; prevTagIndex7 < tagSet.size(); prevTagIndex7++) {
				for (byte prevTagIndex6 = 0; prevTagIndex6 < tagSet.size(); prevTagIndex6++) {
					for (byte prevTagIndex5 = 0; prevTagIndex5 < tagSet.size(); prevTagIndex5++) {
						for (byte prevTagIndex4 = 0; prevTagIndex4 < tagSet.size(); prevTagIndex4++) {
							for (byte prevTagIndex3 = 0; prevTagIndex3 < tagSet.size(); prevTagIndex3++) {
								for (byte prevTagIndex2 = 0; prevTagIndex2 < tagSet.size(); prevTagIndex2++) {
									for (byte prevTagIndex1 = 0; prevTagIndex1 < tagSet.size(); prevTagIndex1++) {

										for (byte j = 0; j < tagSet.size(); j++) {
											double maxProb = 0;
											//for all current possible tags (target) do...
											for (int currentTagIndex = 0; currentTagIndex < tagSet.size(); currentTagIndex++) {
												double currentProb = pathProbs[currentWordIndex][currentTagIndex] + getTransitionProb(tagGramCoded, j);
												if (currentProb > maxProb) {
													maxProb = currentProb;
													bestTags[currentWordIndex] = prevTagIndex1;
												}
											}
											pathProbs[currentWordIndex + 1][j] = maxProb + getEmitProb(j, featureVector);
										}
										tagGramCoded++;
									}
									if (gramCount < 2)
										break;
									tagGramCoded += 0x100L;
								}
								if (gramCount < 3)
									break;
								tagGramCoded += 0x10000L;
							}
							if (gramCount < 4)
								break;
							tagGramCoded += 0xF1000000L;
						}
						if (gramCount < 5)
							break;
						tagGramCoded += 0x100000000L;
					}
					if (gramCount < 6)
						break;
					tagGramCoded += 0x10000000000L;
				}
				if (gramCount < 7)
					break;
				tagGramCoded += 0x1000000000000L;
			}
		}

		//double resultProb = 0;
		double resultProb = 0;
		double currentProb = 0;
		for (byte i = 0; i < tagSet.size(); i++) {
			currentProb = pathProbs[sentence.length()][i];
			if (currentProb > resultProb) {
				resultProb = currentProb;
				bestTags[sentence.length() - 1] = i;
			}
		}
		sentence.setTags(bestTags);
		return sentence;

	}

	private double getTransitionProb(long prevTags, byte currentTag) {
		return transitionProbs.get((prevTags << tagSet.tagBoundBitCount) + currentTag);
	}

	private double getEmitProb(byte tag, FeatureVector featureVector) {
	   // TODO: implement getEmitProb!
	}

}

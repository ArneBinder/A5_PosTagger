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
	// prevGramPosTag<<8+posTag --> probability
	HashMap<Long, Double> transitionProbs;
	// pos-tag x feature-index x feature-value --> probability
	HashMap<String, Double>[][] emissionProbs;

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


	public void train() {
		Multiset<Long> transitionCounts = HashMultiset.create();
		Multiset<Long> totalTransitions = HashMultiset.create();
		Multiset<Map.Entry<Byte, String>>[] emissionCounts = new Multiset[FeatureVector.size]; // saves for every feature (-->Array) how often a posGram(Long) emits a specific value (String)
		Multiset<Byte> totalEmissions = HashMultiset.create(); //saves how often a posGram occurs in total
		int allTransitionCount; //STATS
		for (int i = 0; i < emissionCounts.length; i++) {
			emissionCounts[i] = HashMultiset.create();
		}
		allTransitionCount = 0;
		long tagGram;
		byte currentTag;
		FeatureExtractor featureExtractor = new FeatureExtractor();
		for (Sentence sentence : corpus.getContent()) {
			tagGram = 0;
			for (int i = 0; i < sentence.length(); i++) {
				allTransitionCount++;  //STATS
				totalTransitions.add(tagGram);
				tagGram <<= TagSet.tagBoundBitCount;
				currentTag = sentence.getTag(i);
				tagGram += currentTag;
				transitionCounts.add(tagGram);

				//System.out.println(Helper.tagGramToString(sentence.getPrevTagsCoded(i,3)));
				tagGram &= Helper.gramMask.get(gramCount);
				// --> auch zaehlen fuer normierung der emissionsCounts
				totalEmissions.add(currentTag);

				//System.out.println(tagSet.tagGramToString(tagGram) + ": " + tagSet.tagGramToString(sentence.getTag(i)));

				////
				// Features des aktuellen Wortes extrahieren & (gramTag --> Features) zaehlen

				FeatureVector featureVector = featureExtractor.getFeatures(sentence, i);
				for (int j = 0; j < FeatureVector.size; j++) {
					//TODO: check, if Pair works correct
					emissionCounts[j].add(new Pair<Byte, String>(currentTag/*tagGram*/, featureVector.features[j]));
				}
			}

			System.out.println();
		}

		//// normieren //////////
		/* emissionCounts by totalEmissions */
		// pos-tag x feature-index x feature-value --> probability
		emissionProbs = new HashMap[tagSet.size()][FeatureVector.size];
		for (byte i = 0; i < FeatureVector.size; i++) {
			for (Multiset.Entry<Map.Entry<Byte, String>> entry : emissionCounts[i].entrySet()) {
				byte posTag = entry.getElement().getKey();
				double logProb = Math.log(entry.getCount()) - Math.log(totalEmissions.count(posTag));
				//int featureIndex = i;
				emissionProbs[posTag][i].put(entry.getElement().getValue(), logProb);
				// --> array aus posTagGram, featureIndex, featureValue, logProb

			}
		}

		/* transitionCounts by totalTransitions */
		transitionProbs = new HashMap<Long, Double>();
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
		byte[][] sourceTags = new byte[sentence.length()][tagSet.size()];
		// set initial transition probabilities
		for (byte i = 0; i < tagSet.size(); i++) {
			pathProbs[0][i] = getTransitionProb(i);
		}

		//byte[] bestTags = new byte[sentence.length()];
		long tagGramCoded = 0x100L;

		// for all words do...
		for (int currentWordIndex = 0; currentWordIndex < sentence.length(); currentWordIndex++) {
			FeatureVector featureVector = featureExtractor.getFeatures(sentence, currentWordIndex);
			for (byte currentTagIndex = 0; currentTagIndex < tagSet.size(); currentTagIndex++) {
				double maxProb = 0;
				tagGramCoded++;
				// for all possible tagGrams (sources) do...
				for (byte prevTagIndex7 = 0; prevTagIndex7 < tagSet.size(); prevTagIndex7++) {
					for (byte prevTagIndex6 = 0; prevTagIndex6 < tagSet.size(); prevTagIndex6++) {
						for (byte prevTagIndex5 = 0; prevTagIndex5 < tagSet.size(); prevTagIndex5++) {
							for (byte prevTagIndex4 = 0; prevTagIndex4 < tagSet.size(); prevTagIndex4++) {
								for (byte prevTagIndex3 = 0; prevTagIndex3 < tagSet.size(); prevTagIndex3++) {
									for (byte prevTagIndex2 = 0; prevTagIndex2 < tagSet.size(); prevTagIndex2++) {
										for (byte prevTagIndex1 = 0; prevTagIndex1 < tagSet.size(); prevTagIndex1++) {

											//for all current possible tags (target) do...
											double currentProb = pathProbs[currentWordIndex][currentTagIndex] + getTransitionProb(tagGramCoded);
											if (currentProb > maxProb) {
												maxProb = currentProb;
												sourceTags[currentWordIndex][currentTagIndex] = prevTagIndex1;
											}
											tagGramCoded += 0x100L;
										}

										if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 2)
											break;
										tagGramCoded &= 0xFFFFFFFFFFFF00FFL;
										tagGramCoded += 0x10100L;
									}
									if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 3)
										break;
									tagGramCoded &= 0xFFFFFFFFFF0000FFL;
									tagGramCoded += 0x1010100L;
								}
								if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 4)
									break;
								tagGramCoded &= 0xFFFFFFFF000000FFL;
								tagGramCoded += 0x101010100L;
							}
							if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 5)
								break;
							tagGramCoded &= 0xFFFFFF00000000FFL;
							tagGramCoded += 0x10101010100L;
						}
						if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 6)
							break;
						tagGramCoded &= 0xFFFF0000000000FFL;
						tagGramCoded += 0x1010101010100L;
					}
					if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 7)
						break;
					tagGramCoded &= 0xFF000000000000FFL;
					tagGramCoded += 0x101010101010100L;
				}
				pathProbs[currentWordIndex + 1][currentTagIndex] = maxProb + getEmitProb((byte) (currentTagIndex + 1), featureVector);
			}


		}

		double resultProb = 0;
		double currentProb;
		byte lastTagIndex = 0;
		for (byte i = 0; i < tagSet.size(); i++)
		{
			currentProb = pathProbs[sentence.length()][i];
			if (currentProb > resultProb) {
				resultProb = currentProb;
				//bestTags[sentence.length() - 1] = i;
				lastTagIndex = i;
			}
		}
		byte[] resultTags = new byte[sentence.length()];
		byte nextTagIndex = lastTagIndex;
		for (int i = sentence.length()-1; i>=0; i--) {
			resultTags[i] = (byte)(nextTagIndex + 1);
			nextTagIndex = sourceTags[i][nextTagIndex];
		}

		sentence.setTags(resultTags);
		return sentence;

	}

	private double getTransitionProb(long tags) {
		// TODO: Smoothing!
		return transitionProbs.get(tags);
	}

	private double getEmitProb(byte tag, FeatureVector featureVector) {
		double resultProb = 0;
		for (int i = 0; i < FeatureVector.size; i++) {
			// pos-tag x feature-index x feature-value --> probability
			// TODO: implement weights!
			// TODO: implement smoothing! (if value doesn't exist --> error at the moment)
			resultProb += emissionProbs[tag][i].get(featureVector.features[i]);
		}
		return resultProb;

	}

	/**
	 * read model (from file))
	 */
	public void readModelFromFile() {
		//TODO: implement!
	}

	/**
	 * write model (to file)
	 */
	public void writeModelToFile() {
		//TODO: implement!
		//TODO: write also Tagset?!
	}

}

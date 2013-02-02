import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import java.io.*;
import java.nio.CharBuffer;
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


	private Corpus corpus;
	private int gramCount;
	private TagSet tagSet;

	//////// Methods ///////////

	HMM(Corpus corpus, int gramCount, TagSet tagSet) {
		//System.out.println(gramCount);
		//System.out.println(Helper.maxGramCount);
		assert gramCount <= Helper.maxGramCount : "given gram count = " + gramCount + " is bigger than max gram count = " + Helper.maxGramCount + ".";
		this.corpus = corpus;
		this.gramCount = gramCount;
		this.tagSet = tagSet;

	}

	HMM(String fileName) {
		readModelFromFile(fileName);
	}

	public Corpus getCorpus() {
		return corpus;
	}

	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	public TagSet getTagSet() {
		return tagSet;
	}

	public void train() {
		System.out.println("corpus.size(): " + corpus.size());
		Multiset<Long> transitionCounts = HashMultiset.create();
		Multiset<Long> totalTransitions = HashMultiset.create();
		Multiset<String>[][] emissionCounts = new Multiset[FeatureExtractor.featureSize][tagSet.size()]; // saves for every feature (-->Array) how often a posGram(Long) emits a specific value (String)
		Multiset<Byte> totalEmissions = HashMultiset.create(); //saves how often a posGram occurs in total
		int allTransitionCount; //STATS
		for (int i = 0; i < emissionCounts.length; i++) {
			for (int j = 0; j < tagSet.size(); j++) {
				emissionCounts[i][j] = HashMultiset.create();
			}

		}
		allTransitionCount = 0;
		long tagGram;
		byte currentTag;
		int[] lastSize = new int[FeatureExtractor.featureSize];
		for (int i = 0; i < FeatureExtractor.featureSize; i++) {
			lastSize[i] = 0;
		}
		FeatureExtractor featureExtractor = new FeatureExtractor();
		int curSenIndex = 0;
		long time = System.currentTimeMillis();
		for (Sentence sentence : corpus.getContent()) {
			tagGram = 0;
			//System.out.print(".");
			if (curSenIndex % 1000 == 0) {
				//System.out.println();
				System.out.println(curSenIndex + "\t" + (System.currentTimeMillis() - time) + "ms");
				time = System.currentTimeMillis();
			}
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

				for (int j = 0; j < FeatureExtractor.featureSize; j++) {
					//TODO: check, if Pair works correct
					//lastSize[j] = emissionCounts[j].elementSet().size();
					emissionCounts[j][currentTag - 1].add(featureVector.features[j]);


				}
				//if(emissionCounts[4].elementSet().size() - lastSize[4] < 1)
				//	System.out.println(emissionCounts[4].size()+((double)curSenIndex / (double)corpus.size()));

			}
			//System.out.println(curSenIndex);
			curSenIndex++;
			//System.out.println();
		}
		System.out.println(tagSet);
		int countEmissionCount = 0;
		int totalEmissionValues = 0;
		for (int i = 0; i < FeatureExtractor.featureSize; i++) {
			for (int j = 0; j < tagSet.size(); j++) {
				countEmissionCount += emissionCounts[i][j].elementSet().size();
				totalEmissionValues += emissionCounts[i][j].size();
			}
		}
		System.out.println("countEmissionCount: " + countEmissionCount);
		System.out.println("total emnission values: " + totalEmissionValues);
		//// normieren //////////
		/* emissionCounts by totalEmissions */
		// pos-tag x feature-index x feature-value --> probability
		//int t = tagSet.size();
		emissionProbs = new HashMap[tagSet.size()][FeatureExtractor.featureSize];
		for (int i = 0; i < tagSet.size(); i++) {
			for (int j = 0; j < FeatureExtractor.featureSize; j++) {
				emissionProbs[i][j] = new HashMap<String, Double>();
			}
		}
		System.out.println("normalize emissionProbs...");
		System.out.println();
		for (byte featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
			for (int tagIndex = 0; tagIndex < tagSet.size(); tagIndex++) {


				//emissionCounts[featureIndex].entrySet().iterator()
				for (Multiset.Entry<String> entry : emissionCounts[featureIndex][tagIndex].entrySet()) {
					int entryCount = entry.getCount();
					int totalCount = totalEmissions.count(tagIndex + 1);
					//byte posTag = entry.getElement().getKey();
					double logProb = Math.log(entryCount) - Math.log(totalCount);
					if (totalEmissions.count(tagIndex + 1) != 0)
						System.out.println(entryCount + "\t" + totalEmissions.count(tagIndex + 1));
					//int featureIndex = featureIndex;
					//if (logProb != 1d / 0)
					//	System.out.println("no INf " + logProb);
					emissionProbs[tagIndex][featureIndex].put(entry.getElement(), logProb);
					//if(emissionCounts[featureIndex].remove(entry.getElement(), entryCount)>0)
					//	System.out.println("removed");
					// --> array aus posTagGram, featureIndex, featureValue, logProb

				}

				emissionCounts[featureIndex][tagIndex] = HashMultiset.create();
				//System.out.println(emissionCounts[featureIndex][tagIndex].size());
			}
			//System.out.println();
		}
		System.out.println("emissionProbs done.");

		/* transitionCounts by totalTransitions */
		transitionProbs = new HashMap<Long, Double>();
		for (Multiset.Entry<Long> entry : transitionCounts.entrySet()) {
			// reconstruct source posGram
			Long posGram = (entry.getElement() >> TagSet.tagBoundBitCount);

			double logProb = Math.log(entry.getCount()) - Math.log(totalTransitions.count(posGram));

			// --> array aus fromPosTagGram, toPosTagGram, logProb
			transitionProbs.put(entry.getElement(), logProb);

		}
		System.out.println("transitionProbs done.");
		////////////////////////


		////DEBUG & STATS
		//System.out.println();
		//for (Multiset.Entry<Long> entry : transitionCounts.entrySet()) {
		//	System.out.println(tagSet.tagGramToString(entry.getElement()) + ": " + entry.getCount());
		//}

		System.out.println();
		System.out.println("allTransitionCount: " + allTransitionCount);
		System.out.println("discriminative Transitions: " + transitionCounts.elementSet().size());

		float mean = (float) allTransitionCount / (float) transitionCounts.elementSet().size();
		System.out.println("mean: " + mean);

		//////////////////
	}

	public Corpus tag() {
		System.out.println("sentences: " + corpus.size());
		Corpus taggedCorpus = new Corpus(tagSet);
		for (int i = 0; i < corpus.size(); i++) {
			System.out.print(i + ": ");
			Sentence taggedSentence = tagSentence(corpus.getSentence(i));
			taggedCorpus.addSentence(taggedSentence);
			System.out.println(taggedSentence);
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
			//System.out.println(pathProbs[0][i]);
		}

		//byte[] bestTags = new byte[sentence.length()];
		long tagGramCoded = 0x100L;

		// for all words do...
		for (int currentWordIndex = 0; currentWordIndex < sentence.length(); currentWordIndex++) {
			String word = sentence.getWord(currentWordIndex);
			FeatureVector featureVector = featureExtractor.getFeatures(sentence, currentWordIndex);
			for (byte currentTagIndex = 0; currentTagIndex < tagSet.size(); currentTagIndex++) {
				double maxProb = -Double.MAX_VALUE;
				//System.out.println(maxProb);
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
											//System.out.println(getTransitionProb(tagGramCoded));
											double currentProb = pathProbs[currentWordIndex][currentTagIndex] + getTransitionProb(tagGramCoded);

											if (currentProb > maxProb) {
												//System.out.println(word+"/"+tagSet.tagToString((byte)(prevTagIndex1+1))+": current: "+ currentProb+" max: "+maxProb);
												maxProb = currentProb;
												//System.out.println(word+"/"+tagSet.tagToString((byte)(prevTagIndex1+1))+": current: "+ currentProb+" max: "+maxProb);
												sourceTags[currentWordIndex][currentTagIndex] = prevTagIndex1;
											}
											tagGramCoded += 0x100L;
										}

										if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 2)
											break;
										//System.out.println("asdasd");
										tagGramCoded &= 0xFFFFFFFFFFFF00FFL;
										tagGramCoded += 0x10100L;
									}
									if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 3)
										break;
									//System.out.println("asdasd");
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
				//System.out.println(maxProb);
				pathProbs[currentWordIndex + 1][currentTagIndex] = maxProb + getEmitProb(currentTagIndex, featureVector);
			}


		}


		for (int j = 0; j < tagSet.size(); j++) {
			for (int i = 0; i < sentence.length(); i++) {
				System.out.print(tagSet.tagToString((byte) (sourceTags[i][j] + 1)) + "\t");
			}
			System.out.println();
		}

		double resultProb = 0;
		double currentProb;
		byte lastTagIndex = 0;
		for (byte i = 0; i < tagSet.size(); i++) {
			currentProb = pathProbs[sentence.length()][i];
			if (currentProb > resultProb) {
				resultProb = currentProb;
				//bestTags[sentence.length() - 1] = i;
				lastTagIndex = i;
			}
		}
		byte[] resultTags = new byte[sentence.length()];
		byte nextTagIndex = lastTagIndex;
		for (int i = sentence.length() - 1; i >= 0; i--) {
			resultTags[i] = (byte) (nextTagIndex + 1);
			nextTagIndex = sourceTags[i][nextTagIndex];
		}

		sentence.setTags(resultTags);
		return sentence;

	}

	private double getTransitionProb(long tags) {
		try {
			return transitionProbs.get(tags);
		} catch (Exception e) {
			return Helper.smoothing;
		}
	}

	private double getEmitProb(byte tag, FeatureVector featureVector) {
		double resultProb = 0;
		for (int i = 0; i < FeatureExtractor.featureSize; i++) {
			// pos-tag x feature-index x feature-value --> probability
			// TODO: implement weights!
			try {
				resultProb += emissionProbs[tag][i].get(featureVector.features[i]);
			} catch (Exception e) {
				resultProb += Helper.smoothing;
			}
		}
		return resultProb;

	}

	/**
	 * read model (from file))
	 */
	public void readModelFromFile(String fileName) {
		DataInputStream inputStream;
		try {
			inputStream = new DataInputStream(new FileInputStream(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return;
		}

		// pos-tag x feature-index x feature-value --> probability
		try {
			// read gramCount
			gramCount = inputStream.readInt();

			// read tagSet
			int tagSetSize = inputStream.readInt();
			char[] tags = new char[tagSetSize];
			for (int i = 0; i < tagSetSize; i++) {
				tags[i] = inputStream.readChar();
			}
			tagSet = new TagSet(String.valueOf(tags));

			// read emissionProbs
			emissionProbs = new HashMap[tagSetSize][FeatureExtractor.featureSize];
			// pos-tag x feature-index x feature-value --> probability
			for (int posTagIndex = 0; posTagIndex < tagSet.size(); posTagIndex++) {
				for (int featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
					int listLength = inputStream.readInt();
					emissionProbs[posTagIndex][featureIndex] = new HashMap<String, Double>(listLength);
					for (int i = 0; i < listLength; i++) {
						int strLength = inputStream.readInt();
						char[] chars = new char[strLength];
						for (int j = 0; j < strLength; j++) {
							chars[j] = inputStream.readChar();
						}
						emissionProbs[posTagIndex][featureIndex].put(String.valueOf(chars), inputStream.readDouble());
					}
				}
			}

			// read transitionProbs
			int transitionHashMapSize = inputStream.readInt();
			transitionProbs = new HashMap<Long, Double>(transitionHashMapSize);
			for (int i = 0; i < transitionHashMapSize; i++) {
				long key = inputStream.readLong();
				double value = inputStream.readDouble();
				transitionProbs.put(key, value);
			}
			inputStream.close();

		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	public void writeModelToFile(String fileName) {
		DataOutputStream outputStream;
		try {
			outputStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			return;
		}

		try {
			//write gramCount
			outputStream.writeInt(gramCount);

			// write tagSet
			char[] tagSetString = tagSet.toString().toCharArray();
			outputStream.writeInt(tagSetString.length);
			for (char c : tagSetString) {
				outputStream.writeChar(c);
			}

			// write emissionProbs
			// pos-tag x feature-index x feature-value --> probability
			for (int posTagIndex = 0; posTagIndex < tagSet.size(); posTagIndex++) {
				for (int featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
					//ein Eintrag! vorher anzahl speichern
					outputStream.writeInt((emissionProbs[posTagIndex][featureIndex].size()));
					//System.out.println(posTagIndex + "\t" + featureIndex + "\t" + emissionProbs[posTagIndex][featureIndex].size());
					for (Map.Entry<String, Double> entry : emissionProbs[posTagIndex][featureIndex].entrySet()) {
						char[] featureValue = entry.getKey().toCharArray();
						outputStream.writeInt(featureValue.length);
						for (char c : featureValue) {
							outputStream.writeChar(c);
						}
						outputStream.writeDouble(entry.getValue());
						if (!entry.getValue().isInfinite())
							System.out.println(posTagIndex + "\t" + featureIndex + "\t" + entry.getKey() + "\t" + entry.getValue());
					}
				}
			}

			//write transitionProbs
			outputStream.writeInt(transitionProbs.size());
			for (Map.Entry<Long, Double> entry : transitionProbs.entrySet()) {
				outputStream.writeLong(entry.getKey());
				outputStream.writeDouble(entry.getValue());
			}

			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}

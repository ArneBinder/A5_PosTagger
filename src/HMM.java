import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import java.io.*;

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
	//HashMap<Long, Double> transitionProbs;
	double[][] transitionProbs;

	// pos-tag x feature-index x feature-value --> probability
	//HashMap<Integer, Double>[][] emissionProbs;
	double[][][] emissionProbs;


	private Corpus corpus;
	private int gramCount;
	private TagSet tagSet;
	private int featureValueCount;


	//////// Methods ///////////

	HMM(Corpus corpus, /*int gramCount,*/ TagSet tagSet, int featureValueCount) {
		//System.out.println(gramCount);
		//System.out.println(Helper.maxGramCount);
		assert gramCount <= Helper.maxGramCount : "given gram count = " + gramCount + " is bigger than max gram count = " + Helper.maxGramCount + ".";
		this.corpus = corpus;
		this.gramCount = 1;
		this.tagSet = tagSet;
		this.featureValueCount = featureValueCount;

	}

	HMM(String fileName) {
		readModelFromFile(fileName);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof HMM)) return false;

		HMM hmm = (HMM) o;

		if (featureValueCount != hmm.featureValueCount) return false;
		if (gramCount != hmm.gramCount) return false;
		//if (!corpus.equals(hmm.corpus)) return false;
		if (!tagSet.equals(hmm.tagSet)) return false;

		for (int fromTag = 0; fromTag < tagSet.size()+1; fromTag++) {
			for (int toTag = 0; toTag < tagSet.size(); toTag++) {
				if(transitionProbs[fromTag][toTag]!=hmm.transitionProbs[fromTag][toTag])
					return false;
			}
		}

		for (int tag = 0; tag < tagSet.size(); tag++) {
			for (int featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
				for (int featureValue = 0; featureValue < featureValueCount; featureValue++) {
					if(emissionProbs[tag][featureIndex][featureValue]!=hmm.emissionProbs[tag][featureIndex][featureValue])
						return false;
				}
			}
		}


		return true;
	}

	/*@Override
	public int hashCode() {
		int result = corpus.hashCode();
		result = 31 * result + gramCount;
		result = 31 * result + tagSet.hashCode();
		result = 31 * result + featureValueCount;
		return result;
	} */

	public Corpus getCorpus() {
		return corpus;
	}

	public void setCorpus(Corpus corpus) {
		this.corpus = corpus;
	}

	public TagSet getTagSet() {
		return tagSet;
	}

	/*public void printTransitionProbs() {
		System.out.println("transitionProbs(" + transitionProbs.size() + "):");
		for (Map.Entry<Long, Double> entry : transitionProbs.entrySet()) {
			long key = entry.getKey();
			System.out.println(tagSet.tagGramToString(key >> TagSet.tagBoundBitCount) + "\t--> " + tagSet.tagToString((byte) ((key & 0xFF) - 1)) + ": \t" + Math.exp(entry.getValue()));
		}
	} */

	/*public void printEmissionProbs(int featureIndex) {
		System.out.println("emissionProbs:");
		for (byte posTag = 0; posTag < tagSet.size(); posTag++) {
			String tagStr = tagSet.tagToString(posTag);
			int size = emissionProbs[posTag][featureIndex].size();
			for (Map.Entry<Integer, Double> entry : emissionProbs[posTag][featureIndex].entrySet()) {
				System.out.println(tagStr + "\t	--> " + corpus.getFeatureExtractor().getFeatureValueString(entry.getKey(),featureIndex) + ":\t" + Math.exp(entry.getValue()));
			}
		}
	} */

	public void train() {
		System.out.println(featureValueCount);
		//System.out.println("corpus.size(): " + corpus.size());
		Multiset<Integer> transitionCounts = HashMultiset.create();
		Multiset<Integer> totalTransitions = HashMultiset.create();
		Multiset<Integer>[][] emissionCounts = new Multiset[FeatureExtractor.featureSize][tagSet.size()]; // saves for every feature (-->Array) how often a posGram(Long) emits a specific value (String)
		Multiset<Byte> totalEmissions = HashMultiset.create(); //saves how often a posGram occurs in total
		int allTransitionCount; //STATS
		for (int i = 0; i < emissionCounts.length; i++) {
			for (int j = 0; j < tagSet.size(); j++) {
				emissionCounts[i][j] = HashMultiset.create();
			}

		}
		allTransitionCount = 0;
		int tagGram;
		byte currentTag;
		int[] lastSize = new int[FeatureExtractor.featureSize];
		for (int i = 0; i < FeatureExtractor.featureSize; i++) {
			lastSize[i] = 0;
		}
		//FeatureExtractor featureExtractor = new FeatureExtractor();
		int curSenIndex = 0;
		long time = System.currentTimeMillis();

		for (int k = 0; k < corpus.size(); k++) {
		//for (Sentence sentence : corpus.getContent()) {
			tagGram = 0;
			//System.out.print(".");
			if (curSenIndex % 1000 == 0 && curSenIndex > 0) {
				//System.out.println();
				System.out.println(curSenIndex + "\t" + (System.currentTimeMillis() - time) + "ms");
				time = System.currentTimeMillis();
			}
			String currentTransition = "";
			String currentTagGram = "";

			Sentence actSentence = corpus.getSentence(k);
			for (int i = 0; i < actSentence.length(); i++) {
				allTransitionCount++;  //STATS
				currentTagGram = tagSet.tagGramToString(tagGram);
				totalTransitions.add(tagGram);
				tagGram <<= TagSet.tagBoundBitCount;
				currentTag = actSentence.getTag(i);
				tagGram += currentTag + 1;
				currentTransition = tagSet.tagGramToString(tagGram);
				transitionCounts.add(tagGram);
				//System.out.println(currentTagGram+"\t"+currentTransition);
				//System.out.println(Helper.tagGramToString(sentence.getPrevTagsCoded(i,3)));
				tagGram &= Helper.gramMask.get(gramCount);
				// --> auch zaehlen fuer normierung der emissionsCounts
				totalEmissions.add(currentTag);

				//System.out.println(tagSet.tagGramToString(tagGram) + ": " + tagSet.tagGramToString(sentence.getTag(i)));

				////
				// Features des aktuellen Wortes extrahieren & (gramTag --> Features) zaehlen

				//FeatureVector featureVector = featureExtractor.getFeatures(sentence, i);
				FeatureVector featureVector = actSentence.getFeature(i);

				for (int j = 0; j < FeatureExtractor.featureSize; j++) {
					//TODO: check, if Pair works correct
					//lastSize[j] = emissionCounts[j].elementSet().size();
					emissionCounts[j][currentTag].add(featureVector.features[j]);


				}
				//if(emissionCounts[4].elementSet().size() - lastSize[4] < 1)
				//	System.out.println(emissionCounts[4].size()+((double)curSenIndex / (double)corpus.size()));

			}
			//System.out.println(curSenIndex);
			curSenIndex++;
			//System.out.println();
		}
		//System.out.println(tagSet);

		//for (Multiset.Entry<Byte> entry : totalEmissions.entrySet()) {
		//	System.out.println(tagSet.tagToString(entry.getElement())+"\t"+entry.getCount()+"\t"+totalEmissions.count(entry.getElement()));
		//}

		/*int countEmissionCount = 0;
		int totalEmissionValues = 0;
		for (int i = 0; i < FeatureExtractor.featureSize; i++) {
			for (int j = 0; j < tagSet.size(); j++) {
				countEmissionCount += emissionCounts[i][j].elementSet().size();
				totalEmissionValues += emissionCounts[i][j].size();
			}
		}
		System.out.println("countEmissionCount: " + countEmissionCount);
		System.out.println("total emnission values: " + totalEmissionValues);
		*/
		//// normieren //////////
		/* emissionCounts by totalEmissions */
		// pos-tag x feature-index x feature-value --> probability
		//int t = tagSet.size();
		//emissionProbs = new HashMap[tagSet.size()][FeatureExtractor.featureSize];
		emissionProbs = new double[tagSet.size()][FeatureExtractor.featureSize][featureValueCount];
		for (int i = 0; i < tagSet.size(); i++) {
			for (int j = 0; j < FeatureExtractor.featureSize; j++) {
				for (int k = 0; k < featureValueCount; k++) {
					emissionProbs[i][j][k] = Helper.smoothing;
				}
				//emissionProbs[i][j] = new HashMap<Integer, Double>();
			}
		}
		System.out.println("normalize emissionProbs...");
		//System.out.println();
		for (byte featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
			for (byte tagIndex = 0; tagIndex < tagSet.size(); tagIndex++) {


				//emissionCounts[featureIndex].entrySet().iterator()
				for (Multiset.Entry<Integer> entry : emissionCounts[featureIndex][tagIndex].entrySet()) {
					int entryCount = entry.getCount();
					int totalCount = totalEmissions.count(tagIndex);
					//byte posTag = entry.getElement().getKey();
					double logProb = Math.log(entryCount) - Math.log(totalCount);
					//if (totalEmissions.count(tagIndex) != 0)
					//	System.out.println(entryCount + "\t" + totalEmissions.count(tagIndex));
					//int featureIndex = featureIndex;
					//if (logProb != 1d / 0)
					//	System.out.println("no INf " + logProb);
					if (logProb > Helper.smoothing)
						emissionProbs[tagIndex][featureIndex][entry.getElement()] = logProb;
					//emissionProbs[tagIndex][featureIndex].put(entry.getElement(), logProb);
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
		System.out.println("normalize transitionProbs...");
		/* transitionCounts by totalTransitions */
		//transitionProbs = new HashMap<Long, Double>();
		transitionProbs = new double[tagSet.size() + 1][tagSet.size()];
		for (Multiset.Entry<Integer> entry : transitionCounts.entrySet()) {
			// reconstruct source prevTag
			int transition = entry.getElement();
			int prevTag = (transition >> TagSet.tagBoundBitCount);
			int tag = (transition & 0xFF) - 1;
			//String currentPosGram = tagSet.tagGramToString(prevTag);
			//String currentTransition = tagSet.tagGramToString(entry.getElement());
			//System.out.println(currentTransition+"\t"+entry.getCount()+"("+currentPosGram+": "+totalTransitions.count(prevTag)+")");
			double logProb = Math.log(entry.getCount()) - Math.log(totalTransitions.count(prevTag));
			if (tag < 0)
				System.out.println(tag);
			if (logProb > Helper.smoothing)
				// --> array aus fromPosTagGram, toPosTagGram, logProb
				transitionProbs[prevTag][tag] = logProb;
			//transitionProbs.put(entry.getElement(), logProb);

		}
		System.out.println("transitionProbs done.");
		////////////////////////


		////DEBUG & STATS
		//System.out.println();
		//for (Multiset.Entry<Long> entry : transitionCounts.entrySet()) {
		//	System.out.println(tagSet.tagGramToString(entry.getElement()) + ": " + entry.getCount());
		//}

		/*System.out.println();
		System.out.println("allTransitionCount: " + allTransitionCount);
		System.out.println("discriminative Transitions: " + transitionCounts.elementSet().size());

		float mean = (float) allTransitionCount / (float) transitionCounts.elementSet().size();
		System.out.println("mean: " + mean);
        */
		//////////////////
	}

	public Corpus tag() {
		System.out.println("sentences: " + corpus.size());
		Corpus taggedCorpus = new Corpus(tagSet/* corpus.getFeatureExtractor()*/);
		for (int i = 0; i < corpus.size(); i++) {
			//System.out.print(i + ": ");
			Sentence sentence = corpus.getSentence(i);
			Sentence taggedSentence = new Sentence(sentence.getWords(), tagSentence(sentence), tagSet);
			taggedCorpus.addSentence(taggedSentence);
			//System.out.println(taggedSentence);
			//if(i>10)
			//	break;
		}
		return taggedCorpus;
	}

	private byte[] tagSentence(Sentence sentence) {
		//FeatureExtractor featureExtractor = new FeatureExtractor();
		double[][] pathProbs = new double[sentence.length() + 1][tagSet.size()];
		byte[][] sourceTags = new byte[sentence.length()][tagSet.size()];
		// set initial transition probabilities
		for (byte i = 0; i < tagSet.size(); i++) {
			pathProbs[0][i] = 0;//getTransitionProb(i + 1);
			//System.out.println(pathProbs[0][i]);
		}

		//byte[] bestTags = new byte[sentence.length()];
		//int tagGramCoded = 0;//0x100L;

		// for all words do...
		for (int currentWordIndex = 0; currentWordIndex < sentence.length(); currentWordIndex++) {
			String word = sentence.getWord(currentWordIndex);
			//FeatureVector featureVector = featureExtractor.getFeatures(sentence, currentWordIndex);
			FeatureVector featureVector = sentence.getFeature(currentWordIndex);
			//tagGramCoded &= 0xFFFFFFFFFFFFFF00L;
			for (byte currentTagIndex = 0; currentTagIndex < tagSet.size(); currentTagIndex++) {
				double maxProb = -Double.MAX_VALUE;

				//System.out.println(maxProb);
				/*tagGramCoded++;
				String tagGramStr = tagSet.tagGramToString(tagGramCoded >> TagSet.tagBoundBitCount);
				String lastTag = "";
				String tagGramTrans = tagSet.tagGramToString(tagGramCoded);  */
				// for all possible tagGrams (sources) do...
				/*for (byte prevTagIndex7 = 0; prevTagIndex7 < tagSet.size(); prevTagIndex7++) {
					tagGramCoded &= 0xFF000000000000FFL;
					if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) >= 7)
						tagGramCoded += 0x100000000000000L;
					for (byte prevTagIndex6 = 0; prevTagIndex6 < tagSet.size(); prevTagIndex6++) {
						tagGramCoded &= 0xFFFF0000000000FFL;
						if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) >= 6)
							tagGramCoded += 0x1000000000000L;
						for (byte prevTagIndex5 = 0; prevTagIndex5 < tagSet.size(); prevTagIndex5++) {
							tagGramCoded &= 0xFFFFFF00000000FFL;
							if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) >= 5)
								tagGramCoded += 0x10000000000L;
							for (byte prevTagIndex4 = 0; prevTagIndex4 < tagSet.size(); prevTagIndex4++) {
								tagGramCoded &= 0xFFFFFFFF000000FFL;
								if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) >= 4)
									tagGramCoded += 0x100000000L;
								for (byte prevTagIndex3 = 0; prevTagIndex3 < tagSet.size(); prevTagIndex3++) {
									tagGramCoded &= 0xFFFFFFFFFF0000FFL;
									if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) >= 3)
										tagGramCoded += 0x1000000L;*/
				/*for (byte prevTagIndex2 = 0; prevTagIndex2 < tagSet.size(); prevTagIndex2++) {
					tagGramCoded &= 0xFFFF00FF;
					if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) >= 2)
						tagGramCoded += 0x10000; */
				for (byte prevTagIndex1 = 0; prevTagIndex1 < tagSet.size(); prevTagIndex1++) {
						/*if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) >= 1)
							tagGramCoded += 0x100;
						lastTag = tagSet.tagToString(prevTagIndex1);
						tagGramStr = tagSet.tagGramToString(tagGramCoded >> TagSet.tagBoundBitCount);
						//for all current possible tags (target) do...
						//System.out.println(getTransitionProb(tagGramCoded));
						tagGramTrans = tagSet.tagGramToString(tagGramCoded); */
					double transitionProb = transitionProbs[prevTagIndex1 + 1][currentTagIndex];//getTransitionProb(tagGramCoded);
					double currentPathProb = pathProbs[currentWordIndex][prevTagIndex1];
					double currentProb = currentPathProb + transitionProb;
					if (currentProb >= maxProb) {
						//System.out.println(word+"/"+tagSet.tagToString((byte)(prevTagIndex1+1))+": current: "+ currentPathProb+" max: "+maxProb);
						maxProb = currentProb;
						//System.out.println(word+"/"+tagSet.tagToString((byte)(prevTagIndex1+1))+": current: "+ currentPathProb+" max: "+maxProb);
						sourceTags[currentWordIndex][currentTagIndex] = prevTagIndex1;
					}
					//tagGramCoded += 0x100L;
						/*tagGramStr = tagSet.tagGramToString(tagGramCoded >> TagSet.tagBoundBitCount);
						if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 1)
							break;            */
		/*			}

					if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 2)
						break;
					//System.out.println("asdasd");

					//tagGramCoded += 0x10100L;
				}

									if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 3)
										break;
									//System.out.println("asdasd");

									//tagGramCoded += 0x1010100L;
								}
								if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 4)
									break;

								//tagGramCoded += 0x101010100L;
							}
							if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 5)
								break;

							//tagGramCoded += 0x10101010100L;
						}
						if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 6)
							break;

						//tagGramCoded += 0x1010101010100L;
					}
					if ((gramCount < currentWordIndex ? gramCount : currentWordIndex) < 7)
						break;

					//tagGramCoded += 0x101010101010100L;
				}   */
					//System.out.println(maxProb);
				}
				double emitProb = getEmitProb(currentTagIndex, featureVector);
				pathProbs[currentWordIndex + 1][currentTagIndex] = maxProb + emitProb;

			}


		}


		double resultProb = -Double.MAX_VALUE;
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

		/*for (byte j = 0; j < tagSet.size(); j++) {
			for (int i = 0; i < sentence.length(); i++) {
				System.out.print(tagSet.tagToString(sourceTags[i][j]) + "\t");
			}

			System.out.print(pathProbs[sentence.length()][j]);
			if(j==lastTagIndex)
				System.out.print("\t<------------");
			System.out.println();
		} */

		byte[] resultTags = new byte[sentence.length()];
		byte nextTagIndex = lastTagIndex;
		for (int i = sentence.length() - 1; i >= 0; i--) {
			resultTags[i] = nextTagIndex;
			nextTagIndex = sourceTags[i][nextTagIndex];
		}

		//sentence.setTags(resultTags);
		//return sentence;
		return resultTags;
	}

	/*private double getTransitionProb(long tags) {
		try {
			return transitionProbs.get(tags);
		} catch (Exception e) {
			return Helper.smoothing;
		}
	} */

	private double getEmitProb(byte tag, FeatureVector featureVector) {
		double resultProb = 0;
		for (int i = 0; i < FeatureExtractor.featureSize; i++) {
			// pos-tag x feature-index x feature-value --> probability
			// TODO: implement weights!
			//try {
			resultProb += emissionProbs[tag][i][featureVector.features[i]];
			//} catch (Exception e) {
			//	resultProb += Helper.smoothing;
			//}
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

			// read featureValueCount
			featureValueCount = inputStream.readInt();

			// read tagSet
			int tagSetSize = inputStream.readInt();
			char[] tags = new char[tagSetSize];
			for (int i = 0; i < tagSetSize; i++) {
				tags[i] = inputStream.readChar();
			}
			tagSet = new TagSet(String.valueOf(tags));

			// read emissionProbs
			//emissionProbs = new HashMap[tagSetSize][FeatureExtractor.featureSize];
			emissionProbs = new double[tagSetSize][FeatureExtractor.featureSize][featureValueCount];
			// pos-tag x feature-index x feature-value --> probability
			for (int posTagIndex = 0; posTagIndex < tagSet.size(); posTagIndex++) {
				for (int featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
					//int listLength = inputStream.readInt();
					//emissionProbs[posTagIndex][featureIndex] = new HashMap<Integer, Double>(listLength);
					for (int featureValue = 0; featureValue < featureValueCount; featureValue++) {

						//int featureValue = inputStream.readInt();
						/*int strLength = inputStream.readInt();
						char[] chars = new char[strLength];
						for (int j = 0; j < strLength; j++) {
							chars[j] = inputStream.readChar();
						} */
						//emissionProbs[posTagIndex][featureIndex].put(String.valueOf(chars), inputStream.readDouble());
						//emissionProbs[posTagIndex][featureIndex].put(featureValue, inputStream.readDouble());
						emissionProbs[posTagIndex][featureIndex][featureValue] = inputStream.readDouble();
					}
				}
			}

			// read transitionProbs
			//int transitionHashMapSize = inputStream.readInt();
			//transitionProbs = new HashMap<Long, Double>(transitionHashMapSize);
			transitionProbs = new double[tagSet.size() + 1][tagSet.size()];
			for (int transitionFromValue = 0; transitionFromValue < tagSet.size() + 1; transitionFromValue++) {
				//int mask = transitionFromValue << TagSet.tagBoundBitCount;
				for (int transitionToValue = 0; transitionToValue < tagSet.size(); transitionToValue++) {
					double value = inputStream.readDouble();
					transitionProbs[transitionFromValue][transitionToValue] = value;
				}
				//int key = inputStream.readInt();

			}

			/*
			// read featureValues
			char[][] featureValues = new char[FeatureExtractor.featureSize][];
			for (int featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
				int valueSize = inputStream.readInt();
				featureValues[featureIndex] = new char[valueSize];
				for (int valueIndex = 0; valueIndex < valueSize; valueIndex++) {
					featureValues[featureIndex][valueIndex] = inputStream.readChar();
				}
			}
			FeatureExtractor featureExtractor = new FeatureExtractor(featureValues);
			corpus.setFeatureExtractor(featureExtractor);
             */
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

			// write featureValueCount
			outputStream.writeInt(featureValueCount);

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
					//outputStream.writeInt((emissionProbs[posTagIndex][featureIndex].size()));
					//System.out.println(posTagIndex + "\t" + featureIndex + "\t" + emissionProbs[posTagIndex][featureIndex].size());
					//for (Map.Entry<Integer, Double> entry : emissionProbs[posTagIndex][featureIndex].entrySet()) {
					for (int featureValue = 0; featureValue < featureValueCount; featureValue++) {


						//char[] featureValue = entry.getKey().toCharArray();
						//int featureValue = entry.getKey();
						//outputStream.writeInt(featureValue.length);
						//outputStream.writeInt(featureValue);
						/*for (char c : featureValue) {
							outputStream.writeChar(c);
						} */
						//outputStream.writeDouble(entry.getValue());
						outputStream.writeDouble(emissionProbs[posTagIndex][featureIndex][featureValue]);
						//if (!entry.getValue().isInfinite())
						//	System.out.println(posTagIndex + "\t" + featureIndex + "\t" + entry.getKey() + "\t" + entry.getValue());
					}
				}
			}

			//write transitionProbs
			//outputStream.writeInt(transitionProbs.size());
			//for (Map.Entry<Long, Double> entry : transitionProbs.entrySet()) {
			//System.out.println(transitionProbs.length+" x "+transitionProbs[0].length);
			for (int transitionFromValue = 0; transitionFromValue < tagSet.size() + 1; transitionFromValue++) {
				//int mask = transitionFromValue << TagSet.tagBoundBitCount;
				for (int transitionToValue = 0; transitionToValue < tagSet.size(); transitionToValue++) {
					//System.out.println(transitionFromValue+" x "+transitionToValue);
					outputStream.writeDouble(transitionProbs[transitionFromValue][transitionToValue]);
				}
				//outputStream.writeLong(entry.getKey());
				//outputStream.writeDouble(entry.getValue());

			}

			/*
			//write featureValues
			char[][] featureValues = corpus.getFeatureExtractor().getFeatureValues();
			for (int featureIndex = 0; featureIndex < FeatureExtractor.featureSize; featureIndex++) {
				int size = featureValues[featureIndex].length;
				outputStream.writeInt(size);
				for (int valueIndex = 0; valueIndex < size; valueIndex++) {
					outputStream.writeChar(featureValues[featureIndex][valueIndex]);
				}
			}
            */
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}

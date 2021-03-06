import java.io.*;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 21.01.13
 * Time: 21:28
 * To change this template use File | Settings | File Templates.
 */
public class Corpus {

	//////// Features ///////////
	/**
	 * - file content: List of sentences.
	 * - content partition: [partitionCount] Lists of sentences
	 * - partitionCount: count of partitions the List of sentences is split to
	 */
	//private List<Sentence> content;
	//private List<Sentence>[] partition;
	private Sentence[] contentArray;
	private Sentence[][] partitionArray;
	int sizeContent;
	int sizePartition;
	private TagSet tagSet;
	//private FeatureExtractor featureExtractor;

	///////// Methods ////////////

	Corpus(TagSet tagSet/*, FeatureExtractor featureExtractor*/) {
		//content = new ArrayList<Sentence>();
		contentArray = new Sentence[60000];
		sizeContent = 0;
		sizePartition = 0;
		this.tagSet = tagSet;
		//this.featureExtractor = featureExtractor;
	}

	public Sentence[] getContent() {
		return contentArray;
	}
	/*
	public List<Sentence> getContent() {
		return content;
	} */

	public Sentence getSentence(int index){
		return contentArray[index];
	}

	/*
	public Sentence getSentence(int index){
		return content.get(index);
	}*/

	public void setSentence(Sentence sentence, int index){
		contentArray[index]= sentence;
	}
	/*
	public void setSentence(Sentence sentence, int index){
		content.set(index,sentence);
	} */

	public void addSentence(Sentence sentence){
		setSentence(sentence, sizeContent);
		sizeContent++;
	}
	/*
	public void addSentence(Sentence sentence){
		content.add(sentence);
	};*/

	public int size(){
		return sizeContent;
	}
	/*
	public int size(){
		return content.size();
	} */

	public TagSet getTagSet() {
		return tagSet;
	}
/*public FeatureExtractor getFeatureExtractor() {
		return featureExtractor;
	}

	public void setFeatureExtractor(FeatureExtractor featureExtractor) {
		this.featureExtractor = featureExtractor;
	}
*/

	/**
	 * read text (from file)
	 * - tagged or
	 * - untagged
	 */
	public void addContentFromFile(String fileName, FeatureExtractor featureExtractor) {
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				if (strLine.length() > 0) {
					addSentence(new Sentence(strLine, tagSet, featureExtractor));
					//content.add(new Sentence(strLine, tagSet, featureExtractor));
				}
			}
			//Close the input stream
			in.close();
		} catch (Exception e) {//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public void constructPartition(int partitionCount) {
		//this.partitionCount = partitionCount;
		partitionArray = new Sentence[partitionCount][60000 / partitionCount];
		//partition = (ArrayList<Sentence>[]) new ArrayList[partitionCount];
		//for (int i = 0; i < partitionCount; i++) {
			//partition[i] = new ArrayList<Sentence>(content.size() / partitionCount + 1);

		int j = 0;
		//for (Sentence sentence : content) {
		//	partition[j % partitionCount].add(sentence);
		//	j++;
		//}
		for (int i = 0; i < sizeContent; i++) {
			partitionArray[j % partitionCount][sizePartition] = contentArray[j];
			j++;
			if (j % partitionCount == 0)
				sizePartition++;
		}
	}

	/*public void constructTrainTagData(int tagID) {
		trainData = new ArrayList<Sentence>(content.size() - content.size() / partitionCount + 1);
		tagData = new ArrayList<Sentence>(content.size() / partitionCount + 1);
		for (int i = 0; i < partition.length; i++) {
			if (i != tagID)
				trainData.addAll(partition[i]);
			else
				tagData.addAll(partition[i]);
		}
	} */

	public Corpus getTrainCorpus(int excludeIndex, int partitionCount){
		Corpus result = new Corpus(tagSet);
		/*for (int i = 0; i < partition.length; i++) {
			if (i != excludeIndex)
				result.content.addAll(partition[i]);
		} */
		for (int i = 0; i < partitionCount; i++)
		{
			if (i != excludeIndex)
				for (int j = 0; j < sizePartition; j++)
					result.addSentence(partitionArray[i][j]);
		}
		return result;
	}

	public Corpus getEvaluationCorpus(int index){
		Corpus result = new Corpus(tagSet);
		//result.content.addAll(partition[index]);
		for (int j = 0; j < sizePartition; j++)
			result.addSentence(partitionArray[index][j]);
		return result;
	}


	/**
	 * write tagged text (to file)
	 */
	public void writeContentToFile(String fileName) {
		try{
		    // Create file
		    FileWriter fstream = new FileWriter(fileName);
		    BufferedWriter out = new BufferedWriter(fstream);

			//int i = 0;
			/*for (Sentence sentence : content) {
				i++;
				out.write("\n\n\n");
				out.write(sentence.toString());
				System.out.println(fileName + " Schreibe Satz: " + i);
			} */
			for (int i = 0; i < sizeContent; i++) {
				out.write("\n\n\n");
				out.write(contentArray[i].toString());
				//System.out.println(fileName + " Schreibe Satz: " + i);
			}

			//Close the output stream
		    out.flush();
		    out.close();
		}catch (Exception e){//Catch exception if any
		    System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}



}

import com.sun.corba.se.spi.orb.StringPair;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
	private List<Sentence> content;
	private List<Sentence>[] partition;
	private TagSet tagSet;

	///////// Methods ////////////

	Corpus(TagSet tagSet) {
		content = new ArrayList<Sentence>();
		this.tagSet = tagSet;
	}

	public List<Sentence> getContent() {
		return content;
	}

	public Sentence getSentence(int index){
		return content.get(index);
	}

	public void setSentence(Sentence sentence, int index){
		content.set(index,sentence);
	}

	public int size(){
		return content.size();
	}

	/**
	 * read text (from file)
	 * - tagged or
	 * - untagged
	 */
	public void addContentFromFile(String fileName) {
		try {
			FileInputStream fstream = new FileInputStream(fileName);
			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			//Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				if (strLine.length() > 0) {
					content.add(new Sentence(strLine, tagSet));
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
		partition = (ArrayList<Sentence>[]) new ArrayList[partitionCount];
		for (int i = 0; i < partitionCount; i++) {
			partition[i] = new ArrayList<Sentence>(content.size() / partitionCount + 1);
		}
		int i = 0;
		for (Sentence sentence : content) {
			partition[i % partitionCount].add(sentence);
			i++;
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

	public Corpus getTrainCorpus(int excludeIndex){
		Corpus result = new Corpus(tagSet);
		for (int i = 0; i < partition.length; i++) {
			if (i != excludeIndex)
				result.content.addAll(partition[i]);
		}
		return result;
	}

	public Corpus getEvaluationCorpus(int excludeIndex){
		Corpus result = new Corpus(tagSet);
		result.content.addAll(partition[excludeIndex]);
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

			for (Sentence sentence : content) {
				out.write("\n\n\n");
				out.write(sentence.toString());
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

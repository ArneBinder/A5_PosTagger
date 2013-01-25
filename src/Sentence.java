import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 24.01.13
 * Time: 21:38
 * To change this template use File | Settings | File Templates.
 */
public class Sentence {
	private String[] words;
	private byte[] tags;
	private int length;

	public int length() {
		return length;
	}

	public String[] getWords() {
		return words;
	}

	public byte[] getTags() {
		return tags;
	}

	Sentence(String sentence) {
		sentence = sentence.trim();
		String[] tempWords = sentence.split(" ");
		length = tempWords.length;
		this.words = new String[length];
		this.tags = new byte[length];
		String[] taggedWord;
		for (int i = 0; i < length; i++) {
			if (tempWords[i].contains("/")) {
				taggedWord = tempWords[i].split("/");
				this.words[i] = taggedWord[0];
				this.tags[i] = Helper.tagToByte(taggedWord[1]);
			} else {
				this.words[i] = tempWords[i];
				this.tags[i] = -1;
			}
		}

	}

	public String toString() {
		String result = "";
		for (int i = 0; i < words.length; i++) {
			result += words[i];
			if (tags[i] != -1)
				result += "/" + Helper.tagToString(tags[i]);
			result += " ";
		}
		return result;
	}

	public String getPrevTags(int index, int count) {
		String result = "";//new String[count];
		for (int i = index - count; i < 0; i++) {
		   result += "/";
		}
		for (int i =  (index-count<0?0:index-count); i < index; i++) {
			result += "/"+tags[i];
		}
		return result;
	}

	public long getPrevTagsCoded(int index, int count) {
		long result = 0;
		for (int i = (index-count<0?0:index-count); i < index; i++) {
			result <<= Helper.tagBoundBitCount;
			result += tags[i];
		}
		return result;
	}

	public byte getTag(int index){
		return tags[index];
	}





}

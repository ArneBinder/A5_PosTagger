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
	private short[] tags;
	private int length;
	private TagSet tagSet;

	public int length() {
		return length;
	}

	public String[] getWords() {
		return words;
	}

	public String getWord(int index) {
		return words[index];
	}

	public short[] getTags() {
		return tags;
	}

	Sentence(String sentence, TagSet tagSet) {
		this.tagSet = tagSet;
		sentence = sentence.trim();
		String[] tempWords = sentence.split(" ");
		length = tempWords.length;
		this.words = new String[length];
		this.tags = new short[length];
		String[] taggedWord;
		for (int i = 0; i < length; i++) {
			if (tempWords[i].contains(Helper.tagDelimiter + "")) {
				taggedWord = tempWords[i].split(Helper.tagDelimiter + "");
				this.words[i] = taggedWord[0];
				for (int j = 1; j < taggedWord.length - 2; j++) {
					this.words[i] += taggedWord[j];
				}
				this.tags[i] = tagSet.tagToByte(taggedWord[taggedWord.length - 1]);
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
				result += Helper.tagDelimiter + tagSet.tagToString(tags[i]);
			result += " ";
		}
		return result;
	}

	public void setTags(short[] tags) {
		System.arraycopy(tags, 0, this.tags, 0, this.tags.length);
	}

	public String getPrevTags(int index, int count) {
		String result = "";//new String[count];
		for (int i = index - count; i < 0; i++) {
			result += Helper.tagDelimiter;
		}
		for (int i = (index - count < 0 ? 0 : index - count); i < index; i++) {
			result += Helper.tagDelimiter + tags[i];
		}
		return result;
	}

	public long getPrevTagsCoded(int index, int count) {
		long result = 0;
		for (int i = (index - count < 0 ? 0 : index - count); i < index; i++) {
			result <<= TagSet.tagBoundBitCount;
			result += tags[i];
		}
		return result;
	}

	public short getTag(int index) {
		return tags[index];
	}


}

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 25.01.13
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class TagSet {
	public final int tagBound = 128;
	public final int tagBoundBitCount = 8;

	/**
	 * tagMap contains a mapping from tag strings to byte indices (Attention: 1 .. (tagBound-1))
	 */
	private BidiMap<String, Byte> tagMap = new BidiMap<String, Byte>();

	/**
	 * @param tagString have to be structured like "tagA/tagB/tagC/" don't forget the last slash!
	 */
	public TagSet(String tagString) {
		if (tagString.length() > 0) {
			String[] tags = tagString.split(Helper.tagDelimiter + "");
			assert tags.length < tagBound : "unable to set up tags, size of tag list=" + (tags.length - 1) + " exceeds tagBound=" + tagBound + ".";
			for (byte i = 1; i < tags.length + 1; i++) {
				tagMap.put(tags[i - 1], i);
			}
		}
	}

	public String toString() {
		String result = "";
		for (byte i = 1; i < tagMap.size() + 1; i++) {
			result += tagMap.getKey(i) + Helper.tagDelimiter;
		}
		return result;
	}

	public byte tagToByte(String tag) {
		try {
			return tagMap.get(tag);
		} catch (java.lang.NullPointerException e) {
			assert tagMap.size() + 1 < tagBound : "unable to set up new tag, tagBound=" + tagBound + " reached.";
			tagMap.put(tag, (byte) (tagMap.size() + 1));
			return tagMap.get(tag);
		}

	}

	public String tagToString(byte tag) {
		return tagMap.getKey(tag);
	}

	public String tagGramToString(long tagGram) {
		String result = "";
		while (tagGram != 0) {
			result = tagToString((byte) (tagGram & 0xFF)) + Helper.tagDelimiter + result;
			tagGram >>= tagBoundBitCount;
		}

		return result;
	}
}
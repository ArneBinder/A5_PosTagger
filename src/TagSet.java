/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 25.01.13
 * Time: 15:10
 * To change this template use File | Settings | File Templates.
 */
public class TagSet {
	public static final int tagBound = 128;
	public static final int tagBoundBitCount = 8;

	//TODO: tagListe fuellen!

	/**
	 * tagMap contains a mapping from tag strings to byte indices (Attention: 1 .. (tagBound-1))
	 */
	private BidiMap<String, Byte> tagMap = new BidiMap<String, Byte>();

	public int size() {
		return tagMap.size();
	}

	/**
	 * @param tagString have to be structured like "tagA/tagB/tagC/" don't forget the last slash!
	 */
	public TagSet(String tagString) {
		if (tagString.length() > 0) {
			String[] tags = tagString.split(Helper.tagDelimiter + "");
			assert tags.length < tagBound : "unable to set up tags, size of tag list=" + tags.length + " exceeds tagBound=" + tagBound + ".";
			for (byte i = 0; i < tags.length; i++) {
				tagMap.put(tags[i], i);
			}
		}
	}

	public String toString() {
		String result = "";
		for (byte i = 0; i < tagMap.size(); i++) {
			result += tagMap.getKey(i) + Helper.tagDelimiter;
		}
		return result;
	}

	public byte tagToByte(String tag) {
		try {
			return tagMap.get(tag);
		} catch (java.lang.NullPointerException e) {
			assert tagMap.size() < tagBound : "unable to set up new tag, tagBound=" + tagBound + " reached.";
			tagMap.put(tag, (byte) tagMap.size());
			//if (tagMap.get(tag) < 0)
			//	System.out.println("BLÖÖÖÖDE " + tagMap.size());
			return tagMap.get(tag);
		}

	}

	public String tagToString(byte tag) {
		return tagMap.getKey(tag);
	}

	public String tagGramToString(long tagGram) {
		String result = "";
		while (tagGram != 0) {
			/*if(tagToString((byte) ((tagGram & 0xFF)))==null){
				System.out.println("XXXXXXXXXXXXXXXXXXXXX "+((tagGram & 0xFF)-1)+"\t"+tagGram);
			} */
			result = tagToString((byte) ((tagGram & 0xFF)-1)) + Helper.tagDelimiter + result;
			tagGram >>= tagBoundBitCount;
		}

		return result;
	}
}

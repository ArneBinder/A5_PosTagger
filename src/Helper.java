
import java.text.Bidi;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 24.01.13
 * Time: 22:26
 * To change this template use File | Settings | File Templates.
 */
public class Helper {
	public static final int tagBound = 128;
	public static final int tagBoundBitCount = 8;
	private static BidiMap<String, Byte> tagMap = new BidiMap<String, Byte>();
	static HashMap<Integer, Long> gramMask = new HashMap<Integer, Long>(8);


	/**
	 * max tag-value: 127
	 * min tag-value: 1
	 */

	static {
		//TODO: tagListe fuellen!
		//tagMap.put("as", (byte)0);

		gramMask.put(1, 0xFFL);
		gramMask.put(2, 0xFFFFL);
		gramMask.put(3, 0xFFFFFFL);
		gramMask.put(4, 0xFFFFFFFFL);
		gramMask.put(5, 0xFFFFFFFFFFL);
		gramMask.put(6, 0xFFFFFFFFFFFFL);
	}

	public static byte tagToByte(String tag) {
		try {
			return tagMap.get(tag);
		} catch (java.lang.NullPointerException e) {
			assert tagMap.size() + 1 < tagBound : "unable to set up new tag, tagBound="+tagBound+" reached.";
			tagMap.put(tag, (byte) (tagMap.size()+1));
			return tagMap.get(tag);
		}

	}

	public static String tagToString(byte tag) {
		return tagMap.getKey(tag);
	}

	public static String tagGramToString(long tagGram) {
		String result = "";
		while (tagGram != 0) {
			result = tagToString((byte) (tagGram & 0xFF)) + "/" + result;
			tagGram >>= tagBoundBitCount;
		}

		return result;
	}




}

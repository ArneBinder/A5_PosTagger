
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

	static HashMap<Integer, Long> gramMask = new HashMap<Integer, Long>(8);
	static final char tagDelimiter = '/';

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






}


import java.io.File;
import java.io.FilenameFilter;
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
	static final int maxGramCount = 7;
	static HashMap<Integer, Long> gramMask = new HashMap<Integer, Long>(maxGramCount);
	static final char tagDelimiter = '/';
	static final double smoothing = Math.log(0.000001);

	/**
	 * max tag-value: 127
	 * min tag-value: 1
	 */

	static {

		//tagMap.put("as", (byte)0);

		gramMask.put(1, 0xFFL);
		gramMask.put(2, 0xFFFFL);
		gramMask.put(3, 0xFFFFFFL);
		gramMask.put(4, 0xFFFFFFFFL);
		gramMask.put(5, 0xFFFFFFFFFFL);
		gramMask.put(6, 0xFFFFFFFFFFFFL);
		gramMask.put(7, 0xFFFFFFFFFFFFFFL);
	}

	public static String[] getFileList(String directoryPath){
		File dir = new File(directoryPath);
		String[] fileList = dir.list();
		return fileList;
	}

	/*public static int ipow(int base, int exp)
	{
		int result = 1;
		while (exp != 0)
		{
			if ((exp & 1) !=0)
				result *= base;
			exp >>= 1;
			base *= base;
		}

		return result;
	} */




}

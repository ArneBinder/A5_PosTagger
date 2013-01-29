import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 21.01.13
 * Time: 21:28
 * To change this template use File | Settings | File Templates.
 */
public class FeatureExtractor {
	public static int featureSize = 34;

	public FeatureVector getFeatures(Sentence sentence, int n) {

		String[] feats = new String[featureSize];
		for (int i = 0; i < feats.length; i++) {
			feats[i] = "";
		}

		//ObjectToDoubleMap<String> feats
		//		= new ObjectToDoubleMap<String>();

		boolean bos = n == 0;
		boolean eos = (n + 1) >= sentence.length();

		String tokenCat = tokenCat(n);
		String prevTokenCat = bos ? null : tokenCat(n - 1);
		String nextTokenCat = eos ? null : tokenCat(n + 1);

		String token = normToken(sentence.getWord(n));
		String prevToken = bos ? null : normToken(sentence.getWord(n - 1));
		String nextToken = eos ? null : normToken(sentence.getWord(n + 1));

		//String posTag = mPosTagging.tag(n);
		//String prevPosTag = bos ? null : mPosTagging.tag(n - 1);
		//String nextPosTag = eos ? null : mPosTagging.tag(n + 1);

		if (bos)
			//feats.set("BOS", 1.0);
			feats[0] = "1";
		//else feats[0] = "0";
		if (eos)
			//feats.set("EOS", 1.0);
			feats[1] = "1";
		//else feats[1] = "0";
		if (!bos && !eos)
			//feats.set("!BOS!EOS", 1.0);
			feats[2] = "1";
		//else feats[2] = "0";
		//feats.set("TOK_" + token, 1.0);
		feats[4] = token;
		if (!bos)
			//feats.set("TOK_PREV_" + prevToken, 1.0);
			feats[5] = prevToken;
		//else feats[5] = "";
		if (!eos)
			//feats.set("TOK_NEXT_" + nextToken, 1.0);
			feats[6] = nextToken;
		//else feats[6] = "";
		//feats.set("TOK_CAT_" + tokenCat, 1.0);
		feats[7] = tokenCat;
		if (!bos)
			//feats.set("TOK_CAT_PREV_" + prevTokenCat, 1.0);
			feats[8] = prevToken;
		//else feats[8] = "";
		if (!eos)
			//feats.set("TOK_CAT_NEXT_" + nextTokenCat, 1.0);
			feats[9] = nextTokenCat;
		//else feats[9] = "";

		/*feats.set("POS_" + posTag, 1.0);
		if (!bos)
			feats.set("POS_PREV_" + prevPosTag, 1.0);
		if (!eos)
			feats.set("POS_NEXT_" + nextPosTag, 1.0);
        */
		//List<String> suffix = suffixes(token);
		int startIndex = 10;
		int index = startIndex;
		for (String suffix : suffixes(token))
			feats[index++] = suffix;
		startIndex += MAX_SUFFIX_LENGTH;
		index = startIndex;
		for (String prefix : prefixes(token))
			feats[index++] = prefix;
		startIndex += MAX_PREFIX_LENGTH;
		index = startIndex;
		if (!bos){
			for (String suffix : suffixes(prevToken))
				feats[index++] = suffix;
			startIndex += MAX_SUFFIX_LENGTH;
			index = startIndex;
			for (String prefix : prefixes(prevToken))
				feats[index++] = prefix;
			startIndex += MAX_PREFIX_LENGTH;
			index = startIndex;
		}  else {
			startIndex += MAX_PREFIX_LENGTH +MAX_SUFFIX_LENGTH;
			index = startIndex;
		}



		if (!eos){
			for (String suffix : suffixes(nextToken))
				feats[index++] = suffix;
			startIndex += MAX_SUFFIX_LENGTH;
			index = startIndex;
			for (String prefix : prefixes(nextToken))
				feats[index++] = prefix;
			startIndex += MAX_PREFIX_LENGTH;
			index = startIndex;
		} else{
			startIndex += MAX_PREFIX_LENGTH +MAX_SUFFIX_LENGTH;
			index = startIndex;
		}

		//if(dict.getDictEntries("Short").contains(token))
		//	feats.set("DICT_SHORT", 1.0);
		//if(dict.getDictEntries("Full").contains(token))
		//	feats.set("DICT_FULL", 1.0);
		//if(dict.getDictEntries("Stop").contains(token))
		//	feats.set("STOPWORD", 1.0);
		FeatureVector featureVector = new FeatureVector(feats);
		// Todo: implement getFeatures!
		return featureVector;
	}


	public static String normToken(String token) {
		return token.replaceAll("\\d+", "*$0*").replaceAll("\\d", "D");
	}

	// e.g. 12/3/08 to *DD*/*D*/*DD*
	/*public String normedToken(int n) {
		//return token(n).replaceAll("\\d+", "*$0*").replaceAll("\\d", "D");
		return normToken(token(n));
	} */


	public String tokenCat(int n) {
		return IndoEuropeanTokenCategorizer.CATEGORIZER.categorize(token(n));
	}


	// unfolding this would go faster with less GC
	static int MAX_PREFIX_LENGTH = 4;

	static List<String> prefixes(String s) {
		int numPrefixes = Math.min(MAX_PREFIX_LENGTH, s.length());
		if (numPrefixes == 0)
			return Collections.emptyList();
		if (numPrefixes == 1)
			return Collections.singletonList(s);
		List<String> result = new ArrayList<String>(numPrefixes);
		for (int i = 1; i <= Math.min(MAX_PREFIX_LENGTH, s.length()); ++i)
			result.add(s.substring(0, i));
		return result;
	}

	// unfolding this would go faster with less GC
	static int MAX_SUFFIX_LENGTH = 4;

	static List<String> suffixes(String s) {
		int numSuffixes = Math.min(s.length(), MAX_SUFFIX_LENGTH);
		if (numSuffixes <= 0)
			return Collections.emptyList();
		if (numSuffixes == 1)
			return Collections.singletonList(s);
		List<String> result = new ArrayList<String>(numSuffixes);
		for (int i = s.length() - numSuffixes; i < s.length(); ++i)
			result.add(s.substring(i));
		return result;
	}
}

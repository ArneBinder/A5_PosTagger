import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 21.01.13
 * Time: 21:40
 * To change this template use File | Settings | File Templates.
 */
public class FeatureVector {
	public String[] features;
	//public static final byte size = 34; // ATTENTION: does not exceed 128
	FeatureVector(String[] feats){
		this.features = new String[feats.length];
		System.arraycopy(feats,0,features,0,feats.length);

	}
}

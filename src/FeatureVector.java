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
	public static final byte size = 4; // ATTENTION: does not exceed 128
	FeatureVector(){
		this.features = new String[size];
	}
}

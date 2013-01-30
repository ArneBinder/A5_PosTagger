

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Arne
 * Date: 25.01.13
 * Time: 19:12
 * To change this template use File | Settings | File Templates.
 */
public class Pair<K, V> implements Map.Entry<K, V> {
	private final K key;
	private V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	//@Override
	public K getKey() {
		return key;
	}

	//@Override
	public V getValue() {
		return value;
	}

	//@Override
	public V setValue(V value) {
		V old = this.value;
		this.value = value;
		return old;
	}

	@Override
	public boolean equals(Object other){
		if (other == null) return false;
		if (other == this) return true;
		if (!(other instanceof Pair))return false;
		//System.out.println("EQUALTest");
		return ((Pair<K, V>)other).key.equals(key) && ((Pair<K, V>)other).value.equals(value);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).
				append(key).
				append(value).
				toHashCode();
	}

}

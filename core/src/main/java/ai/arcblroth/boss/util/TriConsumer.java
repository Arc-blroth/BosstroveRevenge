package ai.arcblroth.boss.util;

@FunctionalInterface
public interface TriConsumer<K1, K2, V> {
	
	public void accept(K1 k1, K2 k2, V v);
	
}
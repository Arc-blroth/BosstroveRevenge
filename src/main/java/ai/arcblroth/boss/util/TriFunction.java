package ai.arcblroth.boss.util;

@FunctionalInterface
public interface TriFunction<T, U, V, R> {
	
	public R apply(T t, U u, V v);

}

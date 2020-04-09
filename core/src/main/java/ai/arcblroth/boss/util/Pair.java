package ai.arcblroth.boss.util;

public class Pair<T1, T2> {

	// Concurrency can cause getFirst and getSecond to be called before the constructor finishes, so we lock the object before that.
	// This prevents NullPointerExceptions, since objects are initialized to null.
	private final Object lock = new Object();
	private T1 first;
	private T2 second;
	
	public Pair(T1 first, T2 second) {
		synchronized (lock) {
			this.first = first;
			this.second = second;
		}
	}

	public T1 getFirst() {
		synchronized (lock) {
			return first;
		}
	}

	public T2 getSecond() {
		synchronized (lock) {
			return second;
		}
	}
	
}

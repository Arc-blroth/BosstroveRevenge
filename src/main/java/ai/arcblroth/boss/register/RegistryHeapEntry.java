package ai.arcblroth.boss.register;

import ai.arcblroth.boss.util.Pair;

/**
 * I have probably invoked the wrath of the Generic Gods by writing this class.<br>
 * Left in the codebase to demostrate why it is not a good idea to write code at 12am.
 * @author Arc'blroth
 */
@Deprecated
public class RegistryHeapEntry<K extends Comparable<K>, T> extends Pair<K, T> implements Comparable<RegistryHeapEntry<K, T>> {

	public RegistryHeapEntry(K first, T second) {
		super(first, second);
		if(first == null || second == null) throw new NullPointerException();
	}

	@Override
	public int compareTo(RegistryHeapEntry<K, T> entry) {
		return this.getFirst().compareTo(entry.getFirst());
	}

}

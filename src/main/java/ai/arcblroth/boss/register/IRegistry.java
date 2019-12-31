package ai.arcblroth.boss.register;

import ai.arcblroth.boss.engine.tile.FloorTile;

public interface IRegistry<K, T> {
	
	public void register(K key, T toRegister);
	
	public T getRegistered(K key);
	
}

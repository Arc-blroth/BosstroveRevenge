package ai.arcblroth.boss.engine;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.in.Keybind;

public interface IInteractable {
	
	public enum Side {NORTH, EAST, SOUTH, WEST};
	
	/**
	 * Called whenever an entity steps on edge of an object.
	 * @param s - which direction the entity came from
	 */
	public void onEntityHit(IEntity e, Side s);
	
	/**
	 * Called whenever an entity steps over an object.
	 */
	public void onEntityStep(IEntity e);
	
	/**
	 * Called whenever the player "interacts," via
	 * additional inputs, with an object. Punching, 
	 * for instance, would call this method.
	 * @param char - the input that the player used to interact with
	 */
	public void onPlayerInteract(Keybind keybind);

}
package ai.arcblroth.boss.engine.entity;

import ai.arcblroth.boss.engine.IInteractable.Direction;

public interface IDirected {
	
	public Direction getDirection();
	
	public void setDirection(Direction dir);
	
}

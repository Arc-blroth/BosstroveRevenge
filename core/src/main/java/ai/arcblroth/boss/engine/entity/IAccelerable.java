package ai.arcblroth.boss.engine.entity;

import ai.arcblroth.boss.engine.Direction;
import ai.arcblroth.boss.util.Vector2D;

public interface IAccelerable {

	public void accelerate(Direction d, double magnitude);
	
	public void accelerate(Vector2D vector);
	
	public void setAccelerationVector(Vector2D accel);
	
	public Vector2D getAccelerationVector();
	
	public double getFrictionFactor();
	
	public Direction getDirection();
	
	public void setDirection(Direction dir);
	
}

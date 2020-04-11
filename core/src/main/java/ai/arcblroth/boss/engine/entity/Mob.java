package ai.arcblroth.boss.engine.entity;

import ai.arcblroth.boss.engine.Direction;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.Vector2D;

public abstract class Mob implements IEntity, IMortal, IAccelerable {

	private Texture texture;
	private Position pos;
	private double health;
	private Direction dir;
	private Vector2D accel;
	private double friction;
	
	protected Mob(Texture texture, Position initialPos, double frictionFactor, double initialHealth) {
		this.texture = texture;
		this.pos = initialPos;
		this.health = initialHealth;
		this.accel = new Vector2D(0D, 0D);
		this.friction = frictionFactor;
		this.dir = Direction.NORTH;
	}
	
	@Override
	public void onStep() {
		
	}
	
	@Override
	public void onEntityHit(IEntity e, Direction s) {
		
	}

	@Override
	public void onEntityStep(IEntity e) {
		
	}

	@Override
	public void onPlayerInteract(Keybind keybind) {
		
	}

	@Override
	public Texture getTexture() {
		return texture;
	}

	@Override
	public Position getPosition() {
		return pos;
	}

	@Override
	public void setPosition(Position pos) {
		this.pos = pos;
	}

	@Override
	public void accelerate(Direction d, double magnitude) {
		if(d == Direction.NORTH) {accelerate(new Vector2D( 0D,        -magnitude));}
		if(d == Direction.SOUTH) {accelerate(new Vector2D( 0D,         magnitude));}
		if(d == Direction.EAST ) {accelerate(new Vector2D( magnitude,  0D       ));}
		if(d == Direction.WEST ) {accelerate(new Vector2D(-magnitude,  0D       ));}
	}
	
	@Override
	public void accelerate(Vector2D vector) {
		this.accel = this.accel.add(vector);
	}
	
	@Override
	public void setAccelerationVector(Vector2D accel) {
		this.accel = accel;
	}

	@Override
	public Vector2D getAccelerationVector() {
		return accel;
	}
	
	public double getFrictionFactor() {
		return this.friction;
	}

	@Override
	public double getHealth() {
		return health;
	}

	@Override
	public void setHealth(double health) {
		this.health = health;
	}

	@Override
	public void damage(double baseDamage) {
		health -= baseDamage;
	}

	@Override
	public void heal(double baseHealth) {
		health += baseHealth;
	}

	@Override
	public Hitbox getHitbox() {
		return new Hitbox(0, 0, 0, 0).resolveRelativeTo(pos);
	}

	@Override
	public Direction getDirection() {
		return dir;
	}

	@Override
	public void setDirection(Direction dir) {
		this.dir = dir;
	}
	
}

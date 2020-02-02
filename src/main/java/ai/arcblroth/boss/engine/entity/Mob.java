package ai.arcblroth.boss.engine.entity;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.IInteractable.Direction;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.util.TextureUtils;

public class Mob implements IEntity, IMortal, IDirected {

	private Texture texture;
	private Position pos;
	private double health;
	private Direction dir;
	
	public Mob(Texture texture, Position initialPos, double initialHealth) {
		this.texture = texture;
		this.pos = initialPos;
		this.health = initialHealth;
		this.dir = Direction.NORTH;
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

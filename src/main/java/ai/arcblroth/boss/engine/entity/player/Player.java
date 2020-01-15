package ai.arcblroth.boss.engine.entity.player;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.IHitboxed;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.entity.*;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.util.StaticDefaults;

public class Player implements IEntity, IMortal, IDirected {
	
	private Position pos;
	private double health;
	private Direction dir;

	public Player(Position initialPos, double initialHealth) {
		this.pos = initialPos;
		this.health = initialHealth;
		this.dir = Direction.NORTH;
	}
	
	@Override
	public void onEntityHit(IEntity e, Direction s) {
		
	}

	@Override
	public void onEntityStep(IEntity e) {
		System.out.println("Stepped entity " + e);
	}

	@Override
	public void onPlayerInteract(Keybind keybind) {
		// "Stop playing with yourself!"
	}

	@Override
	public Texture getTexture() {
		return BosstrovesRevenge.get().getTextureCache().get(new InternalResource("yeet.png"));
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
		return new Hitbox(-0.5, -0.5, 1, 1).resolveRelativeTo(pos);
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

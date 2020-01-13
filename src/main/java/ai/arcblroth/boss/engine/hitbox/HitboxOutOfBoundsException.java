package ai.arcblroth.boss.engine.hitbox;

public class HitboxOutOfBoundsException extends Exception {
	
	public HitboxOutOfBoundsException(Hitbox h, Hitbox bounds) {
		super("Hitbox " + h.toString() + " is out of the bounds " + bounds.toString());
	}
	
}

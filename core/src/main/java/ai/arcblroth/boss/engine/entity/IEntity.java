package ai.arcblroth.boss.engine.entity;

import ai.arcblroth.boss.engine.IHitboxed;
import ai.arcblroth.boss.engine.IInteractable;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.render.Texture;

public interface IEntity extends IRegistrable<IEntity>, IInteractable, IHitboxed {

	public String getId();

	public Texture getTexture();
	
	public Position getPosition();

	/**
	 * Gets the absolute hitbox, or the hitbox after a resolveRelativeTo
	 * this entity's position, of this entity
	 * @return absolute hitbox
	 */
	public Hitbox getHitbox();
	
	public void setPosition(Position pos);
	
}

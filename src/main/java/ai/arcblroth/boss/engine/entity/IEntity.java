package ai.arcblroth.boss.engine.entity;

import ai.arcblroth.boss.engine.IInteractable;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.register.*;
import ai.arcblroth.boss.render.Texture;

public interface IEntity extends IRegistrable<IEntity>, IInteractable {

	public Texture getTexture();
	
	public Position getPosition();
	
}

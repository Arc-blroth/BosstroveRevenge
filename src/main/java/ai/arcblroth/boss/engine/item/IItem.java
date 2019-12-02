package ai.arcblroth.boss.engine.item;

import ai.arcblroth.boss.register.IRegistrable;
import ai.arcblroth.boss.render.Texture;

public interface IItem extends IRegistrable<IItem> {
	
	public Texture getTexture();
	
}

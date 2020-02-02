package ai.arcblroth.boss.engine.entity;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.resource.InternalResource;

public class Xulpir extends Mob {

	public Xulpir(Position initialPos, double initialHealth) {
		super(BosstrovesRevenge.get().getTextureCache().get(new InternalResource("data/texture/entity/xulpir.btex")), initialPos, initialHealth);
	}
	
	@Override
	public Hitbox getHitbox() {
		return new Hitbox(-0.5, -0.5, 1, 1).resolveRelativeTo(super.getPosition());
	}

}

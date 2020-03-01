package ai.arcblroth.boss.game.entity;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.entity.Mob;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.resource.InternalResource;

public class Xulpir extends Mob {

	public Xulpir(Position initialPos) {
		super(
				BosstrovesRevenge.instance().getTextureCache().get(new InternalResource("data/texture/entity/xulpir.btex")),
				initialPos,
				1,
				20
		);
	}
	
	@Override
	public Hitbox getHitbox() {
		return new Hitbox(-0.5, -0.5, 1, 1).resolveRelativeTo(super.getPosition());
	}

}

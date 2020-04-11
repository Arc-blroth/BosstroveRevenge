package ai.arcblroth.boss.game.entity;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.Mob;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.resource.InternalResource;

public class Xulpir extends Mob {

	private int hurtPlayerCooldown = 0;

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
		return new Hitbox(-1, -1, 2, 2).resolveRelativeTo(super.getPosition());
	}

	@Override
	public void onEntityStep(IEntity e) {
		if(e instanceof Player) {
			hurtPlayerCooldown--;
			if(hurtPlayerCooldown <= 0) {
				((Player) e).setHealth(((Player) e).getHealth() - 1);
				hurtPlayerCooldown = 5;
			}
		}
	}

	@Override
	public String getId() {
		return "boss.xulpir";
	}
}

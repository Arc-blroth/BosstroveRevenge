package ai.arcblroth.boss.engine.area;

import ai.arcblroth.boss.engine.Direction;
import ai.arcblroth.boss.engine.IHitboxed;
import ai.arcblroth.boss.engine.IInteractable;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.register.IRegistrable;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * An Area is an invisible hitbox that players and entities can interact with.
 */
public abstract class Area implements IRegistrable<Area>, IInteractable, IHitboxed {

	private Room room;
	private Hitbox hitbox;

	public Area(Room room, Hitbox hitbox, JsonObject context) {
		this.room = room;
		this.hitbox = hitbox;
	}

	@Override
	public void onStep() {}

	@Override
	public void onEntityHit(IEntity e, Direction d) {}

	@Override
	public void onEntityStep(IEntity e) {}

	@Override
	public void onPlayerInteract(Keybind keybind) {}

	public Room getRoom() {
		return room;
	}

	@Override
	public Hitbox getHitbox() {
		return hitbox;
	}

	public void setHitbox(Hitbox hitbox) {
		this.hitbox = Objects.requireNonNull(hitbox);
	}

	public abstract String getId();

}

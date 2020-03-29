package ai.arcblroth.boss.register;

import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.util.TriFunction;

public abstract class EntityBuilder<E extends IEntity> implements TriFunction<Room, Position, JsonObject, E> {

	public abstract E build(Room room, Position position, JsonObject context);
	
	@Override
	public final E apply(Room room, Position position, JsonObject context) {
		return build(room, position, context);
	}

}

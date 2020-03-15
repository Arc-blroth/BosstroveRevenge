package ai.arcblroth.boss.register;

import com.google.gson.JsonObject;

import java.util.concurrent.ConcurrentHashMap;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.TriConsumer;

public class EntityRegistry {
	
	private static final EntityRegistry INSTANCE = new EntityRegistry();
	private final ConcurrentHashMap<String, Pair<Class<? extends IEntity>, EntityBuilder<? extends IEntity>>> map;
	
	private EntityRegistry() {
		map = new ConcurrentHashMap<>();
	}
	
	public static EntityRegistry instance() {
		return INSTANCE;
	}
	
	public IEntity buildEntity(String entityId, Room room, Position position, JsonObject context) {
		return map.get(entityId).getSecond().build(room, position, context);
	}

	public void register(String key, Pair<Class<? extends IEntity>, EntityBuilder<? extends IEntity>> entityDef) {
		map.put(key, entityDef);
	}

	public void forEach(TriConsumer<String, Class<? extends IEntity>, EntityBuilder<? extends IEntity>> action) {
		map.forEach((key, value) -> action.accept(key, value.getFirst(), value.getSecond()));
	}
	
}

package ai.arcblroth.boss.register;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.util.Pair;

public class EntityRegistry extends ConcurrentHashMap<String, Pair<Class<? extends IEntity>, EntityBuilder<? extends IEntity>>> {
	
	private static final EntityRegistry INSTANCE = new EntityRegistry();
	private Gson gson;
	
	private EntityRegistry() {
		super();
		gson = new Gson();
	}
	
	public static EntityRegistry instance() {
		return INSTANCE;
	}
	
	public IEntity buildEntity(String entityId, Room room, Position position, JsonObject context) {
		return get(entityId).getSecond().build(room, position, context);
	}

	public void register(String key, Pair<Class<? extends IEntity>, EntityBuilder<? extends IEntity>> entityDef) {
		put(key, entityDef);
	}
	
}

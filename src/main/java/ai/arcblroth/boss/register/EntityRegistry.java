package ai.arcblroth.boss.register;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;

import java.util.concurrent.ConcurrentHashMap;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.util.Pair;

public class EntityRegistry extends ConcurrentHashMap<String, Pair<Class<? extends IEntity>, JsonDeserializer<? extends IEntity>>>
		implements IRegistry<String, Pair<Class<? extends IEntity>, JsonDeserializer<? extends IEntity>>> {
	
	private static final EntityRegistry INSTANCE = new EntityRegistry();
	private Object gsonLock;
	private final GsonBuilder gsonBuilder;
	private Gson gson;
	
	private EntityRegistry() {
		super();
		gsonLock = new Object();
		gsonBuilder = new GsonBuilder();
		gson = gsonBuilder.create();
	}
	
	public static EntityRegistry instance() {
		return INSTANCE;
	}
	
	public IEntity buildEntity(String entityId, JsonObject parameters) {
		synchronized(gsonLock) {
			return gson.fromJson(parameters, get(entityId).getFirst());
		}
	}
	
	@Override
	public Pair<Class<? extends IEntity>, JsonDeserializer<? extends IEntity>> getRegistered(String key) {
		return get(key);
	}

	@Override
	public void register(String key, Pair<Class<? extends IEntity>, JsonDeserializer<? extends IEntity>> entityDef) {
		synchronized(gsonLock) {
			put(key, entityDef);
			gsonBuilder.registerTypeAdapter(entityDef.getFirst(), entityDef.getSecond());
			gson = gsonBuilder.create();
		}
	}
	
}

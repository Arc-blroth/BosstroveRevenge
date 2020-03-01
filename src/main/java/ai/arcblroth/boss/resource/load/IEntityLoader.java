package ai.arcblroth.boss.resource.load;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.tile.EmptyFloorTile;
import ai.arcblroth.boss.engine.tile.EmptyWallTile;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.register.EntityRegistry;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.render.PixelGrid;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.load.exception.MalformedSpecificationException;
import ai.arcblroth.boss.resource.load.exception.UnsupportedSpecificationVersionException;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.TextureUtils;

public final class IEntityLoader extends AbstractIRegisterableLoader {
	
	public static final String BENT_EXTENSION = ".bent";
	private final Logger logger;
	
	public IEntityLoader() {
		this.logger = Logger.getLogger("IEntityLoader");
	}
	
	@Override
	public boolean accepts(Resource specification) {
		return specification.getPath().endsWith(BENT_EXTENSION);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void register(Gson gson, Resource specification) {
		if(!accepts(specification)) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is not a .bent file.");
			return;
		}
		if(!specification.exists()) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is does not exist.");
			return;
		}
		
		try {
			JsonObject bent = gson.fromJson(ResourceLoader.loadTextFile(specification), JsonObject.class);
			try {
				JsonElement versionIdEle = bent.get("versionId");
				long versionId = versionIdEle.getAsLong();
				if(versionId == 1) {
					
					String entityId = bent.get("entityId").getAsString();
					String classString = bent.get("class").getAsString();
					String deserializerString = bent.get("deserializer").getAsString();
					
					Class<?> entityClass0 = Class.forName(classString);
					if(!IEntity.class.isAssignableFrom(entityClass0)) {
						throw new IllegalArgumentException("class must implement IEntity");
					}
					Class<? extends IEntity> entityClass = (Class<? extends IEntity>)entityClass0;
					
					Class<?> deserializerClass0 = Class.forName(deserializerString);
					if(!(
							JsonDeserializer.class.isAssignableFrom(deserializerClass0)
							&& deserializerClass0.getMethod("deserialize", JsonElement.class, Type.class, JsonDeserializationContext.class).getReturnType().equals(entityClass)
						)) {
						throw new IllegalArgumentException("deserializer must implement JsonDeserializer<class>");
					}
					Class<? extends JsonDeserializer<? extends IEntity>> deserializerClass = (Class<? extends JsonDeserializer<? extends IEntity>>)deserializerClass0;
					
					EntityRegistry.instance().register(entityId, 
							new Pair<Class<? extends IEntity>, JsonDeserializer<? extends IEntity>>(entityClass, deserializerClass.newInstance()));
					
				} else {
					throw new UnsupportedSpecificationVersionException(versionId, BENT_EXTENSION);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not load .bent resource " + specification.toString() + ": ", e);
			}
		} catch (JsonSyntaxException e) {
			logger.log(Level.WARNING, "Could not load .bent resource " + specification.toString(), e);
		} catch (NullPointerException | IOException e) {
				logger.log(Level.WARNING, "Could not load .bent resource " + specification.toString(), e);
		}
	}


}

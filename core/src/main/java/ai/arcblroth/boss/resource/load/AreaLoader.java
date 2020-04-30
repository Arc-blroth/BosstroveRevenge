package ai.arcblroth.boss.resource.load;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.area.Area;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.register.AreaRegistry;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.load.exception.UnsupportedSpecificationVersionException;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AreaLoader extends AbstractIRegisterableLoader {

	public static final String BAREA_EXTENSION = ".barea";
	private final Logger logger;

	public AreaLoader() {
		this.logger = Logger.getLogger("AreaLoader");
	}

	@Override
	public boolean accepts(Resource specification) {
		return specification.getPath().endsWith(BAREA_EXTENSION);
	}

	@Override
	public void register(Gson gson, Resource specification) {
		if(!accepts(specification)) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is not a .barea file.");
			return;
		}
		if(!specification.exists()) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is does not exist.");
			return;
		}

		try {
			JsonObject barea = gson.fromJson(ResourceLoader.loadTextFile(specification), JsonObject.class);
			try {
				JsonElement versionIdEle = barea.get("versionId");
				long versionId = versionIdEle.getAsLong();
				if(versionId == 1) {

					String areaId = barea.get("areaId").getAsString();
					String classString = barea.get("class").getAsString();

					Class<?> areaClass0 = Class.forName(classString);
					if(!Area.class.isAssignableFrom(areaClass0)) {
						throw new IllegalArgumentException("class must extend Area");
					}
					if(Modifier.isAbstract(areaClass0.getModifiers())) {
						throw new IllegalArgumentException("class must not be abstract");
					}
					Class<? extends Area>areaClass = (Class<? extends Area>)areaClass0;

					Constructor<? extends Area> areaConstructor = areaClass.getConstructor(Room.class, Hitbox.class, JsonObject.class);

					AreaRegistry.instance().register(areaId, (room, hitbox, context) -> {
						try {
							return areaConstructor.newInstance(room, hitbox, context);
						} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
							logger.log(Level.SEVERE, "Could not load '" + areaId + "' area due to uncaught exception: ", e);
							return null;
						}
					});

				} else {
					throw new UnsupportedSpecificationVersionException(versionId, BAREA_EXTENSION);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not load .barea resource " + specification.toString() + ": ", e);
			}
		} catch (JsonSyntaxException | NullPointerException | IOException e) {
			logger.log(Level.WARNING, "Could not load .barea resource " + specification.toString(), e);
		}

	}
}

package ai.arcblroth.boss.resource.load;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.register.LevelRegistry;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.load.exception.UnsupportedSpecificationVersionException;
import com.google.gson.*;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ILevelLoader extends AbstractIRegisterableLoader {
	
	public static final String BLVL_EXTENSION = ".blvl";
	private TextureCache cache;
	private final Logger logger;
	
	public ILevelLoader(TextureCache cache) {
		this.cache = cache;
		this.logger = Logger.getLogger("ILevelLoader");
	}
	
	@Override
	public boolean accepts(Resource specification) {
		return specification.getPath().endsWith(BLVL_EXTENSION);
	}
	
	@Override
	public void register(Gson gson, Resource specification) {
		if(!accepts(specification)) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is not a .blvl file.");
			return;
		}
		if(!specification.exists()) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is does not exist.");
			return;
		}
		
		try {
			JsonObject blvl = gson.fromJson(ResourceLoader.loadTextFile(specification), JsonObject.class);
			try {
				JsonElement versionIdEle = blvl.get("versionId");
				long versionId = versionIdEle.getAsLong();
				if(versionId == 1) {
					
					String levelId = blvl.get("levelId").getAsString();
					String worldString = blvl.get("world").getAsString();
					String levelString = blvl.get("level").getAsString();
					String titleString = blvl.get("title").getAsString();

					Color introBackgroundColor, introForegroundColor;
					if(blvl.has("introBackgroundColor")) {
						introBackgroundColor = new Color(Integer.parseUnsignedInt("ff" + blvl.get("introBackgroundColor").getAsString().replace("#", ""), 16));
					} else {
						introBackgroundColor = Color.BLACK;
					}
					if(blvl.has("introForegroundColor")) {
						introForegroundColor = new Color(Integer.parseUnsignedInt("ff" + blvl.get("introForegroundColor").getAsString().replace("#", ""), 16));
					} else {
						introForegroundColor = Color.WHITE;
					}

					String initialRoom0 = blvl.get("initialRoom").getAsString();

					JsonArray palette = blvl.has("palette") ? blvl.get("palette").getAsJsonArray() : new JsonArray();

					Map<String, Room> rooms = RoomLoader.loadRooms(blvl.get("rooms").getAsJsonArray(), palette);
					if(!rooms.containsKey(initialRoom0)) {
						String[] s = new String[1];
						rooms.keySet().toArray(s);
						initialRoom0 = s[0];
					}

					final String initialRoom = initialRoom0;

					LevelRegistry.instance().register(levelId, worldEngine -> {
						ai.arcblroth.boss.engine.Level level = new ai.arcblroth.boss.engine.Level(worldString, levelString, titleString, introBackgroundColor, introForegroundColor, worldEngine);
						rooms.forEach(level::addRoom);
						level.setInitialRoom(initialRoom);
						return level;
					});
					
				} else {
					throw new UnsupportedSpecificationVersionException(versionId, BLVL_EXTENSION);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not load .blvl resource " + specification.toString() + ": ", e);
			}
		} catch (JsonSyntaxException e) {
			logger.log(Level.WARNING, "Could not load .blvl resource " + specification.toString(), e);
		} catch (NullPointerException | IOException e) {
				logger.log(Level.WARNING, "Could not load .blvl resource " + specification.toString(), e);
		}
	}


}

package ai.arcblroth.boss.resource.load;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.load.exception.UnsupportedSpecificationVersionException;

public final class ILevelLoader extends AbstractIRegisterableLoader {
	
	public static final String BLVL_EXTENSION = ".blvl";
	private TextureCache cache;
	private final Gson gson;
	private final Logger logger;
	
	public ILevelLoader(TextureCache cache) {
		this.cache = cache;
		this.gson = new Gson();
		this.logger = Logger.getLogger("ILevelLoader");
	}
	
	@Override
	public boolean accepts(Resource specification) {
		return specification.getPath().endsWith(BLVL_EXTENSION);
	}
	
	@Override
	public void register(Resource specification) {
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

package ai.arcblroth.boss.resource.load;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import ai.arcblroth.boss.render.AnimatedTexture;
import ai.arcblroth.boss.render.MultiFrameTexture;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.load.exception.MalformedSpecificationException;
import ai.arcblroth.boss.resource.load.exception.UnsupportedSpecificationVersionException;
import ai.arcblroth.boss.util.TextureUtils;

public class MultiFrameTextureLoader {

	public static final String BTEX_EXTENSION = ".btex";
	private TextureCache cache;
	private final Gson gson;
	private final Logger logger;
	
	public MultiFrameTextureLoader(TextureCache cache) {
		this.cache = cache;
		this.gson = new Gson();
		this.logger = Logger.getLogger("MultiFrameTextureLoader");
	}
	
	public boolean accepts(Resource specification) {
		return specification.getPath().endsWith(BTEX_EXTENSION);
	}

	public MultiFrameTexture register(Resource specification) {
		if(!accepts(specification)) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is not a .btex file.");
			return null;
		}
		if(!specification.exists()) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is does not exist.");
			return null;
		}
		
		try {
			JsonObject btex = gson.fromJson(ResourceLoader.loadTextFile(specification), JsonObject.class);
			try {
				JsonElement versionIdEle = btex.get("versionId");
				long versionId = versionIdEle.getAsLong();
				if(versionId == 1) {

					int frames = btex.get("frames").getAsInt();
					int width = btex.get("width").getAsInt();
					int height = btex.get("height").getAsInt();
					boolean animated = btex.has("animated") ? btex.get("animated").getAsBoolean() : false;
					
					Resource spritesheetRes = new InternalResource(btex.get("spritesheet").getAsString());
					if(!spritesheetRes.exists()) throw new MalformedSpecificationException("Spritesheet does not point to a valid file.");
					Texture spritesheet = ResourceLoader.loadPNG(spritesheetRes);
					
					int framesWidth = (int)Math.floor(spritesheet.getWidth() / (double)width);
					int framesHeight = (int)Math.floor(spritesheet.getHeight() / (double)height);
					if(framesWidth * framesHeight < frames) {
						throw new MalformedSpecificationException("Expected " + frames + " frames, got " + framesWidth + " x " + framesHeight
								+ " = " + (framesWidth * framesHeight) + " frames");
					}
					
					Texture[] spritesheetFrames = new Texture[frames];
					int currentSpritesheetFrame = 0;
					for(int offY = 0; offY < framesHeight && currentSpritesheetFrame < frames; offY++) {
						for(int offX = 0; offX < framesWidth && currentSpritesheetFrame < frames; offX++) {
							spritesheetFrames[currentSpritesheetFrame] = new Texture(TextureUtils.sub(spritesheet, offX * width, offY * height, width, height));
							currentSpritesheetFrame++;
						}
					}
					
					if(animated) {
						int stepsPerFrame = btex.has("rate") ? btex.get("rate").getAsInt() : 1;
						return new AnimatedTexture(spritesheetFrames, stepsPerFrame);
					} else {
						return new MultiFrameTexture(spritesheetFrames);
					}
					
				} else {
					throw new UnsupportedSpecificationVersionException(versionId, BTEX_EXTENSION);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not load .btex resource " + specification.toString() + ": ", e);
				return null;
			}
		} catch (JsonSyntaxException e) {
			logger.log(Level.WARNING, "Could not load .btex resource " + specification.toString(), e);
			return null;
		} catch (NullPointerException | IOException e) {
			logger.log(Level.WARNING, "Could not load .btex resource " + specification.toString(), e);
			return null;
		}
	}
	
}

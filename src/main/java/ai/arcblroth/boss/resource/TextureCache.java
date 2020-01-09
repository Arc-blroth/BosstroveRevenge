package ai.arcblroth.boss.resource;

import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.load.ResourceLoader;

public final class TextureCache {
	
	private final TreeMap<Resource, Texture> cache;
	private final Logger logger;
	
	public TextureCache() {
		this.cache = new TreeMap<Resource, Texture>();
		this.logger = Logger.getLogger("TextureCache");
	}
	
	public void add(Resource key, Texture texture) {
		cache.put(key, texture);
	}
	
	/**
	 * Retrieves the texture located at the specified resource,
	 * loading it if it is not already cached.
	 * 
	 * @param key location of texture.
	 * @return texture at resource, or null if it doesn't exist.
	 */
	public Texture get(Resource key) {
		if(cache.containsKey(key)) {
			return cache.get(key);
		} else {
			try {
				Texture loadedTexture = ResourceLoader.loadPNG(key);
				cache.put(key, loadedTexture);
				return loadedTexture;
			} catch (NullPointerException e) {
				logger.log(Level.WARNING, "Could not load texture at " + key.getPath() + ": ", e);
				return null;
			}
		}
	}
	
}

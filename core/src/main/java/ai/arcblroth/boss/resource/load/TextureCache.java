package ai.arcblroth.boss.resource.load;

import java.lang.ref.WeakReference;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ai.arcblroth.boss.render.AnimatedTexture;
import ai.arcblroth.boss.render.MultiFrameTexture;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.Resource;

public final class TextureCache {
	
	private final TreeMap<Resource, Texture> cache;
	private final TreeMap<Resource, AnimatedTexture> animatedTextureCache;
	private final Logger logger;
	private MultiFrameTextureLoader multiFrameTextureLoader;
	
	public TextureCache() {
		this.cache = new TreeMap<Resource, Texture>();
		this.animatedTextureCache = new TreeMap<>();
		this.logger = Logger.getLogger("TextureCache");
		this.multiFrameTextureLoader = new MultiFrameTextureLoader(this);
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
			if(multiFrameTextureLoader.accepts(key)) {
				MultiFrameTexture loadedTexture = multiFrameTextureLoader.register(key);
				cache.put(key, loadedTexture);
				if(loadedTexture instanceof AnimatedTexture) {
					animatedTextureCache.put(key, (AnimatedTexture) loadedTexture);
				}
				return loadedTexture;
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

	public void stepAnimatedTextures() {
		animatedTextureCache.forEach((resource, texture) -> {
			texture.advanceFrame();
		});
	}
	
}

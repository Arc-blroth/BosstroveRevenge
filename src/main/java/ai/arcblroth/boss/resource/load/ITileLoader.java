package ai.arcblroth.boss.resource.load;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.WallTile;
import ai.arcblroth.boss.register.FloorTileBuilder;
import ai.arcblroth.boss.register.FloorTileRegistry;
import ai.arcblroth.boss.register.WallTileBuilder;
import ai.arcblroth.boss.register.WallTileRegistry;
import ai.arcblroth.boss.render.PixelGrid;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.resource.Resource;
import ai.arcblroth.boss.resource.load.exception.MalformedSpecificationException;
import ai.arcblroth.boss.resource.load.exception.UnsupportedSpecificationVersionException;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.TextureUtils;

public final class ITileLoader extends AbstractIRegisterableLoader {
	
	public static final String BTILE_EXTENSION = ".btile";
	private TextureCache cache;
	private final Logger logger;
	
	public ITileLoader(TextureCache cache) {
		this.cache = cache;
		this.logger = Logger.getLogger("ITileLoader");
	}
	
	@Override
	public boolean accepts(Resource specification) {
		return specification.getPath().endsWith(BTILE_EXTENSION);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void register(Gson gson, Resource specification) {
		if(!accepts(specification)) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is not a .btile file.");
			return;
		}
		if(!specification.exists()) {
			logger.log(Level.WARNING, "Refusing to load resource " + specification.toString() + " as it is does not exist.");
			return;
		}
		
		try {
			JsonObject btile = gson.fromJson(ResourceLoader.loadTextFile(specification), JsonObject.class);
			try {
				JsonElement versionIdEle = btile.get("versionId");
				long versionId = versionIdEle.getAsLong();
				if(versionId == 1) {
					
					final String tileId = btile.get("tileId").getAsString().trim();
					final String tileType = btile.get("tileType").getAsString().trim().toLowerCase();
					if(!tileType.equals("floortile") && !tileType.equals("walltile")) {
						throw new MalformedSpecificationException("tileType", specification);
					}
					
					// Resolve texture
					// This is done last to ensure that we don't load textures
					// for broken specifications.
					Texture tempTexture = StaticDefaults.DEFAULT_TEXTURE;
					if(btile.has("texture")) {
						if(btile.get("texture").isJsonArray()) {
							PixelGrid overlaidTexture = new PixelGrid(StaticDefaults.TILE_WIDTH, StaticDefaults.TILE_HEIGHT);
							JsonArray textureLocations = btile.get("texture").getAsJsonArray();
							for(int i = textureLocations.size() - 1; i >= 0; i--) {
								Texture specifiedTexture = cache.get(new InternalResource(textureLocations.get(i).getAsString()));
								if(specifiedTexture == null) specifiedTexture = StaticDefaults.DEFAULT_TEXTURE;
								
								// First overlay existing texture onto a bigger texture so that larger textures
								// are not clipped.
								if(specifiedTexture.getWidth() > overlaidTexture.getWidth()
										|| specifiedTexture.getHeight() > overlaidTexture.getHeight()) {
									overlaidTexture = TextureUtils.overlay(overlaidTexture, new PixelGrid(
											Math.max(specifiedTexture.getWidth(), overlaidTexture.getWidth()),
											Math.max(specifiedTexture.getHeight(), overlaidTexture.getHeight())
									));
								}
								
								overlaidTexture = TextureUtils.overlay(specifiedTexture, overlaidTexture);
							}
							tempTexture = new Texture(overlaidTexture);
						} else {
							Texture specifiedTexture = cache.get(new InternalResource(btile.get("texture").getAsString()));
							if(specifiedTexture != null) tempTexture = specifiedTexture;
						}
					}
					final Texture texture = tempTexture;
					
					if(tileType.equals("floortile")) {
						
						// Attempt to resolve builder
						boolean useCustomBuilder = false;
						Class<? extends FloorTileBuilder<? extends FloorTile>> builder = null;
						if(btile.has("builder")) {
							try {
								Class<?> builder0 = Class.forName(btile.get("builder").getAsString());
								if(FloorTileBuilder.class.isAssignableFrom(builder0)) {
									builder = (Class<? extends FloorTileBuilder<? extends FloorTile>>)builder0;
									useCustomBuilder = true;
								}
							} catch (Exception e) {
								logger.log(Level.WARNING, "Could not load builder for floor tile \"" + tileId + "\". Using default builder.", e);
							}
						}
						
						if(useCustomBuilder) {
							FloorTileRegistry.instance().register(tileId, builder.getConstructor(Texture.class).newInstance(texture));
						} else {
							FloorTileRegistry.instance().register(tileId, texture, (room, tilePos) -> {
								
								final boolean isPassable = btile.get("passable").getAsBoolean();
								final double viscosity = btile.get("viscosity").getAsDouble();
								
								return new FloorTile(room, tilePos, texture) {
	
									@Override
									public boolean isPassable() {
										return isPassable;
									}
	
									@Override
									public double getViscosity() {
										return viscosity;
									};
								};
							});
						}
						
					} else if(tileType.equals("walltile")) {
						
						// Attempt to resolve builder
						boolean useCustomBuilder = false;
						Class<? extends WallTileBuilder<? extends WallTile>> builder = null;
						if(btile.has("builder")) {
							try {
								Class<?> builder0 = Class.forName(btile.get("builder").getAsString());
								if(WallTileBuilder.class.isAssignableFrom(builder0)) {
									builder = (Class<? extends WallTileBuilder<? extends WallTile>>)builder0;
									useCustomBuilder = true;
								}
							} catch (Exception e) {
								logger.log(Level.WARNING, "Could not load builder for wall tile \"" + tileId + "\". Using default builder.", e);
							}
						}
						
						if(useCustomBuilder) {
							WallTileRegistry.instance().register(tileId, builder.getConstructor(Texture.class).newInstance(texture));
						} else {
							
							final boolean isPassable = btile.get("passable").getAsBoolean();
							final double viscosity = btile.get("viscosity").getAsDouble();
							
							WallTileRegistry.instance().register(tileId, texture, (room, tilePos) -> {
								return new WallTile(room, tilePos, texture) {
	
									@Override
									public boolean isPassable() {
										return isPassable;
									}
	
									@Override
									public double getViscosity() {
										return viscosity;
									}
									
								};
							});
						}
						
					} else {
						// This should be impossible, because of the earlier check.
						RuntimeException re = new RuntimeException("tileType is a quantum superposition of every and no tile type");
						re.initCause(new MalformedSpecificationException("tileType", specification));
						throw re;
					}
				} else {
					throw new UnsupportedSpecificationVersionException(versionId, BTILE_EXTENSION);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not load .btile resource " + specification.toString() + ": ", e);
			}
		} catch (JsonSyntaxException e) {
			logger.log(Level.WARNING, "Could not load .btile resource " + specification.toString(), e);
		} catch (NullPointerException | IOException e) {
				logger.log(Level.WARNING, "Could not load .btile resource " + specification.toString(), e);
		}
	}


}

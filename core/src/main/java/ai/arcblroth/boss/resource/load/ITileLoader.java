package ai.arcblroth.boss.resource.load;

import ai.arcblroth.boss.engine.Direction;
import ai.arcblroth.boss.engine.tile.FloorTile;
import ai.arcblroth.boss.engine.tile.SmartFloorTile;
import ai.arcblroth.boss.engine.tile.SmartWallTile;
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
import com.google.gson.*;

import java.io.IOException;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
					final boolean smart = btile.has("smart") && btile.get("smart").getAsBoolean();

					// Resolve texture
					// This is done last to ensure that we don't load textures
					// for broken specifications.
					if(!smart) {
						Texture texture;
						if(btile.has("texture")) {
							texture = resolveTexture(btile.get("texture"));
						} else {
							texture = StaticDefaults.DEFAULT_TEXTURE;
							logger.log(Level.WARNING, tileType + " \"" + tileId + "\" does not specify a texture!");
						}

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

								final boolean isPassable = btile.has("passable") && btile.get("passable").getAsBoolean();
								final double viscosity = btile.has("viscosity") ? btile.get("viscosity").getAsDouble() : 0.0D;

								FloorTileRegistry.instance().register(tileId, texture, (room, tilePos) -> new FloorTile(room, tilePos, texture) {

									@Override
									public String getId() {
										return tileId;
									}

									@Override
									public boolean isPassable() {
										return isPassable;
									}

									@Override
									public double getViscosity() {
										return viscosity;
									}

								});
							}
						} else {
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

								final boolean isPassable = btile.has("passable") && btile.get("passable").getAsBoolean();
								final double viscosity = btile.has("viscosity") ? btile.get("viscosity").getAsDouble() : 0.0D;

								WallTileRegistry.instance().register(tileId, texture, (room, tilePos) -> new WallTile(room, tilePos, texture) {

									@Override
									public String getId() {
										return tileId;
									}

									@Override
									public boolean isPassable() {
										return isPassable;
									}

									@Override
									public double getViscosity() {
										return viscosity;
									}

								});
							}
						}
					} else {
						final boolean isPassable = btile.has("passable") && btile.get("passable").getAsBoolean();
						final double viscosity = btile.has("viscosity") ? btile.get("viscosity").getAsDouble() : 0.0D;

						TreeMap<Byte, Texture> directionTextureMappings = new TreeMap<>();
						if(btile.has("texture")) {
							if(btile.get("texture").isJsonArray()) {
								btile.get("texture").getAsJsonArray().forEach(mapping -> {
									try {
										JsonArray directions = mapping.getAsJsonObject().get("directions").getAsJsonArray();
										byte directionMask = 0;
										if(directions.contains(new JsonPrimitive("north"))) {
											directionMask = (byte) (directionMask | Direction.NORTH.getMask());
										}
										if(directions.contains(new JsonPrimitive("south"))) {
											directionMask = (byte) (directionMask | Direction.SOUTH.getMask());
										}
										if(directions.contains(new JsonPrimitive("east"))) {
											directionMask = (byte) (directionMask | Direction.EAST.getMask());
										}
										if(directions.contains(new JsonPrimitive("west"))) {
											directionMask = (byte) (directionMask | Direction.WEST.getMask());
										}
										Texture mappedTexture = mapping.getAsJsonObject().has("texture")
												? resolveTexture(mapping.getAsJsonObject().get("texture"))
												: StaticDefaults.DEFAULT_TEXTURE;

										directionTextureMappings.put(directionMask, mappedTexture);
									} catch(Exception e) {
										logger.log(Level.WARNING, "Could not load one of the textures for smart " + tileType + " \"" + tileId + "\": ", e);
									}
								});
							} else {
								logger.log(Level.WARNING, "Smart " + tileType + " \"" + tileId + "\" needs to specify its texture as a json array.");
							}
						} else {
							logger.log(Level.WARNING, "Smart " + tileType + " \"" + tileId + "\" does not specify any textures!");
						}

						if(btile.has("builder")) logger.log(Level.WARNING, "Smart " + tileType + " \"" + tileId + "\" cannot have a custom builder.");

						if(tileType.equals("floortile")) {
							FloorTileRegistry.instance().register(tileId, StaticDefaults.EMPTY_TEXTURE, (room, tilePos) -> new SmartFloorTile(room, tilePos, directionTextureMappings) {

								@Override
								public String getId() {
									return tileId;
								}

								@Override
								public boolean isPassable() {
									return isPassable;
								}

								@Override
								public double getViscosity() {
									return viscosity;
								}

							});
						} else {
							WallTileRegistry.instance().register(tileId, StaticDefaults.EMPTY_TEXTURE, (room, tilePos) -> new SmartWallTile(room, tilePos, directionTextureMappings) {

								@Override
								public String getId() {
									return tileId;
								}

								@Override
								public boolean isPassable() {
									return isPassable;
								}

								@Override
								public double getViscosity() {
									return viscosity;
								}

							});
						}

					}

				} else {
					throw new UnsupportedSpecificationVersionException(versionId, BTILE_EXTENSION);
				}
			} catch (Exception e) {
				logger.log(Level.WARNING, "Could not load .btile resource " + specification.toString() + ": ", e);
			}
		} catch (JsonSyntaxException | NullPointerException | IOException e) {
			logger.log(Level.WARNING, "Could not load .btile resource " + specification.toString(), e);
		}
	}

	private Texture resolveTexture(JsonElement texture) {
		Texture tempTexture = StaticDefaults.DEFAULT_TEXTURE;
		if(texture.isJsonArray()) {
			PixelGrid overlaidTexture = new PixelGrid(StaticDefaults.TILE_WIDTH, StaticDefaults.TILE_HEIGHT);
			JsonArray textureLocations = texture.getAsJsonArray();
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

				TextureUtils.overlay(specifiedTexture, overlaidTexture);
			}
			tempTexture = new Texture(overlaidTexture);
		} else {
			Texture specifiedTexture = cache.get(new InternalResource(texture.getAsString()));
			if(specifiedTexture != null) tempTexture = specifiedTexture;
		}
		return tempTexture;
	}


}

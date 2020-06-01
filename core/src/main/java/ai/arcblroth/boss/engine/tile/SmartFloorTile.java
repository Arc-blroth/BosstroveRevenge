package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.Direction;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.StaticDefaults;

import java.util.Map;

public abstract class SmartFloorTile extends FloorTile {

	public static final byte DEFAULT_TEXTURE_MASK = 1 << 4;
	private Map<Byte, Texture> textureMappings;

	public SmartFloorTile(Room room, TilePosition pos, Map<Byte, Texture> textureMappings) {
		super(room, pos, textureMappings.get(Direction.directionsToMask(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)));
		this.textureMappings = textureMappings;
	}

	@Override
	public Texture getTexture() {
		final String thisId = this.getId();
		byte currentMask = 0;
		TilePosition pos = this.getPosition();
		FloorTile northTile = getRoom().getFloorTiles().getOrNull(pos.getX(), pos.getY() - 1);
		if(northTile != null && northTile.getId().equals(thisId)) {
			currentMask = (byte) (currentMask | Direction.NORTH.getMask());
		}
		FloorTile southTile = getRoom().getFloorTiles().getOrNull(pos.getX(), pos.getY() + 1);
		if(southTile != null && southTile.getId().equals(thisId)) {
			currentMask = (byte) (currentMask | Direction.SOUTH.getMask());
		}
		FloorTile eastTile = getRoom().getFloorTiles().getOrNull(pos.getX() + 1, pos.getY());
		if(eastTile != null && eastTile.getId().equals(thisId)) {
			currentMask = (byte) (currentMask | Direction.EAST.getMask());
		}
		FloorTile westTile = getRoom().getFloorTiles().getOrNull(pos.getX() - 1, pos.getY());
		if(westTile != null && westTile.getId().equals(thisId)) {
			currentMask = (byte) (currentMask | Direction.WEST.getMask());
		}
		Texture texture = textureMappings.get(currentMask);
		if(texture == null) texture = textureMappings.get(DEFAULT_TEXTURE_MASK);
		if(texture == null) texture = StaticDefaults.EMPTY_TEXTURE;
		return texture;
	}

}

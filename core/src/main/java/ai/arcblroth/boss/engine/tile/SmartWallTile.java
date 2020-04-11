package ai.arcblroth.boss.engine.tile;

import ai.arcblroth.boss.engine.Direction;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.StaticDefaults;

import java.util.Map;

public abstract class SmartWallTile extends WallTile {

	private Map<Byte, Texture> textureMappings;

	public SmartWallTile(Room room, TilePosition pos, Map<Byte, Texture> textureMappings) {
		super(room, pos, textureMappings.get(Direction.directionsToMask(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST)));
		this.textureMappings = textureMappings;
	}

	@Override
	public Texture getTexture() {
		final String thisId = this.getId();
		byte currentMask = 0;
		TilePosition pos = this.getPosition();
		WallTile northTile = getRoom().getWallTiles().getOrNull(pos.getX(), pos.getY() - 1);
		if(northTile != null && northTile.getId().equals(thisId)) {
			currentMask = (byte) (currentMask | Direction.NORTH.getMask());
		}
		WallTile southTile = getRoom().getWallTiles().getOrNull(pos.getX(), pos.getY() + 1);
		if(southTile != null && southTile.getId().equals(thisId)) {
			currentMask = (byte) (currentMask | Direction.SOUTH.getMask());
		}
		WallTile eastTile = getRoom().getWallTiles().getOrNull(pos.getX() + 1, pos.getY());
		if(eastTile != null && eastTile.getId().equals(thisId)) {
			currentMask = (byte) (currentMask | Direction.EAST.getMask());
		}
		WallTile westTile = getRoom().getWallTiles().getOrNull(pos.getX() - 1, pos.getY());
		if(westTile != null && westTile.getId().equals(thisId)) {
			currentMask = (byte) (currentMask | Direction.WEST.getMask());
		}
		Texture texture = textureMappings.get(currentMask);
		if(texture == null) texture = StaticDefaults.EMPTY_TEXTURE;
		return texture;
	}

}

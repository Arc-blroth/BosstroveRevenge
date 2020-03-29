package ai.arcblroth.boss.game.tile;

import com.google.gson.JsonObject;

import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.register.WallTileBuilder;
import ai.arcblroth.boss.render.MultiFrameTexture;
import ai.arcblroth.boss.render.Texture;

public class LeverBuilder extends WallTileBuilder<LeverTile> {

	public LeverBuilder(Texture texture) {
		super(texture);
	}

	@Override
	public LeverTile build(Room room, TilePosition tilePos, JsonObject context) {
		boolean activated = context.get("activated").getAsBoolean();
		return new LeverTile(room, tilePos, (MultiFrameTexture)getTileTexture(), activated);
	}

}

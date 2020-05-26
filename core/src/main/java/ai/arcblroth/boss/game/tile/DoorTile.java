package ai.arcblroth.boss.game.tile;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.game.shader.RoomTransitionShader;
import ai.arcblroth.boss.register.WallTileBuilder;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.resource.InternalResource;
import ai.arcblroth.boss.util.StaticDefaults;
import com.google.gson.JsonObject;

public class DoorTile extends RoomChangeTile {

	public DoorTile(Room room, TilePosition pos, Texture texture, String targetRoom) {
		super(room, pos, texture, targetRoom);
	}

	@Override
	public void onEntityStep(IEntity entity) {
		if(entity instanceof Player) {
			WorldEngine engine = getRoom().getLevel().getEngine();
			if(!engine.getState().equals(WorldEngine.State.LOADING)) {
				engine.setState(WorldEngine.State.LOADING);
				RoomTransitionShader rts = new RoomTransitionShader(engine, entity.getPosition(), entity.getPosition());
				engine.getRenderer().putWorldShader(1, rts);
				Runnable advanceTransitionFrame = new Runnable() {
					@Override
					public void run() {
						rts.advanceFrame();
						if (rts.canAdvanceFrame()) engine.runLater(this, 1);
					}
				};
				engine.runLater(advanceTransitionFrame, 1);
				engine.runLater(() -> engine.setCurrentRoomId(targetRoom), StaticDefaults.LEVEL_INTRO_ANIMATION_LENGTH);
				engine.runLater(() -> {
					engine.setState(WorldEngine.State.IN_WORLD);
					engine.getRenderer().removeWorldShader(rts);
				}, StaticDefaults.LEVEL_INTRO_ANIMATION_LENGTH * 2);
			}
		}
	}

	public String getId() {
		return "boss.door";
	}

	public static final class Builder extends WallTileBuilder<RoomChangeTile> {

		public Builder(Texture texture) {
			super(texture);
		}

		@Override
		public RoomChangeTile build(Room room, TilePosition tilePos, JsonObject context) {
			if(context.has("target")) {
				String target = context.get("target").getAsString();
				Texture texture = context.has("texture")
						? BosstrovesRevenge.instance().getTextureCache().get(new InternalResource(context.get("texture").getAsString()))
						: StaticDefaults.DEFAULT_TEXTURE;
				return new DoorTile(room, tilePos, texture, target);
			} else {
				throw new IllegalArgumentException("Must specify a target room for door tile!");
			}
		}

	}

}

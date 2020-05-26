package ai.arcblroth.boss.game.tile;

import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.engine.Room;
import ai.arcblroth.boss.engine.TilePosition;
import ai.arcblroth.boss.engine.entity.IEntity;
import ai.arcblroth.boss.engine.entity.player.Player;
import ai.arcblroth.boss.engine.hitbox.Hitbox;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.game.shader.RoomTransitionShader;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.key.KeybindRegistry;
import ai.arcblroth.boss.register.WallTileBuilder;
import ai.arcblroth.boss.render.Texture;
import ai.arcblroth.boss.util.StaticDefaults;
import com.google.gson.JsonObject;

public abstract class DoorTile extends RoomChangeTile {

	private final Position targetPos;

	public DoorTile(Room room, TilePosition pos, Texture texture, String targetRoom, Position targetPos) {
		super(room, pos, texture, targetRoom);
		this.targetPos = targetPos;
	}

	@Override
	public void onEntityStep(IEntity entity) {
		if(entity instanceof Player) {
			getRoom().getLevel().getEngine().getGUI().toast(1, "Press SPACE or ENTER to enter");
		}
	}

	@Override
	public void onPlayerInteract(Keybind keybind) {
		if(keybind.equals(KeybindRegistry.KEYBIND_USE) || keybind.equals(KeybindRegistry.KEYBIND_ENTER)) {
			Player player = getRoom().getPlayer();
			WorldEngine engine = getRoom().getLevel().getEngine();
			if (!engine.getState().equals(WorldEngine.State.LOADING)) {
				engine.setState(WorldEngine.State.LOADING);
				RoomTransitionShader rts = new RoomTransitionShader(
						engine,
						new Position(this.getPosition().getX() + 0.5, this.getPosition().getY() + 0.5),
						targetPos
				);
				engine.getRenderer().putWorldShader(1, rts);
				Runnable advanceTransitionFrame = new Runnable() {
					@Override
					public void run() {
						rts.advanceFrame();
						if (rts.canAdvanceFrame()) engine.runLater(this, 1);
					}
				};
				engine.runLater(advanceTransitionFrame, 1);
				engine.runLater(() -> {
					engine.setCurrentRoomId(targetRoom);
					player.setPosition(targetPos);
				}, StaticDefaults.ROOM_TRANSITION_ANIMATION_LENGTH / 2);
				engine.runLater(() -> {
					engine.setState(WorldEngine.State.IN_WORLD);
					engine.getRenderer().removeWorldShader(rts);
				}, StaticDefaults.ROOM_TRANSITION_ANIMATION_LENGTH);
			}
		}
	}

	public String getId() {
		return "boss.door";
	}

	public static final class Builder extends WallTileBuilder<RoomChangeTile> {

		public Builder(Texture texture, Hitbox hitbox) {
			super(texture, hitbox != null ? hitbox : StaticDefaults.DEFAULT_TILE_HITBOX);
		}

		@Override
		public RoomChangeTile build(Room room, TilePosition tilePos, JsonObject context) {
			if(context.has("target")) {
				String target = context.get("target").getAsString();
				Position targetPos = new Position(context.get("targetX").getAsDouble(), context.get("targetY").getAsDouble());
				return new DoorTile(room, tilePos, this.getTileTexture(), target, targetPos) {
					@Override
					public Hitbox getHitbox() {
						return getTileHitbox();
					}
				};
			} else {
				throw new IllegalArgumentException("Must specify a target room for door tile!");
			}
		}

	}

}

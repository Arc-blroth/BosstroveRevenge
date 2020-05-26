package ai.arcblroth.boss.game.shader;

import ai.arcblroth.boss.engine.IShader;
import ai.arcblroth.boss.engine.Position;
import ai.arcblroth.boss.game.WorldEngine;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.StaticDefaults;

public class RoomTransitionShader implements IShader {

	private WorldEngine worldEngine;
	private Position exitPosition, entrancePosition;
	private int timer = 0;

	public RoomTransitionShader(WorldEngine worldEngine, Position exitPosition, Position entrancePosition) {
		this.worldEngine = worldEngine;
		this.exitPosition = exitPosition;
		this.entrancePosition = entrancePosition;
	}

	public void advanceFrame() {
		if(canAdvanceFrame()) timer++;
	}

	public boolean canAdvanceFrame() {
		return timer < StaticDefaults.ROOM_TRANSITION_ANIMATION_LENGTH;
	}

	@Override
	public void render(PixelAndTextGrid target) {
		Position exitOrEntrance = timer < (StaticDefaults.ROOM_TRANSITION_ANIMATION_LENGTH / 2) ? exitPosition : entrancePosition;
		Position focusedPosition = new Position(
				StaticDefaults.TILE_WIDTH * exitOrEntrance.getX() - worldEngine.getRenderer().getRenderOffsetX(),
				StaticDefaults.TILE_HEIGHT * exitOrEntrance.getY() - worldEngine.getRenderer().getRenderOffsetY());
		double maxSpotlightRadius = Math.max(target.getWidth(), target.getHeight());
		double progress = timer < StaticDefaults.ROOM_TRANSITION_ANIMATION_LENGTH / 2
				? 1D - (double)timer / (StaticDefaults.ROOM_TRANSITION_ANIMATION_LENGTH / 2)
				: (double)timer / (StaticDefaults.ROOM_TRANSITION_ANIMATION_LENGTH / 2) - 1D;
		double spotlightRadius = progress * maxSpotlightRadius;
		for(int y = 0; y < target.getHeight(); y++) {
			for(int x = 0; x < target.getWidth(); x++) {
				if(Math.hypot(x - focusedPosition.getX(), y - focusedPosition.getY()) >= spotlightRadius) {
					target.setPixel(x, y, Color.BLACK);
				}
			}
		}
	}

}

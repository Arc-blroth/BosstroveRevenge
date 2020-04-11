package ai.arcblroth.boss.util;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelGrid;
import ai.arcblroth.boss.render.Texture;

import java.util.Random;

public final class StaticDefaults {

	private StaticDefaults() {throw new RuntimeException("Programmer used Reflection! It wasn't very effective!");}
	
	public static final int CHARACTER_WIDTH = 16;
	public static final int CHARACTER_HEIGHT = 32;
	public static final Character RESET_CHAR = '\0';
	
	public static final int STEPS_PER_SECOND = 30;
	public static final double MILLISECONDS_PER_STEP = 1000D / (double)STEPS_PER_SECOND;
	
	public static final int TILE_WIDTH = 8;
	public static final int TILE_HEIGHT = 8;
	
	public static final Texture DEFAULT_TEXTURE;
	public static final Texture EMPTY_TEXTURE;
	static {
		int size = 16;
		Random r = new Random(size);
		PixelGrid pg = new PixelGrid(size, size);
		for(int y = 0; y < size; y++) {
			for(int x = 0; x < size; x++) {
				pg.setPixel(x, y, Color.getFromHSBA(r.nextDouble()/5+0.5, 0.5, 0.8));
			}
		}
		DEFAULT_TEXTURE = new Texture(TextureUtils.sub(pg, 0, 0, 8, 8));
		EMPTY_TEXTURE = new Texture(0, 0);
	}
	
	public static final double MAX_PLAYER_HEALTH = 10;
	
	public static final long DEFAULT_KEYBIND_DELAY = 4;

	public static final int LEVEL_INTRO_ANIMATION_LENGTH = 30;

}

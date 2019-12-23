package ai.arcblroth.boss.util;

import ai.arcblroth.boss.render.Color;

public final class StaticDefaults {

	private StaticDefaults() {throw new RuntimeException("Programmer used Reflection! It wasn't very effective!");}
	
	public static final int OUTPUT_HEIGHT = 96;
	public static final int OUTPUT_WIDTH = 128;
	public static final Color RESET_COLOR = Color.BLACK;
	public static final Character RESET_CHAR = ' ';
	
	public static final int TILE_WIDTH = 16;
	public static final int TILE_HEIGHT = 16;

}

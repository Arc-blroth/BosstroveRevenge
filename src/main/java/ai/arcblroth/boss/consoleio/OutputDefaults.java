package ai.arcblroth.boss.consoleio;

import ai.arcblroth.boss.render.Color;

public final class OutputDefaults {

	private OutputDefaults() {throw new RuntimeException("Programmer used Reflection! It wasn't very effective!");}
	
	public static final int OUTPUT_HEIGHT = 96;
	public static final int OUTPUT_WIDTH = 128;
	public static final Color RESET_COLOR = Color.BLACK;
	public static final Character RESET_CHAR = ' ';

}
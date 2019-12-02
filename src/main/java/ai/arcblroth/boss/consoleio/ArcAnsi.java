package ai.arcblroth.boss.consoleio;

import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.util.OutputDefaults;
import ai.arcblroth.boss.util.TextureUtils;

public class ArcAnsi {

	private static final String ESC = Character.toString((char) 27) + "[";
	private final StringBuilder sb;

	public enum Color34 {
		BLACK, RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE
	}

	private ArcAnsi() {
		this.sb = new StringBuilder();
	}

	public static ArcAnsi ansi() {
		return new ArcAnsi();
	}

	@Override
	public String toString() {
		return sb.toString();
	}

	// Cursor Positioning
	public ArcAnsi moveCursor(int x, int y) {
		sb.append(ESC + x + ';' + y + 'H');
		return this;
	}

	public ArcAnsi moveCursorUp(int lines) {
		sb.append(ESC + lines + 'A');
		return this;
	}

	public ArcAnsi moveCursorDown(int lines) {
		sb.append(ESC + lines + 'B');
		return this;
	}

	public ArcAnsi moveCursorLeft(int chars) {
		sb.append(ESC + chars + 'D');
		return this;
	}

	public ArcAnsi moveCursorRight(int chars) {
		sb.append(ESC + chars + 'C');
		return this;
	}

	public ArcAnsi saveCursorPosition() {
		sb.append(ESC + 's');
		return this;
	}

	public ArcAnsi restoreCursorPosition() {
		sb.append(ESC + 'u');
		return this;
	}

	// Erase
	public ArcAnsi clearScreen() {
		sb.append(ESC + '2' + 'J');
		return this;
	}

	public ArcAnsi clearScreenAndBuffer() {
		sb.append(ESC + '3' + 'J');
		return this;
	}

	public ArcAnsi clearLineBackwards() {
		sb.append(ESC + '1' + 'K');
		return this;
	}

	public ArcAnsi clearLineForwards() {
		sb.append(ESC + '0' + 'K');
		return this;
	}

	public ArcAnsi clearLine() {
		sb.append(ESC + '2' + 'K');
		return this;
	}

	// Scroll
	public ArcAnsi scrollUp(int lines) {
		sb.append(ESC + lines + 'S');
		return this;
	}

	public ArcAnsi scrollDown(int lines) {
		sb.append(ESC + lines + 'T');
		return this;
	}

	// Display Fonts
	public ArcAnsi resetAll() {
		sb.append(ESC + '0' + 'm');
		return this;
	}

	public ArcAnsi bold() {
		sb.append(ESC + '1' + 'm');
		return this;
	}

	public ArcAnsi italic() {
		sb.append(ESC + '3' + 'm');
		return this;
	}

	public ArcAnsi underline() {
		sb.append(ESC + '4' + 'm');
		return this;
	}

	public ArcAnsi strikethrough() {
		sb.append(ESC + '9' + 'm');
		return this;
	}

	public ArcAnsi resetBold() {
		sb.append(ESC + "21" + 'm');
		return this;
	}

	public ArcAnsi resetItalic() {
		sb.append(ESC + "21" + 'm');
		return this;
	}

	public ArcAnsi resetUnderline() {
		sb.append(ESC + "21" + 'm');
		return this;
	}

	// Color
	public ArcAnsi fgColor(Color34 color) {
		if (color == null)
			throw new NullPointerException();
		int colorOffset = 0;
		if (color == Color34.BLACK)
			colorOffset = 0;
		if (color == Color34.RED)
			colorOffset = 1;
		if (color == Color34.GREEN)
			colorOffset = 2;
		if (color == Color34.YELLOW)
			colorOffset = 3;
		if (color == Color34.BLUE)
			colorOffset = 4;
		if (color == Color34.MAGENTA)
			colorOffset = 5;
		if (color == Color34.CYAN)
			colorOffset = 6;
		if (color == Color34.WHITE)
			colorOffset = 7;
		sb.append(ESC + "3" + colorOffset + 'm');
		return this;
	}

	public ArcAnsi fgColor(byte colorCode) {
		sb.append(ESC + "38;5;" + colorCode + 'm');
		return this;
	}

	public ArcAnsi fgColor(Color color24bit) {
		color24bit = TextureUtils.interpolate(OutputDefaults.RESET_COLOR, color24bit, color24bit.getAlpha() / 255D);
		sb.append(ESC + "38;2;" + color24bit.getRed() + ";" + color24bit.getGreen() + ";" + color24bit.getBlue() +'m');
		return this;
	}

	public ArcAnsi bgColor(Color34 color) {
		if (color == null)
			throw new NullPointerException();
		int colorOffset = 0;
		if (color == Color34.BLACK)
			colorOffset = 0;
		if (color == Color34.RED)
			colorOffset = 1;
		if (color == Color34.GREEN)
			colorOffset = 2;
		if (color == Color34.YELLOW)
			colorOffset = 3;
		if (color == Color34.BLUE)
			colorOffset = 4;
		if (color == Color34.MAGENTA)
			colorOffset = 5;
		if (color == Color34.CYAN)
			colorOffset = 6;
		if (color == Color34.WHITE)
			colorOffset = 7;
		sb.append(ESC + "4" + colorOffset + 'm');
		return this;
	}

	public ArcAnsi bgColor(byte colorCode) {
		sb.append(ESC + "48;5;" + colorCode + 'm');
		return this;
	}

	public ArcAnsi bgColor(Color color24bit) {
		color24bit = TextureUtils.interpolate(OutputDefaults.RESET_COLOR, color24bit, color24bit.getAlpha() / 255D);
		sb.append(ESC + "48;2;" + color24bit.getRed() + ";" + color24bit.getGreen() + ";" + color24bit.getBlue() + 'm');
		return this;
	}

	// String
	public ArcAnsi append(CharSequence cs) {
		sb.append(cs);
		return this;
	}

}
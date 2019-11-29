package ai.arcblroth.boss.consoleio;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Erase;
import org.jline.terminal.*;
import ai.arcblroth.boss.Main;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class AnsiOutputRenderer implements IOutputRenderer {

	private static final String PIXEL_CHAR = "\u2580";
	private Terminal terminal;
	private static final boolean SHOW_FPS = true;
	private double fps = 1;
	private long lastRenderTime;
	private static final ArcAnsi CLEAR = ArcAnsi.ansi().moveCursor(0, 0).clearScreenAndBuffer().resetAll();
	
	public AnsiOutputRenderer() {
		try {
			this.terminal = TerminalBuilder.builder().name("Bosstrove's Revenge").jansi(true).jna(true)
					.nativeSignals(true)
					.signalHandler(true ? Terminal.SignalHandler.SIG_DFL : Terminal.SignalHandler.SIG_IGN).build();
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				terminal.setSize(new Size(OutputDefaults.OUTPUT_HEIGHT, OutputDefaults.OUTPUT_WIDTH));
			} else {
				
			}
			terminal.enterRawMode();
			lastRenderTime = System.currentTimeMillis();
		} catch (Exception e) {
			System.err.println("Could not init terminal, aborting launch...");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void render(PixelAndTextGrid pg) {
		//if(pg != null) {
			if(!(System.getProperty(Main.FORCE_NORENDER) != null && System.getProperty(Main.FORCE_NORENDER).equals("true"))) {
				Size s = terminal.getSize();
				if (s.getColumns() >= pg.getWidth() && s.getRows() >= pg.getHeight() / 2) {
					
					double leftPadSpaces = (s.getColumns() - pg.getWidth()) / 2D;
					double topPadSpaces = (s.getRows() - pg.getHeight() / 2D) / 2D;
					
					String leftPad = PadUtils.leftPad("", (int)Math.ceil(leftPadSpaces) - 1);
					String rightPad = PadUtils.leftPad("", (int)Math.floor(leftPadSpaces));
					String linePad = PadUtils.stringTimes(" ", s.getColumns());
					String blankLinesTop = PadUtils.stringTimes(linePad + "\n", (int)Math.ceil(topPadSpaces) - (SHOW_FPS ? 2 : 1));
					String blankLinesBottom = PadUtils.stringTimes(linePad + "\n", (int)Math.floor(topPadSpaces));
					
					//The top lines
					ArcAnsi ansiBuilder = ArcAnsi.ansi().moveCursor(0, 0).resetAll().bgColor(OutputDefaults.RESET_COLOR);
					if(SHOW_FPS) ansiBuilder.append(PadUtils.rightPad(String.format("%.0f FPS", fps), s.getColumns()));
					ansiBuilder.append(blankLinesTop);
					
					//Print out each row like a printer would
					for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
						ArcAnsi rowBuilder = ArcAnsi.ansi();
						rowBuilder.bgColor(OutputDefaults.RESET_COLOR).append(leftPad);
						ArrayList<Color> row1 = pg.getRow(rowNum);
						ArrayList<Color> row2 = pg.getRow(rowNum + 1);
						ArrayList<Character> rowTxt = pg.getCharacterRow(rowNum);
						for (int colNum = 0; colNum < pg.getWidth(); colNum++) {
							if(rowTxt.get(colNum) == OutputDefaults.RESET_CHAR) {
								rowBuilder.fgColor(row1.get(colNum)).bgColor(row2.get(colNum)).append(PIXEL_CHAR);
							} else {
								Pair<Color, Color> colors = pg.getColorsAt(colNum, rowNum);
								rowBuilder.fgColor(colors.getFirst()).bgColor(colors.getSecond()).append(rowTxt.get(colNum).toString());
							}
						}
						rowBuilder.resetAll().bgColor(OutputDefaults.RESET_COLOR);
						ansiBuilder.append(rowBuilder.toString());
						ansiBuilder.append(rightPad + " \n");
					}
					
					//The bottom lines
					ansiBuilder.resetAll().bgColor(OutputDefaults.RESET_COLOR).append(blankLinesBottom).append(linePad).moveCursorLeft(s.getColumns());
					
					//PRINT
					if (terminal.getType() != Terminal.TYPE_DUMB) {
						terminal.writer().print(ansiBuilder);
					} else {
						System.out.print(CLEAR);
						System.out.print(ansiBuilder);
					}
				} else {
					String toPrint = Ansi.ansi().cursor(1, 1).reset()
							.a(String.format("Screen resolution too small [%s×%s]", s.getColumns(), s.getRows()))
							.eraseScreen(Erase.FORWARD).toString();
					if (terminal.getType() != Terminal.TYPE_DUMB) {
						terminal.writer().print(toPrint + "\n");
					} else {
						System.out.print(toPrint + "\n");
					}
				}
				
				//FPS Benchmarking
				long currTime = System.currentTimeMillis();
				fps = 1000D / (currTime - lastRenderTime);
				lastRenderTime = currTime;
			}
		//}
	}
	
	public void clear() {
		if(!(System.getProperty(Main.FORCE_NORENDER) != null && System.getProperty(Main.FORCE_NORENDER).equals("true"))) {
			if (terminal.getType() != Terminal.TYPE_DUMB) {
				terminal.writer().print(CLEAR);
			} else {
				System.out.print(CLEAR);
			}
		}
	}
	
	public Terminal getTerminal() {
		return terminal;
	}
	
	public double getFps() {
		return fps;
	}

}
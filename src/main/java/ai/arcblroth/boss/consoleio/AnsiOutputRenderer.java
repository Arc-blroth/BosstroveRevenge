package ai.arcblroth.boss.consoleio;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Erase;
import org.jline.terminal.*;
import org.jline.terminal.Terminal.MouseTracking;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.Main;
import ai.arcblroth.boss.consoleio.ArcAnsi.Color34;
import ai.arcblroth.boss.event.AutoSubscribeClass;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.event.TestEvent;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.ThreadUtils;

import java.util.concurrent.*;
import java.io.File;
import java.io.IOException;

public class AnsiOutputRenderer implements OutputRenderer {

	private static final String PIXEL_CHAR = "\u2580";
	private Terminal terminal;
	public static final int OUTPUT_HEIGHT = 96;
	public static final int OUTPUT_WIDTH = 128;
	private static final ArcAnsi CLEAR = ArcAnsi.ansi().clearScreen().moveCursor(1, 1).resetAll();
	public static final Color RESET_COLOR = Color.BLACK;

	public AnsiOutputRenderer() {
		try {
			this.terminal = TerminalBuilder.builder().name("Bosstrove's Revenge").jansi(true).jna(true)
					.nativeSignals(true)
					.signalHandler(true ? Terminal.SignalHandler.SIG_DFL : Terminal.SignalHandler.SIG_IGN).build();
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				terminal.setSize(new Size(OUTPUT_HEIGHT, OUTPUT_WIDTH));
			} else {
				
			}
			terminal.trackMouse(MouseTracking.Button);
			terminal.enterRawMode();
		} catch (Exception e) {
			System.err.println("Could not init terminal, aborting launch...");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void render(PixelGrid pg) {
		if(!(System.getProperty(Main.FORCE_NORENDER) != null && System.getProperty(Main.FORCE_NORENDER).equals("true"))) {
			Size s = terminal.getSize();
			if (s.getColumns() >= pg.getWidth() && s.getRows() >= pg.getHeight() / 2) {
				
				double leftPadSpaces = (s.getColumns() - pg.getWidth()) / 2D;
				double topPadSpaces = (s.getRows() - pg.getHeight() / 2D) / 2D;
				
				String leftPad = PadUtils.leftPad("", (int)Math.ceil(leftPadSpaces) - 1);
				String rightPad = PadUtils.leftPad("", (int)Math.floor(leftPadSpaces));
				String linePad = PadUtils.stringTimes(" ", s.getColumns());
				String blankLinesTop = PadUtils.stringTimes(linePad + "\n", (int)Math.ceil(topPadSpaces) - 1);
				String blankLinesBottom = PadUtils.stringTimes(linePad + "\n", (int)Math.floor(topPadSpaces));
				
				//The top lines
				ArcAnsi ansiBuilder = ArcAnsi.ansi().moveCursor(0, 0).resetAll().bgColor(RESET_COLOR).append(blankLinesTop);
				
				//Print out each row like a printer would
				for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
					ArcAnsi rowBuilder = ArcAnsi.ansi();
					rowBuilder.bgColor(RESET_COLOR).append(leftPad);
					ConcurrentHashMap<Integer, Color> row1 = pg.get(rowNum);
					ConcurrentHashMap<Integer, Color> row2 = pg.get(rowNum + 1);
					for (int colNum = 0; colNum < pg.getWidth(); colNum++) {
						rowBuilder.fgColor(row1.get(colNum)).bgColor(row2.get(colNum)).append(PIXEL_CHAR);
					}
					rowBuilder.resetAll().bgColor(RESET_COLOR);
					ansiBuilder.append(rowBuilder.toString());
					ansiBuilder.append(rightPad + " \n");
				}
				
				//The bottom lines
				ansiBuilder.resetAll().bgColor(RESET_COLOR).append(blankLinesBottom).append(linePad).moveCursorLeft(s.getColumns());
				
				//PRINT
				if (terminal.getType() != Terminal.TYPE_DUMB) {
					terminal.writer().print(ansiBuilder);
				} else {
					System.out.print(Ansi.ansi().eraseScreen(Erase.ALL).reset());
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
		}
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

}
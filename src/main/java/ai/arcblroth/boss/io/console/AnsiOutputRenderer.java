package ai.arcblroth.boss.io.console;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Erase;
import org.jline.terminal.*;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.Relauncher;
import ai.arcblroth.boss.crash.CrashReportGenerator;
import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class AnsiOutputRenderer implements IOutputRenderer {
	
	private static final String PIXEL_CHAR = "\u2580";
	private static final String FULL_CHAR = "\u2588";
	private Terminal terminal;
	ConsoleInputHandler cih = new ConsoleInputHandler();
	private Throwable error;
	
	private static final boolean SHOW_FPS = true;
	private double fps = 1;
	private long lastRenderTime;
	private static final long BYTES_IN_MEGABYTE = 1000000;
	
	public String debugLine = "";
	
	private static final ArcAnsi CLEAR = ArcAnsi.ansi().moveCursor(0, 0).clearScreenAndBuffer().resetAll();
	
	public AnsiOutputRenderer() {
		try {
			this.terminal = TerminalBuilder.builder().name("Bosstrove's Revenge").jansi(true).jna(true)
					.nativeSignals(true)
					.signalHandler(true ? Terminal.SignalHandler.SIG_DFL : Terminal.SignalHandler.SIG_IGN).build();
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				terminal.setSize(new Size(StaticDefaults.OUTPUT_HEIGHT, StaticDefaults.OUTPUT_WIDTH));
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
	
	public void init() {
		
	}

	public void render(PixelAndTextGrid pg) {
		
		if(error != null) return;
		
		//if(pg != null) {
			if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {
				Size s = terminal.getSize();
				if (terminal.getType().equals(Terminal.TYPE_DUMB) || (s.getColumns() >= pg.getWidth() && s.getRows() >= pg.getHeight() / 2)) {
					
					double leftPadSpaces = (s.getColumns() - pg.getWidth()) / 2D;
					double topPadSpaces = (s.getRows() - pg.getHeight() / 2D) / 2D;
					
					String leftPad = PadUtils.leftPad("", (int)Math.ceil(leftPadSpaces) - 1);
					String rightPad = PadUtils.leftPad("", (int)Math.floor(leftPadSpaces));
					String linePad = PadUtils.stringTimes(" ", s.getColumns());
					String blankLinesTop = PadUtils.stringTimes(linePad + "\n", (int)Math.ceil(topPadSpaces) - (SHOW_FPS ? 2 : 1));
					String blankLinesBottom = PadUtils.stringTimes(linePad + "\n", (int)Math.floor(topPadSpaces) - 1);
					
					//The top lines
					ArcAnsi ansiBuilder = ArcAnsi.ansi().moveCursor(0, 0).resetAll().bgColor(BosstrovesRevenge.instance().getResetColor()).fgColor(Color.WHITE);
					
					//FPS + memory
					if(SHOW_FPS) {
						ansiBuilder.append(PadUtils.rightPad(String.format("%.0f FPS", fps), (int)Math.floor(s.getColumns() / 2D)));
						ansiBuilder.append(PadUtils.leftPad(String.format("%s MB / %s MB", 
								Runtime.getRuntime().freeMemory() / BYTES_IN_MEGABYTE,
								Runtime.getRuntime().totalMemory() / BYTES_IN_MEGABYTE
						), (int)Math.ceil(s.getColumns() / 2D)));
					}
					ansiBuilder.append(blankLinesTop);
					
					//Print out each row like a printer would
					for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
						ArcAnsi rowBuilder = ArcAnsi.ansi();
						rowBuilder.bgColor(BosstrovesRevenge.instance().getResetColor()).fgColor(BosstrovesRevenge.instance().getResetColor()).append(leftPad);
						ArrayList<Color> row1 = pg.getRow(rowNum);
						ArrayList<Color> row2 = pg.getRow(rowNum + 1);
						ArrayList<Character> rowTxt = pg.getCharacterRow(rowNum);
						
						//Optimization 1: Don't reprint colors for every pixel.
						Color previousFgColor = BosstrovesRevenge.instance().getResetColor();
						Color previousBgColor = BosstrovesRevenge.instance().getResetColor();
						
						for (int colNum = 0; colNum < pg.getWidth(); colNum++) {

							if(rowTxt.get(colNum) == StaticDefaults.RESET_CHAR) {
								String toPrint = PIXEL_CHAR;
								
								if(!previousFgColor.equals(row1.get(colNum))) {
									previousFgColor = row1.get(colNum);
									rowBuilder.fgColor(previousFgColor);
								}
								//Optimization 2: If both colors are the same,
								//print a space instead of the normal character.
								if(previousFgColor.equals(row2.get(colNum))) {
									toPrint = FULL_CHAR;
								} else {
									if(previousBgColor != row1.get(colNum)) {
										previousBgColor = row2.get(colNum);
										rowBuilder.bgColor(previousBgColor);
									}
								}
								
								rowBuilder.append(toPrint);
							} else {
								Pair<Color, Color> colors = pg.getColorsAt(colNum, rowNum);
								if(previousFgColor != colors.getFirst()) {
									previousFgColor = colors.getFirst();
								}
								if(previousBgColor != colors.getSecond()) {
									previousBgColor = colors.getSecond();
								}
								
								rowBuilder.fgColor(previousFgColor);
								rowBuilder.bgColor(previousBgColor);
								rowBuilder.append(rowTxt.get(colNum).toString());
							}
							
							
						}
						rowBuilder.resetAll().bgColor(BosstrovesRevenge.instance().getResetColor());
						ansiBuilder.append(rowBuilder.toString());
						ansiBuilder.append(rightPad + " \n");
					}
					
					//The bottom lines
					ansiBuilder.resetAll().bgColor(BosstrovesRevenge.instance().getResetColor()).append(blankLinesBottom).append(linePad).append("\n");
					if(debugLine.length() > s.getColumns()) {
						ansiBuilder.append(debugLine.substring(0, s.getColumns()));
					} else {
						ansiBuilder.append(debugLine);
					}
					ansiBuilder.append(PadUtils.stringTimes(" ", Math.max(0, s.getColumns() - debugLine.length())));
					ansiBuilder.moveCursorLeft(s.getColumns());
					
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
	
	public void setDebugLine(String s) {
		debugLine = s;
	}

	public void displayFatalError(Throwable e) {
		error = e;
		System.out.print(ArcAnsi.ansi()
				.moveCursor(1, 1).bgColor(Color.WHITE).fgColor(Color.RED)
				.clearScreenAndBuffer().clearScreen()
				.moveCursor(1, 1).bgColor(Color.WHITE).fgColor(Color.RED));
		System.out.print(CrashReportGenerator.generateCrashReport(e));
	}
	
	public void clear() {
		if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {
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

	@Override
	public void pollInput() {
		try {
			cih.handleInput(terminal);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
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
import ai.arcblroth.boss.util.TextureUtils;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class AnsiOutputRenderer implements IOutputRenderer {
	
	private static final String PIXEL_CHAR = "\u2580";
	private static final String FULL_CHAR = "\u2588";
	private static final long BYTES_IN_MEGABYTE = 1000000;
	private static final ArcAnsi CLEAR = ArcAnsi.ansi().moveCursor(0, 0).clearScreenAndBuffer().resetAll();
	
	private Terminal terminal;
	ConsoleInputHandler cih = new ConsoleInputHandler();
	private Throwable error;
	
	private PrintStream originalOut;
	private PrintStream redirectedOut;
	private PipedOutputStream redirectedOutPipe;
	private PipedInputStream redirectedOutAsInPipe;
	private BufferedReader redirectedOutAsIn;
	
	private boolean showFPS = true;
	private double fps = 1;
	private long lastRenderTime;
	private volatile Pair<Integer, Integer> lastSize;
	
	private String debugLine = "";
	
	public AnsiOutputRenderer() {
		try {
			
			originalOut = System.out;
			redirectedOutPipe = new PipedOutputStream();
			redirectedOutAsInPipe = new PipedInputStream(redirectedOutPipe);
			redirectedOut = new PrintStream(redirectedOutPipe);
			redirectedOutAsIn = new BufferedReader(new InputStreamReader(redirectedOutAsInPipe));
			System.setOut(redirectedOut);
			System.setErr(redirectedOut);
			
			this.terminal = TerminalBuilder.builder().name("Bosstrove's Revenge").jansi(true).jna(true)
					.nativeSignals(true)
					.signalHandler(true ? Terminal.SignalHandler.SIG_DFL : Terminal.SignalHandler.SIG_IGN).build();
			terminal.enterRawMode();
			lastRenderTime = System.currentTimeMillis();
			lastSize = new Pair<Integer, Integer>(0, 0);
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
		
		//Read in debugLine
		try {
			if(redirectedOutAsIn.ready()) {
				debugLine = redirectedOutAsIn.readLine();
			}
		} catch(IOException e) {
			debugLine = "Error in reading outputDebug: " + e.getClass().getName() + ": " + e.getMessage();
		}
		
		//if(pg != null) {
			if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {
				Size size = terminal.getSize();
				lastSize = new Pair<Integer, Integer>(size.getColumns(), (size.getRows() - (showFPS ? 2 : 0)) * 2);
				if (terminal.getType().equals(Terminal.TYPE_DUMB) || (size.getColumns() >= pg.getWidth() && size.getRows() >= pg.getHeight() / 2)) {
					
					double leftPadSpaces = (size.getColumns() - pg.getWidth()) / 2D;
					double topPadSpaces = (size.getRows() - pg.getHeight() / 2D) / 2D;
					
					String leftPad = PadUtils.leftPad("", (int)Math.ceil(leftPadSpaces) - 1);
					String rightPad = PadUtils.leftPad("", (int)Math.ceil(leftPadSpaces) - 1);
					String linePad = PadUtils.stringTimes(" ", size.getColumns());
					String blankLinesTop = PadUtils.stringTimes(linePad + "\n", (int)Math.ceil(topPadSpaces) - (showFPS ? 2 : 1));
					String blankLinesBottom = PadUtils.stringTimes(linePad + "\n", (int)Math.ceil(topPadSpaces) - 1);
					
					//The top lines
					ArcAnsi ansiBuilder = ArcAnsi.ansi().moveCursor(0, 0).resetAll().bgColor(BosstrovesRevenge.instance().getResetColor()).fgColor(Color.WHITE);
					
					//FPS + memory
					if(showFPS) {
						ansiBuilder.fgColor(TextureUtils.invert(BosstrovesRevenge.instance().getResetColor()));
						ansiBuilder.append(PadUtils.rightPad(String.format("%.0f FPS", fps), (int)Math.floor(size.getColumns() / 2D)));
						ansiBuilder.append(PadUtils.leftPad(String.format("%s MB / %s MB", 
								Runtime.getRuntime().freeMemory() / BYTES_IN_MEGABYTE,
								Runtime.getRuntime().totalMemory() / BYTES_IN_MEGABYTE
						), (int)Math.ceil(size.getColumns() / 2D)));
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
								Pair<Color, Color> textColors = pg.getColorsAt(colNum, rowNum);
								Color bgColor = TextureUtils.interpolateRGB(
										TextureUtils.interpolateRGB(row1.get(colNum), row2.get(colNum), 0.5, 0.5),
										textColors.getSecond(),
										textColors.getSecond().getAlpha()/255D
								);
								if(previousFgColor != textColors.getFirst()) {
									previousFgColor = textColors.getFirst();
								}
								if(previousBgColor != bgColor) {
									previousBgColor = bgColor;
								}
								
								rowBuilder.fgColor(previousFgColor);
								rowBuilder.bgColor(previousBgColor);
								rowBuilder.append(rowTxt.get(colNum).toString());
							}
							
							
						}
						rowBuilder.resetAll().bgColor(BosstrovesRevenge.instance().getResetColor());
						ansiBuilder.append(rowBuilder.toString());
						ansiBuilder.append(rightPad);
						ansiBuilder.append("\n");
					}
					
					//The bottom lines
					if(!showFPS) ansiBuilder.deleteCharsFromEnd(1);
					ansiBuilder.resetAll().bgColor(BosstrovesRevenge.instance().getResetColor()).append(blankLinesBottom);
					if(showFPS) {
						ansiBuilder.fgColor(TextureUtils.invert(BosstrovesRevenge.instance().getResetColor()));
						if(debugLine.length() > size.getColumns()) {
							ansiBuilder.append(debugLine.substring(0, size.getColumns()));
						} else {
							ansiBuilder.append(debugLine);
						}
						ansiBuilder.append(PadUtils.stringTimes(" ", Math.max(0, size.getColumns() - debugLine.length())));
					}
					ansiBuilder.moveCursorLeft(size.getColumns());
					
					//PRINT
					if (terminal.getType() != Terminal.TYPE_DUMB) {
						terminal.writer().print(ansiBuilder);
					} else {
						originalOut.print(CLEAR);
						originalOut.print(ansiBuilder);
					}
				} else {
					String toPrint = Ansi.ansi().cursor(1, 1).reset()
							.a(String.format("Screen resolution too small [%s×%s]", size.getColumns(), size.getRows()))
							.eraseScreen(Erase.FORWARD).toString();
					if (terminal.getType() != Terminal.TYPE_DUMB) {
						terminal.writer().print(toPrint + "\n");
					} else {
						originalOut.print(toPrint + "\n");
					}
				}
				
				//FPS Benchmarking
				long currTime = System.currentTimeMillis();
				fps = 1000D / (currTime - lastRenderTime);
				lastRenderTime = currTime;
			}
		//}
	}

	public void displayFatalError(Throwable e) {
		error = e;
		originalOut.print(ArcAnsi.ansi()
				.moveCursor(1, 1).bgColor(Color.WHITE).fgColor(Color.RED)
				.clearScreenAndBuffer().clearScreen()
				.moveCursor(1, 1).bgColor(Color.WHITE).fgColor(Color.RED));
		originalOut.print(CrashReportGenerator.generateCrashReport(e));
	}
	
	public void clear() {
		if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {
			if (terminal.getType() != Terminal.TYPE_DUMB) {
				terminal.writer().print(CLEAR);
			} else {
				originalOut.print(CLEAR);
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

	@Override
	public Pair<Integer, Integer> getSize() {
		return lastSize;
	}

	@Override
	public boolean isShowingFPS() {
		return showFPS;
	}

	@Override
	public void setShowingFPS(boolean showFPS) {
		this.showFPS = showFPS;
	}

}
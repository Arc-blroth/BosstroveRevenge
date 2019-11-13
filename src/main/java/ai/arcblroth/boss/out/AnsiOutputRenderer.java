package ai.arcblroth.boss.out;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Erase;
import org.jline.terminal.*;

import ai.arcblroth.boss.BosstroveRevenge;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.PadUtils;

import java.util.concurrent.*;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

public class AnsiOutputRenderer implements OutputRenderer {
	
  private static final String IS_RELAUNCHED = "ai.arcblroth.boss.out.AnsiOutputRenderer.isRelaunched";
  private static final String PIXEL_CHAR = "\u2580";
  private Terminal terminal;
  public static final int OUTPUT_HEIGHT = 96;
  public static final int OUTPUT_WIDTH = 128;

  public AnsiOutputRenderer() {
    try {
		if(System.getProperty("os.name").toLowerCase().contains("win") && System.getProperty(IS_RELAUNCHED) == null) {
			new ProcessBuilder(
					"C:\\Windows\\System32\\cmd", "/K",
					"start", "Bosstrove's Revenge",
					//"echo", "off",
					//"&", "mode", (OUTPUT_WIDTH + "," + OUTPUT_HEIGHT/2),
					//"&",
					System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
						"-D" + IS_RELAUNCHED + "=true",
						"-cp", System.getProperty("java.class.path") + File.pathSeparator + BosstroveRevenge.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath(),
						"Main"
			).start();
			System.exit(0);
		} else {
		    AnsiConsole.systemInstall();
			this.terminal = TerminalBuilder.builder()
			        .name("Bosstrove's Revenge")
			        .jansi(true)
			        .jna(true)
			        .nativeSignals(true)
			        .signalHandler(true ? Terminal.SignalHandler.SIG_DFL : Terminal.SignalHandler.SIG_IGN)
			        .build();
			if(!System.getProperty("os.name").toLowerCase().contains("win")) {
				terminal.setSize(new Size(OUTPUT_HEIGHT, OUTPUT_WIDTH));
			} else {
			    //Runtime.getRuntime().exec("C:\\Windows\\System32\\cmd /C mode " + OUTPUT_WIDTH + "," + OUTPUT_HEIGHT/2);
			}
		}
 	} catch (Exception e) {
	      System.err.println("Could not init terminal, aborting launch...");
	      e.printStackTrace();
	      System.exit(-1);
	}
  }

  public void render(PixelGrid pg) {
	Size s = terminal.getSize();
	String leftPad = PadUtils.leftPad("", (s.getColumns() - pg.getWidth()) / 2);
	String blankLines = PadUtils.stringTimes(PadUtils.stringTimes(" ", s.getColumns()) + "\n", (s.getRows() - pg.getHeight()/2) / 2);
    ArcAnsi ansiBuilder = ArcAnsi.ansi()
    		.resetAll()
    		.moveCursor(1, 1)
    		.resetAll()
    		.append(blankLines);
    for(int rowNum = 0; rowNum < (pg.getHeight()/2)*2; rowNum += 2) {
      ArcAnsi rowBuilder = ArcAnsi.ansi();
      rowBuilder.append(leftPad);
      ConcurrentHashMap<Integer, Color> row1 = pg.get(rowNum);
      ConcurrentHashMap<Integer, Color> row2 = pg.get(rowNum + 1);
      for(int colNum = 0; colNum < pg.getWidth(); colNum++) {
    	  rowBuilder.fgColor(row1.get(colNum)).bgColor(row2.get(colNum)).append(PIXEL_CHAR);
      }
      rowBuilder.resetAll();
      ansiBuilder.append(rowBuilder.toString());
      ansiBuilder.append(leftPad + " \n");
    }
    ansiBuilder.resetAll().append(blankLines);
    if(terminal.getType() != Terminal.TYPE_DUMB) {
		terminal.writer().print(ansiBuilder);
	} else {
		System.out.print(ansiBuilder);
		System.out.print(Ansi.ansi().eraseScreen(Erase.ALL).reset());
	}
  }
  
}
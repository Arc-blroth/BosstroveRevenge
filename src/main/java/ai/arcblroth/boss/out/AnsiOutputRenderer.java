package ai.arcblroth.boss.out;

import org.jline.terminal.*;

import ai.arcblroth.boss.render.*;
import java.util.concurrent.*;
import java.awt.Color;
import java.io.IOException;

public class AnsiOutputRenderer implements OutputRenderer {

  private static final String PIXEL_CHAR = "\u2580";
  private Terminal terminal;
public static final int OUTPUT_HEIGHT = 128;
public static final int OUTPUT_WIDTH = 96;

  public AnsiOutputRenderer() {
    try {
		this.terminal = TerminalBuilder.builder()
		        .name("Bosstrove's Revenge")
		        .jansi(true)
		        .jna(true)
		        .nativeSignals(true)
		        .signalHandler(true ? Terminal.SignalHandler.SIG_DFL : Terminal.SignalHandler.SIG_IGN)
		        .build();
		try {
			if(System.getProperty("os.name").toLowerCase().contains("win")) {
				Runtime.getRuntime().exec("cmd /C mode " + OUTPUT_WIDTH + "," + OUTPUT_HEIGHT/2);
			} else {
				terminal.setSize(new Size(OUTPUT_HEIGHT, OUTPUT_WIDTH));
			}
		} catch(Exception e) {
			System.err.println("WARN: Could not resize terminal");
		}
 	} catch (IOException e) {
	      System.err.println("Could not init terminal, aborting launch...");
	      System.exit(-1);
	}
  }

  public void render(PixelGrid pg) {
    ArcAnsi ansiBuilder = ArcAnsi.ansi().clearScreen();
    for(int rowNum = 0; rowNum < (pg.getHeight()/2)*2; rowNum += 2) {
      ConcurrentHashMap<Integer, Color> row1 = pg.get(rowNum);
      ConcurrentHashMap<Integer, Color> row2 = pg.get(rowNum + 1);
      for(int colNum = 0; colNum < pg.getWidth(); colNum++) {
        ansiBuilder.fgColor(row1.get(colNum)).bgColor(row2.get(colNum)).append(PIXEL_CHAR);
      }
      ansiBuilder.append("\n");
    }
    ansiBuilder.resetAll();
    if(terminal.getType() != Terminal.TYPE_DUMB) {
			terminal.writer().println(ansiBuilder);
		} else {
			System.out.println(ansiBuilder);
		}
  }

}
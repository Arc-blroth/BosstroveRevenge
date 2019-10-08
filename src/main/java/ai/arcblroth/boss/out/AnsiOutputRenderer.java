package ai.arcblroth.boss.out;

import org.jline.terminal.*;

import ai.arcblroth.boss.render.*;
import java.util.concurrent.*;
import java.awt.Color;

public class AnsiOutputRenderer implements OutputRenderer {

  private static final String PIXEL_CHAR = "\u2580";
  private final Terminal terminal;

  public AnsiOutputRenderer(Terminal t) {
    this.terminal = t;
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
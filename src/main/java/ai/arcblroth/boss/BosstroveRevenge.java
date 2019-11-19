package ai.arcblroth.boss;

import java.awt.Color;

import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.*;

import ai.arcblroth.boss.event.EventBus;
import ai.arcblroth.boss.out.*;
import ai.arcblroth.boss.render.*;

public class BosstroveRevenge extends Thread {

  private static final BosstroveRevenge INSTANCE = new BosstroveRevenge();
  private EventBus eventBus = new EventBus();
  private OutputRenderer renderer;

  private BosstroveRevenge() {
      this.renderer = new AnsiOutputRenderer();
      System.out.println(ArcAnsi.ansi().clearScreen().moveCursor(1, 1).resetAll());
  }

  public static BosstroveRevenge get() {
    return INSTANCE;
  }

  public void run() {

    PixelGrid reallyBadGrid = new PixelGrid(AnsiOutputRenderer.OUTPUT_WIDTH, AnsiOutputRenderer.OUTPUT_HEIGHT);
    reallyBadGrid.setPixel(0, 0, Color.blue);
    while(true) {
    	renderer.render(reallyBadGrid);
    }
  }


}
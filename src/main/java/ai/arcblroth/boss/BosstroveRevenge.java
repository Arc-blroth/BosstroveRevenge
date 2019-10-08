package ai.arcblroth.boss;

import org.jline.terminal.*;

import ai.arcblroth.boss.out.*;
import ai.arcblroth.boss.render.*;

public class BosstroveRevenge extends Thread {

  public static final int OUTPUT_WIDTH = 128;
  public static final int OUTPUT_HEIGHT = 64;

  private static final BosstroveRevenge INSTANCE = new BosstroveRevenge();
  private Terminal terminal;
  private OutputRenderer renderer;

  private BosstroveRevenge() {
    try {
      this.terminal = TerminalBuilder.builder()
          .name("Bosstrove's Revenge")
          .jansi(true)
          .nativeSignals(true)
          .signalHandler(false ? Terminal.SignalHandler.SIG_DFL : Terminal.SignalHandler.SIG_IGN)
          .build();
      this.renderer = new AnsiOutputRenderer(terminal);
    } catch(Exception e) {
      System.err.println("Could not init terminal, aborting launch...");
      System.exit(-1);
    }
  }

  public static BosstroveRevenge get() {
    return INSTANCE;
  }

  public void run() {
    
    terminal.setSize(new Size(OUTPUT_WIDTH, OUTPUT_HEIGHT));

    PixelGrid reallyBadGrid = new PixelGrid(OUTPUT_WIDTH, OUTPUT_HEIGHT);
    renderer.render(reallyBadGrid);
  }


}
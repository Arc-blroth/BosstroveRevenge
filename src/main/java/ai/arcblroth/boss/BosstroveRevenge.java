package ai.arcblroth.boss;

import java.awt.Color;

import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.*;

import ai.arcblroth.boss.event.EventBus;
import ai.arcblroth.boss.out.*;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.PadUtils;

public class BosstroveRevenge extends Thread {

	private static final BosstroveRevenge INSTANCE = new BosstroveRevenge();
	public static final String TITLE = "Bosstrove's Revenge";
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
		reallyBadGrid.setPixel(1, 1, Color.blue);
		while (true) {
			renderer.render(reallyBadGrid);
		}
	}

}
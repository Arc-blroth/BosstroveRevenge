package ai.arcblroth.boss;

import java.awt.Color;

import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.*;

import ai.arcblroth.boss.event.EventBus;
import ai.arcblroth.boss.event.TestEvent;
import ai.arcblroth.boss.load.SubscribingClassLoader;
import ai.arcblroth.boss.out.*;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.PadUtils;

public class BosstroveRevenge extends Thread {
	
	private static EventBus globalEventBus;
	private static SubscribingClassLoader globalSubscribingClassLoader;
	
	static {
		//It's crucial that the EventBus is loaded as soon as possible,
		//so that the SubscribingClassLoader can be implemented as soon as possible.
		//This method, however, forces the global* variables to be static.
		globalEventBus = new EventBus();
		ClassLoader originalLoader = BosstroveRevenge.class.getClassLoader();
		globalSubscribingClassLoader = new SubscribingClassLoader(originalLoader, globalEventBus);
	}
	
	private static final BosstroveRevenge INSTANCE = new BosstroveRevenge();
	public  static final String TITLE = "Bosstrove's Revenge";
	private OutputRenderer renderer;

	private BosstroveRevenge() {
		setContextClassLoader(globalSubscribingClassLoader);
		setName(TITLE + "-Main");
	}

	public static BosstroveRevenge get() {
		return INSTANCE;
	}

	public EventBus getGlobalEventBus() {
		return globalEventBus;
	}
	
	public void run() {
		this.renderer = new AnsiOutputRenderer();
		System.out.println(ArcAnsi.ansi().clearScreen().moveCursor(1, 1).resetAll());
		
		PixelGrid reallyBadGrid = new PixelGrid(AnsiOutputRenderer.OUTPUT_WIDTH, AnsiOutputRenderer.OUTPUT_HEIGHT);
		reallyBadGrid.setPixel(1, 1, Color.blue);
		while (true) {
			renderer.render(reallyBadGrid);
			getGlobalEventBus().fireEvent(new TestEvent());
		}
	}

}
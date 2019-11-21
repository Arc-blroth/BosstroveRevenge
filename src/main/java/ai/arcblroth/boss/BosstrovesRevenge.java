package ai.arcblroth.boss;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.fusesource.jansi.AnsiConsole;
import org.jline.terminal.*;

import ai.arcblroth.boss.event.EventBus;
import ai.arcblroth.boss.event.TestEvent;
import ai.arcblroth.boss.load.SubscribingClassLoader;
import ai.arcblroth.boss.out.*;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.PadUtils;

public final class BosstrovesRevenge extends Thread {

	private EventBus globalEventBus;

	// Set the INSTANCE to final
	protected static final BosstrovesRevenge INSTANCE;
	static {
		BosstrovesRevenge preInst = null;
		try {
			preInst = new BosstrovesRevenge(new EventBus());
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		INSTANCE = preInst;
	}

	public static final String TITLE = "Bosstrove's Revenge";
	private OutputRenderer renderer;

	private BosstrovesRevenge(EventBus globalEventBus) throws Exception {
		if (INSTANCE != null)
			throw new IllegalStateException("Class has already been initilized!");

		setName(TITLE + " Main");

		// Register the EventBus subscribing hook
		this.globalEventBus = globalEventBus;
		((SubscribingClassLoader) Main.class.getClassLoader()).addHook((clazz) -> {
			globalEventBus.subscribe(clazz);
		});
	}

	public static BosstrovesRevenge get() {
		if (INSTANCE == null)
			throw new IllegalStateException("Class is not initilized yet!");
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
			// renderer.render(reallyBadGrid);
			getGlobalEventBus().fireEvent(new TestEvent());
		}
	}

}
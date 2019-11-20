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
	private SubscribingClassLoader globalSubscribingClassLoader;

	// Allows us to set the INSTANCE to final but not actually set it.
	protected static final BosstrovesRevenge INSTANCE;
	static {
		INSTANCE = null;
	}

	public static final String TITLE = "Bosstrove's Revenge";
	private OutputRenderer renderer;

	private BosstrovesRevenge(EventBus globalEventBus) throws Exception {
		if (INSTANCE != null)
			throw new IllegalStateException("Class has already been initilized!");
		this.globalEventBus = globalEventBus;
		setName(TITLE + "-Main");

		//Sets the final INSTANCE to this, and then sets it to final again
		Field instance = BosstrovesRevenge.class.getDeclaredField("INSTANCE");
		instance.setAccessible(true);
		Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(instance, instance.getModifiers() & ~Modifier.FINAL);
		instance.set(null, this);
		modifiersField.setInt(instance, instance.getModifiers() &  Modifier.FINAL);
		modifiersField.setAccessible(false);
		instance.setAccessible(false);
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
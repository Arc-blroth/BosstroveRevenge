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
import ai.arcblroth.boss.resource.PNGLoader;
import ai.arcblroth.boss.resource.ResourceLocation;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.ThreadUtils;

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
		
		this.renderer = new AnsiOutputRenderer();
	}

	public static BosstrovesRevenge get() {
		if (INSTANCE == null)
			throw new IllegalStateException("Class is not initilized yet!");
		return INSTANCE;
	}

	public void run() {
		renderer.clear();

		PixelGrid reallyBadGrid = PNGLoader.loadPNG(new ResourceLocation("yeet.png"));
		while (true) {
			renderer.render(reallyBadGrid);
		}
	}

	public EventBus getEventBus() {
		return globalEventBus;
	}
}
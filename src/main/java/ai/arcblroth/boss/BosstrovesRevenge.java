package ai.arcblroth.boss;

import ai.arcblroth.boss.consoleio.*;
import ai.arcblroth.boss.event.EventBus;
import ai.arcblroth.boss.load.SubscribingClassLoader;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.resource.PNGLoader;
import ai.arcblroth.boss.resource.ResourceLocation;
import ai.arcblroth.boss.util.TextureUtils;

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
	private AnsiOutputRenderer renderer;

	private BosstrovesRevenge(EventBus globalEventBus) throws Exception {
		if (INSTANCE != null)
			throw new IllegalStateException("Class has already been initilized!");

		setName(TITLE + " Main");

		// Register the EventBus subscribing hook
		this.globalEventBus = globalEventBus;
		((SubscribingClassLoader) Main.class.getClassLoader()).addHook((clazz) -> {
			globalEventBus.subscribe(clazz);
		});
		
		//Register input hook
		
		this.renderer = new AnsiOutputRenderer();
	}

	public static BosstrovesRevenge get() {
		if (INSTANCE == null)
			throw new IllegalStateException("Class is not initilized yet!");
		return INSTANCE;
	}

	public void run() {
		renderer.clear();

		PixelGrid reallyBadGrid = TextureUtils.tintColor(PNGLoader.loadPNG(new ResourceLocation("bitmap.png")), new Color(41, 187, 255));
		while (true) {
			renderer.render(reallyBadGrid);
		}
	}

	public EventBus getEventBus() {
		return globalEventBus;
	}
}
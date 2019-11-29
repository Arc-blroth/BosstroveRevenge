package ai.arcblroth.boss;

import java.util.logging.Level;
import java.util.logging.Logger;

import ai.arcblroth.boss.consoleio.*;
import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.event.AutoSubscribeClass;
import ai.arcblroth.boss.event.EventBus;
import ai.arcblroth.boss.load.LoadEngine;
import ai.arcblroth.boss.load.SubscribingClassLoader;
import ai.arcblroth.boss.util.ThreadUtils;

public final class BosstrovesRevenge extends Thread {

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
	private EventBus globalEventBus;
	private AnsiOutputRenderer outputRenderer;
	private IEngine engine;

	private BosstrovesRevenge(EventBus globalEventBus) throws Exception {
		if (INSTANCE != null)
			throw new IllegalStateException("Class has already been initilized!");

		setName(TITLE + " Main");

		// Register the EventBus subscribing hook
		this.globalEventBus = globalEventBus;
		((SubscribingClassLoader) Main.class.getClassLoader()).addHook((clazz) -> {
			if(clazz.isAnnotationPresent(AutoSubscribeClass.class)) {
				globalEventBus.subscribe(clazz);
			}
		});
		
		//Register input hook
		globalEventBus.subscribe(ConsoleInputHandler.class);
		
		this.outputRenderer = new AnsiOutputRenderer();
	}

	public static BosstrovesRevenge get() {
		if (INSTANCE == null)
			throw new IllegalStateException("Class is not initilized yet!");
		return INSTANCE;
	}

	public void run() {
		try {
			outputRenderer.clear();
			
			//the first Engine initilizes all assets and classes
			this.engine = new LoadEngine();
			globalEventBus.subscribe(engine, engine.getClass());
			
			while (true) {
				globalEventBus.fireEvent(new StepEvent());
				outputRenderer.render(engine.getRenderer().render());
				globalEventBus.fireEvent(new ConsoleInputEvent(outputRenderer.getTerminal()));
			}
		} catch (Throwable e) {
			Logger.getGlobal().log(Level.SEVERE, "FATAL ERROR", e);
			ThreadUtils.waitForever();
		}
	}

	public EventBus getEventBus() {
		return globalEventBus;
	}
}
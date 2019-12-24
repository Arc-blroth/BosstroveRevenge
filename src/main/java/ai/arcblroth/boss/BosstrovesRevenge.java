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
	private static final BosstrovesRevenge INSTANCE;
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
	private boolean isRunning = true;
	private EventBus globalEventBus;
	private IOutputRenderer outputRenderer;
	private Thread renderThread;
	private final Object renderLock = new Object();
	private IEngine engine;

	void init(IOutputRenderer renderer) {
		//Render setup
		if (outputRenderer != null)
			throw new IllegalStateException("OutputRenderer has already been initilized!");
		outputRenderer = renderer;
		
		renderThread = new Thread(() -> {
			this.setName(TITLE + " Render Thread");
			while(isRunning) {
				try {
					synchronized(renderLock) {
						renderLock.wait();
						outputRenderer.render(engine.getRenderer().render());
					}
				} catch (InterruptedException e) {
					
				}
			}
		});
	}

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
			setEngine(new LoadEngine());
			renderThread.start();
			
			while (isRunning) {
				globalEventBus.fireEvent(new StepEvent());
				synchronized(renderLock) {
					renderLock.notifyAll();
				}
				if(outputRenderer instanceof AnsiOutputRenderer) {
					globalEventBus.fireEvent(new ConsoleInputEvent(((AnsiOutputRenderer) outputRenderer).getTerminal()));
				}
			}
		} catch (Throwable e) {
			if(!(System.getProperty(Main.FORCE_NORENDER) != null && System.getProperty(Main.FORCE_NORENDER).equals("true"))) {
				outputRenderer.displayFatalError(e);
			} else {
				Logger.getGlobal().log(Level.SEVERE, "FATAL ERROR", e);
			}
			ThreadUtils.waitForever();
		}
	}

	public EventBus getEventBus() {
		return globalEventBus;
	}
	
	public void setOutputDebug(Object o) {
		outputRenderer.setDebugLine(o.toString());
	}
	
	public void setEngine(IEngine e) {
		if(this.engine != null) {
			globalEventBus.unsubscribe(engine.getClass());
		}
		this.engine = e;
		globalEventBus.subscribe(e, e.getClass());
	}
}
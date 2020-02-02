package ai.arcblroth.boss;

import java.util.logging.Level;
import java.util.logging.Logger;

import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.event.AutoSubscribeClass;
import ai.arcblroth.boss.event.EventBus;
import ai.arcblroth.boss.event.SubscribingClassLoader;
import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.io.console.*;
import ai.arcblroth.boss.load.LoadEngine;
import ai.arcblroth.boss.resource.load.TextureCache;
import ai.arcblroth.boss.util.StaticDefaults;
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
	private boolean hasAlreadyShutdown = false;
	private final Logger globalLogger, mainLogger;
	private EventBus globalEventBus;
	private IOutputRenderer outputRenderer;
	private Thread renderThread;
	private final Object renderLock = new Object();
	private volatile boolean isRendering = false;
	private TextureCache globalTextureCache;
	private IEngine engine;

	private BosstrovesRevenge(EventBus globalEventBus) throws Exception {
		if (INSTANCE != null)
			throw new IllegalStateException("Class has already been initilized!");

		Thread.currentThread().setName(TITLE + " Relauncher");
		setName(TITLE + " Main Thread");
		
		this.globalLogger = Logger.getGlobal();
		this.mainLogger = Logger.getLogger("Main");
		
		// Register the EventBus subscribing hook
		this.globalEventBus = globalEventBus;
		((SubscribingClassLoader) Relauncher.class.getClassLoader()).addHook((clazz) -> {
			if(clazz.isAnnotationPresent(AutoSubscribeClass.class)) {
				globalEventBus.subscribe(clazz);
			}
		});
		
		//Register shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			this.shutdown(0);
		}));
	}

	public static BosstrovesRevenge get() {
		if (INSTANCE == null)
			throw new IllegalStateException("Class is not initilized yet!");
		return INSTANCE;
	}

	void init(IOutputRenderer renderer) {
		//Render setup
		if (outputRenderer != null)
			throw new IllegalStateException("OutputRenderer has already been initilized!");
		outputRenderer = renderer;
		
		renderThread = new Thread(() -> {
			Thread.currentThread().setName(TITLE + " Render Thread");
			outputRenderer.init();
			while(isRunning) {
				try {
					synchronized(renderLock) {
						renderLock.wait();
						isRendering = true;
						outputRenderer.render(engine.getRenderer().render());
						isRendering = false;
					}
				} catch (Exception e) {
					BosstrovesRevenge.get().handleRendererCrash(e);
				}
			}
		});
	}

	public void run() {
		try {
			outputRenderer.clear();
			globalTextureCache = new TextureCache();
			
			long lastStepTime = System.currentTimeMillis();
			long lastLoopTime = System.currentTimeMillis();
			
			//the first Engine initilizes all assets and classes
			setEngine(new LoadEngine());
			renderThread.start();
			
			while (isRunning) {

				outputRenderer.pollInput();
				
				long currentStepTime = System.currentTimeMillis();
				globalEventBus.fireEvent(new StepEvent(currentStepTime - lastStepTime));
				lastStepTime = currentStepTime;
				
				if(!isRendering) {
					synchronized(renderLock) {
						renderLock.notifyAll();
					}
				}
				
				if(System.currentTimeMillis() - lastLoopTime < StaticDefaults.MILLISECONDS_PER_STEP) {
					while(System.currentTimeMillis() - lastLoopTime < StaticDefaults.MILLISECONDS_PER_STEP) {
						Thread.sleep(1);
					}
				} else {
					mainLogger.log(Level.WARNING,
							"Loop time exceeded MILLISECONDS_PER_STEP by "
							+ (System.currentTimeMillis() - lastLoopTime)
							+ "ms"
					);
				}
				lastLoopTime = System.currentTimeMillis();
				
			}
		} catch (Throwable e) {
			if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {
				outputRenderer.displayFatalError(e);
			} else {
				globalLogger.log(Level.SEVERE, "FATAL ERROR", e);
			}
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
	
	public TextureCache getTextureCache() {
		return globalTextureCache;
	}

	private void handleRendererCrash(Exception e) {
		globalLogger.log(Level.SEVERE, "Fatal exception in rendering loop: ", e);
		shutdown(-1);
	}
	
	public void shutdown(int exitcode) {
		if(!hasAlreadyShutdown) {
			hasAlreadyShutdown = true;
			isRunning = false;
			try {
				outputRenderer.dispose();
			} catch (Exception e) {
				globalLogger.log(Level.SEVERE, "Exception in shutting down renderer: ", e);
			}
			System.exit(exitcode);
		}
	}

	
}
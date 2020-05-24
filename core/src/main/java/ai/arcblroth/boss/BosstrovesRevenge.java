package ai.arcblroth.boss;

import ai.arcblroth.boss.engine.IEngine;
import ai.arcblroth.boss.engine.StepEvent;
import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.key.CharacterInputEvent;
import ai.arcblroth.boss.load.LoadEngine;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.resource.load.TextureCache;
import ai.arcblroth.boss.util.Pair;
import ai.arcblroth.boss.util.StaticDefaults;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BosstrovesRevenge extends Thread {

	public static final String FORCE_NORENDER = "FORCE_NORENDER";

	// Set the INSTANCE to final
	private static final BosstrovesRevenge INSTANCE;
	static {
		BosstrovesRevenge preInst = null;
		try {
			preInst = new BosstrovesRevenge();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		INSTANCE = preInst;
	}

	public static final String TITLE = "Bosstrove's Revenge";
	private AtomicBoolean isRenderInitialized = new AtomicBoolean(false);
	private boolean isRunning = true;
	private boolean hasAlreadyShutdown = false;
	private final Logger globalLogger, mainLogger;
	private IOutputRenderer outputRenderer;
	private Thread renderThread;
	private TextureCache globalTextureCache;
	private Color resetColor = Color.BLACK;
	private IEngine engine;

	private BosstrovesRevenge() throws Exception {
		if (INSTANCE != null)
			throw new IllegalStateException("Class has already been initilized!");

		Thread.currentThread().setName(TITLE + " Relauncher");
		setName(TITLE + " Main Thread");
		
		this.globalLogger = Logger.getGlobal();
		this.mainLogger = Logger.getLogger("Main");
	}

	public static BosstrovesRevenge instance() {
		if (INSTANCE == null)
			throw new IllegalStateException("Class is not initilized yet!");
		return INSTANCE;
	}

	void init(IOutputRenderer renderer) {
		//Register shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			this.shutdown(0);
		}));

		//Render setup
		if (outputRenderer != null)
			throw new IllegalStateException("OutputRenderer has already been initilized!");
		outputRenderer = renderer;
		
		renderThread = new Thread(() -> {
			Thread.currentThread().setName(TITLE + " Render Thread");
			outputRenderer.init();
			isRenderInitialized.set(true);
			while(isRunning) {
				try {
					outputRenderer.render(engine.getRenderer().render());
				} catch (Exception e) {
					BosstrovesRevenge.instance().handleRendererCrash(e);
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
				engine.step(new StepEvent(currentStepTime - lastStepTime));
				lastStepTime = currentStepTime;
				
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
			if(!(System.getProperty(FORCE_NORENDER) != null && System.getProperty(FORCE_NORENDER).equals("true"))) {
				outputRenderer.displayFatalError(e);
			} else {
				globalLogger.log(Level.SEVERE, "FATAL ERROR", e);
			}
		}
	}

	public void handleInput(CharacterInputEvent characterInputEvent) {
		if(engine != null) engine.handleKeyInput(characterInputEvent);
	}
	
	public Color getResetColor() {
		return resetColor;
	}

	public void setResetColor(Color c) {
		if(c != null) resetColor = c;
	}

	public boolean isRendererInitialized() {
		return isRenderInitialized.get();
	}
	
	public Pair<Integer, Integer> getOutputSize() {
		return outputRenderer.getSize();
	}
	
	public void setEngine(IEngine e) {
		this.engine = e;
	}
	
	public TextureCache getTextureCache() {
		return globalTextureCache;
	}
	
	public boolean isRendererShowingFPS() {
		return outputRenderer.isShowingFPS();
	}
	
	public void setRendererShowingFPS(boolean showFPS) {
		outputRenderer.setShowingFPS(showFPS);
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
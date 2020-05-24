package ai.arcblroth.boss;

import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.llama.ui.LlamaUI;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Llama extends Thread {

	private boolean isRunning = false;
	private Stage stage;
	private LlamaUI ui;
	private ClassLoader ourClassLoader;
	private ClassLoader gameClassLoader;
	private Object gameInstance = null;
	
	Llama(Stage stage) {
		this.stage = stage;
		this.ui = new LlamaUI(this);
		this.ourClassLoader = this.getClass().getClassLoader();
	}
	
	public void run() {
		if(isRunning) return;
		isRunning = true;

		// Ensure the Color and IOutputRenderer classes are loaded
		// as they are needed to bridge the throwaway game instance
		// with LlamaRenderer
		try {
			reloadGameUnsafe();
		} catch (ReflectiveOperationException e) {
			Logger.getLogger("Llama").log(Level.SEVERE, "Failed to bootstrap game instance: ", e);
		}

		ui.display();
	}

	public void reloadGameUnsafe() throws ReflectiveOperationException {
		if(gameClassLoader != null && gameInstance != null) {
			// using Thread.stop() is unsafe they said
			Class<?> bosstrovesRevengeClass = gameInstance.getClass();
			Field renderThreadField = bosstrovesRevengeClass.getDeclaredField("renderThread");
			renderThreadField.setAccessible(true);
			Thread renderThread = (Thread) renderThreadField.get(gameInstance);
			Thread mainThread = (Thread) gameInstance;
			try {
				renderThread.stop();
			} catch (ThreadDeath e) {}
			try {
				mainThread.stop();
			} catch (ThreadDeath e) {}
			gameInstance = null;
			gameClassLoader = null;
			System.gc();
		}
		gameClassLoader = new GameClassLoader(ourClassLoader);
		Class<?> bosstrovesRevengeClass = gameClassLoader.loadClass("ai.arcblroth.boss.BosstrovesRevenge");
		gameInstance = bosstrovesRevengeClass.getMethod("instance").invoke(null);
		Method initMethod = bosstrovesRevengeClass.getDeclaredMethod("init", IOutputRenderer.class);
		initMethod.setAccessible(true);
		initMethod.invoke(gameInstance, ui.getLevelRenderer());
		initMethod.setAccessible(false);

		// Convince the game instance that it's already shutdown so that
		// it doesn't call System.exit
		Field hasAlreadyShutdownField = bosstrovesRevengeClass.getDeclaredField("hasAlreadyShutdown");
		hasAlreadyShutdownField.setAccessible(true);
		hasAlreadyShutdownField.set(gameInstance, true);
		hasAlreadyShutdownField.setAccessible(false);

		bosstrovesRevengeClass.getMethod("start").invoke(gameInstance);
	}

	public Stage getStage() {
		return stage;
	}

	public Object getGameInstance() {
		return gameInstance;
	}
}

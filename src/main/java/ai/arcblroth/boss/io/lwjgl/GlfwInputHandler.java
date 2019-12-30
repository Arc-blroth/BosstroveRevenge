package ai.arcblroth.boss.io.lwjgl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.in.KeyInputEvent;

import static org.lwjgl.glfw.GLFW.*;

public class GlfwInputHandler {
	
	private ConcurrentHashMap<Character, Boolean> keyStore;
	private Logger logger;

	public GlfwInputHandler() {
		this.logger = Logger.getLogger("GlfwInputHandler");
		this.keyStore = new ConcurrentHashMap<Character, Boolean>(256);
	}
	
	public void handleInput(int code, int action) throws Throwable {
		boolean actualAction = (action == GLFW_PRESS || action == GLFW_REPEAT);
		if(code == GLFW_KEY_UNKNOWN) return;
		
		if(code >= GLFW_KEY_SPACE && code <= GLFW_KEY_EQUAL) { 
			//These are the same as unicode
			keyStore.put((char) code, actualAction);
		} else if(code >= GLFW_KEY_A && code <= GLFW_KEY_Z) {
			//Convert A-Z to lowercase
			keyStore.put((char)(code + 32), actualAction);
		} else if(code >= GLFW_KEY_LEFT_BRACKET && code <= GLFW_KEY_GRAVE_ACCENT) { 
			//These are the same as unicode
			keyStore.put((char) code, actualAction);
		} else if(code == GLFW_KEY_ESCAPE || code == GLFW_KEY_PAUSE) {
			keyStore.put('\u001b', actualAction);
		} else if(code == GLFW_KEY_ENTER) {
			//We use newline instead of carriage return because linux rocks
			keyStore.put('\n', actualAction);
		} else if(code == GLFW_KEY_TAB) {
			keyStore.put('\u0009', actualAction);
		} else if(code == GLFW_KEY_BACKSPACE || code == GLFW_KEY_DELETE) {
			keyStore.put('\u0008', actualAction);
		} else if(code == GLFW_KEY_INSERT) {
			// :(
		} else if(code == GLFW_KEY_RIGHT) {
			//Convert to WASD
			keyStore.put('d', actualAction);
		} else if(code == GLFW_KEY_LEFT) {
			keyStore.put('a', actualAction);
		} else if(code == GLFW_KEY_DOWN) {
			keyStore.put('s', actualAction);
		} else if(code == GLFW_KEY_UP) {
			keyStore.put('w', actualAction);
		}
	}
	
	public void clearAllKeys() {
		keyStore.clear();
	}
	
	public void fireEvents() {
		keyStore.forEach((charizard, uses_fire_blast) -> {
			if(uses_fire_blast) {
				try {
					BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent(charizard));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}
	
}

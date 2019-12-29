package ai.arcblroth.boss.io.lwjgl;

import java.util.logging.Logger;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.in.KeyInputEvent;

import static org.lwjgl.glfw.GLFW.*;

public class GlfwInputHandler {
	
	private Logger logger;

	public GlfwInputHandler() {
		this.logger = Logger.getLogger("GlfwInputHandler");
	}
	
	public void handleInput(int code) throws Throwable {
		if(code == GLFW_KEY_UNKNOWN) return;
		
		if(code >= GLFW_KEY_SPACE && code <= GLFW_KEY_EQUAL) { 
			//These are the same as unicode
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent((char) code));
		} else if(code >= GLFW_KEY_A && code <= GLFW_KEY_Z) {
			//Convert A-Z to lowercase
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent((char)(code + 32)));
		} else if(code >= GLFW_KEY_LEFT_BRACKET && code <= GLFW_KEY_GRAVE_ACCENT) { 
			//These are the same as unicode
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent((char) code));
		} else if(code == GLFW_KEY_ESCAPE || code == GLFW_KEY_PAUSE) {
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent('\u001b'));
		} else if(code == GLFW_KEY_ENTER) {
			//We use newline instead of carriage return because linux rocks
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent('\n'));
		} else if(code == GLFW_KEY_TAB) {
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent('\u0009'));
		} else if(code == GLFW_KEY_BACKSPACE || code == GLFW_KEY_DELETE) {
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent('\u0008'));
		} else if(code == GLFW_KEY_INSERT) {
			// :(
		} else if(code == GLFW_KEY_RIGHT) {
			//Convert to WASD
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent('d'));
		} else if(code == GLFW_KEY_LEFT) {
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent('a'));
		} else if(code == GLFW_KEY_DOWN) {
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent('s'));
		} else if(code == GLFW_KEY_UP) {
			BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent('w'));
		}
	}
	
}

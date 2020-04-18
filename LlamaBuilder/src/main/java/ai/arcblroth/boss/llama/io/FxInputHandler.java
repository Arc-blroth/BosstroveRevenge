package ai.arcblroth.boss.llama.io;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.key.CharacterInputEvent;
import javafx.scene.input.KeyCode;

import java.util.concurrent.ConcurrentHashMap;

public class FxInputHandler {

	private ConcurrentHashMap<Character, Boolean> keyStore;

	public FxInputHandler() {
		this.keyStore = new ConcurrentHashMap<>();
	}

	// Sure the code is hacky but it will probably work until I decide to update javafx
	public void handleInput(KeyCode keyCode, boolean pressed) {
		if(keyCode.isLetterKey()) {
			keyStore.put(keyCode.getName().toLowerCase().charAt(0), pressed);
			return;
		}
		if(keyCode.isArrowKey()) {
			if(keyCode.name().contains("Up")) {
				keyStore.put('w', pressed);
			} else if(keyCode.name().contains("Right")) {
				keyStore.put('d', pressed);
			} else if(keyCode.name().contains("Down")) {
				keyStore.put('s', pressed);
			} else if(keyCode.name().contains("Left")) {
				keyStore.put('a', pressed);
			}
			return;
		}
		if(keyCode.isDigitKey()) {
			if(!keyCode.isKeypadKey()) {
				keyStore.put(keyCode.getName().charAt(0), pressed);
			} else {
				keyStore.put(keyCode.getName().charAt(7), pressed);
			}
			return;
		}
		if(keyCode.equals(KeyCode.ENTER)) {
			keyStore.put('\n', pressed);
		}
		if(keyCode.equals(KeyCode.BACK_SPACE)) {
			keyStore.put('\b', pressed);
		}
		if(keyCode.equals(KeyCode.TAB)) {
			keyStore.put('\t', pressed);
		}
		if(keyCode.equals(KeyCode.ESCAPE)) {
			keyStore.put('\u001b', pressed);
		}
		if(keyCode.equals(KeyCode.SPACE)) {
			keyStore.put(' ', pressed);
		}
		if(keyCode.equals(KeyCode.COMMA)) {
			keyStore.put(',', pressed);
		}
		if(keyCode.equals(KeyCode.MINUS)) {
			keyStore.put('-', pressed);
		}
		if(keyCode.equals(KeyCode.PERIOD)) {
			keyStore.put('.', pressed);
		}
		if(keyCode.equals(KeyCode.SLASH)) {
			keyStore.put('/', pressed);
		}
		if(keyCode.equals(KeyCode.SEMICOLON)) {
			keyStore.put(';', pressed);
		}
		if(keyCode.equals(KeyCode.EQUALS)) {
			keyStore.put('=', pressed);
		}
		if(keyCode.equals(KeyCode.OPEN_BRACKET)) {
			keyStore.put('[', pressed);
		}
		if(keyCode.equals(KeyCode.CLOSE_BRACKET)) {
			keyStore.put(']', pressed);
		}
		if(keyCode.equals(KeyCode.BACK_SLASH)) {
			keyStore.put('\\', pressed);
		}
		if(keyCode.equals(KeyCode.BACK_SLASH)) {
			keyStore.put('\\', pressed);
		}
		if(keyCode.equals(KeyCode.BACK_QUOTE)) {
			keyStore.put('`', pressed);
		}
		if(keyCode.equals(KeyCode.QUOTE)) {
			keyStore.put('\'', pressed);
		}
	}

	public void fireEvents() {
		keyStore.forEach((charmander, growls) -> {
			if(growls) {
				try {
					BosstrovesRevenge.instance().handleInput(new CharacterInputEvent(charmander));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}

}

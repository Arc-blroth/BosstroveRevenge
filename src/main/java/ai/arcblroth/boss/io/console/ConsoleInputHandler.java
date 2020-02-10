package ai.arcblroth.boss.io.console;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.key.CharacterInputEvent;

public class ConsoleInputHandler {
	
	private Logger logger;

	public ConsoleInputHandler() {
		this.logger = Logger.getLogger("GlfwInputHandler");
	}
	
	public void handleInput(Terminal t) throws Throwable {
		NonBlockingReader nbe = t.reader();
		try {
			while(nbe.ready()) {
				int intChar = nbe.read();
				if(intChar != -1) {
					BosstrovesRevenge.instance().getEventBus().fireEvent(new CharacterInputEvent((char) intChar));
				}
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not read user input", e);
		}
	}
	
}

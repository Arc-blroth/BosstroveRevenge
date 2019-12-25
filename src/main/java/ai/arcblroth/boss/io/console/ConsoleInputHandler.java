package ai.arcblroth.boss.io.console;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jline.terminal.Terminal;
import org.jline.utils.NonBlockingReader;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.in.KeyInputEvent;

public class ConsoleInputHandler {
	
	private Logger logger;

	public ConsoleInputHandler() {
		this.logger = Logger.getLogger("ConsoleInputHandler");
	}
	
	public void handleInput(Terminal t) throws Throwable {
		NonBlockingReader nbe = t.reader();
		try {
			while(nbe.ready()) {
				int intChar = nbe.read();
				if(intChar != -1) {
					BosstrovesRevenge.get().getEventBus().fireEvent(new KeyInputEvent((char) intChar));
				}
			}
		} catch (IOException e) {
			logger.log(Level.WARNING, "Could not read user input", e);
		}
	}
	
}

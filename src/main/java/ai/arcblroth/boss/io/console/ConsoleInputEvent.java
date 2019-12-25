package ai.arcblroth.boss.io.console;

import org.jline.terminal.Terminal;

import ai.arcblroth.boss.event.IEvent;

public class ConsoleInputEvent implements IEvent {
	
	private final Terminal term;

	public ConsoleInputEvent(Terminal t) {
		this.term = t;
	}
	
	public Terminal getTerminal() {
		return term;
	}
	
}

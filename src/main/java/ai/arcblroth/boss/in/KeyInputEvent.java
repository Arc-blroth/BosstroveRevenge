package ai.arcblroth.boss.in;

import ai.arcblroth.boss.event.IEvent;

public class KeyInputEvent implements IEvent {
	
	private char c;

	public KeyInputEvent(char c) {
		this.c = c;
	}
	
	public char getKey() {
		return c;
	}
	
}

package ai.arcblroth.boss.key;

import ai.arcblroth.boss.event.IEvent;

public class CharacterInputEvent implements IEvent {
	
	private char c;

	public CharacterInputEvent(char c) {
		this.c = c;
	}
	
	public char getKey() {
		return c;
	}
	
}

package ai.arcblroth.boss.engine;

import ai.arcblroth.boss.event.IEvent;

public class StepEvent implements IEvent {
	
	private long millisSinceLastStep;

	public StepEvent(long millisSinceLastStep) {
		this.millisSinceLastStep = millisSinceLastStep;
	}

	public long getMillisSinceLastStep() {
		return millisSinceLastStep;
	}
	
}

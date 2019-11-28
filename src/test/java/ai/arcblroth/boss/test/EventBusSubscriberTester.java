package ai.arcblroth.boss.test;

import org.junit.Test;

import ai.arcblroth.boss.event.EventBus;
import ai.arcblroth.boss.event.IEvent;
import ai.arcblroth.boss.event.SubscribeEvent;

public class EventBusSubscriberTester {
	
	@Test
	public void test() {
		EventBus eb = new EventBus();
		eb.subscribe(EventBusSubscriberTester.class);
		eb.fireEvent(new TestEvent("testing123"));
	}
	
	@SubscribeEvent
	public static void testStaticEvent(TestEvent e) {
		System.out.println("Recieved TestEvent, statically: " + e.getMessage());
	}
	
	@SubscribeEvent
	public void testInstanceEvent(TestEvent e) {
		System.out.println("Recieved TestEvent on instance: " + e.getMessage());
	}
	
	public void testThatThisIsNotCalled(TestEvent e) {
		System.err.println("This method is not annotated and should not have recieved TestEvent: " + e.getMessage());
	}
	
	public class TestEvent implements IEvent {
		private String msg;

		public TestEvent(String msg) {
			this.msg = msg;
		}

		public String getMessage() {
			return msg;
		}
	}
	
}
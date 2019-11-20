package ai.arcblroth.boss.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;

public class EventBus extends Thread {
	
	private static int GLOBAL_ID_COUNTER = 0;
	private int ID;
	private ArrayList<Method> subscribers = new ArrayList<Method>();
	
	public EventBus() {
		super("EventBus-" + ++GLOBAL_ID_COUNTER);
		ID = GLOBAL_ID_COUNTER;
	}
	
	public void fireEvent(IEvent e) {
		for(Method sub : subscribers) {
			Parameter[] args = sub.getParameters();
			if(args.length == 1 && args[0].getType().equals(e.getClass())) {
				try {
					sub.invoke(e);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					//Shut it up!
				}
			}
		}
	}
	
	public <T> void subscribe(Class<T> clazz) {
		for(Method method : clazz.getMethods()) {
			if(method.isAnnotationPresent(EventBusSubscriber.class)) {
				if (Modifier.isStatic(method.getModifiers())) {
					//Since method is static, there are no implict parameters
					if(method.getParameterCount() == 1) {
						subscribers.add(method);
					}
				}
			}
		}
	}
	
}

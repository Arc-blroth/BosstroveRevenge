package ai.arcblroth.boss.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus extends Thread {

	private static int GLOBAL_ID_COUNTER = 0;
	private int ID;
	private Logger logger;
	private HashMap<Class<? extends IEvent>, ArrayList<Method>> subscribers 
		= new HashMap<Class<? extends IEvent>, ArrayList<Method>>();
	private ArrayList<Class<?>> subscribedClasses = new ArrayList<Class<?>>();

	public EventBus() {
		super("EventBus-" + ++GLOBAL_ID_COUNTER);
		ID = GLOBAL_ID_COUNTER;
		logger = Logger.getLogger("EventBus-" + ID);
	}

	public void fireEvent(IEvent e) {
		for (Method sub : subscribers.get(e.getClass())) {
			Parameter[] args = sub.getParameters();
			if (args.length == 1 && args[0].getType().equals(e.getClass())) {
				try {
					logger.log(Level.INFO, "Invoking method: " + sub.getClass().getName() + "::" + sub.getName());
					sub.invoke(null, e);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
					// Shut it up!
					logger.log(Level.INFO,
							"Method invocation failed: " + sub.getClass().getName() + "::" + sub.getName(), e1);
				}
			}
		}
	}

	public <T> void subscribe(Class<T> clazz) {
		if (!subscribedClasses.contains(clazz)) {
			for (Method method : clazz.getMethods()) {
				if (method.isAnnotationPresent(SubscribeEvent.class)) {
					if (Modifier.isStatic(method.getModifiers())) {
						// Since method is static, there are no implict parameters
						if (method.getParameterCount() == 1) {
							if(!subscribers.containsKey(method.getParameterTypes()[0])) {
								subscribers.put((Class<? extends IEvent>)method.getParameterTypes()[0]);
							}
							logger.log(Level.FINE, "Subscribed method " + clazz.getName() + "::" + method.getName());
							subscribers.get(method.getParameterTypes()[0]).add(method);
						}
					}
				}
			}
			subscribedClasses.add(clazz);
		}
	}

}

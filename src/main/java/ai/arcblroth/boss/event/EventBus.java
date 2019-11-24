package ai.arcblroth.boss.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventBus extends Thread {

	private static int GLOBAL_ID_COUNTER = 0;
	private int ID;
	private Logger logger;
	private ConcurrentHashMap<Class<?>, ArrayList<Method>> subscribers = new ConcurrentHashMap<Class<?>, ArrayList<Method>>();
	private ArrayList<Class<?>> subscribedClasses = new ArrayList<Class<?>>();

	public EventBus() {
		this(++GLOBAL_ID_COUNTER);
	}
	
	private EventBus(int id) {
		super("EventBus-" + id);
		this.ID = GLOBAL_ID_COUNTER;
		this.logger = Logger.getLogger("EventBus-" + ID);
	}

	public void fireEvent(IEvent e) {
		if(subscribers.containsKey(e.getClass())) {
			for (Method sub : subscribers.get(e.getClass())) {
				Parameter[] args = sub.getParameters();
				if (args.length == 1 && args[0].getType().equals(e.getClass())) {
					try {
						logger.log(Level.FINE, "Invoking method: " + sub.getClass().getName() + "::" + sub.getName());
						sub.invoke(null, e);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
						// Shut it up!
						logger.log(Level.FINE,
								"Method invocation failed: " + sub.getClass().getName() + "::" + sub.getName(), e1);
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> void subscribe(Class<T> clazz) {
		if (!subscribedClasses.contains(clazz)) {
			for (Method method : clazz.getMethods()) {
				if (method.isAnnotationPresent(SubscribeEvent.class)) {
					if (Modifier.isStatic(method.getModifiers())) {
						// Since method is static, there are no implict parameters
						if (method.getParameterCount() == 1) {
							if(!subscribers.containsKey(method.getParameterTypes()[0])) {
								subscribers.put(method.getParameterTypes()[0], new ArrayList<Method>());
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

package ai.arcblroth.boss.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ai.arcblroth.boss.util.Pair;

public class EventBus {

	private static int GLOBAL_ID_COUNTER = 0;
	private int ID;
	private Logger logger;
	//This HashMap maps an Event to its subscribed classes and methods
	private ConcurrentHashMap<Class<?>, ConcurrentHashMap<Class<?>, ArrayList<Method>>> subscribers = new ConcurrentHashMap<>();
	//This HashMap stores all Subscribed classes, and instances of those classes
	private ConcurrentHashMap<Class<?>, Object> subscribedClasses = new ConcurrentHashMap<>();

	public EventBus() {
		this(++GLOBAL_ID_COUNTER);
	}
	
	private EventBus(int id) {
		//super("EventBus-" + id);
		this.ID = GLOBAL_ID_COUNTER;
		this.logger = Logger.getLogger("EventBus-" + ID);
	}

	@SuppressWarnings("unchecked")
	public synchronized void fireEvent(IEvent e) throws Throwable {
		if(subscribers.containsKey(e.getClass())) {
			if (subscribers.get(e.getClass()) != null) {
				for (ArrayList<Method> subs : subscribers.get(e.getClass()).values()) {
					for (Method sub : (ArrayList<Method>)subs) {
						Parameter[] args = sub.getParameters();
						if (args.length == 1 && args[0].getType().equals(e.getClass())) {
							try {
								logger.log(Level.FINE, "Invoking method: " + sub.getDeclaringClass().getName() + "." + sub.getName());
								sub.invoke(subscribedClasses.get(sub.getDeclaringClass()), e);
							} catch (IllegalAccessException e1) {
								// Shut it up!
								logger.log(Level.FINE,
										"Method invocation failed: " + sub.getDeclaringClass().getName() + "." + sub.getName(), e1);
							} catch (InvocationTargetException e1) {
								throw e1.getCause();
							}
						}
					}
				}
			}
		}
	}
	
	public synchronized <T> void subscribe(T instance, Class<? extends T> clazz) {
		try {
			if (!subscribedClasses.contains(clazz)) {
				for (Method method : clazz.getMethods()) {
					if (method.isAnnotationPresent(SubscribeEvent.class)) {
						if(subscribers.get(method.getParameterTypes()[0]) == null) {
							subscribers.put(method.getParameterTypes()[0], new ConcurrentHashMap<Class<?>, ArrayList<Method>>());
						}
						if(subscribers.get(method.getParameterTypes()[0]).get(clazz) == null) {
							subscribers.get(method.getParameterTypes()[0]).put(clazz, new ArrayList<Method>());
						}
						if (Modifier.isStatic(method.getModifiers())) {
							// Since method is static, there are no implict parameters
							if (method.getParameterCount() == 1) {
								logger.log(Level.FINE, "Subscribed method " + clazz.getName() + "::" + method.getName());
								subscribers.get(method.getParameterTypes()[0]).get(clazz).add(method);
							}
						} else if(method.getParameterCount() == 1) {
								logger.log(Level.FINE, "Subscribed method " + clazz.getName() + "." + method.getName());
								subscribers.get(method.getParameterTypes()[0]).get(clazz).add(method);
						}
					}
				}
				subscribedClasses.put(clazz, instance);
			}
		} catch (Exception e) {
			logger.log(Level.FINE, "Could not subscribe class " + clazz.getName(), e);
		}
	}

	public synchronized <T> void subscribe(Class<T> clazz) {
		try {
			if (!subscribedClasses.contains(clazz)) {
				//We'll have to construct an instance of the class.
				//If there's no default constructor this may fail.
				T instance = clazz.newInstance();
				subscribe(instance, clazz);
			}
		} catch (Exception e) {
			logger.log(Level.FINE, "Could not subscribe class " + clazz.getName(), e);
		}
			
	}
	
	public synchronized <T> void unsubscribe(Class<T> clazz) {
		subscribedClasses.remove(clazz);
		subscribers.values().removeIf((map) -> map.containsKey(clazz));
		logger.log(Level.FINE, "Unsubscribed class " + clazz.getName());
	}

}

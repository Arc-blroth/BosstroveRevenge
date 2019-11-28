package ai.arcblroth.boss.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import ai.arcblroth.boss.util.Pair;

public class EventBus {

	private static int GLOBAL_ID_COUNTER = 0;
	private int ID;
	private Logger logger;
	//This HashMap maps a Class to an instance of that class and the subscribed methods of that class.
	private ConcurrentHashMap<Class<?>, Pair<?, ArrayList<Method>>> subscribers = new ConcurrentHashMap<Class<?>, Pair<?, ArrayList<Method>>>();
	private ArrayList<Class<?>> subscribedClasses = new ArrayList<Class<?>>();

	public EventBus() {
		this(++GLOBAL_ID_COUNTER);
	}
	
	private EventBus(int id) {
		//super("EventBus-" + id);
		this.ID = GLOBAL_ID_COUNTER;
		this.logger = Logger.getLogger("EventBus-" + ID);
	}

	public void fireEvent(IEvent e) {
		if(subscribers.containsKey(e.getClass())) {
			if (subscribers.get(e.getClass()) != null) {
				Object instance = subscribers.get(e.getClass()).getFirst();
				for (Method sub : subscribers.get(e.getClass()).getSecond()) {
					Parameter[] args = sub.getParameters();
					if (args.length == 1 && args[0].getType().equals(e.getClass())) {
						try {
							logger.log(Level.FINE, "Invoking method: " + sub.getClass().getName() + "::" + sub.getName());
							sub.invoke(instance, e);
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
							// Shut it up!
							logger.log(Level.FINE,
									"Method invocation failed: " + sub.getClass().getName() + "::" + sub.getName(), e1);
						}
					}
				}
			}
		}
	}
	
	public <T> void subscribe(T instance, Class<? extends T> clazz) {
		try {
			if (!subscribedClasses.contains(clazz)) {
				for (Method method : clazz.getMethods()) {
					if (method.isAnnotationPresent(SubscribeEvent.class)) {
						if(!subscribers.containsKey(method.getParameterTypes()[0])) {
							subscribers.put(method.getParameterTypes()[0], 
									new Pair<T, ArrayList<Method>>(instance, new ArrayList<Method>()));
						}
						
						if (Modifier.isStatic(method.getModifiers())) {
							// Since method is static, there are no implict parameters
							if (method.getParameterCount() == 1) {
								logger.log(Level.FINE, "Subscribed method " + clazz.getName() + "::" + method.getName());
								subscribers.get(method.getParameterTypes()[0]).getSecond().add(method);
							}
						} else if(method.getParameterCount() == 1) {
								logger.log(Level.FINE, "Subscribed method " + clazz.getName() + "." + method.getName());
								subscribers.get(method.getParameterTypes()[0]).getSecond().add(method);
						}
					}
				}
				subscribedClasses.add(clazz);
			}
		} catch (Exception e) {
			logger.log(Level.INFO, "Could not subscribe class " + clazz.getName());
		}
	}

	public <T> void subscribe(Class<T> clazz) {
		try {
			if (!subscribedClasses.contains(clazz)) {
				//We'll have to construct an instance of the class.
				//If there's no default constructor this may fail.
				T instance = clazz.newInstance();
				subscribe(instance, clazz);
			}
		} catch (Exception e) {
			logger.log(Level.FINE, "Could not subscribe class " + clazz.getName());
		}
			
	}

}

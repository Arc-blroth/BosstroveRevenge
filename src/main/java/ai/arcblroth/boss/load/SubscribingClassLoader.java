package ai.arcblroth.boss.load;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import ai.arcblroth.boss.event.AutoSubscribeClass;
import ai.arcblroth.boss.event.EventBus;

public class SubscribingClassLoader extends ClassLoader {
	
	private static final Logger subClassLoaderLogger = Logger.getLogger("SubscribingClassLoader");
	private EventBus subscribeTarget;
	
    public SubscribingClassLoader(ClassLoader parent, EventBus subscribeTarget) {
        super(parent);
        this.subscribeTarget = subscribeTarget;
    }
    
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
    	subClassLoaderLogger.info("Loading Class \"" + name + "\"");
        Class<?> clazz = super.loadClass(name);
        subscribeClass(clazz);
        return clazz;
    }
    
    private void subscribeClass(Class<?> clazz) {
    	if(clazz.isAnnotationPresent(AutoSubscribeClass.class)) {
        	subClassLoaderLogger.info("Subscribing Class \"" + clazz.getName() + "\"");
        	subscribeTarget.subscribe(clazz);
        }
    }
    
    public EventBus getEventBus() {
    	return subscribeTarget;
    }
    
}

package ai.arcblroth.boss;
import org.fusesource.jansi.*;

import java.util.logging.*;

import ai.arcblroth.boss.*;
import ai.arcblroth.boss.event.EventBus;
import ai.arcblroth.boss.load.SubscribingClassLoader;
import ai.arcblroth.boss.out.*;
import java.awt.Color;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.*;
import java.nio.file.Paths;
import java.net.*;

/*
 * A note on external libraries:
 * all code in org.jline and org.fusesource.jansi
 * were fetched from the external Maven repository,
 * since repl.it doesn't support build tools as of yet.
 * Interestingly, jline is technically already included in
 * jdk.jline, but that module is restricted to the Java 10 JDK.
 *
 * Only code in ai.arcblroth is my own work :)
 */
class Main {
	private static final String IS_RELAUNCHED = "ai.arcblroth.boss.out.AnsiOutputRenderer.isRelaunched";
	private static final String brClassName = "ai.arcblroth.boss.BosstrovesRevenge";

	public static void main(String[] args) throws Exception {
		//[00:00:00][Logger/LEVEL]: Message
		System.setProperty( 
			"java.util.logging.SimpleFormatter.format",
			"[%1$tT][%3$s/%4$s]: %5$s %n"
		);
		System.setProperty(IS_RELAUNCHED, "true");
		Logger.getLogger("org.jline").setLevel(Level.ALL);
		try {
			if (System.getProperty("os.name").toLowerCase().contains("win")
					&& System.getProperty(IS_RELAUNCHED) == null) {
				new ProcessBuilder("C:\\Windows\\System32\\cmd", "/K", "start", "Bosstrove's Revenge",
						// "echo", "off",
						// "&", "mode", (OUTPUT_WIDTH + "," + OUTPUT_HEIGHT/2),
						// "&",
						System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
						"-D" + IS_RELAUNCHED + "=true", "-cp",
						System.getProperty("java.class.path") + File.pathSeparator + Main.class
								.getProtectionDomain().getCodeSource().getLocation().toURI().getPath(),
						Main.class.getName()).start();
				System.exit(0);
			} else {
				System.out.println("Loading...");
				
				//It's crucial that the EventBus is loaded as soon as possible,
				//so that the SubscribingClassLoader can be implemented as soon as possible.
				//This method, however, forces the global* variables to be static.
				EventBus globalEventBus = new EventBus();
				ClassLoader globalSubscribingClassLoader = new SubscribingClassLoader(Main.class.getClassLoader(), globalEventBus);
				Class<?> brClazz = globalSubscribingClassLoader.loadClass(brClassName);
				Constructor<?> brConstruct = brClazz.getDeclaredConstructor(EventBus.class);
				brConstruct.setAccessible(true);
				Object br = brConstruct.newInstance(globalEventBus);
				brConstruct.setAccessible(false);
				brClazz.getMethod("start").invoke(br);
			}
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "FATAL ERROR", e);
		}

	}
}
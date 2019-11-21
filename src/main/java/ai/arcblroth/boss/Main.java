package ai.arcblroth.boss;

import org.fusesource.jansi.*;

import java.util.Locale;
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
	private static final String IS_RELAUNCHED = "ai.arcblroth.boss.Main.IS_RELAUNCHED";
	private static final String FORCE_NOWINDOWS = "ai.arcblroth.boss.Main.FORCE_NOWINDOWS";
	private static final String FORCE_NOSUBSCRIBINGCLASSLOADER = "ai.arcblroth.boss.Main.FORCE_NOSUBSCRIBINGCLASSLOADER";
	private static final String brClassName = "ai.arcblroth.boss.BosstrovesRevenge";

	// Taken from the jansi source:
	// https://github.com/fusesource/jansi/blob/master/jansi/src/main/java/org/fusesource/jansi/AnsiConsole.java
	// ------------------
	static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");

	static final boolean IS_CYGWIN = IS_WINDOWS && System.getenv("PWD") != null && System.getenv("PWD").startsWith("/")
			&& !"cygwin".equals(System.getenv("TERM"));

	static final boolean IS_MINGW_XTERM = IS_WINDOWS && System.getenv("MSYSTEM") != null
			&& System.getenv("MSYSTEM").startsWith("MINGW") && "xterm".equals(System.getenv("TERM"));
	// ------------------

	public static void main(String[] args) throws Exception {

		System.setProperty(FORCE_NOWINDOWS, "true");
		System.setProperty(FORCE_NOSUBSCRIBINGCLASSLOADER, "true");

		// [00:00:00][Logger/LEVEL]: Message
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT][%3$s/%4$s]: %5$s %6$s%n");
		Logger.getLogger("org.jline").setLevel(Level.OFF);

		try {
			if (System.getProperty(IS_RELAUNCHED) == null) {
				String javaExe = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
				String classPath = System.getProperty("java.class.path") + File.pathSeparator
						+ Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				String switchRelaunched = "-D" + IS_RELAUNCHED + 
						(System.getProperty(FORCE_NOSUBSCRIBINGCLASSLOADER) != null ? "=true" : "=false");
				String switchSubscribingClassLoader = "-Djava.system.class.loader="
						+ SubscribingClassLoader.class.getName();
				String className = Main.class.getName();

				if (IS_WINDOWS && !IS_CYGWIN && !IS_MINGW_XTERM && System.getProperty(FORCE_NOWINDOWS) == null) {
					new ProcessBuilder("C:\\Windows\\System32\\cmd", "/K", "start", "Bosstrove's Revenge",
							// "echo", "off", "&", "mode", (OUTPUT_WIDTH + "," + OUTPUT_HEIGHT/2), "&",
							javaExe, switchRelaunched, switchSubscribingClassLoader, "-cp", classPath, className)
									.start();
					System.exit(0);
				} else {
					new ProcessBuilder(javaExe, switchRelaunched, switchSubscribingClassLoader, "-cp", classPath,
							className).inheritIO().start();
					System.exit(0);
				}
			} else {
				System.out.println("Loading...");

				// It's crucial that the SubscribingClassLoader is set, otherwise no hooks will
				// work.
				if (!(Main.class.getClassLoader() instanceof SubscribingClassLoader)) {
					throw new IllegalStateException("The system class loader should be set:"
							+ " -Djava.system.class.loader=" + SubscribingClassLoader.class.getName());
				} else {
					BosstrovesRevenge.get().start();
				}
			}
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "FATAL ERROR", e);
		}

	}
}
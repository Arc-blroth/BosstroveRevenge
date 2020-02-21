package ai.arcblroth.boss;

import java.util.Locale;
import java.util.logging.*;
import ai.arcblroth.boss.util.ThreadUtils;

import java.io.File;

public class Relauncher {
	public static final String IS_RELAUNCHED = "ai.arcblroth.boss.Relauncher.IS_RELAUNCHED";
	public static final String FORCE_NOWINDOWS = "ai.arcblroth.boss.Relauncher.FORCE_NOWINDOWS";
	public static final String FORCE_NORENDER = "ai.arcblroth.boss.Relauncher.FORCE_NORENDER";

	// Taken from the jansi source:
	// https://github.com/fusesource/jansi/blob/master/jansi/src/main/java/org/fusesource/jansi/AnsiConsole.java
	// ------------------
	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");

	public static final boolean IS_CYGWIN = IS_WINDOWS && System.getenv("PWD") != null && System.getenv("PWD").startsWith("/")
			&& !"cygwin".equals(System.getenv("TERM"));

	public static final boolean IS_MINGW_XTERM = IS_WINDOWS && System.getenv("MSYSTEM") != null
			&& System.getenv("MSYSTEM").startsWith("MINGW") && "xterm".equals(System.getenv("TERM"));
	// ------------------

	public static <M> void relaunch(Class<M> main, Runnable afterRelaunch) throws Exception {

		//System.setProperty(FORCE_NOWINDOWS, "true");
		//System.setProperty(FORCE_NOSUBSCRIBINGCLASSLOADER, "true");
		//System.setProperty(FORCE_NORENDER, "true");

		// Note that this is also set in SubscribingClassLoader since that also logs things.
		// [00:00:00][Logger/LEVEL]: Message
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT][%3$s/%4$s]: %5$s %6$s%n");
		Logger.getLogger("org.jline").setLevel(Level.OFF);

		try {
			if (System.getProperty(IS_RELAUNCHED) == null) {
				String javaExe = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
				String classPath = System.getProperty("java.class.path") + File.pathSeparator
						+ Relauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
				String switchRelaunched = "-D" + IS_RELAUNCHED + "=true";
				String className = main.getName();

				if (IS_WINDOWS && !IS_CYGWIN && !IS_MINGW_XTERM && System.getProperty(FORCE_NOWINDOWS) == null) {
					new ProcessBuilder("C:\\Windows\\System32\\cmd", "/C", "start", "Bosstrove's Revenge",
							// "echo", "off", "&", "mode", (OUTPUT_WIDTH + "," + OUTPUT_HEIGHT/2), "&",
							javaExe, switchRelaunched, "-cp", classPath, className)
									.start().waitFor();
					System.exit(0);
				} else {
					new ProcessBuilder(javaExe, switchRelaunched, "-cp", classPath,
							className).inheritIO().start().waitFor();
					System.exit(0);
				}
			} else {
				System.out.println("Loading...");
				afterRelaunch.run();
			}
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "FATAL ERROR", e);
			ThreadUtils.waitForever();
		}

	}
}
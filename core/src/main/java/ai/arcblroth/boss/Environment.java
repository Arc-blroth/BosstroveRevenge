package ai.arcblroth.boss;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Environment {

	// Flags
	public static final String FORCE_NOWINDOWS = "FORCE_NOWINDOWS";

	// Taken from the jansi source:
	// https://github.com/fusesource/jansi/blob/master/jansi/src/main/java/org/fusesource/jansi/AnsiConsole.java
	// ------------------
	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("win");

	public static final boolean IS_CYGWIN = IS_WINDOWS && System.getenv("PWD") != null && System.getenv("PWD").startsWith("/")
			&& !"cygwin".equals(System.getenv("TERM"));

	public static final boolean IS_MINGW_XTERM = IS_WINDOWS && System.getenv("MSYSTEM") != null
			&& System.getenv("MSYSTEM").startsWith("MINGW") && "xterm".equals(System.getenv("TERM"));
	// ------------------

	public static final boolean IS_ACTUALLY_WINDOWS = IS_WINDOWS && !IS_CYGWIN && !IS_MINGW_XTERM && System.getProperty(FORCE_NOWINDOWS) == null;

	public static void setLoggingPattern() {
		System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tT][%3$s/%4$s]: %5$s %6$s%n");
		Logger.getLogger("org.jline").setLevel(Level.OFF);
	}

}

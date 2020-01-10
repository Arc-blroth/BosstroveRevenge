package ai.arcblroth.boss;

import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import ai.arcblroth.boss.io.console.AnsiOutputRenderer;

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
public class ConsoleMain {
	
	public static void main(String[] argz) throws Exception {
		Relauncher.relaunch(ConsoleMain.class, () -> {
			BosstrovesRevenge.get().init(new AnsiOutputRenderer());
			BosstrovesRevenge.get().start();
		});
	}
	
}

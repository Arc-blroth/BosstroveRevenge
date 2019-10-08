import org.fusesource.jansi.*;

import java.util.logging.*;

import ai.arcblroth.boss.*;
import ai.arcblroth.boss.out.*;
import java.awt.Color;
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
  public static void main(String[] args) throws Exception {

    Logger.getLogger("org.jline").setLevel(Level.OFF);
    AnsiConsole.systemInstall();

    System.out.println("Loading...");
    System.out.println(ArcAnsi.ansi().clearScreen().moveCursor(1, 1).resetAll());
    BosstroveRevenge.get().start();
    
  }
}
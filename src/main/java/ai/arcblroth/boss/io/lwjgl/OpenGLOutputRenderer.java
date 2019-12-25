package ai.arcblroth.boss.io.lwjgl;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Erase;
import org.jline.terminal.*;
import ai.arcblroth.boss.Relauncher;
import ai.arcblroth.boss.crash.CrashReportGenerator;
import ai.arcblroth.boss.io.IOutputRenderer;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.StaticDefaults;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;

public class OpenGLOutputRenderer implements IOutputRenderer {
	
	private Terminal terminal;
	private Throwable error;
	
	private static final boolean SHOW_FPS = true;
	private double fps = 1;
	private long lastRenderTime;
	private static final long BYTES_IN_MEGABYTE = 1000000;
	
	public String debugLine = "";
	
	public OpenGLOutputRenderer() {
		try {
			this.terminal = TerminalBuilder.builder().name("Bosstrove's Revenge").jansi(true).jna(true)
					.nativeSignals(true)
					.signalHandler(true ? Terminal.SignalHandler.SIG_DFL : Terminal.SignalHandler.SIG_IGN).build();
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				terminal.setSize(new Size(StaticDefaults.OUTPUT_HEIGHT, StaticDefaults.OUTPUT_WIDTH));
			} else {
				
			}
			terminal.enterRawMode();
			lastRenderTime = System.currentTimeMillis();
		} catch (Exception e) {
			System.err.println("Could not init display, aborting launch...");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void render(PixelAndTextGrid pg) {
		//if(pg != null) {
			if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {
				
				//FPS Benchmarking
				long currTime = System.currentTimeMillis();
				fps = 1000D / (currTime - lastRenderTime);
				lastRenderTime = currTime;
			}
		//}
	}
	
	public void setDebugLine(String s) {
		debugLine = s;
	}

	public void displayFatalError(Throwable e) {
		error = e;
		
	}
	
	public void clear() {
		if(!(System.getProperty(Relauncher.FORCE_NORENDER) != null && System.getProperty(Relauncher.FORCE_NORENDER).equals("true"))) {
			
		}
	}
	
	public Terminal getTerminal() {
		return terminal;
	}
	
	public double getFps() {
		return fps;
	}

}
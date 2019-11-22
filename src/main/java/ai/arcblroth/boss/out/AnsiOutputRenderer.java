package ai.arcblroth.boss.out;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.Ansi.Erase;
import org.jline.terminal.*;

import ai.arcblroth.boss.BosstrovesRevenge;
import ai.arcblroth.boss.event.AutoSubscribeClass;
import ai.arcblroth.boss.event.SubscribeEvent;
import ai.arcblroth.boss.event.TestEvent;
import ai.arcblroth.boss.render.*;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.ThreadUtils;

import java.util.concurrent.*;
import java.awt.Color;
import java.io.File;
import java.io.IOException;

@AutoSubscribeClass
public class AnsiOutputRenderer implements OutputRenderer {

	private static final String PIXEL_CHAR = "\u2580";
	private Terminal terminal;
	public static final int OUTPUT_HEIGHT = 96;
	public static final int OUTPUT_WIDTH = 128;

	public AnsiOutputRenderer() {
		try {
			this.terminal = TerminalBuilder.builder().name("Bosstrove's Revenge").jansi(true).jna(true)
					.nativeSignals(true)
					.signalHandler(true ? Terminal.SignalHandler.SIG_DFL : Terminal.SignalHandler.SIG_IGN).build();
			if (!System.getProperty("os.name").toLowerCase().contains("win")) {
				terminal.setSize(new Size(OUTPUT_HEIGHT, OUTPUT_WIDTH));
			} else {
				// Runtime.getRuntime().exec("C:\\Windows\\System32\\cmd /C mode " +
				// OUTPUT_WIDTH + "," + OUTPUT_HEIGHT/2);
			}
		} catch (Exception e) {
			System.err.println("Could not init terminal, aborting launch...");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void render(PixelGrid pg) {
		Size s = terminal.getSize();
		if (s.getColumns() >= pg.getWidth() && s.getRows() >= pg.getHeight() / 2) {
			String leftPad = PadUtils.leftPad("",
					(int) Math.floor(((double) s.getColumns() - (double) pg.getWidth()) / 2D));
			String blankLines = PadUtils.stringTimes(PadUtils.stringTimes(" ", s.getColumns()) + "\n",
					(int) Math.floor(((double) s.getRows() - (double) pg.getHeight() / 2D) / 2D));
			ArcAnsi ansiBuilder = ArcAnsi.ansi().resetAll().moveCursor(0, 0).resetAll().append(blankLines);
			for (int rowNum = 0; rowNum < (pg.getHeight() / 2) * 2; rowNum += 2) {
				ArcAnsi rowBuilder = ArcAnsi.ansi();
				rowBuilder.append(leftPad);
				ConcurrentHashMap<Integer, Color> row1 = pg.get(rowNum);
				ConcurrentHashMap<Integer, Color> row2 = pg.get(rowNum + 1);
				for (int colNum = 0; colNum < pg.getWidth(); colNum++) {
					rowBuilder.fgColor(row1.get(colNum)).bgColor(row2.get(colNum)).append(PIXEL_CHAR);
				}
				rowBuilder.resetAll();
				ansiBuilder.append(rowBuilder.toString());
				ansiBuilder.append(leftPad + " \n");
			}
			ansiBuilder.resetAll().append(blankLines);
			if (terminal.getType() != Terminal.TYPE_DUMB) {
				terminal.writer().print(ansiBuilder);
			} else {
				System.out.print(Ansi.ansi().eraseScreen(Erase.ALL).reset());
				System.out.print(ansiBuilder);
			}
		} else {
			String toPrint = Ansi.ansi().cursor(1, 1).reset()
					.a(String.format("Screen resolution too small [%s×%s]", s.getColumns(), s.getRows()))
					.eraseScreen(Erase.FORWARD).toString();
			if (terminal.getType() != Terminal.TYPE_DUMB) {
				terminal.writer().print(toPrint + "\n");
			} else {
				System.out.print(toPrint + "\n");
			}
		}
	}

}
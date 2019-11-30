package ai.arcblroth.boss.crash;

import java.util.Random;

public class CrashReportGenerator {
	
	private static final String[] MINECRAFT_REFERENCES = new String[] {
			"// Like Minecraft Crash Report but worse!",
			"Yeetus Deletus we haveth Crashus",
			"Awww man!",
			"Please remain seated until the airplane comes to a full stop.",
			"Ouch! That hurts!",
			"Turn it off and back on!",
			"Super Crasher 2: Electric Boogaloo",
			"Y u do dis?",
			"Curse you, Perry!",
			"I feel like this will end up on tvtropes.",
			"The game is a lie.",
			"Crash sequence initiated",
			"I can't crash you without getting closer!",
			"99% - [insert www.nooooooooooooooo.com button]"
	};
	
	private CrashReportGenerator() {}
	
	public static String generateCrashReport(Throwable t) {
		StringBuilder crashReport = new StringBuilder();
		crashReport.append("---[ CRASH REPORT ]---\n \n");
		
		//Tee hee hee
		Random r = new Random();
		crashReport.append(MINECRAFT_REFERENCES[r.nextInt(MINECRAFT_REFERENCES.length)]);
		crashReport.append("\n \n");
		addException(crashReport, t);
		
		return crashReport.toString();
	}
	
	private static void addException(StringBuilder builder, Throwable t) {
		//Build the stack trace
		builder.append(t.getClass().getName()).append(": ").append(t.getMessage()).append("\n");
		for(StackTraceElement ste : t.getStackTrace()) {
			builder.append("    at ").append(ste.toString()).append("\n");
		}
		if(t.getCause() != null) {
			builder.append("Caused by: ");
			addException(builder, t.getCause());
		}
	}
	
}

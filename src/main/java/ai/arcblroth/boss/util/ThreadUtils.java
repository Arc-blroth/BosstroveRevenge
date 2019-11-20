package ai.arcblroth.boss.util;

public class ThreadUtils {
	
	public static void safeSleep(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch(InterruptedException e) {
			
		}
	}
	
	public static void waitForever() {
		while(true) {
			safeSleep(Long.MAX_VALUE);
		}
	}
	
}

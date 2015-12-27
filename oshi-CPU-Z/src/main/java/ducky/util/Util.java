package main.java.ducky.util;

public class Util {
	public static void sleep(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void sleepAfre(long startTime, long ms){
		long now = System.currentTimeMillis();
		long util = startTime + ms;
		if (now < util) {
			sleep(util - now);
		}
	}
}

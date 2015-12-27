package main.java.ducky.util;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public abstract class FormatUtil {

	final private static long kiloByte = 1024L;
	final private static long megaByte = kiloByte * kiloByte;
	final private static long gigaByte = megaByte * kiloByte;
	final private static long teraByte = gigaByte * kiloByte;
	final private static long peraByte = teraByte * kiloByte;
	final private static long exaByte = peraByte * kiloByte;

	final private static long kiloHertz = 1000L;
	final private static long megaHertz = kiloHertz * kiloHertz;
	final private static long gigaHertz = megaHertz * kiloHertz;
	final private static long teraHertz = gigaHertz * kiloHertz;
	final private static long petaHertz = teraHertz * kiloHertz;
	final private static long exaHertz = petaHertz * kiloHertz;

	public static String formatBytes(long bytes) {
		if (bytes == 1) {
			return String.format("%d byte", bytes);
		} else if (bytes < kiloByte) {
			return String.format("%d bytes", bytes);
		} else if (bytes < megaByte) {
			return String.format("%.1f KB", (double) bytes / kiloByte);
		} else if (bytes < gigaByte) {
			return String.format("%.1f MB", (double) bytes / megaByte);
		} else if (bytes < teraByte) {
			return String.format("%.1f GB", (double) bytes / gigaByte);
		} else if (bytes < peraByte) {
			return String.format("%.1f TB", (double) bytes / teraByte);
		} else if (bytes < exaByte) {
			return String.format("%.1f PB", (double) bytes / peraByte);
		} else {
			return String.format("%f bytes", (double) bytes);
		}
	}

	public static String formatHertz(long hertz) {
		if (hertz < kiloHertz) {
			return String.format("%d Hz", hertz);
		} else if (hertz < megaHertz) {
			return String.format("%.1f kHz", (double) hertz / kiloHertz);
		} else if (hertz < gigaHertz) {
			return String.format("%.1f MHz", (double) hertz / megaHertz);
		} else if (hertz < teraHertz) {
			return String.format("%.1f GHz", (double) hertz / gigaHertz);
		} else if (hertz < petaHertz) {
			return String.format("%.1f THz", (double) hertz / teraHertz);
		} else if (hertz < exaHertz) {
			return String.format("%.1f PHz", (double) hertz / petaHertz);
		} else {
			return String.format("%d hertz",  hertz);
		}
	}

	public static String formatElapsedSecs(long secs) {
		long eTime = secs;
		final long days = TimeUnit.SECONDS.toDays(eTime);
		eTime -= days * 86400;
		final long hrs = TimeUnit.SECONDS.toHours(eTime);
		eTime -= hrs * 3600;
		final long mins = TimeUnit.SECONDS.toMinutes(eTime);
		eTime -= mins * 60;
		final long sec = eTime;

		return String.format("%d days, %02d:%02d:%02d", days, hrs, mins, sec);
	}

	public static float round(float d, int decimalPlace){
		final BigDecimal bd = new BigDecimal(Float.toString(d)).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.floatValue();
	}
	
	public static long getUnsignedInt(int x) {
		// TODO Auto-generated method stub
		return x & 0x00000000ffffffffL;
	}

}

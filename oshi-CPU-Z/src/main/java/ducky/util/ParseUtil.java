package main.java.ducky.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParseUtil {

	final private static String Hertz = "Hz";
	final private static String kiloHertz = "k" + Hertz;
	final private static String megaHertz = "M" + Hertz;
	final private static String gigaHertz = "G" + Hertz;
	final private static String teraHertz = "T" + Hertz;
	final private static String petaHertz = "P" + Hertz;
	final private static Map<String, Long> multipliers;

	static {
		multipliers = new HashMap<>();
		multipliers.put(Hertz, 1L);
		multipliers.put(kiloHertz, 1000L);
		multipliers.put(megaHertz, 1000000L);
		multipliers.put(gigaHertz, 1000000000L);
		multipliers.put(teraHertz, 1000000000000L);
		multipliers.put(petaHertz, 1000000000000000L);
	}

	public static long parseHertz(String hertz) {
		Pattern pattern = Pattern.compile("(\\d+(.\\d+)?) ?([kMGT]?Hz)");
		Matcher matcher = pattern.matcher(hertz.trim());

		if (matcher.find() && matcher.groupCount() == 3) {
			Double value = Double.valueOf(matcher.group(1));
			String unit = matcher.group(3);
			if (multipliers.containsKey(unit)) {
				value *= multipliers.get(unit);
				return value.longValue();
			}
		}
		return -1L;
	}
	
	public static int parseString(String s, int i){
		String[] ss = s.split("\\s+");
		if (ss.length < 2) {
			return i;
		} else {
			return Integer.valueOf(ss[ss.length - 1]);
		}
	}
}

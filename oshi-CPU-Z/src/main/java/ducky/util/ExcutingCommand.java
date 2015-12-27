package main.java.ducky.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ExcutingCommand {
	public static ArrayList<String> runNative(String cmdToRun) {
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmdToRun);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		ArrayList<String> sa = new ArrayList<>();
		try {
			while ((line = reader.readLine()) != null) {
				sa.add(line);
			}
			p.waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return sa;
	}

	public static String getFirstAnswer(String cmd2launch) {
		return getAnswerAt(cmd2launch, 0);
	}

	private static String getAnswerAt(String cmd2launch, int i) {
		// TODO Auto-generated method stub
		List<String> sa = ExcutingCommand.runNative(cmd2launch);
		if (sa != null && i >=0 && i < sa.size()) {
			return sa.get(i);
		}
		return null;
	}
}

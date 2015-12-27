package main.java.ducky.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FileUtil {
	public static List<String> readFile(String fileName) throws IOException{
//		return Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
		return Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8);
	}
}

package main.java.ducky.software.os.linux;

import java.io.FileReader;
import java.util.Scanner;

import main.java.ducky.software.os.OperatingSystemVersion;

public class LinuxOSVersionInfoEx implements OperatingSystemVersion {
	private String version;
	private String codeName;
	private String ver;
	
	public LinuxOSVersionInfoEx(){
		try(Scanner in = new Scanner(new FileReader("/etc/os-release"))) {
			in.useDelimiter("\n");
			while(in.hasNext()){
				String[] splittedLine = in.next().split("=");
				if (splittedLine[0].equals("VERSION_ID")) {
					setVersion(splittedLine[1].replaceAll("^\"|\"$", ""));
				}
				if (splittedLine[0].equals("VERSION")) {
					splittedLine[1] = splittedLine[1].replaceAll("^\"|\"$", "");
					String[] split = splittedLine[1].split("[()]");
					if (split.length <= 1) {
						split = splittedLine[1].split(", ");
					}
					if (split.length > 1) {
						setCodeName(split[1]);
					} else {
						setCodeName(splittedLine[1]);
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			return;
		}
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getCodeName() {
		return codeName;
	}

	public void setCodeName(String codeName) {
		this.codeName = codeName;
	}
}

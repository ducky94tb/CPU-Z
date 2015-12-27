package main.java.ducky.software.os.linux;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import main.java.ducky.software.os.OperatingSystem;
import main.java.ducky.software.os.OperatingSystemVersion;

public class LinuxOperatingSystem implements OperatingSystem{

	private OperatingSystemVersion version;
	private String family;
	
	
	@Override
	public String getFamily() {
		// TODO Auto-generated method stub
		if (this.family == null) {
			try(final Scanner in = new Scanner(new FileReader("/etc/os-release"))){
				in.useDelimiter("\n");
				while(in.hasNext()){
					String[] splittedLine = in.next().split("=");
					if (splittedLine[0].equals("NAME")) {
						this.family = splittedLine[1].replaceAll("^\"|\"$", "");
						break;
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "";
			}
		}
		return this.family;
	}

	@Override
	public String getManufacturer() {
		// TODO Auto-generated method stub
		return "GNU/Linux";
	}

	@Override
	public OperatingSystemVersion getVersion() {
		// TODO Auto-generated method stub
		if (this.version == null) {
			this.version = new LinuxOSVersionInfoEx();
		}
		return this.version;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(getManufacturer());
		sb.append(" ");
		sb.append(getFamily());
		sb.append(" ");
		sb.append(getVersion().toString());
		return sb.toString();
	}
}

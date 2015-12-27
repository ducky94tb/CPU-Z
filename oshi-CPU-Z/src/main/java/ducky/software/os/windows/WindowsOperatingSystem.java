package main.java.ducky.software.os.windows;

import main.java.ducky.software.os.OperatingSystem;
import main.java.ducky.software.os.OperatingSystemVersion;

public class WindowsOperatingSystem implements OperatingSystem{

	private OperatingSystemVersion version;
	
	@Override
	public String getFamily() {
		// TODO Auto-generated method stub
		return "Windows";
	}

	@Override
	public String getManufacturer() {
		// TODO Auto-generated method stub
		return "Microsoft";
	}

	@Override
	public OperatingSystemVersion getVersion() {
		// TODO Auto-generated method stub
		if (this.version == null) {
			this.version = (OperatingSystemVersion) new WindowsOSVersionInfoEx();
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

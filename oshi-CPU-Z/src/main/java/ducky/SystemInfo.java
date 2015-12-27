package main.java.ducky;

import com.sun.jna.Platform;

import main.java.ducky.hardware.HardwareAbstractionLayer;
import main.java.ducky.hardware.platform.linux.LinuxHardwareAbstractionLayer;
import main.java.ducky.hardware.platform.windows.WindowsHardwareAbstractionLayer;
import main.java.ducky.software.os.OperatingSystem;
import main.java.ducky.software.os.linux.LinuxOperatingSystem;
import main.java.ducky.software.os.windows.WindowsOperatingSystem;

public class SystemInfo {
	private OperatingSystem os = null;
	private HardwareAbstractionLayer hLayer = null;
	private PlatformEnum platformEnum;

	{
		if (Platform.isWindows()) {
			this.platformEnum = PlatformEnum.WINDOWS;
		} else if (Platform.isLinux()) {
			this.platformEnum = PlatformEnum.LINUX;
		} else if (Platform.isMac()) {
			this.platformEnum = PlatformEnum.MACOSX;
		} else {
			this.platformEnum = PlatformEnum.UNKNOWN;
		}
	}
	
	public OperatingSystem getOperatingSystem(){
		if (this.os == null) {
			switch (this.platformEnum) {
			case WINDOWS:
				this.os = new WindowsOperatingSystem();
				break;
			case LINUX:
				this.os = new LinuxOperatingSystem();
				break;
			default:
				throw new RuntimeException("Operating system not supported: " + Platform.getOSType());
			}
		}
		return this.os;
	}
	
	public HardwareAbstractionLayer getHardware(){
		if (this.hLayer == null) {
			switch (this.platformEnum) {
			case WINDOWS:
				this.hLayer =  new WindowsHardwareAbstractionLayer();
				break;
			case LINUX:
				this.hLayer =  new LinuxHardwareAbstractionLayer();
				break;

			default:
				throw new RuntimeException("Operating system not supported: " + Platform.getOSType());
			}
		}
		return this.hLayer;
	}
}

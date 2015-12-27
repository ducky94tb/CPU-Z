package main.java.ducky.hardware;

import main.java.ducky.software.os.OSFileStore;

public interface HardwareAbstractionLayer {
	CentralProcessor getProcessor();
	GlobalMemory getMemory();
	PowerSource[] getPowerSources();
	OSFileStore[] getFileStores();
}

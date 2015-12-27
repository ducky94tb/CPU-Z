package main.java.ducky.hardware.platform.windows;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import main.java.ducky.hardware.CentralProcessor;
import main.java.ducky.hardware.GlobalMemory;
import main.java.ducky.hardware.HardwareAbstractionLayer;
import main.java.ducky.hardware.PowerSource;
import main.java.ducky.software.os.OSFileStore;
import main.java.ducky.software.os.windows.WindowsFileSystem;

public class WindowsHardwareAbstractionLayer implements HardwareAbstractionLayer{
	private CentralProcessor processor;
	private GlobalMemory memory;
	
	@Override
	public GlobalMemory getMemory(){
		if (this.memory == null) {
			this.memory = new WindowsGlobalMemory();
		}
		return this.memory;
	}

	@Override
	public CentralProcessor getProcessor() {
		// TODO Auto-generated method stub
		if (this.processor == null) {
			processor = new WindowsCentralProcessor();
			final String cpuRegistryRoot = "HARDWARE\\DESCRIPTION\\System\\CentralProcessor";
			String[] processorIds = Advapi32Util.registryGetKeys(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryRoot);
			if (processorIds.length > 0) {
                String cpuRegistryPath = cpuRegistryRoot + "\\" + processorIds[0];
                processor.setIdentifier(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath,
                        "Identifier"));
                processor.setName(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath,
                        "ProcessorNameString"));
                processor.setVendor(Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE, cpuRegistryPath,
                        "VendorIdentifier"));
            }
		}
		return this.processor;
	}

	@Override
	public PowerSource[] getPowerSources() {
		// TODO Auto-generated method stub
		return WindowsPowerSource.getPowerSources();
	}

	@Override
	public OSFileStore[] getFileStores() {
		// TODO Auto-generated method stub
		return WindowsFileSystem.getFileStores();
	}
}

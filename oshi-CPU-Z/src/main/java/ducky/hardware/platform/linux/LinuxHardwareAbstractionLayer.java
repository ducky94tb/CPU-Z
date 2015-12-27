package main.java.ducky.hardware.platform.linux;

import java.util.List;

import main.java.ducky.hardware.CentralProcessor;
import main.java.ducky.hardware.GlobalMemory;
import main.java.ducky.hardware.HardwareAbstractionLayer;
import main.java.ducky.hardware.PowerSource;
import main.java.ducky.software.os.OSFileStore;
import main.java.ducky.software.os.linux.LinuxFileSystem;
import main.java.ducky.util.FileUtil;

public class LinuxHardwareAbstractionLayer implements HardwareAbstractionLayer {

	private static final String SEPARATOR = "\\s+:\\s";
	private CentralProcessor processor;
	private GlobalMemory memory;

	@Override
	public CentralProcessor getProcessor() {
		// TODO Auto-generated method stub
		if (this.processor == null) {
			List<String> cpuInfo = null;
			try {
				cpuInfo = FileUtil.readFile("/proc/cpuinfo");
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			}
			for (String toBeAnalyzed : cpuInfo) {
				if (toBeAnalyzed.equals("")) {
					break;
				}
				if (processor == null) {
					processor = new LinuxCentralProcessor();
				}
				if (toBeAnalyzed.startsWith("model name\t")) {
					processor.setName(toBeAnalyzed.split(SEPARATOR)[1]);
					continue;
				}
				if (toBeAnalyzed.startsWith("flags\t")) {
					String[] flags = toBeAnalyzed.split(SEPARATOR)[1].split(" ");
					boolean found = false;
					for (String flag : flags) {
						if (flag.equalsIgnoreCase("LM")) {
							found = true;
							break;
						}
					}
					processor.setCPU64(found);
					continue;
				}
				if (toBeAnalyzed.startsWith("cpu family\t")) {
					processor.setFamily(toBeAnalyzed.split(SEPARATOR)[1]);
					continue;
				}
				if (toBeAnalyzed.startsWith("model\t")) {
					processor.setModel(toBeAnalyzed.split(SEPARATOR)[1]);
					continue;
				}
				if (toBeAnalyzed.startsWith("stepping\t")) {
					processor.setStepping(toBeAnalyzed.split(SEPARATOR)[1]);
					continue;
				}
				if (toBeAnalyzed.startsWith("vendor_id")) {
					processor.setVendor(toBeAnalyzed.split(SEPARATOR)[1]);
					continue;
				}
			}
		}
		return this.processor;
	}

	@Override
	public GlobalMemory getMemory() {
		// TODO Auto-generated method stub
		if (this.memory == null) {
			this.memory = new LinuxGlobalMermory();
		}
		return this.memory;
	}

	@Override
	public PowerSource[] getPowerSources() {
		// TODO Auto-generated method stub
		return LinuxPowerSource.getPowerSources();
	}

	@Override
	public OSFileStore[] getFileStores() {
		// TODO Auto-generated method stub
		return LinuxFileSystem.getFileStores();
	}

}

package main.java.ducky.hardware.platform.linux;

import java.util.List;

import main.java.ducky.hardware.GlobalMemory;
import main.java.ducky.jna.platform.linux.Libc;
import main.java.ducky.jna.platform.linux.Libc.Sysinfo;
import main.java.ducky.util.FileUtil;

public class LinuxGlobalMermory implements GlobalMemory {

	private long totalMemory = 0;

	@Override
	public long getTotal() {
		// TODO Auto-generated method stub
		if (this.totalMemory == 0) {
			Sysinfo info = new Sysinfo();
			if (0 != Libc.INSTANCE.sysinfo(info)) {
				return 0L;
			}
			this.totalMemory = info.totalram.longValue() * info.mem_unit;
		}
		return this.totalMemory;
	}

	private long parseMeminfo(String[] memorySplit) {
		if (memorySplit.length < 2) {
			return 0L;
		}
		long memory = Long.valueOf(memorySplit[1]);
		if (memorySplit.length > 2 && memorySplit[2].equals("kB")) {
			memory *= 1024;
		}
		return memory;
	}

	@Override
	public long getAvailable() {
		// TODO Auto-generated method stub
		long availableMemory = 0;
		List<String> memInfo = null;
		try {
			memInfo = FileUtil.readFile("/proc/meminfo");
		} catch (Exception e) {
			// TODO: handle exception
			return availableMemory;
		}
		for (String checkLine : memInfo) {
			if (checkLine.startsWith("MemAvailable:")) {
				String[] memorySplit = checkLine.split("\\s+");
				availableMemory = parseMeminfo(memorySplit);
				break;
			} else if (checkLine.startsWith("MemFree:")) {
				String[] memorySplit = checkLine.split("\\s+");
				availableMemory +=parseMeminfo(memorySplit);
			} else if (checkLine.startsWith("Active(file):")) {
				String[] memorySplit = checkLine.split("\\s+");
				availableMemory +=parseMeminfo(memorySplit);
			} else if (checkLine.startsWith("Inactive(file):")) {
				String[] memorySplit = checkLine.split("\\s+");
				availableMemory +=parseMeminfo(memorySplit);
			} else if (checkLine.startsWith("SReclaimable:")) {
                String[] memorySplit = checkLine.split("\\s+");
                availableMemory += parseMeminfo(memorySplit);
            }
		}
		
		return availableMemory;
	}

}

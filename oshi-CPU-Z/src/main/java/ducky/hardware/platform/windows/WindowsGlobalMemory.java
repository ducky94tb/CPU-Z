package main.java.ducky.hardware.platform.windows;

import com.sun.jna.platform.win32.WinBase.MEMORYSTATUSEX;

import main.java.ducky.hardware.GlobalMemory;
import main.java.ducky.jna.platform.windows.Kernel32;

public class WindowsGlobalMemory implements GlobalMemory {

	private MEMORYSTATUSEX memory = new MEMORYSTATUSEX();

	public WindowsGlobalMemory() {
		if(!Kernel32.INSTANCE.GlobalMemoryStatusEx(this.memory)){
			this.memory = null;
		}
	}

	@Override
	public long getTotal() {
		// TODO Auto-generated method stub
		if (this.memory == null) {
			return 0L;
		}
		return this.memory.ullTotalPhys.longValue();
	}

	@Override
	public long getAvailable() {
		// TODO Auto-generated method stub
		if (this.memory == null) {
			return 0L;
		}
		return this.memory.ullAvailPhys.longValue();
	}

}

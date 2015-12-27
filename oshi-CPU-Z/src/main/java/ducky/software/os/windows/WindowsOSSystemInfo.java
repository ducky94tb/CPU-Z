package main.java.ducky.software.os.windows;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase.SYSTEM_INFO;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;

public class WindowsOSSystemInfo {
	private SYSTEM_INFO si = null;
	public WindowsOSSystemInfo(){
		SYSTEM_INFO si = new SYSTEM_INFO();
		Kernel32.INSTANCE.GetSystemInfo(si);
		IntByReference isWow64 = new IntByReference();
		HANDLE hProcess = Kernel32.INSTANCE.GetCurrentProcess();
		if (Kernel32.INSTANCE.IsWow64Process(hProcess, isWow64)) {
			if (isWow64.getValue() > 0) {
				Kernel32.INSTANCE.GetNativeSystemInfo(si);
			}
		}
		this.si = si;
	}
	
	public WindowsOSSystemInfo(SYSTEM_INFO si) {
		super();
		this.si = si;
	}
	
	public int getNumberOfProcessors(){
		return this.si.dwNumberOfProcessors.intValue();
	}
}

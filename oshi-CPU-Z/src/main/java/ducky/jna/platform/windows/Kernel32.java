package main.java.ducky.jna.platform.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinBase;

public interface Kernel32 extends com.sun.jna.platform.win32.Kernel32{
	Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("Kernel32", Kernel32.class);
	boolean GetSystemTimes(WinBase.FILETIME lpIdleTime, WinBase.FILETIME lpKernelTime, WinBase.FILETIME lpUserTime);
}

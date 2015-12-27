package main.java.ducky.software.os.windows;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.OSVERSIONINFOEX;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinUser;

import main.java.ducky.software.os.OperatingSystemVersion;

public class WindowsOSVersionInfoEx implements OperatingSystemVersion{
	private OSVERSIONINFOEX versionInfo;

	
	
	public WindowsOSVersionInfoEx() {
		this.versionInfo = new OSVERSIONINFOEX();
		if (!Kernel32.INSTANCE.GetVersionEx(this.versionInfo)) {
			this.versionInfo = null;
		}
	}

	public int getMajor(){
		if(this.versionInfo == null){
			return 0;
		}
		return this.versionInfo.dwMajorVersion.intValue();
	}
	
	public int getMinor(){
		if(this.versionInfo == null)
			return 0;
		return this.versionInfo.dwMinorVersion.intValue();
	}
	
	public int getBuildNumber(){
		if (this.versionInfo == null){
			return 0;
		}
		return this.versionInfo.dwBuildNumber.intValue();
	}
	
	public int getPlatformId(){
		if (this.versionInfo == null) {
			return 0;
		}
		return this.versionInfo.dwPlatformId.intValue();
	}
	
	public String getServicePack(){
		if(this.versionInfo == null)
			return "";
		return Native.toString(this.versionInfo.szCSDVersion);
	}
	
	public int getSuiteMask(){
		if(this.versionInfo == null)
			return 0;
		return this.versionInfo.wSuiteMask.intValue();
	}
	
	public byte getProductType(){
		if (this.versionInfo == null) {
			return 0;
		}
		return this.versionInfo.wProductType;
	}
	@Override
    public String toString() {
        if (this.versionInfo == null) {
            return "Unknown";
        }

        String version = null;

        // see
        // http://msdn.microsoft.com/en-us/library/windows/desktop/ms724833%28v=vs.85%29.aspx
        if (getPlatformId() == WinNT.VER_PLATFORM_WIN32_NT) {
            boolean ntWorkstation = getProductType() == WinNT.VER_NT_WORKSTATION;
            if (getMajor() == 10 && getMinor() == 0 && ntWorkstation) {
                version = "10";
            } else if (getMajor() == 10 && getMinor() == 0 && !ntWorkstation) {
                version = "Server 2016";
            } else if (getMajor() == 6 && getMinor() == 3 && ntWorkstation) {
                version = "8.1";
            } else if (getMajor() == 6 && getMinor() == 3 && !ntWorkstation) {
                version = "Server 2012 R2";
            } else if (getMajor() == 6 && getMinor() == 2 && ntWorkstation) {
                version = "8";
            } else if (getMajor() == 6 && getMinor() == 2 && !ntWorkstation) {
                version = "Server 2012";
            } else if (getMajor() == 6 && getMinor() == 1 && ntWorkstation) {
                version = "7";
            } else if (getMajor() == 6 && getMinor() == 1 && !ntWorkstation) {
                version = "Server 2008 R2";
            } else if (getMajor() == 6 && getMinor() == 0 && !ntWorkstation) {
                version = "Server 2008";
            } else if (getMajor() == 6 && getMinor() == 0 && ntWorkstation) {
                version = "Vista";
            } else if (getMajor() == 5 && getMinor() == 2 && !ntWorkstation) {
                version = User32.INSTANCE.GetSystemMetrics(WinUser.SM_SERVERR2) != 0 ? "Server 2003" : "Server 2003 R2";
            } else if (getMajor() == 5 && getMinor() == 2 && ntWorkstation) {
                version = "XP"; // 64 bits
            } else if (getMajor() == 5 && getMinor() == 1) {
                version = "XP"; // 32 bits
            } else if (getMajor() == 5 && getMinor() == 0) {
                version = "2000";
            } else if (getMajor() == 4) {
                version = "NT 4";
                if ("Service Pack 6".equals(getServicePack())) {
                    if (Advapi32Util.registryKeyExists(WinReg.HKEY_LOCAL_MACHINE,
                            "SOFTWARE\\Microsoft\\Windows NT\\CurrentVersion\\Hotfix\\Q246009")) {
                        return "NT4 SP6a";
                    }
                }

            } else {
                throw new RuntimeException("Unsupported Windows NT version: " + this.versionInfo.toString());
            }

            if (this.versionInfo.wServicePackMajor.intValue() > 0) {
                version = version + " SP" + this.versionInfo.wServicePackMajor.intValue();
            }

        } else if (getPlatformId() == WinNT.VER_PLATFORM_WIN32_WINDOWS) {
            if (getMajor() == 4 && getMinor() == 90) {
                version = "ME";
            } else if (getMajor() == 4 && getMinor() == 10) {
                if (this.versionInfo.szCSDVersion[1] == 'A') {
                    version = "98 SE";
                } else {
                    version = "98";
                }
            } else if (getMajor() == 4 && getMinor() == 0) {
                if (this.versionInfo.szCSDVersion[1] == 'C' || this.versionInfo.szCSDVersion[1] == 'B') {
                    version = "95 OSR2";
                } else {
                    version = "95";
                }
            } else {
                throw new RuntimeException("Unsupported Windows 9x version: " + this.versionInfo.toString());
            }
        } else {
            throw new RuntimeException("Unsupported Windows platform: " + this.versionInfo.toString());
        }

        return version;
    }
	
	public WindowsOSVersionInfoEx(OSVERSIONINFOEX versionInfo) {
        this.versionInfo = versionInfo;
    }
}

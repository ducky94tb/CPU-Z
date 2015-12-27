package main.java.ducky.jna.platform.windows;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

public interface PowerProfile extends Library{
	PowerProfile INSTANCE = (PowerProfile) Native.loadLibrary("PowrProf", PowerProfile.class);
	
	static int SYSTEM_BATTERY_STATE = 5;
	
	static class SystemBatteryState extends Structure{

		public byte acOnLine;
		public byte batteryPresent;
		public byte charging;
		public byte discharging;
		public byte[] spare1 = new byte[4];
		public int maxCapacity;
		public int remainingCapacity;
		public int rate;
		public int estimatedTime;
		public int defaultAlert1;
		public int defaultAlert2;
		
		@Override
		protected List getFieldOrder() {
			// TODO Auto-generated method stub
			return Arrays.asList(new String[] { "acOnLine", "batteryPresent", "charging", "discharging", "spare1",
                    "maxCapacity", "remainingCapacity", "rate", "estimatedTime", "defaultAlert1", "defaultAlert2" });
		}
		
	}
	
	int CallNtPowerInformation(int informationLevel, Pointer lpInputBuffer, NativeLong nInputBufferSize,
            Structure lpOutputBuffer, NativeLong nOutputBufferSize);
}

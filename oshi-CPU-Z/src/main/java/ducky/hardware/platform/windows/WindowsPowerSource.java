package main.java.ducky.hardware.platform.windows;

import com.sun.jna.NativeLong;

import main.java.ducky.hardware.PowerSource;
import main.java.ducky.jna.platform.windows.PowerProfile;
import main.java.ducky.util.FormatUtil;
import main.java.ducky.jna.platform.windows.PowerProfile.SystemBatteryState;

public class WindowsPowerSource implements PowerSource {

	private String name;
	private double remainingCapacity;
	private double timeRemaining;

	public WindowsPowerSource(String name, double remainingCapacity, double timeRemaining) {
		super();
		this.name = name;
		this.remainingCapacity = remainingCapacity;
		this.timeRemaining = timeRemaining;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.name;
	}

	@Override
	public double getRemainingCapacity() {
		// TODO Auto-generated method stub
		return this.remainingCapacity;
	}

	@Override
	public double getTimeRemaining() {
		// TODO Auto-generated method stub
		return this.timeRemaining;
	}

	public static PowerSource[] getPowerSources() {
		String name = "System Battery";
		WindowsPowerSource[] psArray = new WindowsPowerSource[1];
		// Get structure
		SystemBatteryState batteryState = new SystemBatteryState();
		if (0 != PowerProfile.INSTANCE.CallNtPowerInformation(PowerProfile.SYSTEM_BATTERY_STATE, null,
				new NativeLong(0), batteryState, new NativeLong(batteryState.size()))
				|| batteryState.batteryPresent == 0) {
			psArray[0] = new WindowsPowerSource("Unknown", 0d, -1d);
		} else {
			int estimatedTime = -2; // -1 = unknown, -2 = unlimited
			if (batteryState.acOnLine == 0 && batteryState.charging == 0 && batteryState.discharging > 0) {
				estimatedTime = batteryState.estimatedTime;
			}
			long maxCapacity = FormatUtil.getUnsignedInt(batteryState.maxCapacity);
			long remainingCapacity = FormatUtil.getUnsignedInt(batteryState.remainingCapacity);

			psArray[0] = new WindowsPowerSource(name, (double) remainingCapacity / maxCapacity, estimatedTime);
		}

		return psArray;
	}

}

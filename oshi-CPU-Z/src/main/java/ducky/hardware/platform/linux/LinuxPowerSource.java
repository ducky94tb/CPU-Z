package main.java.ducky.hardware.platform.linux;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.ChangedCharSetException;

import main.java.ducky.hardware.PowerSource;
import main.java.ducky.util.FileUtil;

public class LinuxPowerSource implements PowerSource {

	private static final String PS_PATH = "/sys/class/power_supply/";
	private String name;
	private double remainingCapacity;
	private double timeRemaining;

	public LinuxPowerSource(String newName, double newRemainingCapacity, double newTimeRemaining) {
		// TODO Auto-generated constructor stub
		this.name = newName;
		this.remainingCapacity = newRemainingCapacity;
		this.timeRemaining = newTimeRemaining;
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

	public static PowerSource[] getPowerSources(){
		File f = new File(PS_PATH);
		String[] psNames = f.list();
		if (psNames == null) {
			psNames = new String[0];
		}
		List<LinuxPowerSource> psList = new ArrayList<>(psNames.length);
		for (String psName : psNames) {
			if (psName.startsWith("ADP")) {
				continue;
			}
			List<String> psInfo;
			try {
				psInfo = FileUtil.readFile(PS_PATH + psName + "/uevent");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
			boolean isPresent = false;
			boolean isCharging = false;
			String name = "Unknown";
			int energyNow = 0;
			int energyFull = 1;
			int powerNow = 1;
			for (String checkLine : psInfo) {
				if (checkLine.startsWith("POWER_SUPPLY_PRESENT")) {
					String[] psSplit = checkLine.split("=");
					if (psSplit.length > 1) {
						isPresent = Integer.parseInt(psSplit[1]) > 0;
					}
					if (!isPresent) {
						continue;
					}
				} else if (checkLine.startsWith("POWER_SUPPLY_NAME")) {
					String[] psSplit = checkLine.split("=");
					if (psSplit.length > 1) {
						name = psSplit[1];
					}
				} else if (checkLine.startsWith("POWER_SUPPLY_ENERGY_NOW")
						|| checkLine.startsWith("POWER_SUPPLY_CHARGE_NOW")) {
					String[] psSplit = checkLine.split("=");
					if (psSplit.length > 1)
                        energyNow = Integer.parseInt(psSplit[1]);
				} else if (checkLine.startsWith("POWER_SUPPLY_ENERGY_FULL")
						|| checkLine.startsWith("POWER_SUPPLY_CHARGE_FULL")) {
					String[] psSplit = checkLine.split("=");
					if (psSplit.length > 1)
                        energyFull = Integer.parseInt(psSplit[1]);
				} else if (checkLine.startsWith("POWER_SUPPLY_STATUS")) {
					String[] psSplit = checkLine.split("=");
					if (psSplit.length > 1 && psSplit[1].equals("Charging")) {
						isCharging = true;
					}
				} else if(checkLine.startsWith("POWER_SUPPLY_POWER_NOW") 
						|| checkLine.startsWith("POWER_SUPPLY_CURRENT_NOW")){
					String[] psSplit = checkLine.split("=");
					if (psSplit.length > 1) {
						powerNow = Integer.parseInt(psSplit[1]);
					}
					if (powerNow <= 0) {
						isCharging = true;
					}
				}
			}
			psList.add(new LinuxPowerSource(name, (double)energyNow/energyFull, 
					isCharging? -2d:3600d * energyNow/powerNow));
		}
		return psList.toArray(new LinuxPowerSource[psList.size()]);
	}

}

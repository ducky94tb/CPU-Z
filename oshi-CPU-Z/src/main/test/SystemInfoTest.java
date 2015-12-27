package main.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.util.Arrays;

import org.junit.Test;

import com.sun.jna.Platform;

import main.java.ducky.SystemInfo;
import main.java.ducky.hardware.GlobalMemory;
import main.java.ducky.hardware.HardwareAbstractionLayer;
import main.java.ducky.hardware.PowerSource;
import main.java.ducky.software.os.OSFileStore;
import main.java.ducky.software.os.OperatingSystem;
import main.java.ducky.software.os.OperatingSystemVersion;
import main.java.ducky.util.FormatUtil;
import main.java.ducky.util.Util;

public class SystemInfoTest {
	@Test
	public void testGetVersion() {
		SystemInfo si = new SystemInfo();
		OperatingSystem os = si.getOperatingSystem();
		assertNotNull(os);
		OperatingSystemVersion version = os.getVersion();
		assertNotNull(version);
		assertTrue(os.toString().length() > 0);
	}

	@Test
	public void testGetProcessor() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		assertTrue(hal.getProcessor().getLogicalProcessorCount() > 0);
	}

	@Test
	public void testGetMemory() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		GlobalMemory memory = hal.getMemory();
		assertNotNull(memory);
		assertTrue(memory.getTotal() > 0);
		assertTrue(memory.getAvailable() >= 0);
		assertTrue(memory.getAvailable() <= memory.getTotal());
	}

	@Test
	public void testCpuLoad() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		assertTrue(hal.getProcessor().getSystemCpuLoadBetweenTicks() >= 0
				&& hal.getProcessor().getSystemCpuLoadBetweenTicks() <= 1);
	}

	@Test
	public void testCpuLoadTicks() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		assertEquals(4, hal.getProcessor().getSystemCpuLoadTicks().length);
	}

	@Test
	public void testProcCpuLoad() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		for (int cpu = 0; cpu < hal.getProcessor().getLogicalProcessorCount(); cpu++) {
			assertTrue(hal.getProcessor().getProcessorCpuLoadBetweenTicks()[cpu] >= 0
					&& hal.getProcessor().getProcessorCpuLoadBetweenTicks()[cpu] <= 1);
		}
	}

	@Test
	public void testProcCpuLoadTicks() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		assertEquals(4, hal.getProcessor().getProcessorCpuLoadTicks()[0].length);
	}

	@Test
	public void testSystemCpuLoad() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		double cpuLoad = hal.getProcessor().getSystemCpuLoad();
		assertTrue(cpuLoad >= 0.0 && cpuLoad <= 1.0);
	}

	@Test
	public void testSystemLoadAverage() {
		if (Platform.isMac() || Platform.isLinux()) {
			SystemInfo si = new SystemInfo();
			HardwareAbstractionLayer hal = si.getHardware();
			assertTrue(hal.getProcessor().getSystemLoadAverage() >= 0.0);
		}
	}

	@Test
	public void testProcessorCounts() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		assertTrue(hal.getProcessor().getPhysicalProcessorCount() >= 1);
		assertTrue(hal.getProcessor().getLogicalProcessorCount() >= hal.getProcessor().getPhysicalProcessorCount());
	}

	@Test
	public void testCpuVendorFreq() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		assertTrue(hal.getProcessor().getVendorFreq() == -1 || hal.getProcessor().getVendorFreq() > 0);
	}

	@Test
	public void testPowerSource() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		if (hal.getPowerSources().length > 1) {
			assertTrue(hal.getPowerSources()[0].getRemainingCapacity() >= 0
					&& hal.getPowerSources()[0].getRemainingCapacity() <= 1);
			double epsilon = 1E-6;
			assertTrue(hal.getPowerSources()[0].getTimeRemaining() > 0
					|| Math.abs(hal.getPowerSources()[0].getTimeRemaining() - -1) < epsilon
					|| Math.abs(hal.getPowerSources()[0].getTimeRemaining() - -2) < epsilon);
		}
	}

	@Test
	public void testFileSystem() throws IOException {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		if (hal.getFileStores().length > 1) {
			assertTrue(hal.getFileStores()[0].getTotalSpace() >= 0);
			assertTrue(hal.getFileStores()[0].getUsableSpace() <= hal.getFileStores()[0].getTotalSpace());
		}
	}

	@Test
	public void testSystemUptime() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		long uptime = hal.getProcessor().getSystemUptime();
		assertTrue(uptime >= 0);
	}

	@Test
	public void testSerialNumber() {
		SystemInfo si = new SystemInfo();
		HardwareAbstractionLayer hal = si.getHardware();
		String sn = hal.getProcessor().getSystemSerialNumber();
		assertTrue(sn.length() >= 0);
	}

	public static void main(String[] args){
//		for (int i = 0; i < 5; i++) {
			run();
			Util.sleep(2000);
//		}
		
	}
	
	public static void run() {
        SystemInfo si = new SystemInfo();
        // software
        // software: operating system
        OperatingSystem os = si.getOperatingSystem();
        System.out.println(os);
        // hardware
        HardwareAbstractionLayer hal = si.getHardware();
        // hardware: processors
        System.out.println(hal.getProcessor());
        System.out.println(" " + hal.getProcessor().getPhysicalProcessorCount() + " physical CPU(s)");
        System.out.println(" " + hal.getProcessor().getLogicalProcessorCount() + " logical CPU(s)");

        System.out.println("Identifier: " + hal.getProcessor().getIdentifier());
        System.out.println("Serial Num: " + hal.getProcessor().getSystemSerialNumber());

        // hardware: memory
        System.out.println("Memory: " + FormatUtil.formatBytes(hal.getMemory().getAvailable()) + "/"
                + FormatUtil.formatBytes(hal.getMemory().getTotal()));
        // uptime
        System.out.println("Uptime: " + FormatUtil.formatElapsedSecs(hal.getProcessor().getSystemUptime()));

        // CPU
        long[] prevTicks = hal.getProcessor().getSystemCpuLoadTicks();
        System.out.println("CPU ticks @ 0 sec:" + Arrays.toString(prevTicks));
        // Wait a second...
        Util.sleep(1000);
        long[] ticks = hal.getProcessor().getSystemCpuLoadTicks();
        System.out.println("CPU ticks @ 1 sec:" + Arrays.toString(ticks));
        long user = ticks[0] - prevTicks[0];
        long nice = ticks[1] - prevTicks[1];
        long sys = ticks[2] - prevTicks[2];
        long idle = ticks[3] - prevTicks[3];
        long totalCpu = user + nice + sys + idle;

        System.out.format("User: %.1f%% Nice: %.1f%% System: %.1f%% Idle: %.1f%%%n", 100d * user / totalCpu, 100d
                * nice / totalCpu, 100d * sys / totalCpu, 100d * idle / totalCpu);
        System.out.format("CPU load: %.1f%% (counting ticks)%n",
                hal.getProcessor().getSystemCpuLoadBetweenTicks() * 100);
        System.out.format("CPU load: %.1f%% (OS MXBean)%n", hal.getProcessor().getSystemCpuLoad() * 100);
        double loadAverage = hal.getProcessor().getSystemLoadAverage();
        System.out.println("CPU load average: " + (loadAverage < 0 ? "N/A" : String.format("%.2f", loadAverage)));
        // per core CPU
        StringBuilder procCpu = new StringBuilder("CPU load per processor:");
        double[] load = hal.getProcessor().getProcessorCpuLoadBetweenTicks();
        for (int cpu = 0; cpu < load.length; cpu++) {
            procCpu.append(String.format(" %.1f%%", load[cpu] * 100));
        }
        System.out.println(procCpu.toString());

        // hardware: power
        StringBuilder sb = new StringBuilder("Power: ");
        if (hal.getPowerSources().length == 0) {
            sb.append("Unknown");
        } else {
            double timeRemaining = hal.getPowerSources()[0].getTimeRemaining();
            if (timeRemaining < -1d)
                sb.append("Charging");
            else if (timeRemaining < 0d)
                sb.append("Calculating time remaining");
            else
                sb.append(String.format("%d:%02d remaining", (int) (timeRemaining / 3600),
                        (int) (timeRemaining / 60) % 60));
        }
        for (PowerSource pSource : hal.getPowerSources()) {
            sb.append(String.format("%n %s @ %.1f%%", pSource.getName(), pSource.getRemainingCapacity() * 100d));
        }
        System.out.println(sb.toString());

        // hardware: file system
        System.out.println("File System:");

        OSFileStore[] fsArray = hal.getFileStores();
        for (OSFileStore fs : fsArray) {
            long usable = fs.getUsableSpace();
            long total = fs.getTotalSpace();
            System.out.format(" %s (%s) %s of %s free (%.1f%%)%n", fs.getName(),
                    fs.getDescription().isEmpty() ? "file system" : fs.getDescription(),
                    FormatUtil.formatBytes(usable), FormatUtil.formatBytes(fs.getTotalSpace()), 100d * usable / total);
        }
    }

}

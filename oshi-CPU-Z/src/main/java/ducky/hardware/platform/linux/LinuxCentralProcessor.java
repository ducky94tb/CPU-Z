package main.java.ducky.hardware.platform.linux;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import main.java.ducky.hardware.CentralProcessor;
import main.java.ducky.util.ExcutingCommand;
import main.java.ducky.util.FileUtil;
import main.java.ducky.util.ParseUtil;
import main.java.ducky.jna.platform.linux.Libc;
import main.java.ducky.jna.platform.linux.Libc.Sysinfo;

@SuppressWarnings("restriction")
public class LinuxCentralProcessor implements CentralProcessor {

	private static final java.lang.management.OperatingSystemMXBean OS_MXBEAN = ManagementFactory
			.getOperatingSystemMXBean();
	private static boolean sunMXBean;

	static {
		try {
			Class.forName("com.sun.management.OperatingSystemMXBean");
			((com.sun.management.OperatingSystemMXBean) OS_MXBEAN).getSystemCpuLoad();
			sunMXBean = true;
		} catch (ClassNotFoundException e) {
			sunMXBean = false;
		}
	}

	// Logical and physical processor counts
	private int logicalProcessorCount = 0;
	private int physicalProcessorCount = 0;

	// Maintain previous ticks to be used for calculating usage between them.
	// System ticks
	private long tickTime;
	private long[] prevTicks;
	private long[] curTicks;

	// Per-processor ticks [cpu][type]
	private long procTickTime;
	private long[][] prevProcTicks;
	private long[][] curProcTicks;

	// Processor info
	private String cpuVendor;
	private String cpuName;
	private String cpuIdentifier;
	private String cpuStepping;
	private String cpuModel;
	private String cpuFamily;
	private Long cuVendorFreq;
	private Boolean cpu64;

	/**
	 * Creat a processor
	 */
	public LinuxCentralProcessor() {
		calculateProcessorCounts();

		// System ticks
		this.prevTicks = new long[4];
		this.curTicks = new long[4];
		updateSystemTicks();

		// Per-processor ticks
		this.prevProcTicks = new long[logicalProcessorCount][4];
		this.curProcTicks = new long[logicalProcessorCount][4];
		updateProcessorTicks();
	}

	private void updateProcessorTicks() {
		// TODO Auto-generated method stub
		for (int cpu = 0; cpu < logicalProcessorCount; cpu++) {
			System.arraycopy(curProcTicks[cpu], 0, prevProcTicks[cpu], 0, curProcTicks[cpu].length);
		}
		this.procTickTime = System.currentTimeMillis();
		long[][] ticks = getProcessorCpuLoadTicks();
		for (int cpu = 0; cpu < logicalProcessorCount; cpu++) {
			System.arraycopy(ticks[cpu], 0, curProcTicks[cpu], 0, ticks[cpu].length);
		}
	}

	private void updateSystemTicks() {
		// TODO Auto-generated method stub
		System.arraycopy(curTicks, 0, prevTicks, 0, curTicks.length);
		this.tickTime = System.currentTimeMillis();
		long[] ticks = getSystemCpuLoadTicks();
		System.arraycopy(ticks, 0, curTicks, 0, ticks.length);
	}

	/**
	 * Updates logical and physical processor counts from /pro/cpuinfo
	 */
	private void calculateProcessorCounts() {
		// TODO Auto-generated method stub
		try {
			List<String> proCpu = FileUtil.readFile("/proc/cpuinfo");
			// Get number of logical processors
			for (String cpu : proCpu) {
				if (cpu.startsWith("processor")) {
					logicalProcessorCount++;
				}
			}
			// Get number of physical processors
			int siblings = 0;
			int cpucores = 0;
			int[] uniqueID = new int[2];
			uniqueID[0] = -1;
			uniqueID[1] = -1;

			Set<String> ids = new HashSet<String>();
			for (String cpu : proCpu) {
				if (cpu.startsWith("siblings")) {
					// if siblings = 1, no hyper threading
					siblings = ParseUtil.parseString(cpu, 1);
					if (siblings == 1) {
						physicalProcessorCount = logicalProcessorCount;
						break;
					}
				}
				if (cpu.startsWith("cpu cores")) {
					// if(siblings > 1) ratio with scores
					cpucores = ParseUtil.parseString(cpu, 1);
					if (siblings > 1) {
						physicalProcessorCount = logicalProcessorCount * cpucores / siblings;
						break;
					}
				}
				// If siblings and cpu cores don't define it, count unique
				// combinations of core id and physical id.
				if (cpu.startsWith("core id") || cpu.startsWith("cpu number")) {
					uniqueID[0] = ParseUtil.parseString(cpu, 0);
				} else if (cpu.startsWith("physical id")) {
					uniqueID[1] = ParseUtil.parseString(cpu, 0);
				}

				if (uniqueID[0] >= 0 && uniqueID[1] >= 0) {
					ids.add(uniqueID[0] + " " + uniqueID[1]);
					uniqueID[0] = -1;
					uniqueID[1] = -1;
				}
			}
			if (physicalProcessorCount == 0) {
				physicalProcessorCount = ids.size();
			}
		} catch (IOException e) {
			System.out.println(e.toString());
		}

		// Force at least one processor
		if (logicalProcessorCount < 1) {
			logicalProcessorCount = 1;
		}
		if (physicalProcessorCount < 1) {
			physicalProcessorCount = 1;
		}
	}

	@Override
	public String getVendor() {
		// TODO Auto-generated method stub
		return this.cpuVendor;
	}

	@Override
	public void setVendor(String vendor) {
		// TODO Auto-generated method stub
		this.cpuVendor = vendor;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return this.cpuName;
	}

	@Override
	public void setName(String name) {
		// TODO Auto-generated method stub
		this.cpuName = name;
	}

	@Override
	public long getVendorFreq() {
		// TODO Auto-generated method stub
		if (this.cuVendorFreq == null) {
			Pattern pattern = Pattern.compile("@ (.*)&$");
			Matcher matcher = pattern.matcher(getName());

			if (matcher.find()) {
				String unit = matcher.group(1);
				this.cuVendorFreq = Long.valueOf(ParseUtil.parseHertz(unit));
			} else {
				this.cuVendorFreq = Long.valueOf(-1L);
			}
		}
		return this.cuVendorFreq;
	}

	@Override
	public void setVendorFreq(long freq) {
		// TODO Auto-generated method stub
		this.cuVendorFreq = Long.valueOf(freq);
	}

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		if (this.cpuIdentifier == null) {
			StringBuilder builder = new StringBuilder();
			if (getVendor().contentEquals("GenuineIntel")) {
				builder.append(isCPU64bit() ? "Intel64" : "x86");
			} else {
				builder.append(getVendor());
			}
			builder.append("Family ");
			builder.append(getFamily());
			builder.append(" Model ");
			builder.append(getModel());
			builder.append(" Stepping ");
			builder.append(getStepping());
			this.cpuIdentifier = builder.toString();

		}
		return this.cpuIdentifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		// TODO Auto-generated method stub
		this.cpuIdentifier = identifier;
	}

	@Override
	public boolean isCPU64bit() {
		// TODO Auto-generated method stub
		return this.cpu64.booleanValue();
	}

	@Override
	public void setCPU64(boolean cpu64) {
		// TODO Auto-generated method stub
		this.cpu64 = Boolean.valueOf(cpu64);
	}

	@Override
	public String getStepping() {
		// TODO Auto-generated method stub
		return this.cpuStepping;
	}

	@Override
	public void setStepping(String stepping) {
		// TODO Auto-generated method stub
		this.cpuStepping = stepping;
	}

	@Override
	public String getModel() {
		// TODO Auto-generated method stub
		return this.cpuModel;
	}

	@Override
	public void setModel(String model) {
		// TODO Auto-generated method stub
		this.cpuModel = model;
	}

	@Override
	public String getFamily() {
		// TODO Auto-generated method stub
		return this.cpuFamily;
	}

	@Override
	public void setFamily(String family) {
		// TODO Auto-generated method stub
		this.cpuFamily = family;
	}

	@Override
	public synchronized double getSystemCpuLoadBetweenTicks() {
		// TODO Auto-generated method stub
		// check if > ~ 0.95 seconds since last tick count.
		long now = System.currentTimeMillis();
		if (now - tickTime > 950) {
			updateSystemTicks();
		}
		long total = 0;
		for (int i = 0; i < curTicks.length; i++) {
			total += (curTicks[i] - prevTicks[i]);
		}

		long idle = curTicks[3] - prevTicks[3];

		return (total > 0 && idle >= 0) ? (double) (total - idle) / total : 0d;
	}

	@Override
	public long[] getSystemCpuLoadTicks() {
		// TODO Auto-generated method stub
		long[] ticks = new long[curTicks.length];
		String tickStr = "";
		try {
			List<String> procStat = FileUtil.readFile("/proc/stat");
			if (!procStat.isEmpty()) {
				tickStr = procStat.get(0);
			}
		} catch (IOException e) {
			return ticks;
		}
		String[] tickArr = tickStr.split("\\s+");
		if (tickArr.length < 5) {
			return ticks;
		}
		for (int i = 0; i < 4; i++) {
			ticks[i] = Long.parseLong(tickArr[i + 1]);
		}
		return ticks;
	}

	@Override
	public double getSystemCpuLoad() {
		// TODO Auto-generated method stub
		if (sunMXBean) {
			return ((com.sun.management.OperatingSystemMXBean) OS_MXBEAN).getProcessCpuLoad();
		}
		return getSystemCpuLoadBetweenTicks();
	}

	@Override
	public double getSystemLoadAverage() {
		// TODO Auto-generated method stub
		return OS_MXBEAN.getSystemLoadAverage();
	}

	@Override
	public double[] getProcessorCpuLoadBetweenTicks() {
		// TODO Auto-generated method stub
		// check if > ~ 0.95 seconds since last tick count
		long now = System.currentTimeMillis();
		if (now - procTickTime > 950) {
			updateProcessorTicks();
		}
		double[] load = new double[logicalProcessorCount];
		for (int cpu = 0; cpu < logicalProcessorCount; cpu++) {
			long total = 0;
			for (int i = 0; i < this.curProcTicks[cpu].length; i++) {
				total += (this.curProcTicks[cpu][i] - this.prevProcTicks[cpu][i]);
			}
			// calculate idle from last field[3];
			long idle = this.curProcTicks[cpu][3] - this.prevProcTicks[cpu][3];
			load[cpu] = (total > 0 && idle >= 0) ? (double) (total - idle) / total : 0d;
		}
		return load;
	}

	@Override
	public long[][] getProcessorCpuLoadTicks() {
		// TODO Auto-generated method stub
		long[][] ticks = new long[logicalProcessorCount][4];
		try{
			int cpu = 0;
			List<String> procStat = FileUtil.readFile("/proc/stat");
			for(String stat : procStat){
				if (stat.startsWith("cpu") && !stat.startsWith("cpu ")) {
					String[] tickArr = stat.split("\\s+");
					if(tickArr.length < 5){
						break;
					}
					for (int i = 0; i < 4; i++) {
						ticks[cpu][i] = Long.parseLong(tickArr[i + 1]);
					}
					if (++cpu >= logicalProcessorCount) {
						break;
					}
				}
			}
		}catch (IOException e){
			System.err.println(e.getMessage());
		}
		return ticks;
	}

	//Unimplemented
	@Override
	public long getSystemUptime() {
		// TODO Auto-generated method stub
		Sysinfo  info = new Sysinfo();
		if(0 != Libc.INSTANCE.sysinfo(info)){
			return 0L;
		}
		return info.uptime.longValue();
	}

	@Override
	public String getSystemSerialNumber() {
		// TODO Auto-generated method stub
		String sn = null;
		ArrayList<String> hwInfo = ExcutingCommand.runNative("dmidecode -t system");
		String marker = "Serial Number:";
		if (hwInfo != null) {
			for(String checkLine: hwInfo){
				if (checkLine.contains(marker)) {
					sn = checkLine.split(marker)[1].trim();
					break;
				}
			}
		}
		if (sn == null) {
			marker = "system.hardware.serial =";
			hwInfo = ExcutingCommand.runNative("lshal");
			if (hwInfo != null) {
				for(String checkLine : hwInfo){
					if (checkLine.contains(marker)) {
						String[] temp = checkLine.split(marker)[1].split("'");
						sn = temp.length > 0? temp[1] : null;
						break;
					}
				}
			}
		}
		return (sn == null) ? "unknown" : sn;
	}

	@Override
	public int getLogicalProcessorCount() {
		// TODO Auto-generated method stub
		return this.logicalProcessorCount;
	}

	@Override
	public int getPhysicalProcessorCount() {
		// TODO Auto-generated method stub
		return this.physicalProcessorCount;
	}

	@Override
    public String toString() {
        return getName();
    }
}

package main.java.ducky.hardware.platform.windows;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.Processor;

import com.sun.jna.Memory;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinBase.SYSTEM_INFO;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinError;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import main.java.ducky.hardware.CentralProcessor;
import main.java.ducky.jna.platform.windows.Kernel32;
import main.java.ducky.jna.platform.windows.Pdh;
import main.java.ducky.jna.platform.windows.Pdh.PdhFmtCounterValue;
import main.java.ducky.util.ExcutingCommand;
import main.java.ducky.util.ParseUtil;

@SuppressWarnings("restriction")
public class WindowsCentralProcessor implements CentralProcessor {

	private static final OperatingSystemMXBean OS_MXBEAN = ManagementFactory.getOperatingSystemMXBean();
	private static boolean sunMXBean;

	static {
		try {
			Class.forName("com.sun.management.OperatingSystemMXBean");
			((com.sun.management.OperatingSystemMXBean) OS_MXBEAN).getSystemCpuLoad();
			sunMXBean = true;
		} catch (Exception e) {
			// TODO: handle exception
			sunMXBean = false;
		}
	}

	private int logicalProcessorCount = 0;
	private int physicalProcessorCount = 0;

	// System ticks
	private long tickTime;
	private long[] prevTicks;
	private long[] curTicks;

	// Per-processor ticks [cpu][type]
	private long procTickTime;
	private long[][] prevProcTicks;
	private long[][] curProcTicks;

	// PDH counters only give increments between calls so we maintain our own
	// "ticks" here
	private long allProTickTime;
	private long[][] allProcTicks;

	// Initialize numCPU and open a Performance Data Helper Thread for
	// monitoring each processor ticks
	private PointerByReference phQuery = new PointerByReference();
	private final IntByReference pZero = new IntByReference();

	// Set up user and idle tick counters for each processor
	private PointerByReference[] phUserCounters;
	private PointerByReference[] phIdleCounters;

	// Set up Performance Data Helper thread for uptime
	private PointerByReference uptimeQuery = new PointerByReference();
	private final IntByReference pOne = new IntByReference(1);
	// set up couter for uptime
	private PointerByReference pUptime;

	private String cpuVendor;
	private String cpuName;
	private String cpuIdentifier;
	private Long cpuVendorFreq;

	/**
	 * Create a Processor
	 */
	public WindowsCentralProcessor() {
		// TODO Auto-generated constructor stub
		// Processor counts
		calculateProcessorCounts();

		// PDH counter setup
		initPhdCounters();

		// System ticks
		this.prevTicks = new long[4];
		this.curTicks = new long[4];
		updateSystemTicks();

		// Per-Processor ticks
		this.allProcTicks = new long[logicalProcessorCount][4];
		this.prevProcTicks = new long[logicalProcessorCount][4];
		this.curProcTicks = new long[logicalProcessorCount][4];
		updateProcessorTicks();
	}

	/**
	 * Updates logical and physical processor counts from/proc/cpuinfo
	 */
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

	/**
	 * Updates system tick information from native call to GetSystemTimes().
	 * Array with four elements representing clock ticks or milliseconds
	 * (platform dependent) spent in User (0), Nice (1), System (2), and Idle
	 * (3) states. By measuring the difference between ticks across a time
	 * interval, CPU load over that interval may be calculated.
	 */
	private void updateSystemTicks() {
		// TODO Auto-generated method stub
		System.arraycopy(curTicks, 0, prevTicks, 0, curTicks.length);
		this.tickTime = System.currentTimeMillis();
		long[] ticks = getSystemCpuLoadTicks();
		System.arraycopy(ticks, 0, curTicks, 0, ticks.length);
	}

	/**
	 * Initializes PDH Tick and Uptime Counters
	 */
	private void initPhdCounters() {
		// TODO Auto-generated method stub
		int pdhOpenTickQueryError = Pdh.INSTANCE.PdhOpenQuery(null, pZero, phQuery);
		if (pdhOpenTickQueryError != 0) {
			System.out.println("Failed to open PDH Tick Query.");
		}

		// Set up counters
		phUserCounters = new PointerByReference[logicalProcessorCount];
		phIdleCounters = new PointerByReference[logicalProcessorCount];

		if (pdhOpenTickQueryError == 0) {
			for (int i = 0; i < logicalProcessorCount; i++) {
				String counterPath = String.format("\\Processor(%d)\\%% user time", i);
				phUserCounters[i] = new PointerByReference();
				int pdhAddTickCounterError = Pdh.INSTANCE.PdhAddEnglishCounterA(phQuery.getValue(), counterPath, pZero,
						phUserCounters[i]);
				if (pdhAddTickCounterError != 0) {
					System.out.println("Failed to add PDH User Tick Counter for processor");
					break;
				}
				counterPath = String.format("\\Processor(%d)\\%% idle time", i);
				phIdleCounters[i] = new PointerByReference();
				pdhAddTickCounterError = Pdh.INSTANCE.PdhAddEnglishCounterA(phQuery.getValue(), counterPath, pZero,
						phIdleCounters[i]);
				if (pdhAddTickCounterError != 0) {
					System.out.println("Failed to add PDH Idle Tick Counter for processor");
					break;
				}
			}
			System.out.println("Tick counter queries added. Initializing with first query.");
			int ret = Pdh.INSTANCE.PdhCollectQueryData(phQuery.getValue());
			if (ret != 0) {
				System.out.println("Failed to update Tick Counters.");
			}
		}
		// Open uptime query
		int pdhOpenUptimeQueryError = Pdh.INSTANCE.PdhOpenQuery(null, pOne, uptimeQuery);
		if (pdhOpenTickQueryError != 0) {
			System.out.println("Failed to open PDH Uptime Query.");
		} else {
			String uptimePath = "\\System\\System Up Time";
			pUptime = new PointerByReference();
			int pdhAddUptimeCounterError = Pdh.INSTANCE.PdhAddEnglishCounterA(uptimeQuery.getValue(), uptimePath, pOne,
					pUptime);
			if (pdhAddUptimeCounterError != 0) {
				System.out.println("Failed to add PDH Uptime Counter.");
			}
		}

		// Set up hook to close the query on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Pdh.INSTANCE.PdhCloseQuery(phQuery.getValue());
				Pdh.INSTANCE.PdhCloseQuery(uptimeQuery.getValue());
			}
		});
	}

	private void calculateProcessorCounts() {
		// TODO Auto-generated method stub
		// Get number of logical processors
		try {
			SYSTEM_INFO info = new SYSTEM_INFO();

			// Loi tai day
			Kernel32.INSTANCE.GetSystemInfo(info);
			this.logicalProcessorCount = info.dwNumberOfProcessors.intValue();
		} catch (Exception e) {
			System.out.println(e.toString());
		}
		// Get number of physical processors
		try {
			WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION[] processors = Kernel32Util.getLogicalProcessorInformation();
			for (SYSTEM_LOGICAL_PROCESSOR_INFORMATION proc : processors) {
				if (proc.relationship == WinNT.LOGICAL_PROCESSOR_RELATIONSHIP.RelationProcessorCore) {
					this.physicalProcessorCount++;
				}
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Failed to get processor information array.");
			int sizePerStruct = new WinNT.SYSTEM_LOGICAL_PROCESSOR_INFORMATION().size();
			System.out.println("Size per structure: " + sizePerStruct);
			WinDef.DWORDByReference bufferSize = new WinDef.DWORDByReference(new WinDef.DWORD(sizePerStruct));
			Memory memory;
			int temp = 0;
			while (true) {
				memory = new Memory(bufferSize.getValue().intValue());
				System.out.println("Iteration " + ++temp + " memory size: " + memory.size() + "....");
				if (!Kernel32.INSTANCE.GetLogicalProcessorInformation(memory, bufferSize)) {
					int err = Kernel32.INSTANCE.GetLastError();
					if (err != WinError.ERROR_INSUFFICIENT_BUFFER) {
						throw new Win32Exception(err);
					}
					System.out.println("Insufficient buffer.");
				} else {
					System.out.println("OK.");
					break;
				}
			}
			System.out.println(
					"Caculating returned structure count with buffer size " + bufferSize.getValue().intValue());
			int returnedStructCount = bufferSize.getValue().intValue() / sizePerStruct;
			System.out.println("Returned structure count is " + returnedStructCount);
			System.out.println("End debug output.");
		}
	}

	/**
	 * Vendor identifier
	 * 
	 * @return Processor vendor
	 */
	@Override
	public String getVendor() {
		// TODO Auto-generated method stub
		return this.cpuVendor;
	}

	/**
	 * Set processor vendor
	 * 
	 * @param vendor
	 *            Vendor.
	 */
	@Override
	public void setVendor(String vendor) {
		// TODO Auto-generated method stub
		this.cpuVendor = vendor;
	}

	/**
	 * Name
	 * 
	 * @return Processor name
	 */
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
		if (this.cpuVendorFreq == null) {
			Pattern pattern = Pattern.compile("@ (.*)$");
			Matcher matcher = pattern.matcher(getName());

			if (matcher.find()) {
				String unit = matcher.group(1);
				this.cpuVendorFreq = Long.valueOf(ParseUtil.parseHertz(unit));
			} else {
				this.cpuVendorFreq = Long.valueOf(-1L);
			}
		}
		return this.cpuVendorFreq.longValue();
	}

	@Override
	public void setVendorFreq(long freq) {
		// TODO Auto-generated method stub
		this.cpuVendorFreq = Long.valueOf(freq);
	}

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return this.cpuIdentifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		// TODO Auto-generated method stub
		this.cpuIdentifier = identifier;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isCPU64bit() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setCPU64(boolean cpu64) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getStepping() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setStepping(String stepping) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getModel() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setModel(String model) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getFamily() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setFamily(String family) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized double getSystemCpuLoadBetweenTicks() {
		// TODO Auto-generated method stub
		// Check if > ~ 0.95 seconds since last tick count.
		long now = System.currentTimeMillis();
		if (now - tickTime > 950) {
			updateSystemTicks();
		}
		long total = 0;
		for (int i = 0; i < curTicks.length; i++) {
			total += curTicks[3] - prevTicks[3];
		}
		// Calculate idle from last field [3]
		long idle = curTicks[3] - prevTicks[3];
		return (total > 0 && idle >= 0) ? (double) (total - idle) / total : 0d;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long[] getSystemCpuLoadTicks() {
		// TODO Auto-generated method stub
		long[] ticks = new long[curTicks.length];
		WinBase.FILETIME lpIdleTime = new WinBase.FILETIME();
		WinBase.FILETIME lpKernelTime = new WinBase.FILETIME();
		WinBase.FILETIME lpUserTime = new WinBase.FILETIME();
		if (!Kernel32.INSTANCE.GetSystemTimes(lpIdleTime, lpKernelTime, lpUserTime)) {
			System.out.println("Failed to update system idle/kernel/user times.");
			return ticks;
		}
		ticks[3] = WinBase.FILETIME.dateToFileTime(lpIdleTime.toDate());
		ticks[2] = WinBase.FILETIME.dateToFileTime(lpKernelTime.toDate()) - ticks[3];
		ticks[1] = 0L; // Windows is not 'nice'
		ticks[0] = WinBase.FILETIME.dateToFileTime(lpUserTime.toDate());

		return ticks;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getSystemCpuLoad() {
		// TODO Auto-generated method stub
		if (sunMXBean) {
			return ((com.sun.management.OperatingSystemMXBean) OS_MXBEAN).getSystemCpuLoad();
		}
		return getSystemCpuLoadBetweenTicks();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getSystemLoadAverage() {
		// TODO Auto-generated method stub
		return OS_MXBEAN.getSystemLoadAverage();
	}

	@Override
	public double[] getProcessorCpuLoadBetweenTicks() {
		// TODO Auto-generated method stub
		// Check if > ~ 0.95 seconds since last tick count.
		long now = System.currentTimeMillis();
		if (now - procTickTime > 950) {
			// Enough time has elapsed.
			// Update latest
			updateProcessorTicks();
		}
		double[] load = new double[logicalProcessorCount];
		for (int cpu = 0; cpu < logicalProcessorCount; cpu++) {
			long total = 0;
			for (int i = 0; i < this.curProcTicks[cpu].length; i++) {
				total += (this.curProcTicks[cpu][i] - this.prevProcTicks[cpu][i]);
			}
			// Calculate idle from last field [3]
			long idle = this.curProcTicks[cpu][3] - this.prevProcTicks[cpu][3];
			// update
			load[cpu] = (total > 0 && idle >= 0) ? (double) (total - idle) / total : 0d;
		}
		return load;
	}

	@Override
	public long[][] getProcessorCpuLoadTicks() {
		// TODO Auto-generated method stub
		long[][] ticks = new long[logicalProcessorCount][4];
		int ret = Pdh.INSTANCE.PdhCollectQueryData(phQuery.getValue());
		if (ret != 0) {
			return ticks;
		}
		long now = System.currentTimeMillis();
		long elapsed = now - allProTickTime;
		for (int cpu = 0; cpu < logicalProcessorCount; cpu++) {
			PdhFmtCounterValue phUserCounterValue = new PdhFmtCounterValue();
			ret = Pdh.INSTANCE.PdhGetFormattedCounterValue(phUserCounters[cpu].getValue(),
					Pdh.PDH_FMT_LARGE | Pdh.PDH_FMT_1000, null, phUserCounterValue);
			if (ret != 0) {
				return ticks;
			}

			PdhFmtCounterValue phIdleCounterValue = new PdhFmtCounterValue();
			ret = Pdh.INSTANCE.PdhGetFormattedCounterValue(phIdleCounters[cpu].getValue(),
					Pdh.PDH_FMT_LARGE | Pdh.PDH_FMT_1000, null, phIdleCounterValue);
			if (ret != 0) {
				return ticks;
			}

			// Returns results in 1000's of percent, e.g. 5% is 5000
			// Multiply by elapsed to get total ms and Divide by 100 * 1000
			// Putting division at end avoids need to cast division to double
			long user = elapsed * phUserCounterValue.value.largeValue / 100000;
			long idle = elapsed * phIdleCounterValue.value.largeValue / 100000;
			// Elasped is only since last read, so increment previous value
			allProcTicks[cpu][0] += user;
			// allProcTicks[cpu][1] is ignored, Windows is not nice
			allProcTicks[cpu][2] += Math.max(0, elapsed - user - idle); // u+i+sys=100%
			allProcTicks[cpu][3] += idle;
		}
		allProTickTime = now;

		// Make a copy of the array to return
		for (int cpu = 0; cpu < logicalProcessorCount; cpu++) {
			System.arraycopy(allProcTicks[cpu], 0, ticks[cpu], 0, allProcTicks[cpu].length);
		}
		return ticks;
	}

	@Override
	public long getSystemUptime() {
		// TODO Auto-generated method stub
		int ret = Pdh.INSTANCE.PdhCollectQueryData(uptimeQuery.getValue());
		if (ret != 0) {
			return 0L;
		}

		PdhFmtCounterValue uptimeCounterValue = new PdhFmtCounterValue();
		ret = Pdh.INSTANCE.PdhGetFormattedCounterValue(pUptime.getValue(), Pdh.PDH_FMT_LARGE, null, uptimeCounterValue);
		if (ret != 0) {
			return 0L;
		}
		return uptimeCounterValue.value.largeValue;
	}

	@Override
	public String getSystemSerialNumber() {
		// TODO Auto-generated method stub
		String sn = null;
		ArrayList<String> hwInfo = ExcutingCommand.runNative("wmic bios get serialnumber");
		for (String checkLine : hwInfo) {
			if (checkLine.length() == 0 || checkLine.toLowerCase().contains("serialnumber")) {
				continue;
			} else {
				sn = checkLine.trim();
				break;
			}
		}

		if (sn == null || sn.length() == 0) {
			hwInfo = ExcutingCommand.runNative("wmic csproduct get identifyingnumber");
			for (String checkLine : hwInfo) {
				if (checkLine.length() == 0 || checkLine.toLowerCase().contains("identifyingnumber")) {
					continue;
				} else {
					sn = checkLine.trim();
				}
			}
		}
		return (sn == null || sn.length() == 0) ? "unknown" : sn;
	}

	@Override
	public int getLogicalProcessorCount() {
		// TODO Auto-generated method stub
		return logicalProcessorCount;
	}

	@Override
	public int getPhysicalProcessorCount() {
		// TODO Auto-generated method stub
		return physicalProcessorCount;
	}

	@Override
	public String toString() {
		return getName();
	}

}

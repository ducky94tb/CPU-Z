package main.java.ducky.hardware;

public interface CentralProcessor {
	String getVendor();

	void setVendor(String vendor);

	String getName();

	void setName(String name);

	long getVendorFreq();

	void setVendorFreq(long freq);

	String getIdentifier();

	void setIdentifier(String identifier);

	boolean isCPU64bit();

	void setCPU64(boolean cpu64);

	String getStepping();

	void setStepping(String stepping);

	String getModel();

	void setModel(String model);

	String getFamily();

	void setFamily(String family);

	double getSystemCpuLoadBetweenTicks();

	long[] getSystemCpuLoadTicks();

	@SuppressWarnings("restriction")
	double getSystemCpuLoad();

	double getSystemLoadAverage();

	double[] getProcessorCpuLoadBetweenTicks();

	long[][] getProcessorCpuLoadTicks();

	long getSystemUptime();

	String getSystemSerialNumber();

	int getLogicalProcessorCount();

	int getPhysicalProcessorCount();
}

package main.java.ducky.software.os;

public interface OperatingSystem {
	String getFamily();

	String getManufacturer();

	OperatingSystemVersion getVersion();
}

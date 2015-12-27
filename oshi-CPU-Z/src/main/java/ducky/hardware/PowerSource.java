package main.java.ducky.hardware;

public interface PowerSource {
	String getName();
	double getRemainingCapacity();
	double getTimeRemaining();
}

package main.java.ducky.software.os;

public class OSFileStore {
	private String name;
	private String description;
	private long usableSpace;
	private long totalSpace;

	public OSFileStore(String name, String description, long usableSpace, long totalSpace) {
		super();
		this.name = name;
		this.description = description;
		this.usableSpace = usableSpace;
		this.totalSpace = totalSpace;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getUsableSpace() {
		return usableSpace;
	}

	public void setUsableSpace(long usableSpace) {
		this.usableSpace = usableSpace;
	}

	public long getTotalSpace() {
		return totalSpace;
	}

	public void setTotalSpace(long totalSpace) {
		this.totalSpace = totalSpace;
	}

}

package main.java.ducky.software.os.linux;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import main.java.ducky.software.os.OSFileStore;

public class LinuxFileSystem {
	public static OSFileStore[] getFileStores(){
		List<OSFileStore> fsList = new ArrayList<>();
		for (FileStore store : FileSystems.getDefault().getFileStores()) {
			String path = store.toString().replace(" (" + store.name() + ")", "");
			if (path.startsWith("/proc") || path.startsWith("/sys") || path.startsWith("/run")
					|| path.startsWith("/dev") || path.startsWith("/dev/pts")) {
				continue;
			}
			String name = store.name();
			if (path.equals("/")) {
				name = "/";
			}
			String description = "Mount Point";
			if (store.name().startsWith("/dev")) {
				description = "Local Disk";
			}
			try{
				fsList.add(new OSFileStore(name, description, store.getUsableSpace(), store.getTotalSpace()));
			}catch(IOException e){
				continue;
			}
		}
		return fsList.toArray(new OSFileStore[fsList.size()]);
	}
}

package main.java.ducky.software.os.windows;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.swing.filechooser.FileSystemView;

import main.java.ducky.software.os.OSFileStore;

public class WindowsFileSystem {
	public static OSFileStore[] getFileStores() {
        // File.listRoots() has more information for Windows
        // than FileSystem.getDefalut().getFileStores()
        final File[] roots = File.listRoots();
        // Need to call FileSystemView on Swing's Event Dispatch Thread to avoid
        // problems
        SwingWorker<List<OSFileStore>, Void> worker = new SwingWorker<List<OSFileStore>, Void>(){

			@Override
			protected List<OSFileStore> doInBackground() throws Exception {
				// TODO Auto-generated method stub
				FileSystemView fsv = FileSystemView.getFileSystemView();
                List<OSFileStore> fsList = new ArrayList<>();
                for (File f : roots) {
                    fsList.add(new OSFileStore(fsv.getSystemDisplayName(f), fsv.getSystemTypeDescription(f), f
                            .getUsableSpace(), f.getTotalSpace()));
                }
                return fsList;
			}
        	
        };
        worker.execute();
        List<OSFileStore> fs = new ArrayList<>();
        try {
            // TODO: Consider a timeout version of this method that passes
            // timeout parameters which are used in this get()
            fs = worker.get();
        } catch (InterruptedException | ExecutionException e) {
        }
        return fs.toArray(new OSFileStore[fs.size()]);
    }
}

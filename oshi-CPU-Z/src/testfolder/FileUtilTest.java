package testfolder;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import main.java.ducky.util.FileUtil;

public class FileUtilTest {
	/** The thisclass. */
    private static String THISCLASS = "src/testfolder/FileUtilTest.java";

    /**
     * Test read file.
     */
    @Test
    public void testReadFile() {
        List<String> thisFile = null;
        try {
            thisFile = FileUtil.readFile(THISCLASS);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // Comment ONE line
        int lineOne = 0;
        // Comment TWO line
        int lineTwo = 0;
        for (int i = 0; i < thisFile.size(); i++) {
            String line = thisFile.get(i);
            if (line.contains("Comment ONE line")) {
                lineOne = i;
            }
            if (line.contains("Comment TWO line")) {
                lineTwo = i;
            }
        }
        assertEquals(3, lineTwo - lineOne);
    }
}

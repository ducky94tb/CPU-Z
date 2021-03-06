package main.test;

import java.text.DecimalFormatSymbols;

import org.junit.BeforeClass;
import org.junit.Test;

import main.java.ducky.util.FormatUtil;

import static org.junit.Assert.assertEquals;

public class FormatUtilTest {
	private static char DECIMAL_SEPARATOR;
	
	@BeforeClass
	public static void setUpClass(){
		DecimalFormatSymbols syms = new DecimalFormatSymbols();
		DECIMAL_SEPARATOR = syms.getDecimalSeparator();
	}
	
	@Test
	public void testFormatBytes(){
		assertEquals("0 bytes", FormatUtil.formatBytes(0));
        assertEquals("1 byte", FormatUtil.formatBytes(1));
        assertEquals("1.0 KB", FormatUtil.formatBytes(1024));
        assertEquals("1.0 MB", FormatUtil.formatBytes(1024 * 1024));
        assertEquals("1.0 GB", FormatUtil.formatBytes(1024 * 1024 * 1024));
        assertEquals("1.0 TB", FormatUtil.formatBytes(1099511627776L));
	}
	
	@Test
    public void testFormatBytesWithDecimalSeparator() {
        String expected1 = "1" + DECIMAL_SEPARATOR + "3 KB";
        String expected2 = "2" + DECIMAL_SEPARATOR + "3 MB";
        String expected3 = "2" + DECIMAL_SEPARATOR + "2 GB";
        String expected4 = "1" + DECIMAL_SEPARATOR + "1 TB";
        assertEquals(expected1, FormatUtil.formatBytes(1340));
        assertEquals(expected2, FormatUtil.formatBytes(2400016));
        assertEquals(expected3, FormatUtil.formatBytes(2400000000L));
        assertEquals(expected4, FormatUtil.formatBytes(1099511627776L + 109951162777L));
    }
	
	@Test
    public void testFormatHertz() {
        assertEquals("1 Hz", FormatUtil.formatHertz(1));
        assertEquals("999 Hz", FormatUtil.formatHertz(999));
        assertEquals("1.0 kHz", FormatUtil.formatHertz(1000));
        assertEquals("1.0 MHz", FormatUtil.formatHertz(1000 * 1000));
        assertEquals("1.0 GHz", FormatUtil.formatHertz(1000 * 1000 * 1000));
        assertEquals("1.0 THz", FormatUtil.formatHertz(1000L * 1000L * 1000L * 1000L));
    }
	
	@Test
	public void testFormatElapsedSecs() {
        assertEquals("0 days, 00:00:00", FormatUtil.formatElapsedSecs(0));
        assertEquals("0 days, 03:25:45", FormatUtil.formatElapsedSecs(12345));
        assertEquals("1 days, 10:17:36", FormatUtil.formatElapsedSecs(123456));
        assertEquals("14 days, 06:56:07", FormatUtil.formatElapsedSecs(1234567));
    }
	
	@Test
    public void testRound() {
        assertEquals(42.42, FormatUtil.round(42.423f, 2), 0.00001f);
        assertEquals(42.43, FormatUtil.round(42.425f, 2), 0.00001f);
        assertEquals(42.5, FormatUtil.round(42.499f, 2), 0.00001f);
        assertEquals(42, FormatUtil.round(42, 2), 0.00001f);
    }
}

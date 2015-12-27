package main.test;

import static org.junit.Assert.*;

import org.junit.Test;

import main.java.ducky.util.ParseUtil;

public class ParseUtilTest {
	@Test
    public void testParseHertz() {
        assertEquals(1L, ParseUtil.parseHertz("1Hz"));
        assertEquals(500L, ParseUtil.parseHertz("500 Hz"));
        assertEquals(1000L, ParseUtil.parseHertz("1kHz"));
        assertEquals(1000000L, ParseUtil.parseHertz("1MHz"));
        assertEquals(1000000000L, ParseUtil.parseHertz("1GHz"));
        assertEquals(1500000000L, ParseUtil.parseHertz("1.5GHz"));
        assertEquals(1000000000000L, ParseUtil.parseHertz("1THz"));
    }
	
	@Test
    public void testParseString() {
        assertEquals(1, ParseUtil.parseString("foo : 1", 0));
        assertEquals(2, ParseUtil.parseString("foo", 2));
    }
}

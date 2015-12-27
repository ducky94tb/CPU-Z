package main.java.ducky.jna.platform.windows;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Union;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Pdh extends Library {
	Pdh INSTANCE = (Pdh) Native.loadLibrary("Pdh", Pdh.class);

	// Counter return types
	static final int PDH_FMT_LONG = 0x00000100; // Native Long
	static final int PDH_FMT_DOUBLE = 0x00000200; // double
	static final int PDH_FMT_LARGE = 0x00000400; // 64 bit long
	// These can be combined with above types with bitwise OR
	static final int PDH_FMT_NOSCALE = 0x00001000; // don't scale
	static final int PDH_FMT_1000 = 0x00002000; // multiply by 1000
	static final int PDH_FMT_NOCAP100 = 0x00008000; // don't cap at 100

	static class ValueUnion extends Union {
		public int longValue;
		public double doubleValue;
		public long largeValue;
		public String AnsiStringValue;
		public WString WideStringValue;
	}

	static class PdhFmtCounterValue extends Structure {

		public int cStatus;
		public ValueUnion value;

		@Override
		protected List getFieldOrder() {
			// TODO Auto-generated method stub
			return Arrays.asList(new String[] { "cStatus", "value" });
		}
	}

	int PdhOpenQuery(String szDataSource, IntByReference dwUserData, PointerByReference phQuery);

	int PdhAddEnglishCounterA(Pointer pointer, String counterPath, IntByReference dwUserData,
			PointerByReference phCounter);

	int PdhCollectQueryData(Pointer pointer);

	int PdhGetFormattedCounterValue(Pointer pointer, int dwFormat, IntByReference lpdwType, PdhFmtCounterValue pValue);

	int PdhCloseQuery(Pointer pointer);
}

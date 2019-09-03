package kin.backupandrestore.sample;

import android.util.Log;
import java.io.PrintWriter;
import java.io.StringWriter;

public final class Utils {

	static void checkNotNull(Object obj, String paramName) {
		if (obj == null) {
			throw new IllegalArgumentException(paramName + " == null");
		}
	}

	static void checkNotEmpty(String string, String paramName) {
		if (string == null || string.isEmpty()) {
			throw new IllegalArgumentException(paramName + " cannot be null or empty.");
		}
	}

	static void logError(Throwable t, String operationName) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		Log.e("KinSampleApp", operationName + "error = " + sw.toString());
	}

}

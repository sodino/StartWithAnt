package lab.sodino.sliding;

import android.util.Log;

/**
 * @author Sodino E-mail:sodinoopen@hotmail.com
 * @version Time：2011-1-12 上午10:09:59
 */
public class LogOut {
	public static final String TAG = "ANDROID_LAB";
	private static final boolean DEBUG = true;

	public static void out(Object obj, String info) {
		if (DEBUG) {
			String tag = TAG;
			if (info.startsWith("length")) {
				tag = TAG + "_TMP";
			}
			if (obj instanceof String) {
				Log.d(tag, ((String) obj) + "->" + info);
			} else {
				Log.d(tag, obj.getClass().toString().substring(6) + "->" + info);
			}
		}
	}

	public static void out(Object obj, String tag, String info) {
		if (DEBUG) {
			if (obj instanceof String) {
				Log.d(TAG + "_" + tag, ((String) obj) + "->" + info);
			} else {
				Log.d(TAG + "_" + tag, obj.getClass().toString().substring(6) + "->" + info);
			}
		}
	}
}
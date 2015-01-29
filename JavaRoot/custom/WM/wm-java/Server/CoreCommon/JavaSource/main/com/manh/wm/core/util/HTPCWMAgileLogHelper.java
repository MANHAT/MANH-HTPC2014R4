package com.manh.wm.core.util;

import com.logistics.javalib.util.Misc;
import com.manh.javalib.logging.LogLevel;

public class HTPCWMAgileLogHelper {

	public static void logEnter() {
		HTPCWMAgileDebugLog.DEBUG_LOG.logDebug(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, "[HTPC - EX01] Entering " + classAndMethodName());
	}

	public static void logEnter(String infoMsg) {
		HTPCWMAgileDebugLog.DEBUG_LOG.logDebug(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, "[HTPC - EX01] Entering " + classAndMethodName()+infoMsg);
	}
	
	public static void logExit(String infoMsg) {
		HTPCWMAgileDebugLog.DEBUG_LOG.logDebug(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, "[HTPC - EX01] Exiting " + classAndMethodName()+infoMsg);
	}
	
	public static void logExit() {
		HTPCWMAgileDebugLog.DEBUG_LOG.logDebug(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, "[HTPC - EX01] Exiting " + classAndMethodName());
	}

	public static void logDebug(String debugMsg) {
		HTPCWMAgileDebugLog.DEBUG_LOG.logDebug(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, "[HTPC - EX01] Class: " +classAndMethodName() + debugMsg);
	}

	public static void logException(Exception ex, String exceptionMsg) {
		HTPCWMAgileDebugLog.DEBUG_LOG.logException(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, ex, "[HTPC - EX01] Class: " +classAndMethodName() + exceptionMsg);
	}

	public static void logException(Exception ex) {
		HTPCWMAgileDebugLog.DEBUG_LOG.logException(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, ex, "[HTPC - EX01] Class: " +classAndMethodName().toString());
	}

	public static void logInfo(String infoMsg) {
		HTPCWMAgileDebugLog.DEBUG_LOG.logInfo(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, "[HTPC - EX01] Class: " +classAndMethodName() + infoMsg);
	}
	
	public static void logError(String debugMsg) {
		HTPCWMAgileDebugLog.DEBUG_LOG.logError(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, "[HTPC - EX01] Class: " +classAndMethodName() + debugMsg);
	}
	
	public static void logHigh(String infoMsg) {
		HTPCWMAgileDebugLog.DEBUG_LOG.logHigh(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY, "[HTPC - EX01] Class: " +classAndMethodName() + infoMsg);
	}

	/**
	 * This method forms a StringBuffer literal with the class name and method
	 * name.
	 * 
	 * @return className.methodName:
	 */
	private static StringBuffer classAndMethodName() {
		StringBuffer sbFinalReturn = new StringBuffer(Misc.EMPTY_STRING);
		try {
			sbFinalReturn.append(Thread.currentThread().getStackTrace()[3].getFileName());
			sbFinalReturn.append(".");
			sbFinalReturn.append(Thread.currentThread().getStackTrace()[3].getLineNumber());
			sbFinalReturn.append(".");
			sbFinalReturn.append(Thread.currentThread().getStackTrace()[3].getMethodName());
			sbFinalReturn.append(": ");
		} catch (Exception e) {
			WMDebugLog.WM_COMMON_CATEGORY.logger().log(LogLevel.INFO,
					"Got exception while trying to prepending the class name and method name in the log");
		}
		return sbFinalReturn;
	}}

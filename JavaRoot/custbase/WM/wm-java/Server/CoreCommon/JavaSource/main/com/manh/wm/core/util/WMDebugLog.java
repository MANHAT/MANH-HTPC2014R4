/*
 * Copyright &#169; Manhattan Associates 2003. Subject to use, access and disclosure
 * restrictions.
 */

package com.manh.wm.core.util;

import com.logistics.javalib.util.JavalibLog;
import com.logistics.javalib.util.LogFile;
import com.manh.bpe.platform.logger.Logger;
import com.manh.common.CommonDebugLog;
import com.manh.ils.ILSDebugLog;

/**
 * The Class WMDebugLog.
 */
public class WMDebugLog extends CommonDebugLog
{

   /** The DEBU g_ log. */
   //public static LogFile DEBUG_LOG = ILSDef.DEF.getDebugLog();

   // WM categories

	 /** The Constant INVENTORYMGMT_CATEGORY. */
	   public static final Category INVENTORYMGMT_CATEGORY = new Category(
				      "com.manh.wmos.services.inventorymgmt",

				      "WM Inventory Mgmt Modules");

	   /** The Constant PUTAWAY_CATEGORY. */
	   public static final Category PUTAWAY_CATEGORY = new Category("com.manh.wmos.services.putaway",
			      "WM putaway Modules");

	   /** The Constant PRERECEIVING_CATEGORY. */
	   public static final Category PRERECEIVING_CATEGORY = new Category(
	      "com.manh.wmos.services.receiving", "WM PreReceiving Modules");

	   /** The Constant RECEIVING_CATEGORY. */
	   public static final Category RECEIVING_CATEGORY = new Category(
			      "com.manh.wmos.services.receiving", "WM Receiving Modules");

	   /** The Constant SOA_CATEGORY. */
	   public static final Category SOA_CATEGORY = new Category(
	         "WM - SOA", "WM SOA Modules");

	   /** The Constant SYSTEM_CONTROL_CATEGORY. */
	   public static final Category SYSTEM_CONTROL_CATEGORY = new Category(
	      "WM - SystemControl",

	      "WM System Control Modules");

	   /** The Constant WM_COMMON_CATEGORY. */
	   public static final Category WM_COMMON_CATEGORY = new Category(
	      "WM - WMCommon", "WM Common Modules");

	   /** The Constant WM_CORE_COMMON_CATEGORY. */
	   public static final Category WM_CORE_COMMON_CATEGORY = new Category(
	      "WM - CoreCommon", "WM Core Common Modules");

	   /** The Constant WM_TRANS_N_FREIGHT_CATEGORY. */
	   public static final Category WM_TRANS_N_FREIGHT_CATEGORY = new Category(
	      "WM - TransNFrieght",

	      "WM Trans N Frieght Modules");

	   /** The Constant TASK_MANAGEMENT_CATEGORY. */
	   public static final Category TASK_MANAGEMENT_CATEGORY = new Category(
	      "com.manh.wmos.services.taskmgmt", "WM Task Management Modules");

	   /** The Constant TASK_CATEGORY. */
	   public static final Category TASK_CATEGORY = new Category("com.manh.wmos.services.taskmgmt",
	      "Task Modules");

	   /** The Constant PRT_Q_MASTER_CATEGORY. */
	   public static final Category PRT_Q_MASTER_CATEGORY = new Category(
	      "PrintQMaster", "PrintQMaster Modules");

	   /** The Constant OUTPUT_CATEGORY. */
	   public static final Category OUTPUT_CATEGORY = new Category(
	      "WM - OutputInterfaces", "WM OutputInterfaces Modules");

	   /** The Constant OUTBOUND_SERVICES_CATEGORY. */
	   public static final Category OUTBOUND_SERVICES_CATEGORY = new Category(
	      "com.manh.wmos.services.outbound",

	      "OutboundServices Modules");

	   /** The Constant Newgistics_Zip_code_Inquiry. */
	   public static final Category Newgistics_Zip_code_Inquiry = new Category(
	      "WM - OutputInterfaces",

	      "Interfaces Modules");

	   /** The Constant WLM_CATEGORY. */
	   public static final Category WLM_CATEGORY = new Category("WM - WLM",
	      "WLM Modules");

	   /** The Constant LPNDISP_CATEGORY. */
	   public static final Category LPNDISP_CATEGORY = new Category(
	      "com.manh.wmos.services.lpndisposition", "LPNDisposition Modules");

	    /** The Constant MHE_INBOUND_CATEGORY. */
	   public static final Category MHE_INBOUND_CATEGORY = new Category("com.manh.wmos.services.mheinbound", "MHE Inbound Services");


	   public static final Category MHE_OUTBOUND_CATEGORY = new Category("com.manh.wmos.services.mheoutbound", "MHE Outbound Services");


	    /** The Constant USER_LOGIN_CATEGORY. */
	    public static final Category USER_LOGIN_CATEGORY = new Category("WM - User Login Info", "User login Info");

	    /** The Constant DEVICE_COMMUNICATION_CATEGORY. */
	    public static final Category DEVICE_COMMUNICATION_CATEGORY = new Category("WM - Device Communication", "Device Communication");

	    /** The Constant EIS_CATEGORY. */
	    public static final Category EIS_CATEGORY = new Category("  WM - EIS  "," EIS Services ");

	    /** The Constant LABEL_PRINTING_SERVICES_CATEGORY. */
	    public static final Category LABEL_PRINTING_SERVICES_CATEGORY = new Category("  WM - Label Printing  "," Label Printing Services ");


	    /** The Constant LABEL_IMPORT_LOG_CATEGORY. */
	    public static final Category LABEL_IMPORT_LOG_CATEGORY = new Category("Import_log  "," Labels Import");

	    /** The Constant LABEL_ERROR_LOG_CATEGORY. */
	    public static final Category LABEL_ERROR_LOG_CATEGORY = new Category("Error_log  "," Labels Error");

	    /** The Constant EWS_CATEGORY. */
	    public static final Category WM_EWS_CATEGORY = new Category("  WM - EWS  "," EWS Services ");
	    
	    /** The Constant WM_PIX_CATEGORY. */
	    public static final Category WM_PIX_CATEGORY = new Category("WM - PIX Export","WM PIX Export");

	    /** The Constant WM_PIX_CATEGORY. */
	    public static final Category WM_SHIPCONFIM_CATEGORY = new Category("WM - Ship Confirm Export","WM Ship Confirm Export");
    
    /** The Constant WM_REST_SERVICE_CATEGORY. */
    public static final Category WM_REST_SERVICE_CATEGORY = new Category("  WM - Rest Services  "," WM Rest Services");
    
    /** The Constant VOICE_CATEGORY. */
    public static final Category VOICE_CATEGORY = new Category("WM - VoCollect Info", "Vocollect Info");


   /**
    * Adds the categories.
    *
    * @param debugLog
    *            the debug log
    */
   public static void addCategories(ILSDebugLog debugLog)
   {

      // PLEASE KEEP ALPHABETICALLY SORTED



      debugLog.addCategory(DEVICE_COMMUNICATION_CATEGORY);

      debugLog.addCategory(EIS_CATEGORY);

      debugLog.addCategory(INVENTORYMGMT_CATEGORY);

      debugLog.addCategory(LABEL_ERROR_LOG_CATEGORY);

      debugLog.addCategory(LABEL_IMPORT_LOG_CATEGORY);

      debugLog.addCategory(LABEL_PRINTING_SERVICES_CATEGORY);

      debugLog.addCategory(PUTAWAY_CATEGORY);

      debugLog.addCategory(PRERECEIVING_CATEGORY);

      debugLog.addCategory(RECEIVING_CATEGORY);

      debugLog.addCategory(SOA_CATEGORY);

      debugLog.addCategory(SYSTEM_CONTROL_CATEGORY);

      debugLog.addCategory(WM_COMMON_CATEGORY);

      debugLog.addCategory(WM_CORE_COMMON_CATEGORY);

      debugLog.addCategory(WLM_CATEGORY);

      debugLog.addCategory(WM_EWS_CATEGORY);
      
      debugLog.addCategory(WM_PIX_CATEGORY);
      
      debugLog.addCategory(WM_SHIPCONFIM_CATEGORY);

      debugLog.addCategory(WM_TRANS_N_FREIGHT_CATEGORY);

      debugLog.addCategory(TASK_MANAGEMENT_CATEGORY);

      debugLog.addCategory(TASK_CATEGORY);

      debugLog.addCategory(PRT_Q_MASTER_CATEGORY);

      debugLog.addCategory(OUTBOUND_SERVICES_CATEGORY);

      debugLog.addCategory(OUTPUT_CATEGORY);

      debugLog.addCategory(LPNDISP_CATEGORY);

      debugLog.addCategory(MHE_INBOUND_CATEGORY);

      debugLog.addCategory(MHE_OUTBOUND_CATEGORY);

      debugLog.addCategory(USER_LOGIN_CATEGORY);
      
      debugLog.addCategory(VOICE_CATEGORY);

      // BPE Category should be part of CommonDebugLog but missing for javalib
      // 1.4 jar hence adding temporarily

      debugLog.addCategory(Logger.BPE_DEFAULT_CATEGORY);


      // PLEASE KEEP ALPHABETICALLY SORTED
	  
	  // Custom log level for HTPC
	  debugLog.addCategory(HTPCWMAgileDebugLog.HTPC_WMAGILE_CATEGORY);

   }

   /**
    * Write with high severity the given exception's message and stack trace
    * for the given category.
    *
    * @param category
    *            The category to which to log this exception.
    * @param exception
    *            The exception to be logged.
    */

   public void logException(LogFile.Category category, Exception exception)
   {
      logError(category, exception.getMessage(), exception);
   }

   /**
    * Checks if is debug log enabled.
    *
    * @param category
    *            the category
    *
    * @return true, if is debug log enabled
    */
   public static boolean isDebugLogEnabled(Category category)
   {
      return JavalibLog.willBeLogged(LogFile.DEBUG, category);
   }

}

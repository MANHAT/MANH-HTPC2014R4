package com.manh.wmos.services.mheoutbound.agile.data;

public class HTPCAgileEnum {
	
	/**
	 * The Enum ErrorSeqNbr is custom to HTPC Agile Communication Process.
	 */
	public enum HTPCErrorSeqNbr
	{
		/** LPN Facility Status is Invalid. Error Sequence Number : 996 */
		INVALID_FACILITY_STATUS(996),
		/** The Order Load Response received from Agile is not proper. Error Sequence Number : 997 */
		IMPROPER_OLR_RESPONSE(997),
		/** Success. Error Sequence Number : 0 */
		SUCCESS(0),
		/** Required Value missing in Post Shipment Confirmation(PSC), Error Sequence Number : 998  */
		MISSING_VALUE_IN_PSC(998),
		/** LPN Facility Status greater than 90, Error Sequence Number : 999  */
		IMPROPER_LPN_FACILTY_STATUS(999);

		/** The value. */
		public int value = 0;

		/**
		 * Instantiates a new HTPCErrorSeqNbr.
		 * 
		 * @param val
		 *            - the value.
		 */
		HTPCErrorSeqNbr(int val)
		{
			value = val;
		}
	};

}

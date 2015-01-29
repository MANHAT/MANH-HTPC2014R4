package com.manh.wmos.services.outbound.agile.data;

import com.manh.ils.wmservices.utils.WMConstants;

/**
 * This interface contains response message element tags values received from Agile to WM
 * @author psindhu
 *
 */
public interface IHTPCWMAgileCommunicationConstants extends WMConstants
{

	public static final String ROOT_ELEMENT = "AgileMessage";
	public static final String LPN = "LPN";
	public static final String MESSAGE_TYPE = "MessageType";
	public static final String ORIGIN = "Origin";	
	public static final String SHIP_VIA= "ShipViaCode";
	public static final String TRACKING_NUMBER= "TrackingNumber";
	public static final String ACTUAL_WEIGHT= "ActualMessage";
	public static final String FREIGHT_CHARGE= "FreightCharge";
	public static final String SURCHARGE= "Surcharge";
	public static final String INSAURANCE_CHARGE= "InsuranceCharge";
	public static final String COD_CHARGE= "CODCharge";
	public static final String EVENT_MESSAGE_ID= "EventMessageID";

	public static final String ERROR = "Error";
	public static final String SUCCESS = "Success";

	public static final String AGILE = "AGL";
	public static final String AGILE_RATING = "Agile_Rating";
	public static final String AGILE_VOID = "Agile_Void";
	public static final String AGILE_RATING_ACK = "Agile_Rating_Acknowledgment";
	public static final String AGILE_VOID_ACK = "Agile_Void_Acknowledgment";
	public static final String MESSAGE_FORMAT = "XML";
	public static final String WM = "WM";
	
	/** PSC_ACK_TMPL = AgilePSCacknowledgment.vm*/
	public static final String PSC_ACK_TMPL = "AgilePSCacknowledgment.vm";

}

package com.manh.wmos.services.outbound.agile.processor;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jdom.Document;
import org.jdom.Element;
import org.springframework.context.ApplicationContext;

import com.logistics.javalib.util.Misc;
import com.manh.ils.ILSApplicationContext;
import com.manh.ils.db.AppContextUtil;
import com.manh.ils.wmservices.hibernate.EventMessage;
import com.manh.ils.wmservices.utils.WMConstants;
import com.manh.te.manifest.presentation.util.SessionUserInfo;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wmos.services.OutputInterfaces.dao.EventManagerDAO;
import com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO;
import com.manh.wmos.services.outbound.agile.data.IHTPCWMAgileCommunicationConstants;

/**
 * EX01 Agile to WM response MSG processor
 * 
 * @author psindhu
 */
@SuppressWarnings("unchecked")
public class HTPCWMAgileResponseProcessor
{
	private String		responseXML;
	private Document	responseDocument;

	public HTPCWMAgileResponseProcessor(String responseXML)
	{
		this.responseXML = responseXML;
	}

	/**
	 * @return
	 */
	public String getResponseXML()
	{
		return responseXML;
	}

	/**
	 * @return
	 */
	public Document getResponseDocument()
	{
		return responseDocument;
	}

	/**
	 * @param responseDocument
	 */
	public void setResponseDocument(Document responseDocument)
	{
		this.responseDocument = responseDocument;
	}

	/**
	 * actual processing of order load response message
	 * 
	 * @param paramXML
	 * @throws Exception
	 */
	public void processOrderLoadResponse(String paramXML) throws Exception
	{
		HTPCWMAgileLogHelper.logEnter("order load Response message processing");

		try
		{
			Element responseElement = responseDocument.getRootElement();

			Map<String, String> commonElementMap = getCommonElements(responseElement);
			Map<String, String> orderLoadResponseElementMap = getOrderLoadResponseElements(responseElement);
			String lpnNumber = commonElementMap.get(IHTPCWMAgileCommunicationConstants.LPN);
			Short lpnFacilityStatus = getResponseDAO().getLpnFacilityStatus(lpnNumber);
			String shipVia = orderLoadResponseElementMap.get(IHTPCWMAgileCommunicationConstants.SHIP_VIA);
			String trackingNumber = orderLoadResponseElementMap.get(IHTPCWMAgileCommunicationConstants.TRACKING_NUMBER);
			String actualWeight = orderLoadResponseElementMap.get(IHTPCWMAgileCommunicationConstants.ACTUAL_WEIGHT);
			String freightCharge = orderLoadResponseElementMap.get(IHTPCWMAgileCommunicationConstants.FREIGHT_CHARGE);
			String surCharge = orderLoadResponseElementMap.get(IHTPCWMAgileCommunicationConstants.SURCHARGE);
			String insauranceCharge = orderLoadResponseElementMap.get(IHTPCWMAgileCommunicationConstants.INSAURANCE_CHARGE);
			String codCharge = orderLoadResponseElementMap.get(IHTPCWMAgileCommunicationConstants.COD_CHARGE);
			String eventMessageId = orderLoadResponseElementMap.get(IHTPCWMAgileCommunicationConstants.EVENT_MESSAGE_ID);
			String lpnRefField10 = null;
			Short lpnFacilityStatusToUpdate = (short) 30;

			if (lpnFacilityStatus != null && lpnFacilityStatus.intValue() >= 90)
			{
				HTPCWMAgileLogHelper.logHigh("LPN facility status >= 90, failing the further order load response message processing");
				getResponseDAO().updateEventMessageForFailure(eventMessageId);
			}
			else if (Misc.isNullString(lpnNumber) || Misc.isNullString(shipVia) || Misc.isNullString(trackingNumber) || Misc.isNullString(freightCharge))
			{
				HTPCWMAgileLogHelper.logHigh("one of mendatry field missing, failing the further order load response message processing. values - lpnNumber : " + lpnNumber + " ship via : " + shipVia
						+ " tracking number : " + trackingNumber + " postage : " + freightCharge);
				getResponseDAO().updateEventMessageForMissingFieldsFailure(eventMessageId);
			}
			else
			{
				/* ORDER_LINE_ITEM update starts */
				getResponseDAO().checkAndUpdateOrderLineItem(lpnNumber);

				/* ORDERS update starts */
				getResponseDAO().checkAndUpdateOrder(lpnNumber);

				// update lpn fields from response

				/* Perform LPN update */
				getResponseDAO().updateLPNForOrderLoadResponse(trackingNumber, surCharge, freightCharge, insauranceCharge, codCharge, shipVia, actualWeight, lpnRefField10, lpnFacilityStatusToUpdate,
						lpnNumber);

				/* Perform EVENT_MESSAGE update */
				getResponseDAO().updateEventMessageForSucess(eventMessageId);

				HTPCWMAgileLogHelper.logDebug("Order load Response Message processed successfully for oLPN: " + lpnNumber);
			}
		}
		catch (Exception ex)
		{
			HTPCWMAgileLogHelper.logException(ex, "Exception Caught. Stop processing order load response.");
			throw ex;
		}
		finally
		{
			HTPCWMAgileLogHelper.logExit("order load Respons Message Processing");
		}
	}

	/**
	 * actual processing of received void response message
	 * 
	 * @param paramXML
	 * @throws Exception
	 */
	public void processVoidResponse(String paramXML) throws Exception
	{
		HTPCWMAgileLogHelper.logEnter("void Response message processing");

		try
		{
			Element responseElement = responseDocument.getRootElement();
			Map<String, String> commonElementMap = getCommonElements(responseElement);

			String lpnNumber = commonElementMap.get(IHTPCWMAgileCommunicationConstants.LPN);
			Short lpnFacilityStatus = getResponseDAO().getLpnFacilityStatus(lpnNumber);
			String messageType = commonElementMap.get(IHTPCWMAgileCommunicationConstants.MESSAGE_TYPE);

			String lpnRefField10 = "V";
			Short lpnFacilityStatusToUpdate = (short) 20;
			String lpnTrackingNumber = Misc.EMPTY_STRING;

			if (Misc.isNullString(lpnNumber) || (lpnFacilityStatus != null && lpnFacilityStatus.intValue() >= 90))
			{
				HTPCWMAgileLogHelper.logHigh("LPN facility status >= 90 or lpnNumber==null , failing the further void response message processing, lpnNumber : " + "" + lpnNumber
						+ " lpnFacilityStatus : " + (lpnFacilityStatus == null ? "null" : lpnFacilityStatus.intValue()));
			}
			else
			{
				// update lpn fields from response

				/* Perform LPN update */
				getResponseDAO().updateLPNForVoidResponse(lpnNumber);

				// check for LPN.MISC_INSTR_CODE_1
				String lpnMiscInstrCode1 = getResponseDAO().getLpnMiscInstrCode(lpnNumber);

				// get parameters form lpn to insert a record into event_message
				Long cFacilityId = getResponseDAO().getLpncFacilityId(lpnNumber);
				int tcCompanyId = getResponseDAO().getTcCompanmyIdForLPN(lpnNumber);
				String lastUpdatedSource = getResponseDAO().getLpnLastUpdatedSource(lpnNumber);

				/* insert record into EVENT_MESSAGE */
				if (!Misc.isNullString(lpnMiscInstrCode1) && lpnMiscInstrCode1.equals("1"))
					insertEventMessageForEvent9001(lpnNumber, cFacilityId, tcCompanyId, lastUpdatedSource);
				else
					insertEventMessageForEvent9003(lpnNumber, cFacilityId, tcCompanyId, lastUpdatedSource);

				/* ORDER_LINE_ITEM update starts */
				getResponseDAO().checkAndUpdateOrderLineItemForVoidResponse(lpnNumber);

				/* ORDERS update starts */
				getResponseDAO().checkAndUpdateOrderForVoidResponse(lpnNumber);

				HTPCWMAgileLogHelper.logDebug("Order load Response Message processed successfully for oLPN: " + lpnNumber);
			}
		}
		catch (Exception ex)
		{
			HTPCWMAgileLogHelper.logException(ex, "Exception Caught. Stop processing order load response.");
			throw ex;
		}
		finally
		{
			HTPCWMAgileLogHelper.logExit("order load Respons Message Processing");
		}
	}

	/**
	 * If the LPN misc instruction code 1 is not equal to 1, insert a record
	 * into EVENT_MESSAGE
	 * 
	 * @param lpnNumber
	 * @param cFacilityId
	 * @param tcCompanyId
	 * @param lastUpdatedSource
	 */
	private void insertEventMessageForEvent9003(String lpnNumber, Long cFacilityId, int tcCompanyId, String lastUpdatedSource)
	{
		HTPCWMAgileLogHelper.logEnter("insert a record into EVENT_MESSAGE for LPN :" + lpnNumber + " Facility Id:" + cFacilityId);

		EventMessage eventMsg = com.manh.wm.WMDomainObjectFactory.getInstance().getEventMessage();
		java.util.Date date = new java.util.Date();

		try
		{
			// TODO : Verify if the event Id is configured on Sys Code. Kallol
			// mentioned
			Integer mheEventId = Integer.valueOf("9003");
			Integer mheEventStatus = Integer.valueOf("20");
			String whse = SessionUserInfo.getFacilityWhseForFacility(cFacilityId.toString());

			eventMsg.setEventId(mheEventId);
			eventMsg.setEkOlpnNbr(lpnNumber);
			eventMsg.setWhse(whse);
			eventMsg.setNbrOfRetry(WMConstants.INT_ZERO);
			eventMsg.setStatCode(mheEventStatus);
			eventMsg.setCreateDateTime(date);
			eventMsg.setModDateTime(date);
			eventMsg.setUserId(lastUpdatedSource);
			eventMsg.setErrorSeqNbr(Integer.valueOf("0"));
			eventMsg.setCdMasterId(tcCompanyId);

			if (eventMsg != null)
			{
				getEventDAO().addEventMessage(eventMsg);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e);
		}
		HTPCWMAgileLogHelper.logExit();
	}

	/**
	 * If the LPN misc instruction code 1 is equal to 1, insert a record into
	 * EVENT_MESSAGE
	 * 
	 * @param lpnNumber
	 * @param cFacilityId
	 * @param tcCompanyId
	 * @param lastUpdatedSource
	 */
	private void insertEventMessageForEvent9001(String lpnNumber, Long cFacilityId, int tcCompanyId, String lastUpdatedSource)
	{
		HTPCWMAgileLogHelper.logEnter("insert a record into EVENT_MESSAGE for LPN :" + lpnNumber + " Facility Id:" + cFacilityId);

		EventMessage eventMsg = com.manh.wm.WMDomainObjectFactory.getInstance().getEventMessage();
		java.util.Date date = new java.util.Date();

		try
		{
			// TODO : Verify if the event Id is configured on Sys Code. Kallol
			// mentioned
			Integer mheEventId = Integer.valueOf("9001");
			Integer mheEventStatus = Integer.valueOf("20");
			String whse = SessionUserInfo.getFacilityWhseForFacility(cFacilityId.toString());

			eventMsg.setEventId(mheEventId);
			eventMsg.setEkOlpnNbr(lpnNumber);
			eventMsg.setWhse(whse);
			eventMsg.setNbrOfRetry(WMConstants.INT_ZERO);
			eventMsg.setStatCode(mheEventStatus);
			eventMsg.setCreateDateTime(date);
			eventMsg.setModDateTime(date);
			eventMsg.setUserId(lastUpdatedSource);
			eventMsg.setErrorSeqNbr(Integer.valueOf("0"));
			eventMsg.setCdMasterId(tcCompanyId);

			if (eventMsg != null)
			{
				getEventDAO().addEventMessage(eventMsg);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e);
		}
		HTPCWMAgileLogHelper.logExit();
	}

	/**
	 * Parse the Elements common for both messages.
	 * 
	 * @param responseElement
	 * @return
	 */
	protected Map<String, String> getCommonElements(Element responseElement)
	{
		HTPCWMAgileLogHelper.logEnter();

		Map<String, String> elementMap = new HashMap<String, String>();
		try
		{
			// Fetch the LPN number
			elementMap.put(IHTPCWMAgileCommunicationConstants.LPN, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.LPN));

			// Fetch the AgileResponse origin
			elementMap.put(IHTPCWMAgileCommunicationConstants.ORIGIN, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.ORIGIN));
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e);
		}
		HTPCWMAgileLogHelper.logExit();
		return elementMap;
	}

	/**
	 * Parse the elements specific to ShipResponseMessage
	 * 
	 * @param responseElement
	 * @return
	 */
	protected Map<String, String> getOrderLoadResponseElements(Element responseElement)
	{
		HTPCWMAgileLogHelper.logEnter();
		Map<String, String> elementMap = new HashMap<String, String>();
		try
		{
			/* Get shipVia */
			/* <ShipViaCode>LPN.SHIP_VIA</ShipViaCode> */
			elementMap.put(IHTPCWMAgileCommunicationConstants.SHIP_VIA, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.SHIP_VIA));

			/* <TrackingNumber>LPN.TRACKING _NBR</TrackingNumber> */
			elementMap.put(IHTPCWMAgileCommunicationConstants.TRACKING_NUMBER, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.TRACKING_NUMBER));

			/* <ActualMessage>LPN.ACTUAL_WEIGHT</ActualMessage> */
			elementMap.put(IHTPCWMAgileCommunicationConstants.ACTUAL_WEIGHT, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.ACTUAL_WEIGHT));

			/* <FreightCharge>LPN.FREIGHT_CHARGE</FreightCharge> */
			elementMap.put(IHTPCWMAgileCommunicationConstants.FREIGHT_CHARGE, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.FREIGHT_CHARGE));

			/* <Surcharge>LPN.ADDNL_OPTION_CHARGE</Surcharge> */
			elementMap.put(IHTPCWMAgileCommunicationConstants.SURCHARGE, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.SURCHARGE));

			/* <InsuranceCharge>LPN.INSUR_CHANGE</InsuranceCharge> */
			elementMap.put(IHTPCWMAgileCommunicationConstants.INSAURANCE_CHARGE, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.INSAURANCE_CHARGE));

			/* <CODCharge>LPN.DIST_CHARGE </CODCharge> */
			elementMap.put(IHTPCWMAgileCommunicationConstants.COD_CHARGE, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.COD_CHARGE));

			/* <EventMessageID>EVENT_MESSAGE.EVENT_MESSAGE_ID</EventMessageID> */
			elementMap.put(IHTPCWMAgileCommunicationConstants.EVENT_MESSAGE_ID, parseElement(responseElement, IHTPCWMAgileCommunicationConstants.EVENT_MESSAGE_ID));

		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e);
		}
		HTPCWMAgileLogHelper.logExit();
		return elementMap;
	}

	/**
	 * Fetch an instance of Request DAO
	 * 
	 * @return
	 */
	protected IHTPCAgileOrderLoadRespDAO getResponseDAO()
	{
		return ILSApplicationContext.getBean(IHTPCAgileOrderLoadRespDAO.class.getName());
	}

	/**
	 * Get Event Manager
	 * 
	 * @return EventManager
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation")
	protected EventManagerDAO getEventDAO()
	{
		ApplicationContext ctx = AppContextUtil.getAppCtx("wm.outputInterfaces");
		EventManagerDAO dao = (EventManagerDAO) ctx.getBean("EventManagerDAO");
		return dao;
	}

	/**
	 * fetch message type from response xml message
	 * 
	 * @param in
	 * @return
	 */
	public String checkMessageType(String in)
	{
		HTPCWMAgileLogHelper.logEnter("fetch message type from response xml");
		String messageType = Misc.EMPTY_STRING;
		try
		{
			Element messageElements = responseDocument.getRootElement().getChild(IHTPCWMAgileCommunicationConstants.MESSAGE_TYPE);
			messageType = messageElements.getTextTrim();
		}
		catch (Exception ex)
		{
			HTPCWMAgileLogHelper.logException(ex, "Exception Caught Reading AgileMessage>MessageType");
			throw ex;
		}
		finally
		{
			HTPCWMAgileLogHelper.logExit("fetched message type from response xml messageType : " + messageType);
		}

		return messageType;
	}

	/**
	 * Parse particular element of xml
	 * 
	 * @param responseElement
	 * @param tagName
	 * @return
	 */
	public String parseElement(Element responseElement, String tagName)
	{
		String tagValue = Misc.EMPTY_STRING;
		try
		{
			tagValue = responseElement.getChild(tagName).getTextTrim();
			HTPCWMAgileLogHelper.logDebug("parsed value of AgileMessage>" + tagName + " := " + tagValue);
		}
		catch (Exception ex)
		{
			HTPCWMAgileLogHelper.logException(ex, "Exception Caught Reading AgileMessage>" + tagName);
		}
		return tagValue;
	}

	/**
	 * tran_log entry for void response
	 * 
	 * @param request
	 */
	public void tranLogEntryForVoidResponse(HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter("tran_log entry for void response");
		getResponseDAO().tranLogEntryForVoidResponse(request);
		HTPCWMAgileLogHelper.logExit();
	}

	/**
	 * tran_log entry for order load request
	 * 
	 * @param request
	 */
	public void tranLogEntryForOrderLoadRequest(HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter("tran_log entry for order load request");
		getResponseDAO().tranLogEntryForOrderLoadRequest(request);
		HTPCWMAgileLogHelper.logExit();
	}

	/**
	 * tran_log entry for void response Acknowledgment from WM to Agile
	 * 
	 * @param request
	 */
	public void tranLogEntryForVoidResponseAck(HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter("tran_log entry for void response Ack");
		getResponseDAO().tranLogEntryForVoidResponseAck(request);
		HTPCWMAgileLogHelper.logExit();
	}

	/**
	 * tran_log entry for order load response Acknowledgment from WM to Agile
	 * 
	 * @param request
	 */
	public void tranLogEntryForOrderLoadRequestAck(HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter("tran_log entry for order load response Acknowledgment from WM to Agile");
		getResponseDAO().tranLogEntryForOrderLoadRequestAck(request);
		HTPCWMAgileLogHelper.logExit();
	}

}

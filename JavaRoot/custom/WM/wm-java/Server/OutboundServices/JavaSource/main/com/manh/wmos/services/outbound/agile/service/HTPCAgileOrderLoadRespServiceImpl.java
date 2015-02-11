// This Class Processes response from Agile for Order Load Request, This class
// is invoked through router

package com.manh.wmos.services.outbound.agile.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.logistics.javalib.util.Misc;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wmos.services.communication.core.WMCommunicationManagerException;
import com.manh.wmos.services.outbound.agile.data.IHTPCWMAgileCommunicationConstants;
import com.manh.wmos.services.outbound.agile.processor.HTPCWMAgileResponseParser;
import com.manh.wmos.services.outbound.agile.processor.HTPCWMAgileResponseProcessor;

/**
 * This class provides code for processing response XML messages received from
 * Agile to WM
 * 
 * @author psindhu
 */
public class HTPCAgileOrderLoadRespServiceImpl implements IHTPCAgileOrderLoadRespService
{

	/**
	 * this method is starting point for response receiving of order load
	 * response message and void response messages
	 * 
	 * @param in
	 * @return
	 * @throws WMCommunicationManagerException
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public String receiveAgileResponse(String responseXML, HttpServletRequest request) throws WMCommunicationManagerException
	{
		int tranLogId = -1;

		HTPCWMAgileLogHelper.logEnter("agile Response XML : " + responseXML);
		String responseStatus = Misc.EMPTY_STRING;
		try
		{
			HTPCWMAgileResponseProcessor responseProcessor = getResponseProcessor(responseXML);

			String messageType = responseProcessor.checkMessageType(responseXML);
			if (messageType.equalsIgnoreCase("Void"))
			{
				HTPCWMAgileLogHelper.logDebug("  message type in response xml  : " + messageType);
				tranLogId = responseProcessor.tranLogEntryForVoidResponse(request);
				HTPCWMAgileLogHelper.logDebug("TRAN_LOG_ID received : " + tranLogId);

				responseStatus = receiveAgileVoidResponse(responseXML, responseProcessor);

				if (responseStatus.equalsIgnoreCase("success") && tranLogId != -1)
				{
					responseProcessor.updateTranLogEntryForVoidResponse(tranLogId, IHTPCWMAgileCommunicationConstants.SUCCESS_RESULT_CODE);
					HTPCWMAgileLogHelper.logDebug("Updated RESULT_CODE of TRAN_LOG table to " + IHTPCWMAgileCommunicationConstants.SUCCESS_RESULT_CODE);
				}
				else if (responseStatus.equalsIgnoreCase("error") && tranLogId != -1)
				{
					responseProcessor.updateTranLogEntryForVoidResponse(tranLogId, IHTPCWMAgileCommunicationConstants.FAILURE_RESULT_CODE);
					HTPCWMAgileLogHelper.logDebug("Updated RESULT_CODE of TRAN_LOG table to " + IHTPCWMAgileCommunicationConstants.FAILURE_RESULT_CODE);
				}

				//responseProcessor.tranLogEntryForVoidResponseAck(request);
			}
			else if (messageType.equalsIgnoreCase("Rating"))
			{
				HTPCWMAgileLogHelper.logDebug("  message type in response xml  : " + messageType);

				tranLogId = responseProcessor.tranLogEntryForOrderLoadRequest(request);
				HTPCWMAgileLogHelper.logDebug("TRAN_LOG_ID received : " + tranLogId);

				responseStatus = receiveAgileOrderLoadResponse(responseXML, responseProcessor);

				if (responseStatus.equalsIgnoreCase("success") && tranLogId != -1)
				{
					responseProcessor.updateTranLogEntryForOrderLoadRequest(tranLogId, IHTPCWMAgileCommunicationConstants.SUCCESS_RESULT_CODE);
					HTPCWMAgileLogHelper.logDebug("Updated RESULT_CODE of TRAN_LOG table to " + IHTPCWMAgileCommunicationConstants.SUCCESS_RESULT_CODE);
				}
				else if (responseStatus.equalsIgnoreCase("error") && tranLogId != -1)
				{
					responseProcessor.updateTranLogEntryForOrderLoadRequest(tranLogId, IHTPCWMAgileCommunicationConstants.FAILURE_RESULT_CODE);
					HTPCWMAgileLogHelper.logDebug("Updated RESULT_CODE of TRAN_LOG table to " + IHTPCWMAgileCommunicationConstants.FAILURE_RESULT_CODE);
				}

				//responseProcessor.tranLogEntryForOrderLoadRequestAck(request);
			}
			else
			{
				HTPCWMAgileLogHelper.logHigh(" invalid message type in response xml messageType : " + messageType);
			}

		}
		catch (Exception ex)
		{
			HTPCWMAgileLogHelper.logException(ex, "Exception Caught in receiveAgileResponse");
			responseStatus = IHTPCWMAgileCommunicationConstants.ERROR;
		}
		HTPCWMAgileLogHelper.logExit("agile Response XML");
		return responseStatus;
	}

	/*
	 * this method is starting point for Parsing of order load response message
	 * received from Agile to WM
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.service.IHTPCAgileOrderLoadRespService
	 * #receiveAgileOrderLoadResponse(java.lang.String)
	 */
	public String receiveAgileOrderLoadResponse(String responseXML, HTPCWMAgileResponseProcessor responseProcessor) throws WMCommunicationManagerException
	{
		HTPCWMAgileLogHelper.logEnter("Order Load Response XML : " + responseXML);
		String responseStatus = Misc.EMPTY_STRING;
		try
		{
			responseProcessor.processOrderLoadResponse(responseXML);

			responseStatus = IHTPCWMAgileCommunicationConstants.SUCCESS;

		}
		catch (Exception ex)
		{
			HTPCWMAgileLogHelper.logException(ex, "Exception Caught in receiveMessage of order load response");
			responseStatus = IHTPCWMAgileCommunicationConstants.ERROR;
		}
		HTPCWMAgileLogHelper.logExit("order load response");
		return responseStatus;
	}

	/*
	 * this method is starting point for Parsing of void response message
	 * received from Agile to WM
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.service.IHTPCAgileOrderLoadRespService
	 * #receiveAgileVoidResponse(java.lang.String)
	 */
	public String receiveAgileVoidResponse(String responseXML, HTPCWMAgileResponseProcessor responseProcessor) throws WMCommunicationManagerException
	{
		HTPCWMAgileLogHelper.logEnter("void Response XML : " + responseXML);
		String responseStatus = Misc.EMPTY_STRING;
		try
		{
			responseProcessor.processVoidResponse(responseXML);
			responseStatus = IHTPCWMAgileCommunicationConstants.SUCCESS;
		}
		catch (Exception ex)
		{
			HTPCWMAgileLogHelper.logException(ex, "Exception Caught in receiveMessage of void response");
			responseStatus = IHTPCWMAgileCommunicationConstants.ERROR;
		}
		HTPCWMAgileLogHelper.logExit("void response");
		return responseStatus;
	}

	/**
	 * initializing all parsing parameter and return response processor instance
	 * 
	 * @param paramXML
	 * @param eventCodeStr
	 * @return
	 * @throws NumberFormatException
	 * @throws Exception
	 */
	public HTPCWMAgileResponseProcessor getResponseProcessor(String paramXML) throws NumberFormatException, Exception
	{
		HTPCWMAgileResponseParser responseParser = new HTPCWMAgileResponseParser();
		HTPCWMAgileResponseProcessor responseProcessor = responseParser.getAgileResponseProcessor(paramXML);

		return responseProcessor;
	}
}

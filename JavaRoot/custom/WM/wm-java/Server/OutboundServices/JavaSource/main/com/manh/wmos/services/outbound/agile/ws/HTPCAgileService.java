package com.manh.wmos.services.outbound.agile.ws;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.logistics.javalib.util.Misc;
import com.manh.ils.ILSApplicationContext;
import com.manh.mps.rest.AbstractService;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wmos.services.outbound.agile.service.IHTPCAgileOrderLoadRespService;

public class HTPCAgileService extends AbstractService
{

	/**
	 * This(ackPSCresponse) is the response from WM to Agile server after
	 * processing the PSC from Agile server.
	 */
	String	ackPSCresponse	= Misc.EMPTY_STRING;

	@POST
	@Path("/receiveMessage")
	@Consumes("text/html")
	/**   
	 * 
	 * Call this using a URL such as:
	 * 
	 * http://blrlw2489.asia.manh.com:10001/services/AgileService/receiveMessage
	 * 
	 * All Parameters are REQUIRED as the URL Must match entirely.
	 * 
	 */
	public String receiveMessage(@Context HttpServletRequest request) throws Exception
	{
		HTPCWMAgileLogHelper.logEnter(" rest call triggering response XML receiving responseXML : " + request.getParameter("xmlString"));

		String status = getResponseService().receiveAgileResponse(request.getParameter("xmlString"), request);

		if (status.equalsIgnoreCase("Success"))
		{
			ackPSCresponse = preparePSacknowledgement("1");
		}
		else
		{
			ackPSCresponse = preparePSacknowledgement("0");
		}

		HTPCWMAgileLogHelper.logExit("*** FINAL RESPONSE OF PSC TO AGILE ***\n" + ackPSCresponse);
		return ackPSCresponse;
	}

	/**
	 * Prepares the acknowledgment from WM for Agile server after processing
	 * PSC(Post Shipment Confirmation) message.
	 * 
	 * @param code
	 *            - the value of Code to be returned in acknowledgment of PSC,
	 *            to Agile server.
	 * @return xml string - the acknowledgment.
	 */
	private String preparePSacknowledgement(String code)
	{
		String failDesc = "ErrorProcessingMessage";
		String successDesc = "Success";

		HTPCWMAgileLogHelper.logEnter("Code received : [" + code + "], creating the acknowledgment for Post Shipment Confirmation message.");

		StringBuffer sb = new StringBuffer("<?xml version=\"1.0\" encoding=\"utf-8\"?><AgileMessageResponse><Status><Code>");
		sb.append(code);
		sb.append("</Code><Description>");
		if (code.equals("1"))
			sb.append(successDesc);
		else
			sb.append(failDesc);
		sb.append("</Description></Status></AgileMessageResponse>");

		HTPCWMAgileLogHelper.logExit("The xml acknowledgement prepared is:\n" + sb);
		return sb.toString();
	}

	/**
	 * Get service instance
	 * 
	 * @return
	 */
	protected IHTPCAgileOrderLoadRespService getResponseService()
	{
		return ILSApplicationContext.getBean(IHTPCAgileOrderLoadRespService.class.getName());
	}

}

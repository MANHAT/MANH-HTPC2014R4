package com.manh.wmos.services.outbound.agile.ws;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import com.logistics.javalib.util.Misc;
import com.manh.ils.ILSApplicationContext;
import com.manh.mps.rest.AbstractService;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wmos.services.outbound.agile.data.IHTPCWMAgileCommunicationConstants;
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

		// JSONObject json = new JSONObject();

		String status = getResponseService().receiveAgileResponse(request.getParameter("xmlString"), request);
		// json.put("status", status);

		if (status.equalsIgnoreCase("Success"))
		{
			ackPSCresponse = preparePSacknowledgement("1");
		}
		else
		{
			ackPSCresponse = preparePSacknowledgement("0");
		}

		HTPCWMAgileLogHelper.logExit();
		return ackPSCresponse;
		// return json.toString();
	}

	/**
	 * Prepares the acknowledgment from WM for Agile server after processing
	 * PSC(Post Shipment Confirmation) message, using velocity template.
	 * 
	 * @param code
	 *            - the value of Code to be returned in acknowledgment of PSC,
	 *            to Agile server.
	 * @return xml string - the acknowledgment.
	 */
	private String preparePSacknowledgement(String code)
	{
		HTPCWMAgileLogHelper.logEnter("creating the acknowledgment for Post Shipment Confirmation message.");
		VelocityEngine ve = new VelocityEngine();
		ve.init();

		HTPCWMAgileLogHelper.logDebug("Code received : [" + code + "], Using velocity template : " + IHTPCWMAgileCommunicationConstants.PSC_ACK_TMPL);
		/* getting the Template - AgilePSCacknowledgment.vm */
		Template t = ve.getTemplate(IHTPCWMAgileCommunicationConstants.PSC_ACK_TMPL);
		VelocityContext context = new VelocityContext();
		context.put("code", code);
		StringWriter writer = new StringWriter();
		t.merge(context, writer);

		HTPCWMAgileLogHelper.logExit("The xml acknowledgement prepared is:\n" + writer);
		return writer.toString();
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

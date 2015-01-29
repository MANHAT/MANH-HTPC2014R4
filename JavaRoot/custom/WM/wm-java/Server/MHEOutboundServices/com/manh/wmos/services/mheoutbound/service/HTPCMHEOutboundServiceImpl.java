package com.manh.wmos.services.mheoutbound.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.manh.ils.wmservices.hibernate.EventMessage;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wmos.services.mheoutbound.agile.HTPCAgileOrderLoadRequest;
import com.manh.wmos.services.mheoutbound.helper.MheOutboundUtil;

public class HTPCMHEOutboundServiceImpl extends MHEOutboundServiceImpl {
	
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	 public List<String> processEventMessage(EventMessage aEventMessage)
	 {
		boolean orderLoadGenStatus = false;
		HTPCWMAgileLogHelper.logDebug("in HTPCMHEOutboundServiceImpl : processEventMessage()");
		try  {
			if(aEventMessage != null) {
				int msgID = aEventMessage.getEventMessageId();
				int eventID = aEventMessage.getEventId();
				log.formatLogDebug("processEventMessage with message id %s", msgID);
				if(eventID==9001 || eventID==9002 || eventID==9003) {				// Checking if the event is one of the Custom Events for HTPC EX01.
					HTPCWMAgileLogHelper.logDebug("processEventMessage() : doing custom message processing for event "+eventID+", for Agile..");
					orderLoadGenStatus = HTPCAgileOrderLoadRequest.customAgileMessaging(eventID,aEventMessage);
				}
				else {
					HTPCWMAgileLogHelper.logDebug("processEventMessage() : sending to base for processing of event : "+eventID);
					super.processEventMessage(aEventMessage);
				}
			} 
			if(orderLoadGenStatus) {
				HTPCWMAgileLogHelper.logDebug("Order Load Request generated successfully.");
			}
		 } catch( Exception e ) {	
				log.logException(e);
				MheOutboundUtil.getMHEOutboundService().updateEventMessage(aEventMessage);
		 }
		
		return new ArrayList<String>();
	 }

}

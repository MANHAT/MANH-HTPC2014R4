package com.manh.wmos.services.mheoutbound.agile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.logistics.javalib.util.Misc;
import com.logistics.messaging.util.common.Constants;
import com.manh.integration.core.exception.RouterException;
import com.manh.integration.core.message.MessageHeader;
import com.manh.integration.core.router.RouterCriteriaData;
import com.manh.integration.core.router.response.IRouterResponse;
import com.manh.integration.core.services.IRouterService;
import com.manh.integration.locking.exception.LockException;
import com.manh.integration.standard.services.IIntegrationServiceFacade;
import com.manh.integration.standard.services.impl.IntegrationServiceFacade;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wmos.services.communication.core.WMCommunicationManagerException;
import com.manh.wmos.services.communication.event.msg.EventRouterInput;
import com.manh.wmos.services.communication.event.processor.XmlMsgProcessor;

public class HTPCAgileXmlMsgProcessor extends XmlMsgProcessor {

	@Override
	public Object getInputForTemplate(Object arg0, List arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String tranform(Object arg0, List arg1, String arg2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int updateSentMsg(List arg0, String arg1)
			throws WMCommunicationManagerException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@SuppressWarnings("deprecation")
	public IRouterResponse send(Object msg,String msgRefId, String origin,String msgType, String companyId, String modKey ) throws RouterException,LockException
	{
		HTPCWMAgileLogHelper.logEnter("Initiating send() from HTPCAgileXmlMsgProcessor..");
		String originType = "HST";
		IIntegrationServiceFacade facade = new IntegrationServiceFacade();
        IRouterService rService = facade.getRouterService();
        RouterCriteriaData data = new RouterCriteriaData();
        if(modKey != null)
        {
        	Map<String, Object> userDefParams = new HashMap<String, Object>();
        	userDefParams.put(Constants.MOD_ID,modKey);
        	data.setUserDefParams(userDefParams);
        }
        data.setOriginId(origin);
        data.setOriginType(originType);
        data.setMessageType(msgType);
        data.setOriginCompany(companyId);
        if(!Misc.isNullTrimmedString(msgRefId)){
        	MessageHeader messageHeader = new MessageHeader();
        	messageHeader.setReferenceId(msgRefId);
        	data.setMessageHeader(messageHeader);
        }
        if(msg instanceof EventRouterInput )
        {
        	Map<String, Object> helperMap = ((EventRouterInput) msg).getAttributesMap();
        	if (isValidRouterDetail(data))
        		return rService.processOutSync(data, msg, helperMap);
        	else
        		return null;
        }
        if (isValidRouterDetail(data))
        	return rService.processOutSync(data,msg);
        else
        	return null;
    }
	
	
}

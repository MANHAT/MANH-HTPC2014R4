package com.manh.wmos.services.communication.event.template.processor;

import com.manh.wmos.services.communication.event.factory.XmlMsgFactory;
import com.manh.wmos.services.communication.event.msg.EventRouterInput;
import com.manh.wmos.services.communication.event.msg.EventXmlMsg;
import com.manh.wmos.services.communication.service.VelocityDefaultHelper;
import com.manh.wmos.services.communication.service.WMVelocityProcessor;
import com.manh.ils.wmservices.hibernate.PixTran;
import com.manh.integration.core.util.LIFUtil;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.manh.wmos.services.communication.service.VelocityPixHelper;

public class PixProcessor implements TemplateTransfer
{
	public String transfer(Object hdrObj,Object msgObj)
	{
		EventRouterInput inputObj = getVelocityInput(hdrObj, msgObj);
		Map<String,Object> objectMap = new HashMap<String,Object>();
		objectMap.put("MESSAGE", inputObj);
       	WMVelocityProcessor processor = new WMVelocityProcessor();
		return processor.processEvaluate("PIX.vm","CONTEXT",objectMap);
	}

	@Override
	public Object getInputForRouter(Object hdrObj, Object msgObj) {
		return getVelocityInput(hdrObj, msgObj);
	}

	protected EventRouterInput getVelocityInput(Object hdrObj, Object msgObj)
	{
		List<PixTran> pixTran = (List<PixTran>)msgObj;
		EventXmlMsg hdrMsg = (EventXmlMsg)hdrObj;

		EventRouterInput inputObj = XmlMsgFactory.getInstance().getEventRouterInput();
		inputObj.setPixList(pixTran);		//The list of PIX

		//Map
		Map<String,Object> attrMap = new HashMap<String,Object>();
		attrMap.put("hdrMsg", hdrMsg);
		VelocityPixHelper pixHelper = new VelocityPixHelper();
		VelocityDefaultHelper helper = new VelocityDefaultHelper();
		attrMap.put("pixHelper",pixHelper);
		attrMap.put("Helper",helper);
		attrMap.put("SOURCE_TAG", LIFUtil.getSource());
	    inputObj.setAttributesMap(attrMap);		//Helper map

	    inputObj.setLockingRequired(false);		//Hard-coding to false; No locking required.
	    if(hdrMsg.getCompanyId() != null
	    		&& hdrMsg.getCompanyId().intValue() > 0 )
	    {
	    	inputObj.setCompanyId(hdrMsg.getCompanyId().intValue());	//company-id
	    }

	    List<String> destList = new ArrayList<String>();
	    destList.add(hdrMsg.getWhse());
	    inputObj.setDestinationList(destList);		//Destination list

       	return inputObj;
	}
}

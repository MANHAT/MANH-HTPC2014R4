package com.manh.wmos.services.mheoutbound.agile;

import java.util.HashMap;
import java.util.Map;

import com.logistics.javalib.util.Misc;
import com.manh.common.codelist.BaseDataWebMethodsCodeList;
import com.manh.ils.wmservices.hibernate.EventMessage;
import com.manh.integration.core.router.response.IRouterResponse;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wmos.services.communication.service.VelocityDefaultHelper;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCPierbridgeOrderLoadRequestData;

public class HTPCAgileTranslator {
	
	public static IRouterResponse routeAgileRequestMessage(EventMessage eventMsg, HTPCPierbridgeOrderLoadRequestData data) {
		
		HTPCWMAgileLogHelper.logEnter("Request Type: [" + eventMsg.getEventId() + "] & TcLpnId: [" + eventMsg.getEkOlpnNbr() + "]");
		IRouterResponse response = null;
		String msgRefId = Misc.EMPTY_STRING;
		try 
		{
			String tcCompanyIdStr = String.valueOf(eventMsg.getCdMasterId());
			
			Map<String, Object> messageMap = getRequestInput(eventMsg, tcCompanyIdStr);
			if (!Misc.isNullMap(messageMap)) 
			{
				messageMap.put("data", data);
				if(null != eventMsg.getEkOlpnNbr())
					msgRefId = eventMsg.getEkOlpnNbr();
				
				HTPCAgileXmlMsgProcessor xmlProcessor = new HTPCAgileXmlMsgProcessor();
				response  = xmlProcessor.send(messageMap, msgRefId, "WMOS", "Agile_OrderLoadRequest", tcCompanyIdStr, null);
			}
		}
		catch (Exception ex) 
		{
			HTPCWMAgileLogHelper.logException(ex);
		}
		
		return response;
	}
	
	
	public static Map<String, Object> getRequestInput(EventMessage message, String tcCompanyIdStr) 
	{
		if (message != null) 
		{
			String sourcePfx = Misc.EMPTY_STRING;
			
			Map<String, Object> objectMap = new HashMap<String, Object>();
			objectMap.put(BaseDataWebMethodsCodeList.TXML_ELEMENT_USER_ID, message.getUserId()); 
			objectMap.put(BaseDataWebMethodsCodeList.TXML_ELEMENT_MESSAGE_TYPE, message.getEventId().toString());
			objectMap.put(BaseDataWebMethodsCodeList.TXML_ELEMENT_COMPANY_ID, tcCompanyIdStr);
			objectMap.put(BaseDataWebMethodsCodeList.TXML_ELEMENT_ACTION_TYPE, "create");
			objectMap.put(BaseDataWebMethodsCodeList.TXML_ELEMENT_BATCH_ID, "");
			objectMap.put(BaseDataWebMethodsCodeList.TXML_ELEMENT_REFERENCE_ID, "Tester");
			objectMap.put(BaseDataWebMethodsCodeList.TXML_ELEMENT_SOURCE, sourcePfx);
			objectMap.put(BaseDataWebMethodsCodeList.TXML_ELEMENT_VERSION, "2011");
			objectMap.put("message", message);
			objectMap.put("appHost", System.getProperty("appserver.hostname", ""));
			objectMap.put("appPort", System.getProperty("appserver.http.port", ""));

			VelocityDefaultHelper helper = new VelocityDefaultHelper();
			objectMap.put("HELPER", helper);
			return objectMap;
		}
		return null;
	}

}

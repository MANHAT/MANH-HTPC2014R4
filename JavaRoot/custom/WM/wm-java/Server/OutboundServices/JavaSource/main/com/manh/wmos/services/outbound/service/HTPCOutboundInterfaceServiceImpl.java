/*
 * Copyright  2008 Manhattan Associates All rights reserved. Do not copy,
 * modify or redistribute this file without specific written permission from
 * Manhattan Associates.
 */

package com.manh.wmos.services.outbound.service;

import com.manh.ils.wmservices.domain.pk.UserWarehouseInfo;
import com.manh.te.manifest.domain.Manifest;
import com.manh.tran.BeanManaged;
import com.manh.wm.core.util.WMDebugLog;
import com.manh.wmos.services.outbound.data.HTPCOutboundManifestRequestData;
import com.manh.wmos.services.outbound.data.OutboundManifestRequestData;
import com.manh.wmos.services.outbound.data.OutboundResponseData;
import com.manh.wmos.services.outbound.helper.WaveHelper;


/**
 * EX-02 weigh LPN logic customization for tracking number update 
 * @author psindhu
 *
 */
@BeanManaged(timeout="${WAVE_TRANSACTION_TIMEOUT}")
public class HTPCOutboundInterfaceServiceImpl  extends OutboundInterfaceServiceImpl
{

	/* 
	 * EX-02 tracking number update logic inclusion eith weigh logic
	 * (non-Javadoc)
	 * @see com.manh.wmos.services.outbound.service.OutboundInterfaceServiceImpl#addOrRemoveLPNFromManifest(com.manh.wmos.services.outbound.data.OutboundManifestRequestData)
	 */
	@Override
	public OutboundResponseData addOrRemoveLPNFromManifest(
			OutboundManifestRequestData request) {
		OutboundResponseData retData = new OutboundResponseData();
		HTPCOutboundManifestRequestData requestWithTrackingNumber=(HTPCOutboundManifestRequestData) request;
		Manifest retObj=null;
		if(request!=null)
		{
			IHTPCTEService teService = (IHTPCTEService) WaveHelper.getTEService();
			Integer[] coIds = {request.getCompanyId()};
			String dummy = request.getFacilityId().toString();
			UserWarehouseInfo info = new UserWarehouseInfo(dummy,
					dummy,dummy,request.getUserId(),
					request.getCompanyId().toString(), coIds,
					request.getCompanyId(), request.getFacilityId()
					,dummy,dummy,request.getCompanyId());
			try
			{
				if(request.getMode()==0)
				{
					retObj=teService.addLPNToManifest(info, request.getContainerNumber(), false,request.getContainerWeight());
				}
				else if(request.getMode()==1)
				{
					retObj=teService.removeLPNToManifest(info, request.getContainerNumber());
				}
				else if(request.getMode() == 2)
				{
					//EX-02 checking for tracking number value
					String trackingNbr=requestWithTrackingNumber.getTrackingNbr();
					WMDebugLog.DEBUG_LOG.logDebug(WMDebugLog.OUTBOUND_SERVICES_CATEGORY,
							"EX-02 : before weigh processing trackingNbr : "+trackingNbr);

					// empty/null, call base weighLPN
					if(trackingNbr==null)
					{
						retObj=teService.weighLPN(info, request.getContainerNumber(),
								false, request.getContainerWeight());
					}
					else
					{
						// not empty/null need to call custom HTPCweighLPN
						retObj=teService.weighLPN(info, request.getContainerNumber(),
								false, request.getContainerWeight(),trackingNbr);
					}
				}
				else if(request.getMode() == 3)
				{
					teService.closeManifest(new Integer(request.getContainerNumber()));
				}
				if(retObj!=null && retObj.hasAnyError()){
					retData.setResponse("errors");
				}
			}
			catch( Exception e )
			{
				WMDebugLog.DEBUG_LOG.logDebug(WMDebugLog.OUTBOUND_SERVICES_CATEGORY,
						"Failed addOrRemoveLPNFromManifest :" + e.getMessage());
				retData.setResponse("errors");
			}
		}
		return retData;
	}

}

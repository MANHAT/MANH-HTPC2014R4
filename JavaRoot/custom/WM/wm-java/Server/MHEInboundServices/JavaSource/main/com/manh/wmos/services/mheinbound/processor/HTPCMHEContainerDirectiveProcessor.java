package com.manh.wmos.services.mheinbound.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.context.ApplicationContext;

import com.logistics.javalib.util.Assertion;
import com.logistics.javalib.util.Misc;
import com.manh.cbo.transactional.domain.location.LocnHdr;
import com.manh.ils.db.AppContextUtil;
import com.manh.ils.wmservices.corba.CorbaException;
import com.manh.ils.wmservices.corba.CorbaHelper;
import com.manh.ils.wmservices.corba.CorbaInitialContextFactory;
import com.manh.ils.wmservices.domain.pk.UserWarehouseInfo;
import com.manh.ils.wmservices.exceptions.WMRuntimeException;
import com.manh.ils.wmservices.utils.WMConstants;
import com.manh.scope.services.common.ServiceException;
import com.manh.scope.services.common.validation.ValidationMessage;
import com.manh.scope.services.common.validation.ValidationResult;
import com.manh.scope.wmos.service.IInventoryService;
import com.manh.scope.wmos.service.ILPNDispositionService;
import com.manh.scope.wmos.service.IPutAwayService;
import com.manh.wm.core.util.WMDebugLog;
import com.manh.wmos.services.corba.PkLogin_IF;
import com.manh.wmos.services.corba.PkLogin_IFHelper;
import com.manh.wmos.services.corba.PkMHEInbound_IF;
import com.manh.wmos.services.corba.TaskMgmt_IF;
import com.manh.wmos.services.corba.WMObject_IFPackage.StringListHolder;
import com.manh.wmos.services.mheinbound.helper.HTPCMHEInboundConstants;
import com.manh.wmos.services.mheinbound.helper.MHEInboundConstants;
import com.manh.wmos.services.mheinbound.helper.MHEInboundHelper;
import com.manh.wmos.services.mheinbound.helper.MHESecuritySource;
import com.manh.wmos.services.mheinbound.helper.MHEUserInfo;
import com.manh.wmos.services.mheinbound.helper.MHEInboundConstants.SORT_DIVERT_ACTION;
import com.manh.wmos.services.outbound.data.HTPCOutboundManifestRequestData;
import com.manh.wmos.services.outbound.data.OutboundLoadRequestData;
import com.manh.wmos.services.outbound.data.OutboundManifestRequestData;
import com.manh.wmos.services.outbound.data.OutboundResponseData;
import com.manh.wmos.services.outbound.service.IOutboundInterfaceService;
import com.manh.wmos.services.systemctrl.helper.SystemCtrlDaoLocator;

/**
 * HTPCMHEContainerDirectiveProcessor class process MHE message
 * @author psindhu
 *
 */
public class HTPCMHEContainerDirectiveProcessor extends MHEContainerDirectiveProcessor{

	/* 
	 * EX-02 , including EX-02 custom code with base code for CARTON_WEIGH_ONLY
	 * (non-Javadoc)
	 * @see com.manh.wmos.services.mheinbound.processor.MHEContainerDirectiveProcessor#processMHEMessage(com.manh.wmos.services.mheinbound.helper.MHEInboundConstants.SORT_DIVERT_ACTION, java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.lang.Short)
	 */
	@Override
	protected int processMHEMessage(SORT_DIVERT_ACTION sortActionPgmFlow, String tcLpnId,
			String taskName, String locationId, Map<String, String> messageMap,Short lpnFacilityStatus) {

		if(SORT_DIVERT_ACTION.getByPgmFlow("260").equals(sortActionPgmFlow))
		{
			log.logEnter("processMHEMessage custom flow EX-02 : ",sortActionPgmFlow,tcLpnId,taskName,locationId);


			MHESecuritySource mheSecuritySource = new MHESecuritySource();
			String contextId = mheSecuritySource.performLogIn(MHEUserInfo.getMheUser());
			String dockDoorBrcd = messageMap.get(MHEInboundConstants.FIELD_PHYSICALDEST);

			int retVal = FAILED;
			try
			{
				ValidationResult validationResult = null;

				MHEInboundHelper
				.logDebug("In MHEContainerDirectiveProcessor::processMHEMessage() - sortActionPgmflow = "
						+ sortActionPgmFlow + ", taskName=" + taskName);

				if( Misc.isNullString(taskName)) 
				{
					MHEInboundHelper.logWarning
					("In MHEContainerDirectiveProcessor::processMHEMessage() - taskName is null = "
							+ taskName);
				}
				String msgId=messageMap.get("MESSAGE_ID");
				switch (sortActionPgmFlow)
				{
				case CARTON_WEIGH_ONLY:
					try
					{
						log.logHigh("Start processing Weighing" );
						ApplicationContext ctx = AppContextUtil.getAppCtx("wm.outbound");
						IOutboundInterfaceService service = (IOutboundInterfaceService) ctx
								.getBean("OutboundInterfaceServiceImpl");
						HTPCOutboundManifestRequestData request=new HTPCOutboundManifestRequestData();
						log.logHigh("ContainerNumber "+ tcLpnId);
						request.setFacilityId(MHEUserInfo.getFacilityId());
						request.setCompanyId( MHEUserInfo.getCompanyId());
						request.setContainerNumber(tcLpnId);
						request.setUserId(MHEUserInfo.getUserWarehouseInfo().getUserId());
						String containerWeight=messageMap.get(MHEInboundConstants.FIELD_WEIGHT);
						String trackingNumber=messageMap.get(HTPCMHEInboundConstants.TRACKING_NUMBER);  // EX-02 custom MHE message field
						log.logHigh("EX-02 : trackingNumber from the message"+ trackingNumber);

						log.logHigh("Container weight from the message"+ containerWeight);
						if(!Misc.isNullTrimmedString(containerWeight)){
							//request.setContainerWeight(new Double(containerWeight.trim()).doubleValue());
							request.setContainerWeight(Double.parseDouble(containerWeight.trim()));
						}
						request.setMode(Integer.valueOf(2));
						// If the messageMap has the key of TrackingNbr and the value is not empty/null
						// If the messageMap dont have the key of TrackingNbr, then proceed with base
						if(trackingNumber==null || !trackingNumber.trim().isEmpty())  
						{
							log.logDebug("EX-02 : proceeding with weigh logic for trackingNumber : "+trackingNumber);
							request.setTrackingNbr(trackingNumber);
							OutboundResponseData retData =service.addOrRemoveLPNFromManifest(request);
							retVal=retData.getResponse().trim().length()>0?FAILED:SUCCESS;
						}
						else 
						{
							// If the messageMap has the key of TrackingNbr but the value is empty/null, then fail the message
							log.logDebug("EX-02 : failing weigh logic for message as tracking number is empty, trackingNumber : "+trackingNumber);
							retVal=FAILED;
						}

						log.logHigh("Completed processing Weighing" );

					}
					catch(Exception e){
						log.logHigh(" Error occured while executing Weighing");
						log.logException(e);
						retVal = FAILED;
					}
					break;


				default:
					Assertion.assertion( false, "Unhandled SORT_DIVERT_ACTION : " + sortActionPgmFlow );
				}
			}
			catch( ServiceException e )
			{
				log.logException(e);
			}

			log.logDebug("Return value of the action : "+ retVal);
			log.logExit("processMHEMessage",sortActionPgmFlow,tcLpnId,taskName,locationId);



			return retVal;
		}
		else
			return super.processMHEMessage( sortActionPgmFlow,  tcLpnId,
					taskName,  locationId,  messageMap, lpnFacilityStatus);

	}
}

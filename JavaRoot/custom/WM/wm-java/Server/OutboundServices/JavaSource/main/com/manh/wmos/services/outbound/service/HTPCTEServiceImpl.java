package com.manh.wmos.services.outbound.service;

import java.util.ArrayList;
import java.util.List;

import com.logistics.javalib.util.Misc;
import com.manh.cbo.transactional.domain.lpn.LPN;
import com.manh.ils.wmservices.domain.pk.UserWarehouseInfo;
import com.manh.ils.wmservices.utils.WMConstants;
import com.manh.te.manifest.domain.Manifest;
import com.manh.te.manifest.foundation.proxy.ILpnService;
import com.manh.te.manifest.foundation.util.ManifestLookUpServices;
import com.manh.wm.core.util.WMDebugLog;
import com.manh.wmos.services.outbound.helper.WaveHelper;

public class HTPCTEServiceImpl extends TEServiceImpl implements IHTPCTEService {
	/**
	 * Weigh LPN. MHE Mode
	 * 
	 * @param userInfo
	 * @param lpnNbr
	 * @param isAsynchronous
	 * @param lpnWeight
	 * @param trackingNbr
	 * @return
	 * @throws Exception
	 */
	@Override
	public Manifest weighLPN(UserWarehouseInfo userInfo, String lpnNbr,
			boolean isAsynchronous, double lpnWeight, String trackingNbr)
			throws Exception {

		Manifest input = null;
		try 
		{
			if (lpnNbr == null || Misc.EMPTY_STRING.equals(lpnNbr) || userInfo == null) 
			{
				WMDebugLog.DEBUG_LOG.logError(WMDebugLog.OUTBOUND_SERVICES_CATEGORY, "Can't weigh the LPN as the input data is invalid ");
				return input;
			}

			Integer[] companyIds = userInfo.getEligibleCompanyIds();

			if (companyIds != null || companyIds.length > WMConstants.INT_ZERO) 
			{
				List<Integer> companyIdList = new ArrayList<Integer>(companyIds.length);

				for (Integer companyId : companyIds) 
				{
					companyIdList.add(companyId);
				}

				LPN lpn = WaveHelper.getLpnService().findLPNByNumber(userInfo,lpnNbr);

				if (lpn != null) 
				{
					input = buildData(lpn, lpn.getShipVia(), userInfo.getUserId(), new Double(lpnWeight));
					input.setIsWeighOnly(true);

					// get the LPN from input and set the tracking number
					LPN lpnToUpdate = input.getManifestInputData().getLpn();
					lpnToUpdate.setTrackingNbr(trackingNbr);
					// set back the lpn to input before calling addLpnToManifest
					input.getManifestInputData().setLpn(lpnToUpdate);

					input = getManifestService().addLpnToManifest(input);

					//EX-02 Tracking Number update on the LPN - Start
					/*if(input.hasAnyError() == true)
					{
						return input;
					}
					LPN lpnToUpdate = input.getManifestInputData().getLpn();

					WMDebugLog.DEBUG_LOG.logDebug(WMDebugLog.OUTBOUND_SERVICES_CATEGORY," EX-02 : setting tracking number :"+trackingNbr+" for lpn "+lpnToUpdate.getTcLpnId()+" lpn number : "+lpnNbr);
					lpnToUpdate.setTrackingNbr(trackingNbr);
					
					ILpnService lpnServices = (ILpnService) ManifestLookUpServices.getService(ILpnService.class);//getLpnServices();
					lpnToUpdate = lpnServices.updateLpn(lpnToUpdate);
					
					if(lpnToUpdate!=null && (lpnToUpdate.hasDataErrors() || lpnToUpdate.hasHardCheckErrors()))
					{
						WMDebugLog.DEBUG_LOG.logDebug(WMDebugLog.OUTBOUND_SERVICES_CATEGORY," EX-02 : LPN Tracking Number update failed. Fail the transaction");
						if(lpnToUpdate.hasDataErrors())
						{
							input.addDataErrorList(lpnToUpdate.getDataErrors());
						}
						else
						{
							input.addHardCheckErrorList((ArrayList)lpnToUpdate.getHardCheckErrors());
						}
						return input;
					}
       
					List<LPN> lpnLst=new ArrayList<LPN>();
					lpnLst.add(lpn);
					WaveHelper.getLpnService().updateLPNs(lpnLst);     
					//EX-02 Tracking Number update on the LPN - End					
					 */
				}
			}
		} 
		catch (Exception e) 
		{
			WMDebugLog.DEBUG_LOG.logException(WMDebugLog.OUTBOUND_SERVICES_CATEGORY, e);
			throw e;
		}
		return input;
	}
}

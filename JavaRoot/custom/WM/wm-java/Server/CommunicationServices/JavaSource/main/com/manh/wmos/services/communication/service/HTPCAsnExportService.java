package com.manh.wmos.services.communication.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import com.logistics.javalib.util.Misc;
import com.manh.baseservices.service.CBOBaseServiceLocator;
import com.manh.cbo.transactional.domain.asn.ASN;
import com.manh.cbo.transactional.domain.lpn.LPN;
import com.manh.cbo.transactional.domain.lpn.subobjects.LPNDetail;
import com.manh.cboframework.application.syscode.ISysCodeConfigService;
import com.manh.ils.wmservices.domain.pk.UserWarehouseInfo;
import com.manh.wm.core.util.WMDebugLog;
import com.manh.wmos.services.receiving.dao.HTPCReceivingManagerDAO;

public class HTPCAsnExportService extends AsnExportService {

	String miscFlagForSRL, miscFlagForSTY, miscFlagForBRD = Misc.EMPTY_STRING;
	Boolean isSTYCodeIdEnabled = false;
	Boolean isSRLCodeIdEnabled = false;
	Boolean isBRDCodeIdEnabled = false;

	public HTPCAsnExportService() {

	}

	@SuppressWarnings("unchecked")
	public void exportAsnToEEM(ASN asn, String msgRefKey, boolean isPOLevelASN,
			UserWarehouseInfo userInfo, String msgType) {

		commlog.formatLogDebug("HTPCAsnExportService::exportAsnToEEM Enter ",
				asn, isPOLevelASN, msgType);
		// get the codeID misc Flag for code STY , BRD , SRL
		getCodeIdMiscFlag();

		// check the Misc flag 1-1 is set to Y/1.
		if (isSRLCodeIdEnabled || isSTYCodeIdEnabled || isBRDCodeIdEnabled) {
			commlog.formatLogDebug(
					"HTPCAsnExportService::exportAsnToEEM parameter miscFlagForSRL = ",
					miscFlagForSRL, miscFlagForSTY, isBRDCodeIdEnabled);
			List lpnDataList = (List) new ArrayList();
			List asnLpnDataList = asn.getLPNDataList();

			for (Object obj : asnLpnDataList) {
				LPN eachLpn = (LPN) obj;

				// get the Serial Number key/pair value ( lpn_detail_id and
				// comma separated serial numbers)
				HashMap<Long, String> serialNbrMap = new HashMap<Long, String>();
				if (isSRLCodeIdEnabled) {
					commlog.formatLogDebug("HTPCAsnExportService::exportAsnToEEM parameter miscFlagForSRL is Enabled");
					serialNbrMap = ((HTPCReceivingManagerDAO) getReceivingManagerDAO())
							.getSerialNbrMap(eachLpn.getLpnId());
				}

				// CodeID STY set instructionCode3 and InstructionCode4
				HashMap<Long, List<String>> serialNbrMapForSty = new HashMap<Long, List<String>>();
				if (isSTYCodeIdEnabled || isBRDCodeIdEnabled) {
					commlog.formatLogDebug("HTPCAsnExportService::exportAsnToEEM parameter miscFlagForSTY or isBRDCodeIdEnabled is Enabled");

					// List serialNbrListForSty = null;
					serialNbrMapForSty = ((HTPCReceivingManagerDAO) getReceivingManagerDAO())
							.getItemStyleAndSuffix(eachLpn.getLpnId());
				}

				List<LPNDetail> lpnDetails = eachLpn.getLpnDetailList();

				@SuppressWarnings("rawtypes")
				List detailList = (List) new ArrayList();
				for (LPNDetail eachLpnDtl : lpnDetails) {

					if (eachLpnDtl != null
							&& eachLpnDtl.getLpnDetailId() != null) {
						if (isSRLCodeIdEnabled
								&& serialNbrMap != null
								&& serialNbrMap.containsKey(eachLpnDtl
										.getLpnDetailId())) {
							eachLpnDtl.setInstrtnCode2(serialNbrMap
									.get(eachLpnDtl.getLpnDetailId()));
						} else {

							commlog.formatLogDebug("HTPCAsnExportService::exportAsnToEEM serialNbrMap entry not found for LpnDetailId: "
									+ Long.toString(eachLpnDtl.getLpnDetailId()));

						}
						if (isSTYCodeIdEnabled
								&& serialNbrMapForSty != null
								&& serialNbrMapForSty.containsKey(eachLpnDtl
										.getLpnDetailId())) {
							// List<String> values = serialNbrMapForSty
							// .get(eachLpnDtl.getLpnDetailId());
							List<String> values = serialNbrMapForSty
									.get(eachLpnDtl.getLpnDetailId());
							if (values != null) {
								commlog.formatLogDebug("HTPCAsnExportService::exportAsnToEEM InstrtnCode3 and InstrtnCode4 is set for item style and suffix");
								eachLpnDtl.setInstrtnCode3(values.get(0));
								eachLpnDtl.setInstrtnCode4(values.get(1));
							} else {
								commlog.formatLogDebug("HTPCAsnExportService::exportAsnToEEM item style and suffix entry not found for LpnDetailId: "
										+ Long.toString(eachLpnDtl
												.getLpnDetailId()));
							}
						} else {
							commlog.formatLogDebug("HTPCAsnExportService::exportAsnToEEM serialNbrMapForSty is null ");
						}
						if (isBRDCodeIdEnabled
								&& serialNbrMapForSty != null
								&& serialNbrMapForSty.containsKey(eachLpnDtl
										.getLpnDetailId())) {
							commlog.formatLogDebug("HTPCAsnExportService::exportAsnToEEM InstrtnCode5 is set for brand");
							List<String> values = serialNbrMapForSty
									.get(eachLpnDtl.getLpnDetailId());
							eachLpnDtl.setInstrtnCode5(values.get(2));
						} else {
							commlog.formatLogDebug("HTPCAsnExportService::exportAsnToEEM Brand entry not found for LpnDetailId: "
									+ Long.toString(eachLpnDtl.getLpnDetailId()));
						}
					}

					detailList.add(eachLpnDtl);
				}

				if (!Misc.isNullList(detailList))
					// eachLpn.setLpnDetailSet((Set<LPNDetail>) detailList);
					eachLpn.setLpnDetailSet(new HashSet<LPNDetail>(detailList));
				lpnDataList.add(eachLpn);

			}
			if (!Misc.isNullList(lpnDataList))
				asn.setLPNDataList(lpnDataList);
		}
		isSRLCodeIdEnabled = false;
		isSTYCodeIdEnabled = false;
		isBRDCodeIdEnabled = false;
		super.exportAsnToEEM(asn, msgRefKey, isPOLevelASN, userInfo, msgType);
	}

	/*
	 * protected static HTPCReceivingManagerDAO getHTPCReceivingManagerDAO() {
	 * ApplicationContext ctx = AppContextUtil.getAppCtx("wm.receiving");
	 * HTPCReceivingManagerDAO dao = (HTPCReceivingManagerDAO) ctx
	 * .getBean("receivingManagerDao"); return dao; }
	 */

	protected ISysCodeConfigService getSysCodeConfigManagerBean() {
		ISysCodeConfigService syscodeMgr = null;
		try {
			syscodeMgr = CBOBaseServiceLocator.getSysCodeConfigService();
		} catch (Exception ex) {
			commlog.formatLogDebug("HTPCAsnExportService::getSysCodeConfigManagerBean() caught exception");
		}
		return syscodeMgr;
	}

	protected void getCodeIdMiscFlag() {
		commlog.formatLogDebug("HTPCAsnExportService::getCodeIdMiscFlag Enter ");

		HashMap map = getMiscCustomCode();
		miscFlagForSRL = (String) map.get("SRL");
		miscFlagForSTY = (String) map.get("STY");
		miscFlagForBRD = (String) map.get("BRD");

		if (!Misc.isNullTrimmedString(miscFlagForSRL)
				&& (miscFlagForSRL.substring(0, 1).equalsIgnoreCase("Y") || miscFlagForSRL
						.substring(0, 1).equals("1"))) {
			isSRLCodeIdEnabled = true;
			commlog.formatLogDebug("parameter miscFlagForSRL is Enabled");
		}
		if (!Misc.isNullTrimmedString(miscFlagForSTY)
				&& (miscFlagForSTY.substring(0, 1).equalsIgnoreCase("Y") || miscFlagForSTY
						.substring(0, 1).equals("1"))) {
			isSTYCodeIdEnabled = true;
			commlog.formatLogDebug("parameter miscFlagForSTY is Enabled");
		}
		if (!Misc.isNullTrimmedString(miscFlagForBRD)
				&& (miscFlagForBRD.substring(0, 1).equalsIgnoreCase("Y") || miscFlagForBRD
						.substring(0, 1).equals("1"))) {
			isBRDCodeIdEnabled = true;
			commlog.formatLogDebug("parameter miscFlagForBRD is Enabled");
		}
	}

	protected HashMap getMiscCustomCode() {
		try {
			ISysCodeConfigService syscodeManager = getSysCodeConfigManagerBean();
			HashMap MiscCustomCode = (HashMap) syscodeManager
					.getMiscFlagsFromSysCode("C", "OAI");
			return MiscCustomCode;
		}

		catch (Exception ce) {
			WMDebugLog.DEBUG_LOG
					.logException(WMDebugLog.WM_COMMON_CATEGORY, ce);
			return null;
		}
	}

}

package com.manh.wmos.services.outbound.agile.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.logistics.javalib.JavalibDef;
import com.logistics.javalib.persistence.jdbc.JDBCConnectionCreator;
import com.logistics.javalib.persistence.jdbc.JDBCFunc;
import com.logistics.javalib.util.Misc;
import com.manh.common.fv.SourceType;
import com.manh.lif.util.LIFLogger;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wm.dao.WMDAOImpl;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCAgileEnum;
import com.manh.wmos.services.mheoutbound.dao.IMHEOutboundDAO;
import com.manh.wmos.services.mheoutbound.helper.MheOutboundUtil;
import com.manh.wmos.services.outbound.agile.data.IHTPCWMAgileCommunicationConstants;

/**
 * This class provides Data Base interaction level code for processing response
 * XML messages received from Agile to WM
 * 
 * @author psindhu
 */
public class HTPCAgileOrderLoadRespDAOImpl extends WMDAOImpl implements IHTPCAgileOrderLoadRespDAO
{

	/**
	 * This holds JDBC connection
	 */
	private JDBCConnectionCreator	connCreator;

	/**
	 * Default constructor that initializes the JDBC connection
	 */
	public HTPCAgileOrderLoadRespDAOImpl()
	{
		connCreator = JavalibDef.CONNECTION_CREATOR;

	}

	private IMHEOutboundDAO	mheOutboundDao	= null;

	/*
	 * Fetch out LPN.LPN_FACILITY_STATUS corresponding tc_lpn_id
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * getLpnFacilityStatus(java.lang.String)
	 */
	@Override
	public Short getLpnFacilityStatus(String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter(" tcLpnId :-" + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" select lpn.facilityStatusId ");
		hqlQuery.append(" from com.manh.cbo.transactional.domain.lpn.LPN lpn ");
		hqlQuery.append(" where lpn.tcLpnId = :lpnNumber and lpn.inboundOutboundIndicator='O' ");

		String[] nameList =
		{ "lpnNumber" };
		Object[] parmValueList =
		{ lpnNumber };

		Short lpnFacilityStatus = null;
		try
		{

			List<?> resultList = directQuery(hqlQuery.toString(), nameList, parmValueList);
			if (!Misc.isNullList(resultList))
			{
				lpnFacilityStatus = (Short) resultList.get(0);
				HTPCWMAgileLogHelper.logDebug("LPN_FACILITY_STATUS for lpn : " + lpnNumber + " is :" + lpnFacilityStatus);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Excption while fetching LPN facility status for LPN: " + lpnNumber);
		}
		HTPCWMAgileLogHelper.logExit();
		return lpnFacilityStatus;
	}

	/*
	 * Update EVENT_MESSAGE.ERROR_SEQ_CODE = ‘999’,EVENT_MESSAGE.STAT_CODE =
	 * ‘96’ (Error) for invalid LPN status(>=90)
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * updateEventMessageForFailure(java.lang.String)
	 */
	@Override
	public void updateEventMessageForFailure(String eventMessageId)
	{
		HTPCWMAgileLogHelper.logEnter();
		HTPCWMAgileLogHelper.logDebug("Parameters:- eventMessageId : " + eventMessageId);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" update com.manh.ils.wmservices.hibernate.EventMessage em ");
		hqlQuery.append(" set em.statCode = :statCodeVal, em.errorSeqNbr = :errSeq , em.modDateTime = :modDateTime ");
		hqlQuery.append(" where em.eventMessageId = :eventMessageId ");

		Integer statCodeVal = new Integer(96);

		String[] nameList =
		{ "statCodeVal", "errSeq", "eventMessageId", "modDateTime" };
		Object[] parmValueList =
		{ statCodeVal, HTPCAgileEnum.HTPCErrorSeqNbr.IMPROPER_LPN_FACILTY_STATUS, Integer.parseInt(eventMessageId), new Timestamp(System.currentTimeMillis()) };

		HTPCWMAgileLogHelper.logDebug("Query to Update EVENT_MESSAGE : " + hqlQuery.toString());

		int updatedRecord = 0;
		try
		{
			updatedRecord = directUpdate(hqlQuery.toString(), nameList, parmValueList);
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Exception while updating Freight fields for EVENT_MESSAGE ID : " + eventMessageId);
		}
		HTPCWMAgileLogHelper.logDebug("updated records in EVENT_MESSAGE table :- " + updatedRecord);
		HTPCWMAgileLogHelper.logExit();
	}

	/*
	 * Update EVENT_MESSAGE.ERROR_SEQ_CODE = ‘998’,EVENT_MESSAGE.STAT_CODE =
	 * ‘96’ (Error) for required value missing in Post Shipment
	 * Confirmation(PSC).
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * updateEventMessageForMissingFieldsFailure(java.lang.String)
	 */
	@Override
	public void updateEventMessageForMissingFieldsFailure(String eventMessageId)
	{
		HTPCWMAgileLogHelper.logEnter();
		HTPCWMAgileLogHelper.logDebug("Parameters:- eventMessageId : " + eventMessageId);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" update com.manh.ils.wmservices.hibernate.EventMessage em ");
		hqlQuery.append(" set em.statCode = :statCodeVal, em.errorSeqNbr = :errSeq , em.modDateTime = :modDateTime  ");
		hqlQuery.append(" where em.eventMessageId = :eventMessageId ");

		Integer statCodeVal = new Integer(96);

		String[] nameList =
		{ "statCodeVal", "errSeq", "eventMessageId", "modDateTime" };
		Object[] parmValueList =
		{ statCodeVal, HTPCAgileEnum.HTPCErrorSeqNbr.MISSING_VALUE_IN_PSC.value, Integer.parseInt(eventMessageId), new Timestamp(System.currentTimeMillis()) };

		HTPCWMAgileLogHelper.logDebug("Query to Update EVENT_MESSAGE : " + hqlQuery.toString());

		int updatedRecord = 0;
		try
		{
			updatedRecord = directUpdate(hqlQuery.toString(), nameList, parmValueList);
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Exception while updating Freight fields for EVENT_MESSAGE ID : " + eventMessageId);
		}
		HTPCWMAgileLogHelper.logDebug("updated records in EVENT_MESSAGE table :- " + updatedRecord);
		HTPCWMAgileLogHelper.logExit();
	}

	/*
	 * update lpn corresponding order load message response
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * updateLPNForOrderLoadResponse(java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.Short)
	 */
	@Override
	public void updateLPNForOrderLoadResponse(String trackingNumber, String surCharge, String freightCharge, String insauranceCharge, String codCharge, String shipVia, String actualWeight,
			String lpnRefField10, Short lpnFacilityStatusToUpdate, String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter();
		HTPCWMAgileLogHelper.logDebug("Parameters:- trackingNumber: " + trackingNumber + " surCharge: " + surCharge + " freightCharge: " + freightCharge + "\n insauranceCharge: " + insauranceCharge
				+ " codCharge: " + codCharge + "shipVia: " + shipVia + " actualWeight: " + actualWeight + "lpnRefField10: " + lpnRefField10 + " lpnFacilityStatusToUpdate: "
				+ lpnFacilityStatusToUpdate);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" update com.manh.cbo.transactional.domain.lpn.LPN lpn ");
		hqlQuery.append(" set lpn.commonLPNFields.trackingNbr = :trackingNumber, lpn.refField10String = :lpnRefField10, lpn.manifestLPNFields.addnlOptnChrg = :surCharge,");
		hqlQuery.append(" lpn.manifestLPNFields.freightChrg = :freightCharge, lpn.manifestLPNFields.insurChrg = :insauranceCharge, ");
		hqlQuery.append(" lpn.manifestLPNFields.distChrg = :codCharge, lpn.commonLPNFields.shipVia = :shipVia, ");
		hqlQuery.append(" lpn.manifestLPNFields.actualChrg = :actualWeight, lpn.facilityStatusId = :lpnFacilityStatusToUpdate, ");
		hqlQuery.append(" lpn.commonLPNFields.lastUpdatedDttm  = :lastUpdatedDttm, ");
		hqlQuery.append(" lpn.hibernateVersion = lpn.hibernateVersion+1, ");
		hqlQuery.append(" lpn.commonLPNFields.lastUpdatedSourceType = :lastUpdatedSourceType ");
		hqlQuery.append(" where lpn.tcLpnId = :lpnNumber and lpn.inboundOutboundIndicator='O'");

		String[] nameList =
		{ "trackingNumber", "lpnRefField10", "surCharge", "freightCharge", "insauranceCharge", "codCharge", "shipVia", "actualWeight", "lpnFacilityStatusToUpdate", "lastUpdatedDttm",
				"lastUpdatedSourceType", "lpnNumber" };
		Object[] parmValueList =
		{ trackingNumber, lpnRefField10, Double.parseDouble(surCharge), Double.parseDouble(freightCharge), Double.parseDouble(insauranceCharge), Double.parseDouble(codCharge), shipVia,
				Double.parseDouble(actualWeight), lpnFacilityStatusToUpdate, new Timestamp(System.currentTimeMillis()), Long.parseLong(SourceType.USER.getCode()), lpnNumber };

		HTPCWMAgileLogHelper.logDebug("Query to Update Lpn: " + hqlQuery.toString());

		int updatedRecord = 0;
		try
		{
			updatedRecord = directUpdate(hqlQuery.toString(), nameList, parmValueList);
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Exception while updating fields for oLPN: " + lpnNumber);
		}
		HTPCWMAgileLogHelper.logDebug("updated records in lpn table :- " + updatedRecord);
		HTPCWMAgileLogHelper.logExit();
	}

	/*
	 * Update EVENT_MESSAGE.STAT_CODE = 90
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * updateEventMessageForSucess(java.lang.String)
	 */
	@Override
	public void updateEventMessageForSucess(String eventMessageId)
	{
		HTPCWMAgileLogHelper.logEnter();
		HTPCWMAgileLogHelper.logDebug("Parameters:- eventMessageId : " + eventMessageId);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" update com.manh.ils.wmservices.hibernate.EventMessage em ");
		hqlQuery.append(" set em.statCode = :statCodeVal, em.modDateTime = :modDateTime  ");
		hqlQuery.append(" where em.eventMessageId = :eventMessageId ");

		Integer statCodeVal = new Integer(90);

		String[] nameList =
		{ "statCodeVal", "eventMessageId", "modDateTime" };
		Object[] parmValueList =
		{ statCodeVal, Integer.parseInt(eventMessageId), new Timestamp(System.currentTimeMillis()) };

		HTPCWMAgileLogHelper.logDebug("Query to Update EVENT_MESSAGE : " + hqlQuery.toString());

		int updatedRecord = 0;
		try
		{
			updatedRecord = directUpdate(hqlQuery.toString(), nameList, parmValueList);
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Exception while updating Freight fields for EVENT_MESSAGE ID : " + eventMessageId);
		}
		HTPCWMAgileLogHelper.logDebug("updated records in EVENT_MESSAGE table :- " + updatedRecord);
		HTPCWMAgileLogHelper.logExit();
	}

	/*
	 * If the last LPN holding items for a given order line gets weighed, update
	 * the order line item status (ORDER_LINE_ITEM.DO_DTL_STATUS) = ‘160’
	 * (Weighed).
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * checkAndUpdateOrderLineItem(java.lang.String)
	 */
	@Override
	public void checkAndUpdateOrderLineItem(String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter(" tcLpnId :-" + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" select count(lpn.tcLpnId) ");
		hqlQuery.append(" from com.manh.cbo.transactional.domain.lpn.LPN lpn ");
		hqlQuery.append(" where lpn.facilityStatusId<:facilityStatus ");
		hqlQuery.append(" and lpn.outboundLPNFields.itemId = (select lp.outboundLPNFields.itemId from com.manh.cbo.transactional.domain.lpn.LPN lp  where lp.tcLpnId = :lpnNumber)");
		hqlQuery.append(" and lpn.commonLPNFields.orderId =(select l.commonLPNFields.orderId  from com.manh.cbo.transactional.domain.lpn.LPN l where l.tcLpnId = :lpnNumber)");

		Short facilityStatus = (short) 30;
		String[] nameList =
		{ "facilityStatus", "lpnNumber" };
		Object[] parmValueList =
		{ facilityStatus, lpnNumber };

		Long totalLpnWithSameItemForParticularOrderLessThenWeigh = null;
		try
		{

			@SuppressWarnings("rawtypes")
			List resultList = directQuery(hqlQuery.toString(), nameList, parmValueList);
			if (!Misc.isNullList(resultList))
			{
				totalLpnWithSameItemForParticularOrderLessThenWeigh = (long) resultList.get(0);
				HTPCWMAgileLogHelper.logDebug("totalLpnWithSameItemForParticularOrderLessThenWeigh for lpn : " + lpnNumber + " is :" + totalLpnWithSameItemForParticularOrderLessThenWeigh);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Excption while fetching totalLpnWithSameItemForParticularOrderLessThenWeigh for LPN: " + lpnNumber);
		}

		if (totalLpnWithSameItemForParticularOrderLessThenWeigh == 1)
		{
			StringBuffer hqlUpdateQuery = new StringBuffer();
			hqlUpdateQuery.append(" update com.manh.labelprinting.printing.hibernate.LPOrderLineItem ol ");
			hqlUpdateQuery.append(" set ol.doDtlStatus = :doDtlStatus, ol.lastUpdatedDttm  = :lastUpdatedDttm, ");
			hqlUpdateQuery.append(" ol.hibernateVersion = ol.hibernateVersion+1 ");
			hqlUpdateQuery.append(" where ol.orderId = (select l.commonLPNFields.orderId  from com.manh.cbo.transactional.domain.lpn.LPN l where l.tcLpnId = :lpnNumber) ");
			hqlUpdateQuery.append(" and ol.itemId = (select lp.outboundLPNFields.itemId from com.manh.cbo.transactional.domain.lpn.LPN lp  where lp.tcLpnId = :lpnNumber)) ");

			Integer doDtlStatus = 160;

			String[] nameUpdateList =
			{ "doDtlStatus", "lpnNumber", "lastUpdatedDttm" };
			Object[] parmUpdateValueList =
			{ doDtlStatus, lpnNumber, new Timestamp(System.currentTimeMillis()) };

			HTPCWMAgileLogHelper.logDebug("Query to Update ORDER_LINE_ITEM : " + hqlUpdateQuery.toString());

			int updatedRecord = 0;
			try
			{
				updatedRecord = directUpdate(hqlUpdateQuery.toString(), nameUpdateList, parmUpdateValueList);
				HTPCWMAgileLogHelper.logDebug(" successfully updation the order line item status (ORDER_LINE_ITEM.DO_DTL_STATUS) = ‘160’ (Weighed),  lpnNumber : " + lpnNumber + " updatedRecord : "
						+ updatedRecord);
			}
			catch (Exception e)
			{
				HTPCWMAgileLogHelper.logException(e, "Exception while updating doDtlStatus field for ORDER_LINE_ITEM,  : lpnNumber" + lpnNumber);
			}
		}
		HTPCWMAgileLogHelper.logExit();
	}

	/*
	 * If the LPN is the only LPN for the order in less than weighed status,
	 * update ORDERS.DO_STATUS = ‘160’ (Weighed)
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * checkAndUpdateOrder(java.lang.String)
	 */
	@Override
	public void checkAndUpdateOrder(String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter(" tcLpnId :-" + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" select count(lpn.tcLpnId) ");
		hqlQuery.append(" from com.manh.cbo.transactional.domain.lpn.LPN lpn ");
		hqlQuery.append(" where lpn.facilityStatusId<:facilityStatus ");
		hqlQuery.append(" and lpn.commonLPNFields.orderId =(select l.commonLPNFields.orderId  from com.manh.cbo.transactional.domain.lpn.LPN l where l.tcLpnId = :lpnNumber)");

		Short facilityStatus = (short) 30;
		String[] nameList =
		{ "facilityStatus", "lpnNumber" };
		Object[] parmValueList =
		{ facilityStatus, lpnNumber };

		Long totalLpnForParticularOrderLessThenWeigh = null;
		try
		{

			@SuppressWarnings("rawtypes")
			List resultList = directQuery(hqlQuery.toString(), nameList, parmValueList);
			if (!Misc.isNullList(resultList))
			{
				totalLpnForParticularOrderLessThenWeigh = (long) resultList.get(0);
				HTPCWMAgileLogHelper.logDebug("totalLpnForParticularOrderLessThenWeigh for lpn : " + lpnNumber + " is :" + totalLpnForParticularOrderLessThenWeigh);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Excption while fetching totalLpnForParticularOrderLessThenWeigh for LPN: " + lpnNumber);
		}

		if (totalLpnForParticularOrderLessThenWeigh == 1)
		{
			StringBuffer hqlUpdateQuery = new StringBuffer();
			hqlUpdateQuery.append(" update com.manh.labelprinting.printing.hibernate.LPOrders ord ");
			hqlUpdateQuery.append(" set ord.doStatus = :doStatus, ord.lastUpdatedDttm = :lastUpdatedDttm, ");
			hqlUpdateQuery.append(" ord.hibernateVersion = ord.hibernateVersion+1 ");
			hqlUpdateQuery.append(" where ord.orderId = (select l.commonLPNFields.orderId  from com.manh.cbo.transactional.domain.lpn.LPN l where l.tcLpnId = :lpnNumber) ");

			Integer doStatus = 160;

			String[] nameUpdateList =
			{ "doStatus", "lpnNumber", "lastUpdatedDttm" };
			Object[] parmUpdateValueList =
			{ doStatus, lpnNumber, new Timestamp(System.currentTimeMillis()) };

			HTPCWMAgileLogHelper.logDebug("Query to Update Order : " + hqlUpdateQuery.toString());

			int updatedRecord = 0;
			try
			{
				updatedRecord = directUpdate(hqlUpdateQuery.toString(), nameUpdateList, parmUpdateValueList);
				HTPCWMAgileLogHelper.logDebug(" successfully updation , ORDERS.DO_STATUS = ‘160’ (Weighed) ,  lpnNumber : " + lpnNumber + " updatedRecord : " + updatedRecord);
			}
			catch (Exception e)
			{
				HTPCWMAgileLogHelper.logException(e, "Exception while updating doStatus field for ORDERS,  : lpnNumber" + lpnNumber);
			}
		}
		HTPCWMAgileLogHelper.logExit();
	}

	/*
	 * update lpn corresponding void message response
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * updateLPNForOrderLoadResponse(java.lang.String)
	 */
	@Override
	public void updateLPNForVoidResponse(String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter();
		HTPCWMAgileLogHelper.logDebug("Parameters:- lpnNumber: " + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" update com.manh.cbo.transactional.domain.lpn.LPN lpn ");
		hqlQuery.append(" set lpn.commonLPNFields.trackingNbr = :trackingNumber, lpn.refField10String = :lpnRefField10, ");
		hqlQuery.append(" lpn.facilityStatusId = :lpnFacilityStatusToUpdate, ");
		hqlQuery.append(" lpn.commonLPNFields.lastUpdatedDttm  = :lastUpdatedDttm, ");
		hqlQuery.append(" lpn.hibernateVersion = lpn.hibernateVersion+1, ");
		hqlQuery.append(" lpn.commonLPNFields.lastUpdatedSourceType = :lastUpdatedSourceType ");
		hqlQuery.append(" where lpn.tcLpnId = :lpnNumber and lpn.inboundOutboundIndicator='O'");

		String trackingNumber = null;
		String lpnRefField10 = "V";
		Short lpnFacilityStatusToUpdate = (short) 20;

		String[] nameList =
		{ "trackingNumber", "lpnRefField10", "lpnFacilityStatusToUpdate", "lastUpdatedDttm", "lastUpdatedSourceType", "lpnNumber" };
		Object[] parmValueList =
		{ trackingNumber, lpnRefField10, lpnFacilityStatusToUpdate, new Timestamp(System.currentTimeMillis()), Long.parseLong(SourceType.USER.getCode()), lpnNumber };

		HTPCWMAgileLogHelper.logDebug("Query to Update Lpn: " + hqlQuery.toString());

		int updatedRecord = 0;
		try
		{
			updatedRecord = directUpdate(hqlQuery.toString(), nameList, parmValueList);
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Exception while updating fields for oLPN: " + lpnNumber);
		}
		HTPCWMAgileLogHelper.logDebug("updated records in lpn table :- " + updatedRecord);
		HTPCWMAgileLogHelper.logExit();
	}

	/*
	 * fetch for LPN.MISC_INSTR_CODE_1
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * getLpnMiscInstrCode(java.lang.String)
	 */
	@Override
	public String getLpnMiscInstrCode(String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter(" tcLpnId :-" + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" select lpn.commonLPNFields.miscInstrCode1 ");
		hqlQuery.append(" from com.manh.cbo.transactional.domain.lpn.LPN lpn ");
		hqlQuery.append(" where lpn.tcLpnId = :lpnNumber and lpn.inboundOutboundIndicator='O' ");

		String[] nameList =
		{ "lpnNumber" };
		Object[] parmValueList =
		{ lpnNumber };

		String miscInstrCode1 = null;
		try
		{

			List<?> resultList = directQuery(hqlQuery.toString(), nameList, parmValueList);
			if (!Misc.isNullList(resultList))
			{
				miscInstrCode1 = (String) resultList.get(0);
				HTPCWMAgileLogHelper.logDebug("MISC_INSTR_CODE_1 for lpn : " + lpnNumber + " is :" + miscInstrCode1);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Excption while fetching miscInstrCode1 for LPN: " + lpnNumber);
		}
		HTPCWMAgileLogHelper.logExit();
		return miscInstrCode1;
	}

	/*
	 * fetch LPN's cFacilityId
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * getLpncFacilityId(java.lang.String)
	 */
	@Override
	public Long getLpncFacilityId(String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter(" tcLpnId :-" + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" select lpn.commonLPNFields.currentFacilityId ");
		hqlQuery.append(" from com.manh.cbo.transactional.domain.lpn.LPN lpn ");
		hqlQuery.append(" where lpn.tcLpnId = :lpnNumber and lpn.inboundOutboundIndicator='O' ");

		String[] nameList =
		{ "lpnNumber" };
		Object[] parmValueList =
		{ lpnNumber };

		Long currentFacilityId = null;
		try
		{

			List<?> resultList = directQuery(hqlQuery.toString(), nameList, parmValueList);
			if (!Misc.isNullList(resultList))
			{
				currentFacilityId = (Long) resultList.get(0);
				HTPCWMAgileLogHelper.logDebug("currentFacilityId for lpn : " + lpnNumber + " is :" + currentFacilityId);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Excption while fetching currentFacilityId for LPN: " + lpnNumber);
		}
		HTPCWMAgileLogHelper.logExit();
		return currentFacilityId;
	}

	/*
	 * fetch LPN's tcCompanyId
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * getTcCompanmyIdForLPN(java.lang.String)
	 */
	@Override
	public int getTcCompanmyIdForLPN(String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter(" tcLpnId :-" + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" select lpn.tcCompanyId ");
		hqlQuery.append(" from com.manh.cbo.transactional.domain.lpn.LPN lpn ");
		hqlQuery.append(" where lpn.tcLpnId = :lpnNumber and lpn.inboundOutboundIndicator='O' ");

		String[] nameList =
		{ "lpnNumber" };
		Object[] parmValueList =
		{ lpnNumber };

		int tcCompanyId = -1;
		try
		{

			@SuppressWarnings("rawtypes")
			List resultList = directQuery(hqlQuery.toString(), nameList, parmValueList);
			if (!Misc.isNullList(resultList))
			{
				tcCompanyId = (int) resultList.get(0);
				HTPCWMAgileLogHelper.logDebug("tcCompanyId for lpn : " + lpnNumber + " is :" + tcCompanyId);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Excption while fetching tcCompanyId for LPN: " + lpnNumber);
		}
		HTPCWMAgileLogHelper.logExit();
		return tcCompanyId;
	}

	/*
	 * fetxh lpn's "lastUpdatedSource"
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * getLpnLastUpdatedSource(java.lang.String)
	 */
	@Override
	public String getLpnLastUpdatedSource(String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter(" tcLpnId :-" + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" select lpn.commonLPNFields.lastUpdatedSource ");
		hqlQuery.append(" from com.manh.cbo.transactional.domain.lpn.LPN lpn ");
		hqlQuery.append(" where lpn.tcLpnId = :lpnNumber and lpn.inboundOutboundIndicator='O' ");

		String[] nameList =
		{ "lpnNumber" };
		Object[] parmValueList =
		{ lpnNumber };

		String lastUpdatedSource = null;
		try
		{

			List<?> resultList = directQuery(hqlQuery.toString(), nameList, parmValueList);
			if (!Misc.isNullList(resultList))
			{
				lastUpdatedSource = (String) resultList.get(0);
				HTPCWMAgileLogHelper.logDebug("lastUpdatedSource for lpn : " + lpnNumber + " is :" + lastUpdatedSource);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Excption while fetching lastUpdatedSource for LPN: " + lpnNumber);
		}
		HTPCWMAgileLogHelper.logExit();
		return lastUpdatedSource;
	}

	/*
	 * If the order line associated with the LPN is in weighed status, the order
	 * line status needs to be updated back to packed status
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * checkAndUpdateOrderLineItemForVoidResponse(java.lang.String)
	 */
	@Override
	public void checkAndUpdateOrderLineItemForVoidResponse(String lpnNumber)
	{

		HTPCWMAgileLogHelper.logEnter(" tcLpnId :-" + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" select ol.doDtlStatus ");
		hqlQuery.append(" from com.manh.labelprinting.printing.hibernate.LPOrderLineItem ol ");
		hqlQuery.append(" where ol.itemId = (select lp.outboundLPNFields.itemId from com.manh.cbo.transactional.domain.lpn.LPN lp  where lp.tcLpnId = :lpnNumber)");
		hqlQuery.append(" and ol.orderId =(select l.commonLPNFields.orderId  from com.manh.cbo.transactional.domain.lpn.LPN l where l.tcLpnId = :lpnNumber)");

		String[] nameList =
		{ "lpnNumber" };
		Object[] parmValueList =
		{ lpnNumber };

		Integer doDtlStatus = null;
		try
		{

			List<?> resultList = directQuery(hqlQuery.toString(), nameList, parmValueList);
			if (!Misc.isNullList(resultList))
			{
				doDtlStatus = (Integer) resultList.get(0);
				HTPCWMAgileLogHelper.logDebug("doDtlStatus of OrderLineItem for lpn : " + lpnNumber + " is :" + doDtlStatus);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Excption while fetching doDtlStatus of OrderLineItem for LPN: " + lpnNumber);
		}

		Integer doDtlStatusWeigh = 160;
		if (doDtlStatus != null && doDtlStatus.equals(doDtlStatusWeigh))
		{
			StringBuffer hqlUpdateQuery = new StringBuffer();
			hqlUpdateQuery.append(" update com.manh.labelprinting.printing.hibernate.LPOrderLineItem ol ");
			hqlUpdateQuery.append(" set ol.doDtlStatus = :doDtlStatusPacked, ol.lastUpdatedDttm  = :lastUpdatedDttm, ");
			hqlUpdateQuery.append(" ol.hibernateVersion = ol.hibernateVersion+1 ");
			hqlUpdateQuery.append(" where ol.orderId = (select l.commonLPNFields.orderId  from com.manh.cbo.transactional.domain.lpn.LPN l where l.tcLpnId = :lpnNumber) ");
			hqlUpdateQuery.append(" and ol.itemId = (select lp.outboundLPNFields.itemId from com.manh.cbo.transactional.domain.lpn.LPN lp  where lp.tcLpnId = :lpnNumber)) ");

			Integer doDtlStatusPacked = 150;

			String[] nameUpdateList =
			{ "doDtlStatusPacked", "lpnNumber", "lastUpdatedDttm" };
			Object[] parmUpdateValueList =
			{ doDtlStatusPacked, lpnNumber, new Timestamp(System.currentTimeMillis()) };

			HTPCWMAgileLogHelper.logDebug("Query to Update OrderLineItem : " + hqlUpdateQuery.toString());

			int updatedRecord = 0;
			try
			{
				updatedRecord = directUpdate(hqlUpdateQuery.toString(), nameUpdateList, parmUpdateValueList);
				HTPCWMAgileLogHelper.logDebug(" successfully updation the order line item status (ORDER_LINE_ITEM.DO_DTL_STATUS) = ‘150’ (Packed),  lpnNumber : " + lpnNumber + " updatedRecord : "
						+ updatedRecord);
			}
			catch (Exception e)
			{
				HTPCWMAgileLogHelper.logException(e, "Exception while updating doDtlStatus field for ORDER_LINE_ITEM,  : lpnNumber" + lpnNumber);
			}
		}
		HTPCWMAgileLogHelper.logExit();

	}

	/*
	 * If the order for the LPN (LPN.TC_ORDER_ID) is in weighed status, the
	 * order needs be updated back to packed status (ORDERS.DO_STATUS = 150)
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * checkAndUpdateOrderForVoidResponse(java.lang.String)
	 */
	@Override
	public void checkAndUpdateOrderForVoidResponse(String lpnNumber)
	{
		HTPCWMAgileLogHelper.logEnter(" tcLpnId :-" + lpnNumber);

		StringBuffer hqlQuery = new StringBuffer();
		hqlQuery.append(" select ord.doStatus ");
		hqlQuery.append(" from com.manh.labelprinting.printing.hibernate.LPOrders ord ");
		hqlQuery.append(" where ord.orderId =(select l.commonLPNFields.orderId  from com.manh.cbo.transactional.domain.lpn.LPN l where l.tcLpnId = :lpnNumber)");

		String[] nameList =
		{ "lpnNumber" };
		Object[] parmValueList =
		{ lpnNumber };

		Integer doStatus = null;
		try
		{

			List<?> resultList = directQuery(hqlQuery.toString(), nameList, parmValueList);
			if (!Misc.isNullList(resultList))
			{
				doStatus = (Integer) resultList.get(0);
				HTPCWMAgileLogHelper.logDebug("ORDERS.doStatus for lpn : " + lpnNumber + " is :" + doStatus);
			}
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e, "Excption while fetching ORDERS.doStatus for LPN: " + lpnNumber);
		}

		Integer doStatusWeigh = 160;

		if (doStatus != null && doStatus.equals(doStatusWeigh))
		{
			StringBuffer hqlUpdateQuery = new StringBuffer();
			hqlUpdateQuery.append(" update com.manh.labelprinting.printing.hibernate.LPOrders ord ");
			hqlUpdateQuery.append(" set ord.doStatus = :doStatusPacked, ord.lastUpdatedDttm = :lastUpdatedDttm, ");
			hqlUpdateQuery.append(" ord.hibernateVersion = ord.hibernateVersion+1 ");
			hqlUpdateQuery.append(" where ord.orderId = (select l.commonLPNFields.orderId  from com.manh.cbo.transactional.domain.lpn.LPN l where l.tcLpnId = :lpnNumber) ");

			Integer doStatusPacked = 150;

			String[] nameUpdateList =
			{ "doStatusPacked", "lpnNumber", "lastUpdatedDttm" };
			Object[] parmUpdateValueList =
			{ doStatusPacked, lpnNumber, new Timestamp(System.currentTimeMillis()) };

			HTPCWMAgileLogHelper.logDebug("Query to Update Order : " + hqlUpdateQuery.toString());

			int updatedRecord = 0;
			try
			{
				updatedRecord = directUpdate(hqlUpdateQuery.toString(), nameUpdateList, parmUpdateValueList);
				HTPCWMAgileLogHelper.logDebug(" successfully updation , ORDERS.DO_STATUS = ‘150’ (Packed) ,  lpnNumber : " + lpnNumber + " updatedRecord : " + updatedRecord);
			}
			catch (Exception e)
			{
				HTPCWMAgileLogHelper.logException(e, "Exception while updating doStatus field for ORDERS,  : lpnNumber" + lpnNumber);
			}
		}
		HTPCWMAgileLogHelper.logExit();
	}

	/*
	 * tran_log entry for void response
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * tranLogEntryForVoidResponse()
	 */
	@Override
	public int tranLogEntryForVoidResponse(HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter();

		String sqlQuery = " INSERT INTO TRAN_LOG "
				+ "(TRAN_LOG_ID, SEQUENCE_NUMBER, MESSAGE_ID, ROUTER_ID, ORIG_PROCESS_DATE, DEST_PROCESS_DATE, DIRECTION,"
				+ "ORIGIN_ID, ORIGIN_TYPE, ORIGIN_FORMAT, MSG_TYPE, ORIG_COMPANY,"
				+ "DEST_COMPANY, DEST_ID, DEST_TYPE, DEST_FORMAT, ADDL_CRITERIA_EXPR, ADDL_CRITERIA_VALUE, "
				+ "OBJECT_ID, OBJECT_TYPE, USER_ID,BATCH_ID, REFERENCE_ID, ACTION_TYPE, MSG_LOCALE, VERSION, INTERNAL_REFERENCE_ID, "
				+ "INTERNAL_DATE_TIME_STAMP, EXTERNAL_REFERENCE_ID_TYPE, EXTERNAL_REFERENCE_ID, EXTERNAL_DATE_TIME_STAMP, RESULT_CODE, ERROR_DETAILS, "
				+ "PROCESSED_AT, HAS_ERRORS, BATCH_SIZE) "
				+ "VALUES (SEQ_TRAN_LOG_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try
		{
			return createOrUpdateTranLogForVoidResponse(sqlQuery, request);
		}
		catch (Exception e)
		{
			LIFLogger.logError("Exception while saving TranLog entry", e);
		}
		HTPCWMAgileLogHelper.logExit("successfully inserted into tran_log");
		return -1;
	}

	/**
	 * Creates or updates the TRAN_LOG entry
	 * 
	 * @param sqlQuery
	 * @return
	 */
	private int createOrUpdateTranLogForVoidResponse(String sqlQuery, HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter();
		PreparedStatement stmt = null;
		Connection conn = null;
		int recordInserted = 0;
		try
		{
			conn = connCreator.createConnection();
			stmt = conn.prepareStatement(sqlQuery);
			String userId = "";

			int i = 1;
			stmt.setString(i++, ""); // ROUTER_ID

			Timestamp origDate = new Timestamp(System.currentTimeMillis());
			Timestamp destDate = new Timestamp(System.currentTimeMillis());
			if (origDate != null)
			{
				stmt.setTimestamp(i++, origDate); // ORIG_PROCESS_DATE
			}

			if (destDate != null)
			{
				stmt.setTimestamp(i++, destDate); // DEST_PROCESS_DATE
			}
			stmt.setString(i++, "I"); // DIRECTION

			stmt.setString(i++, Misc.EMPTY_STRING); // ORIGIN_ID
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.AGILE); // ORIGIN_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // ORIGIN_FORMAT
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.AGILE_VOID); // MSG_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // ORIG_COMPANY
			stmt.setString(i++, Misc.EMPTY_STRING); // DEST_COMPANY
			stmt.setString(i++, Misc.EMPTY_STRING); // DEST_ID
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.WM); // DEST_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // DEST_FORMAT
			stmt.setString(i++, Misc.EMPTY_STRING); // ADDL_CRITERIA_EXPR
			stmt.setString(i++, Misc.EMPTY_STRING); // ADDL_CRITERIA_VALUE

			// LAST_UPDATED_DTTM - that is inserted by database
			stmt.setString(i++, Misc.EMPTY_STRING); // OBJECT_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // OBJECT_TYPE
			stmt.setString(i++, userId); // USER_ID

			/*
			 * stmt.setString(i++, Misc.EMPTY_STRING); // TRAN_LOG_LEVEL
			 * stmt.setString(i++, Misc.EMPTY_STRING);// IS_MSG_STORED
			 */

			// because we we have a prepared statement, we need to always set
			// all the parameters before executing it

			stmt.setString(i++, Misc.EMPTY_STRING); // BATCH_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // REFERENCE_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // ACTION_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // MSG_LOCALE
			stmt.setString(i++, Misc.EMPTY_STRING); // VERSION
			stmt.setString(i++, Misc.EMPTY_STRING); // INTERNAL_REFERENCE_ID

			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis())); // INTERNAL_DATE_TIME_STAMP

			stmt.setString(i++, Misc.EMPTY_STRING); // EXTERNAL_REFERENCE_ID_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // EXTERNAL_REFERENCE_ID
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis())); // EXTERNAL_DATE_TIME_STAMP

			stmt.setInt(i++, 0); // RESULT_CODE
			stmt.setString(i++, Misc.EMPTY_STRING); // ERROR_DETAILS

			String processingHost = request.getServerName() + ":" + request.getServerPort();
			stmt.setString(i++, processingHost); // PROCESSED_AT
			stmt.setInt(i++, 0); // HAS_ERRORS
			stmt.setInt(i++, 0); // BATCH_SIZE

			recordInserted = stmt.executeUpdate();
			
			String lastTranLogId = "select SEQ_TRAN_LOG_ID.currval from dual";
			PreparedStatement stmt2 = conn.prepareStatement(lastTranLogId);
			ResultSet rs = stmt2.executeQuery();
			rs.next();
			int tranLogId = rs.getInt(1);
			stmt2.close();
			HTPCWMAgileLogHelper.logExit("HTPCAgileOrderLoadRespDAOImpl::createOrUpdateTranLogForVoidResponse : tran_log_id received is : " + tranLogId);
			return tranLogId;
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e);
		}
		finally
		{
			JDBCFunc.closeJDBCResources(stmt, conn);
			HTPCWMAgileLogHelper.logExit(" records inserted into tran_log int recordInserted : " + recordInserted);
		}
		HTPCWMAgileLogHelper.logExit("createOrUpdateTranLogForVoidResponse :: After entering record in tran_log, could not fetch the tran_log_id, returning -1.");
		return -1;
	}

	/*
	 * Creates or updates the TRAN_LOG entry
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * tranLogEntryForOrderLoadRequest()
	 */
	@Override
	public int tranLogEntryForOrderLoadRequest(HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter();

		String sqlQuery = " INSERT INTO TRAN_LOG "
				+ "(TRAN_LOG_ID, SEQUENCE_NUMBER, MESSAGE_ID, ROUTER_ID, ORIG_PROCESS_DATE, DEST_PROCESS_DATE, DIRECTION,"
				+ "ORIGIN_ID, ORIGIN_TYPE, ORIGIN_FORMAT, MSG_TYPE, ORIG_COMPANY,"
				+ "DEST_COMPANY, DEST_ID, DEST_TYPE, DEST_FORMAT, ADDL_CRITERIA_EXPR, ADDL_CRITERIA_VALUE, "
				+ "OBJECT_ID, OBJECT_TYPE, USER_ID,BATCH_ID, REFERENCE_ID, ACTION_TYPE, MSG_LOCALE, VERSION, INTERNAL_REFERENCE_ID, "
				+ "INTERNAL_DATE_TIME_STAMP, EXTERNAL_REFERENCE_ID_TYPE, EXTERNAL_REFERENCE_ID, EXTERNAL_DATE_TIME_STAMP, RESULT_CODE, ERROR_DETAILS, "
				+ "PROCESSED_AT, HAS_ERRORS, BATCH_SIZE) "
				+ "VALUES (SEQ_TRAN_LOG_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try
		{
			return createOrUpdateTranLogForOrderLoadRequest(sqlQuery, request);
		}
		catch (Exception e)
		{
			LIFLogger.logError("Exception while saving TranLog entry", e);
		}
		HTPCWMAgileLogHelper.logExit("successfully inserted into tran_log");
		return -1;
	}

	/**
	 * Creates or updates the TRAN_LOG entry
	 * 
	 * @param sqlQuery
	 * @return
	 */
	private int createOrUpdateTranLogForOrderLoadRequest(String sqlQuery, HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter();
		PreparedStatement stmt = null;
		Connection conn = null;
		int recordInserted = 0;
		try
		{
			conn = connCreator.createConnection();
			stmt = conn.prepareStatement(sqlQuery);
			String userId = "";

			int i = 1;
			stmt.setString(i++, ""); // ROUTER_ID

			Timestamp origDate = new Timestamp(System.currentTimeMillis());
			Timestamp destDate = new Timestamp(System.currentTimeMillis());
			if (origDate != null)
			{
				stmt.setTimestamp(i++, origDate); // ORIG_PROCESS_DATE
			}

			if (destDate != null)
			{
				stmt.setTimestamp(i++, destDate); // DEST_PROCESS_DATE
			}
			stmt.setString(i++, "I"); // DIRECTION

			stmt.setString(i++, Misc.EMPTY_STRING); // ORIGIN_ID
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.AGILE); // ORIGIN_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // ORIGIN_FORMAT
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.AGILE_RATING); // MSG_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // ORIG_COMPANY
			stmt.setString(i++, Misc.EMPTY_STRING); // DEST_COMPANY
			stmt.setString(i++, Misc.EMPTY_STRING); // DEST_ID
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.WM); // DEST_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // DEST_FORMAT
			stmt.setString(i++, Misc.EMPTY_STRING); // ADDL_CRITERIA_EXPR
			stmt.setString(i++, Misc.EMPTY_STRING); // ADDL_CRITERIA_VALUE

			// LAST_UPDATED_DTTM - that is inserted by database
			stmt.setString(i++, Misc.EMPTY_STRING); // OBJECT_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // OBJECT_TYPE
			stmt.setString(i++, userId); // USER_ID
			/*
			 * stmt.setString(i++,"Always"); // TRAN_LOG_LEVEL
			 * stmt.setString(i++, Misc.EMPTY_STRING);// IS_MSG_STORED
			 */

			// because we we have a prepared statement, we need to always set
			// all the parameters before executing it

			stmt.setString(i++, Misc.EMPTY_STRING); // BATCH_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // REFERENCE_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // ACTION_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // MSG_LOCALE
			stmt.setString(i++, Misc.EMPTY_STRING); // VERSION
			stmt.setString(i++, Misc.EMPTY_STRING); // INTERNAL_REFERENCE_ID

			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis())); // INTERNAL_DATE_TIME_STAMP

			stmt.setString(i++, Misc.EMPTY_STRING); // EXTERNAL_REFERENCE_ID_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // EXTERNAL_REFERENCE_ID
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis())); // EXTERNAL_DATE_TIME_STAMP

			stmt.setInt(i++, 0); // RESULT_CODE
			stmt.setString(i++, Misc.EMPTY_STRING); // ERROR_DETAILS

			String processingHost = request.getServerName() + ":" + request.getServerPort();
			stmt.setString(i++, processingHost); // PROCESSED_AT
			stmt.setInt(i++, 0); // HAS_ERRORS
			stmt.setInt(i++, 0); // BATCH_SIZE

			recordInserted = stmt.executeUpdate();

			String lastTranLogId = "select SEQ_TRAN_LOG_ID.currval from dual";
			PreparedStatement stmt2 = conn.prepareStatement(lastTranLogId);
			ResultSet rs = stmt2.executeQuery();
			rs.next();
			int tranLogId = rs.getInt(1);
			stmt2.close();
			HTPCWMAgileLogHelper.logExit("HTPCAgileOrderLoadRespDAOImpl::createOrUpdateTranLogForOrderLoadRequest : tran_log_id received is : " + tranLogId);
			return tranLogId;

		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e);
		}
		finally
		{
			JDBCFunc.closeJDBCResources(stmt, conn);
			HTPCWMAgileLogHelper.logExit(" records inserted into tran_log int recordInserted : " + recordInserted);
		}
		HTPCWMAgileLogHelper.logExit("createOrUpdateTranLogForOrderLoadRequest :: After entering record in tran_log, could not fetch the tran_log_id, returning -1.");
		return -1;
	}

	/*
	 * tran_log entry for void response Acknowledgment from WM to Agile
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * tranLogEntryForVoidResponseAck()
	 */
	@Override
	public void tranLogEntryForVoidResponseAck(HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter();

		String sqlQuery = " INSERT INTO TRAN_LOG "
				+ "(TRAN_LOG_ID, SEQUENCE_NUMBER, MESSAGE_ID, ROUTER_ID, ORIG_PROCESS_DATE, DEST_PROCESS_DATE, DIRECTION,"
				+ "ORIGIN_ID, ORIGIN_TYPE, ORIGIN_FORMAT, MSG_TYPE, ORIG_COMPANY,"
				+ "DEST_COMPANY, DEST_ID, DEST_TYPE, DEST_FORMAT, ADDL_CRITERIA_EXPR, ADDL_CRITERIA_VALUE, "
				+ "OBJECT_ID, OBJECT_TYPE, USER_ID,BATCH_ID, REFERENCE_ID, ACTION_TYPE, MSG_LOCALE, VERSION, INTERNAL_REFERENCE_ID, "
				+ "INTERNAL_DATE_TIME_STAMP, EXTERNAL_REFERENCE_ID_TYPE, EXTERNAL_REFERENCE_ID, EXTERNAL_DATE_TIME_STAMP, RESULT_CODE, ERROR_DETAILS, "
				+ "PROCESSED_AT, HAS_ERRORS, BATCH_SIZE) "
				+ "VALUES (SEQ_TRAN_LOG_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try
		{
			createOrUpdateTranLogForVoidResponseAck(sqlQuery, request);
		}
		catch (Exception e)
		{
			LIFLogger.logError("Exception while saving TranLog entry", e);
		}
		HTPCWMAgileLogHelper.logExit("successfully inserted into tran_log");
	}

	/**
	 * tran_log entry for void response Acknowledgment from WM to Agile
	 * 
	 * @param sqlQuery
	 */
	private void createOrUpdateTranLogForVoidResponseAck(String sqlQuery, HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter();
		PreparedStatement stmt = null;
		Connection conn = null;
		int recordInserted = 0;
		try
		{
			conn = connCreator.createConnection();
			stmt = conn.prepareStatement(sqlQuery);
			String userId = "";

			int i = 1;
			stmt.setString(i++, ""); // ROUTER_ID

			Timestamp origDate = new Timestamp(System.currentTimeMillis());
			Timestamp destDate = new Timestamp(System.currentTimeMillis());
			if (origDate != null)
			{
				stmt.setTimestamp(i++, origDate); // ORIG_PROCESS_DATE
			}

			if (destDate != null)
			{
				stmt.setTimestamp(i++, destDate); // DEST_PROCESS_DATE
			}
			stmt.setString(i++, "I"); // DIRECTION

			stmt.setString(i++, Misc.EMPTY_STRING); // ORIGIN_ID
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.AGILE); // ORIGIN_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // ORIGIN_FORMAT
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.AGILE_VOID_ACK); // MSG_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // ORIG_COMPANY
			stmt.setString(i++, Misc.EMPTY_STRING); // DEST_COMPANY
			stmt.setString(i++, Misc.EMPTY_STRING); // DEST_ID
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.WM); // DEST_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // DEST_FORMAT
			stmt.setString(i++, Misc.EMPTY_STRING); // ADDL_CRITERIA_EXPR
			stmt.setString(i++, Misc.EMPTY_STRING); // ADDL_CRITERIA_VALUE

			// LAST_UPDATED_DTTM - that is inserted by database
			stmt.setString(i++, Misc.EMPTY_STRING); // OBJECT_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // OBJECT_TYPE
			stmt.setString(i++, userId); // USER_ID

			/*
			 * stmt.setString(i++, Misc.EMPTY_STRING); // TRAN_LOG_LEVEL
			 * stmt.setString(i++, Misc.EMPTY_STRING);// IS_MSG_STORED
			 */

			// because we we have a prepared statement, we need to always set
			// all the parameters before executing it

			stmt.setString(i++, Misc.EMPTY_STRING); // BATCH_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // REFERENCE_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // ACTION_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // MSG_LOCALE
			stmt.setString(i++, Misc.EMPTY_STRING); // VERSION
			stmt.setString(i++, Misc.EMPTY_STRING); // INTERNAL_REFERENCE_ID

			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis())); // INTERNAL_DATE_TIME_STAMP

			stmt.setString(i++, Misc.EMPTY_STRING); // EXTERNAL_REFERENCE_ID_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // EXTERNAL_REFERENCE_ID
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis())); // EXTERNAL_DATE_TIME_STAMP

			stmt.setInt(i++, 0); // RESULT_CODE
			stmt.setString(i++, Misc.EMPTY_STRING); // ERROR_DETAILS

			String processingHost = request.getServerName() + ":" + request.getServerPort();
			stmt.setString(i++, processingHost); // PROCESSED_AT
			stmt.setInt(i++, 0); // HAS_ERRORS
			stmt.setInt(i++, 0); // BATCH_SIZE

			recordInserted = stmt.executeUpdate();
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e);
		}
		finally
		{
			JDBCFunc.closeJDBCResources(stmt, conn);
			HTPCWMAgileLogHelper.logExit(" records inserted into tran_log int recordInserted : " + recordInserted);
		}
	}

	/*
	 * tran_log entry for order load response Acknowledgment from WM to Agile
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.manh.wmos.services.outbound.agile.dao.IHTPCAgileOrderLoadRespDAO#
	 * tranLogEntryForOrderLoadRequestAck()
	 */
	@Override
	public void tranLogEntryForOrderLoadRequestAck(HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter();

		String sqlQuery = " INSERT INTO TRAN_LOG "
				+ "(TRAN_LOG_ID, SEQUENCE_NUMBER, MESSAGE_ID, ROUTER_ID, ORIG_PROCESS_DATE, DEST_PROCESS_DATE, DIRECTION,"
				+ "ORIGIN_ID, ORIGIN_TYPE, ORIGIN_FORMAT, MSG_TYPE, ORIG_COMPANY,"
				+ "DEST_COMPANY, DEST_ID, DEST_TYPE, DEST_FORMAT, ADDL_CRITERIA_EXPR, ADDL_CRITERIA_VALUE, "
				+ "OBJECT_ID, OBJECT_TYPE, USER_ID,BATCH_ID, REFERENCE_ID, ACTION_TYPE, MSG_LOCALE, VERSION, INTERNAL_REFERENCE_ID, "
				+ "INTERNAL_DATE_TIME_STAMP, EXTERNAL_REFERENCE_ID_TYPE, EXTERNAL_REFERENCE_ID, EXTERNAL_DATE_TIME_STAMP, RESULT_CODE, ERROR_DETAILS, "
				+ "PROCESSED_AT, HAS_ERRORS, BATCH_SIZE) "
				+ "VALUES (SEQ_TRAN_LOG_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try
		{
			createOrUpdateTranLogForOrderLoadRequestAck(sqlQuery, request);
		}
		catch (Exception e)
		{
			LIFLogger.logError("Exception while saving TranLog entry", e);
		}
		HTPCWMAgileLogHelper.logExit("successfully inserted into tran_log");
	}

	/**
	 * tran_log entry for order load response Acknowledgment from WM to Agile
	 * 
	 * @param sqlQuery
	 */
	private void createOrUpdateTranLogForOrderLoadRequestAck(String sqlQuery, HttpServletRequest request)
	{
		HTPCWMAgileLogHelper.logEnter();
		PreparedStatement stmt = null;
		Connection conn = null;
		int recordInserted = 0;
		try
		{
			conn = connCreator.createConnection();
			stmt = conn.prepareStatement(sqlQuery);
			String userId = "";

			int i = 1;
			stmt.setString(i++, ""); // ROUTER_ID

			Timestamp origDate = new Timestamp(System.currentTimeMillis());
			Timestamp destDate = new Timestamp(System.currentTimeMillis());
			if (origDate != null)
			{
				stmt.setTimestamp(i++, origDate); // ORIG_PROCESS_DATE
			}

			if (destDate != null)
			{
				stmt.setTimestamp(i++, destDate); // DEST_PROCESS_DATE
			}
			stmt.setString(i++, "I"); // DIRECTION

			stmt.setString(i++, Misc.EMPTY_STRING); // ORIGIN_ID
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.AGILE); // ORIGIN_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // ORIGIN_FORMAT
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.AGILE_RATING_ACK); // MSG_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // ORIG_COMPANY
			stmt.setString(i++, Misc.EMPTY_STRING); // DEST_COMPANY
			stmt.setString(i++, Misc.EMPTY_STRING); // DEST_ID
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.WM); // DEST_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // DEST_FORMAT
			stmt.setString(i++, Misc.EMPTY_STRING); // ADDL_CRITERIA_EXPR
			stmt.setString(i++, Misc.EMPTY_STRING); // ADDL_CRITERIA_VALUE

			// LAST_UPDATED_DTTM - that is inserted by database
			stmt.setString(i++, Misc.EMPTY_STRING); // OBJECT_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // OBJECT_TYPE
			stmt.setString(i++, userId); // USER_ID

			/*
			 * stmt.setString(i++, Misc.EMPTY_STRING); // TRAN_LOG_LEVEL
			 * stmt.setString(i++, Misc.EMPTY_STRING);// IS_MSG_STORED
			 */

			// because we we have a prepared statement, we need to always set
			// all the parameters before executing it

			stmt.setString(i++, Misc.EMPTY_STRING); // BATCH_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // REFERENCE_ID
			stmt.setString(i++, Misc.EMPTY_STRING); // ACTION_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // MSG_LOCALE
			stmt.setString(i++, Misc.EMPTY_STRING); // VERSION
			stmt.setString(i++, Misc.EMPTY_STRING); // INTERNAL_REFERENCE_ID

			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis())); // INTERNAL_DATE_TIME_STAMP

			stmt.setString(i++, Misc.EMPTY_STRING); // EXTERNAL_REFERENCE_ID_TYPE
			stmt.setString(i++, Misc.EMPTY_STRING); // EXTERNAL_REFERENCE_ID
			stmt.setTimestamp(i++, new Timestamp(System.currentTimeMillis())); // EXTERNAL_DATE_TIME_STAMP

			stmt.setInt(i++, 0); // RESULT_CODE
			stmt.setString(i++, Misc.EMPTY_STRING); // ERROR_DETAILS

			String processingHost = request.getServerName() + ":" + request.getServerPort();
			stmt.setString(i++, processingHost); // PROCESSED_AT
			stmt.setInt(i++, 0); // HAS_ERRORS
			stmt.setInt(i++, 0); // BATCH_SIZE

			recordInserted = stmt.executeUpdate();
		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e);
		}
		finally
		{
			JDBCFunc.closeJDBCResources(stmt, conn);
			HTPCWMAgileLogHelper.logExit(" records inserted into tran_log int recordInserted : " + recordInserted);
		}
	}

	public IMHEOutboundDAO getMheOutboundDao()
	{
		HTPCWMAgileLogHelper.logEnter("In getMheOutboundDao..");
		if (mheOutboundDao == null)
			mheOutboundDao = MheOutboundUtil.getMheOutboundServicesDao();
		return mheOutboundDao;
	}

	/** Updates <code>RESULT_CODE</CODE> in TRAN_LOG table.
	 * @param tranLogId
	 * @param result_code
	 */
	@Override
	public void updateTranLogEntry(int tranLogId, String result_code)
	{
		HTPCWMAgileLogHelper.logEnter("Going to update TRAN_LOG.");
		String query = "update TRAN_LOG set result_code = :resCode where tran_log_id = :tranLogId ";
		String[] nameList =
		{ "resCode", "tranLogId" };
		Object[] paramValueList =
		{ result_code, tranLogId };
		@SuppressWarnings("deprecation")
		int rows = directSQLUpdate(query, nameList, paramValueList);
		HTPCWMAgileLogHelper.logExit("Updated " + rows + " rows in TRAN_LOG.");
	}

}

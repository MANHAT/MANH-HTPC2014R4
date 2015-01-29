package com.manh.wmos.services.receiving.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.math.BigDecimal;


import com.manh.wm.core.util.WMLogger;

public class HTPCReceivingManagerDAOImpl extends ReceivingManagerDAOImpl implements HTPCReceivingManagerDAO {

	public HashMap getSerialNbrMap(Long lpnId) {
		WMLogger log = getWMLogger();
		
		log.logDebug("EX23: HTPCReceivingManagerDAOImpl::getSerialNbrMap. lpnId: "
				+ lpnId);
		
		StringBuffer query =  new StringBuffer(
				"SELECT LD.LPN_DETAIL_ID, LISTAGG(SNT.SRL_NBR, ',') WITHIN GROUP (ORDER BY SNT.SRL_NBR_TRK_ID ASC) AS SERIAL_NUMBER "
				+ " FROM LPN L"
				+ " JOIN LPN_DETAIL LD ON LD.LPN_ID = L.LPN_ID"
				+ " LEFT JOIN SRL_NBR_TRACK SNT ON SNT.LPN_ID = L.LPN_ID AND SNT.LPN_DETAIL_ID = LD.LPN_DETAIL_ID"
				+ " WHERE L.LPN_ID = :lpnId" + " GROUP BY LD.LPN_DETAIL_ID");
	

		HashMap<Long, String> serialNbrMap = new HashMap<Long, String>();

		String[] nameList = { "lpnId" };
		Object[] parmValueList = { lpnId };
		List returnList = directSQLQuery(query.toString(), nameList,
				parmValueList, null, null);

		if (returnList != null && returnList.size() > 0) {
			Iterator it = returnList.iterator();
			while (it.hasNext()) {
				Object[] obj = (Object[]) it.next();
				serialNbrMap.put(((BigDecimal) obj[0]).longValue(),
						(String) obj[1]);
			}
		}

		return serialNbrMap;

	}

	public HashMap getItemStyleAndSuffix(Long lpnId) {
		WMLogger log = getWMLogger();
		
		log.logDebug("EX23: HTPCReceivingManagerDAOImpl::getItemStyleAndSuffix. lpnId: "
				+ lpnId);

		StringBuffer query =  new StringBuffer("SELECT LD.LPN_DETAIL_ID, CBO.ITEM_STYLE, CBO.ITEM_STYLE_SFX, CBO.BRAND "
				+ "FROM LPN L JOIN LPN_DETAIL LD ON LD.LPN_ID = L.LPN_ID "
				+ "JOIN ITEM_CBO CBO ON CBO.ITEM_ID = LD.ITEM_ID "
				+ "WHERE L.LPN_ID = :lpnId");

	

		HashMap<Long, List<String>> serialNbrMap = new HashMap<Long, List<String>>();

		String[] nameList = { "lpnId" };
		Object[] parmValueList = { lpnId };
		List returnList = directSQLQuery(query.toString(), nameList, parmValueList, null,
				null);

		if (returnList != null && returnList.size() > 0) {
			Iterator it = returnList.iterator();
			while (it.hasNext()) {
				Object[] obj = (Object[]) it.next();
				List<String> values = new ArrayList<String>();
				values.add((String) obj[1]);
				values.add((String) obj[2]);
				values.add((String) obj[3]);
				serialNbrMap.put(((BigDecimal) obj[0]).longValue(),values);
			}
		}

		return serialNbrMap;

	}
}

package com.manh.wmos.services.mheoutbound.agile.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.logistics.javalib.util.Misc;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wm.dao.WMDAOImpl;
import com.manh.wmos.services.mheoutbound.dao.IMHEOutboundDAO;
import com.manh.wmos.services.mheoutbound.helper.MheOutboundUtil;

public class HTPCAgileDAOImpl extends WMDAOImpl
{

	private IMHEOutboundDAO mheOutboundDao = null;

	public HashMap<String, String> getHeaderData(String tcLpnId, String warehouse)
	{

		HashMap<String, String> resultMap = new HashMap<String, String>();

		StringBuffer query = new StringBuffer(
				"SELECT L.LPN_ID, L.LPN_FACILITY_STATUS, L.TC_LPN_ID, O.DELIVERY_END_DTTM, O.D_NAME, O.D_ADDRESS_1, O.D_ADDRESS_2, O.D_ADDRESS_3, O.D_CITY, O.D_STATE_PROV, ");
		query.append(" O.D_POSTAL_CODE, O.D_COUNTRY_CODE, O.D_PHONE_NUMBER, O.D_EMAIL, ");
		query.append(" CASE WHEN EXISTS (SELECT 1 FROM ORDER_LINE_ITEM OLI WHERE OLI.ORDER_ID = O.ORDER_ID AND UPPER(OLI.REF_FIELD2)='TRUE') AND SUBSTR(S.MISC_FLAGS,0,1) IN('Y', '1') THEN S.CODE_ID ELSE O.DSG_SHIP_VIA END AS DSG_SHIP_VIA, ");
		query.append(" L.ORDER_ID, L.REF_FIELD_2 FROM ORDERS O, LPN L, FACILITY F, SYS_CODE S ");
		query.append(" WHERE L.ORDER_ID = O.ORDER_ID AND L.INBOUND_OUTBOUND_INDICATOR = 'O' AND L.TC_LPN_ID = :tcLpnId AND L.C_FACILITY_ID = F.FACILITY_ID AND F.WHSE = :warehouse	AND S.CODE_TYPE = 'HZT' AND S.REC_TYPE = 'C' ");

		HTPCWMAgileLogHelper.logDebug("Header Query formed is : ["+query+"]");
		
		String[] nameList =
		{ "tcLpnId", "warehouse" };
		Object[] paramValueList =
		{ tcLpnId, warehouse };
		List<?> list = directSQLQuery(query.toString(), nameList, paramValueList, null, null);

		if (!Misc.isNullList(list))
		{
			Object[] values = (Object[]) list.get(0);
			resultMap.put("LPN_ID", String.valueOf(values[0]));
			resultMap.put("LPN_FACILITY_STATUS", String.valueOf(values[1]));
			resultMap.put("TC_LPN_ID", String.valueOf(values[2]));
			resultMap.put("DELIVERY_END_DTTM", String.valueOf(values[3]));
			resultMap.put("D_NAME", String.valueOf(values[4]));
			resultMap.put("D_ADDRESS_1", String.valueOf(values[5]));
			resultMap.put("D_ADDRESS_2", String.valueOf(values[6]));
			resultMap.put("D_ADDRESS_3", String.valueOf(values[7]));
			resultMap.put("D_CITY", String.valueOf(values[8]));
			resultMap.put("D_STATE_PROV", String.valueOf(values[9]));
			resultMap.put("D_POSTAL_CODE", String.valueOf(values[10]));
			resultMap.put("D_COUNTRY_CODE", String.valueOf(values[11]));
			resultMap.put("D_PHONE_NUMBER", String.valueOf(values[12]));
			resultMap.put("D_EMAIL", String.valueOf(values[13]));
			resultMap.put("DSG_SHIP_VIA", String.valueOf(values[14]));
			resultMap.put("ORDER_ID", String.valueOf(values[15]));
			resultMap.put("REF_FIELD_2", String.valueOf(values[16]));
		}

		HTPCWMAgileLogHelper.logDebug("Header Data for " + tcLpnId + ": " + resultMap);

		return resultMap;
	}

	public List<HashMap<String, String>> getLineItemsData(String lpnId, String orderId)
	{

		List<?> list = new ArrayList<>();
		List<HashMap<String, String>> output = new ArrayList<>();

		if (!Misc.isNullTrimmedString(lpnId) && !Misc.isNullTrimmedString(orderId))
		{
			StringBuffer query = new StringBuffer("SELECT IC.ITEM_NAME, IC.DESCRIPTION, LD.SIZE_VALUE, LD.ESTIMATED_WEIGHT, OLI.PRICE, SU.SIZE_UOM, LD.CNTRY_OF_ORGN");
			query.append(" FROM LPN_DETAIL LD, ITEM_CBO IC, ORDER_LINE_ITEM OLI, SIZE_UOM SU, LPN L, ORDERS O");
			query.append(" WHERE LD.LPN_ID = :lpnId and  OLI.ORDER_ID = :orderid and OLI.ITEM_ID = LD.ITEM_ID and SU.SIZE_UOM_ID = IC.BASE_STORAGE_UOM_ID");
			query.append(" and LD.ITEM_ID = IC.ITEM_ID and L.LPN_ID = LD.LPN_ID and L.ORDER_ID = O.ORDER_ID and LD.SIZE_VALUE > 0");

			HTPCWMAgileLogHelper.logDebug("Line Item Query formed is : ["+query+"]");
			
			String[] nameList =
			{ "lpnId", "orderid" };
			Object[] paramValueList =
			{ lpnId, orderId };
			list = directSQLQuery(query.toString(), nameList, paramValueList, null, null);

			if (!Misc.isNullList(list))
			{
				Iterator<?> itr = list.iterator();
				while (itr.hasNext())
				{

					HashMap<String, String> hm = new HashMap<String, String>();
					Object values[] = (Object[]) itr.next();
					hm.put("ITEM_NAME", String.valueOf(values[0]));
					hm.put("DESCRIPTION", String.valueOf(values[1]));
					hm.put("SIZE_VALUE", String.valueOf(values[2]));
					hm.put("ESTIMATED_WEIGHT", String.valueOf(values[3]));
					hm.put("PRICE", String.valueOf(values[4]));
					hm.put("SIZE_UOM", String.valueOf(values[5]));
					hm.put("CNTRY_OF_ORGN", String.valueOf(values[6]));
					output.add(hm);
				}
			}

		}
		return output;
	}

	@SuppressWarnings("deprecation")
	public int updateStatCode(int eventId, String tc_lpn_id, String newStatCode)
	{
		String query = "UPDATE EVENT_MESSAGE SET STAT_CODE = :statcode WHERE EK_OLPN_NBR = :olpn AND EVENT_ID = :eventid";
		String[] nameList =
		{ "statcode", "olpn", "eventid" };
		Object[] paramValueList =
		{ newStatCode, tc_lpn_id, eventId };
		int rowUpdated = directSQLUpdate(query, nameList, paramValueList);
		return rowUpdated;
	}

	@SuppressWarnings("deprecation")
	public void updateLpnRefField2(String orderHeaderId, String lpnId)
	{
		String query = "UPDATE LPN SET REF_FIELD_2 = :orderHeaderId WHERE LPN_ID = :lpnId";
		HTPCWMAgileLogHelper.logDebug("LPN update query after agile response : " + query);
		String[] nameList =
		{ "orderHeaderId", "lpnId" };
		Object[] paramValueList =
		{ orderHeaderId, lpnId };
		directSQLUpdate(query, nameList, paramValueList);
	}

	public IMHEOutboundDAO getMheOutboundDao()
	{
		HTPCWMAgileLogHelper.logEnter("In getMheOutboundDao..");
		if (mheOutboundDao == null)
			mheOutboundDao = MheOutboundUtil.getMheOutboundServicesDao();
		return mheOutboundDao;
	}

}

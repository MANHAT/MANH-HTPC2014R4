package com.manh.wmos.services.mheoutbound.agile.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.logistics.javalib.JavalibDef;
import com.logistics.javalib.persistence.jdbc.JDBCConnectionCreator;
import com.logistics.javalib.persistence.jdbc.JDBCFunc;
import com.logistics.javalib.util.Misc;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wm.dao.WMDAOImpl;
import com.manh.wmos.services.mheoutbound.dao.IMHEOutboundDAO;
import com.manh.wmos.services.mheoutbound.helper.MheOutboundUtil;
import com.manh.wmos.services.outbound.agile.data.IHTPCWMAgileCommunicationConstants;

public class HTPCAgileDAOImpl extends WMDAOImpl
{

	public HTPCAgileDAOImpl()
	{
		super();
		this.connCreator = JavalibDef.CONNECTION_CREATOR;
	}

	/**
	 * This holds JDBC connection
	 */
	private JDBCConnectionCreator	connCreator;

	private IMHEOutboundDAO			mheOutboundDao	= null;

	public HashMap<String, String> getHeaderData(String tcLpnId, String warehouse)
	{

		HashMap<String, String> resultMap = new HashMap<String, String>();

		StringBuffer query = new StringBuffer(
				"SELECT L.LPN_ID, L.LPN_FACILITY_STATUS, L.TC_LPN_ID, O.DELIVERY_END_DTTM, O.D_NAME, O.D_ADDRESS_1, O.D_ADDRESS_2, O.D_ADDRESS_3, O.D_CITY, O.D_STATE_PROV, ");
		query.append(" O.D_POSTAL_CODE, O.D_COUNTRY_CODE, O.D_PHONE_NUMBER, O.D_EMAIL, ");
		query.append(" CASE WHEN EXISTS (SELECT 1 FROM ORDER_LINE_ITEM OLI WHERE OLI.ORDER_ID = O.ORDER_ID AND UPPER(OLI.REF_FIELD2)='TRUE') AND SUBSTR(S.MISC_FLAGS,0,1) IN('Y', '1') THEN S.CODE_ID ELSE O.DSG_SHIP_VIA END AS DSG_SHIP_VIA, ");
		query.append(" L.ORDER_ID, L.REF_FIELD_2 FROM ORDERS O, LPN L, FACILITY F, SYS_CODE S ");
		query.append(" WHERE L.ORDER_ID = O.ORDER_ID AND L.INBOUND_OUTBOUND_INDICATOR = 'O' AND L.TC_LPN_ID = :tcLpnId AND L.C_FACILITY_ID = F.FACILITY_ID AND F.WHSE = :warehouse	AND S.CODE_TYPE = 'HZT' AND S.REC_TYPE = 'C' ");

		HTPCWMAgileLogHelper.logDebug("Header Query formed is : [" + query + "]");

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

			HTPCWMAgileLogHelper.logDebug("Line Item Query formed is : [" + query + "]");

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

	/**
	 * Updates STAT_CODE of EVENT_MESSAGE table.
	 * 
	 * @param eventId
	 * @param tc_lpn_id
	 * @param newStatCode
	 * @return
	 */
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

	/**
	 * Updates LPN.REF_FIELD_2
	 * 
	 * @param orderHeaderId
	 * @param lpnId
	 */
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

	/**
	 * Creates a TRAN_LOG and TRAN_LOG_MESSAGE table entry with the messages.
	 * 
	 * @param message
	 *            - The <code>MSG_TYPE</code> in TRAN_LOG table.
	 * @param msg_line_text
	 *            - The <code>MSG_LINE_TEXT</code> in TRAN_LOG_MESSAGE table.
	 */
	public void createTranLogAndTranLogMessageEntry(String message, String msg_line_text)
	{
		HTPCWMAgileLogHelper.logEnter("Entering createTranLogAndTranLogMessageEntry()..");

		String sqlQuery = " INSERT INTO TRAN_LOG "
				+ "(TRAN_LOG_ID, SEQUENCE_NUMBER, MESSAGE_ID, DIRECTION, ORIGIN_TYPE, ORIGIN_FORMAT, MSG_TYPE, DEST_TYPE, DEST_FORMAT, RESULT_CODE, TRAN_LOG_LEVEL, HAS_ERRORS) "
				+ "VALUES (SEQ_TRAN_LOG_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, SEQ_TRAN_LOG_MESSAGE_ID.nextval, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String tranLogIdQuery = "SELECT SEQ_TRAN_LOG_ID.currval AS TRAN_LOG_ID FROM DUAL";

		String tranLogMsgQuery = "INSERT INTO TRAN_LOG_MESSAGE (TRAN_LOG_ID, MSG_LINE_NUMBER, MSG_LINE_TEXT) VALUES (?,?,?)";

		Connection conn = null;
		PreparedStatement stmt = null, stmt2 = null;
		int recordsInserted = 0;
		int tranLogId = 0;
		try
		{
			conn = connCreator.createConnection();
			stmt = conn.prepareStatement(sqlQuery);

			int i = 1;
			stmt.setString(i++, "I"); // DIRECTION
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.AGILE); // ORIGIN_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // ORIGIN_FORMAT
			stmt.setString(i++, message); // MSG_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.WM); // DEST_TYPE
			stmt.setString(i++, IHTPCWMAgileCommunicationConstants.MESSAGE_FORMAT); // DEST_FORMAT
			stmt.setInt(i++, 0); // RESULT_CODE
			stmt.setString(i++, "Always"); // TRAN_LOG_LEVEL
			stmt.setInt(i++, 0); // HAS_ERRORS

			recordsInserted = stmt.executeUpdate();
			HTPCWMAgileLogHelper.logDebug("Records Inserted in TRAN_LOG: " + recordsInserted);

			ResultSet rs = stmt.executeQuery(tranLogIdQuery);
			if (rs.next())
			{
				HTPCWMAgileLogHelper.logDebug("ResultSet has data in it..");
				tranLogId = rs.getInt("TRAN_LOG_ID");
			}
			HTPCWMAgileLogHelper.logDebug("TRAN_LOG_ID : " + tranLogId);

			stmt2 = conn.prepareStatement(tranLogMsgQuery);
			stmt2.setInt(1, tranLogId); //TRAN_LOG_ID
			stmt2.setInt(2, 0); //MSG_LINE_NUMBER
			stmt2.setString(3, msg_line_text); //MSG_LINE_TEXT

			recordsInserted = stmt2.executeUpdate();
			HTPCWMAgileLogHelper.logDebug("Records Inserted in TRAN_LOG_MESSAGE: " + recordsInserted);

		}
		catch (Exception e)
		{
			HTPCWMAgileLogHelper.logException(e);
		}
		finally
		{
			JDBCFunc.closeJDBCResources(stmt2);
			JDBCFunc.closeJDBCResources(stmt, conn);
			HTPCWMAgileLogHelper.logExit("Exiting createTranLogAndTranLogMessageEntry()..");
		}
	}

	public IMHEOutboundDAO getMheOutboundDao()
	{
		HTPCWMAgileLogHelper.logEnter("In getMheOutboundDao..");
		if (mheOutboundDao == null)
			mheOutboundDao = MheOutboundUtil.getMheOutboundServicesDao();
		return mheOutboundDao;
	}

}

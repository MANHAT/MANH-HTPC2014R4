package com.manh.labelprinting.printing.dao;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.logistics.javalib.util.Misc;
import com.manh.ils.db.Type;
import com.manh.ils.wmservices.utils.WMMisc;

public class HTPCLabelPrintClassDAOImpl extends LabelPrintClassDAOImpl implements HTPCLabelPrintClassDAO
{

	@Override
	public String getDynamicColValueFromOrderNote(int distributionOrderId, String noteType, String colName)
	{

		log.logEnter(" getDynamicColValueFromOrderNote ");
		String retValue = Misc.EMPTY_STRING;
		StringBuilder query = new StringBuilder();

		query.append("select orderNote.");
		query.append(colName);
		query.append(" from ORDER_NOTE orderNote where orderNote.order_id = :distributionOrderId and orderNote.note_type = :noteType  ");

		String[] nameList =
		{ "distributionOrderId", "noteType" };
		Object[] parmValueList =
		{ distributionOrderId, noteType };

		List<String> columnNames = new ArrayList<String>();
		List<Type> columnTypes = new ArrayList<Type>();

		columnNames.add(colName);
		columnTypes.add(Type.STRING);

		List returnList = directSQLQuery(query.toString(), nameList, parmValueList, columnNames, columnTypes);

		if (returnList != null && returnList.size() >= 1)
		{
			Iterator<String> itr = returnList.iterator();
			while (itr.hasNext())
			{
				retValue = Misc.isNullTrimmedString(retValue) ? itr.next() : (retValue + " " + itr.next());
			}
		}
		log.logExit(" getDynamicColValueFromOrderNote retValue: " + retValue);
		return retValue;

	}

	@Override
	public Map<String, List<String>> getOrderLineItemMap(String tc_lpn_id)
	{
		log.logEnter("HTPCContentShippingLabelDataImpl :: getOrderLineItemMap, TC_LPN_ID received is : [" + tc_lpn_id + "]");

		Map<String, List<String>> oliMap = new HashMap<String, List<String>>();
		try
		{
			String aQuery = "SELECT OLI.LINE_ITEM_ID, OLI.IS_GIFT, OLI.PRICE, OLI.UNIT_TAX_AMOUNT, OLI.DESCRIPTION "
					+ " FROM ORDER_LINE_ITEM OLI JOIN LPN_DETAIL LD ON LD.DISTRIBUTION_ORDER_DTL_ID = OLI.LINE_ITEM_ID " + " JOIN LPN L ON L.LPN_ID = LD.LPN_ID WHERE L.TC_LPN_ID = :tcLpnId";

			String[] nameList =
			{ "tcLpnId" };
			Object[] paramList =
			{ tc_lpn_id };
			List<?> list = directSQLQuery(aQuery, nameList, paramList, null, null);

			if (!Misc.isNullList(list))
			{
				for (Object objVal : list)
				{
					Object[] values = (Object[]) objVal;

					List<String> fieldsList = new ArrayList<String>();

					String LINE_ITEM_ID = Misc.isNullTrimmedString(getBigDecimalString(values[0])) ? Misc.EMPTY_STRING : getBigDecimalString(values[0]);
					log.logDebug("---LINE ITEM ID : " + LINE_ITEM_ID);

					String IS_GIFT = Misc.isNullTrimmedString(getBigDecimalString(values[1])) ? Misc.EMPTY_STRING : getBigDecimalString(values[1]);
					log.logDebug("---IS_GIFT : " + IS_GIFT);
					fieldsList.add(IS_GIFT);

					String PRICE = Misc.isNullTrimmedString(getBigDecimalString(values[2])) ? Misc.EMPTY_STRING : getBigDecimalString(values[2]);
					log.logDebug("---PRICE : " + PRICE);
					fieldsList.add(PRICE);

					String UNIT_TAX_AMOUNT = Misc.isNullTrimmedString(getBigDecimalString(values[3])) ? Misc.EMPTY_STRING : getBigDecimalString(values[3]);
					log.logDebug("---UNIT_TAX_AMOUNT : " + UNIT_TAX_AMOUNT);
					fieldsList.add(UNIT_TAX_AMOUNT);

					String DESCRIPTION = Misc.isNullTrimmedString((String) values[4]) ? Misc.EMPTY_STRING : (String) values[4];
					log.logDebug("---DESCRIPTION : " + DESCRIPTION);
					fieldsList.add(DESCRIPTION);

					oliMap.put(LINE_ITEM_ID, fieldsList);
				}
			}
		}
		catch (Exception ex)
		{
			log.logException(ex);
		}
		log.logExit(" Exiting HTPCContentShippingLabelDataImpl :: getOrderLineItemMap");

		return oliMap;
	}

	/**
	 * This method returns a map of code_id mapped with code description.
	 */
	@Override
	public HashMap<String, String> getSysCodeIDAndDescMapping(String rec_type, String code_type)
	{
		log.logEnter("getSysCodeIDAndDescMapping..");
		String QUERY = " select sc.codeId,sc.codeDesc from SysCode sc where sc.recType = :recType and sc.codeType = :codeType";
		String[] nameList =
		{ "recType", "codeType" };
		Object[] paramList =
		{ rec_type, code_type };
		HashMap<String, String> codeIdCodeDescMap = new HashMap<String, String>();
		List<?> dbList = directQuery(QUERY, nameList, paramList);
		if (!Misc.isNullList(dbList))
		{
			for (int i = 0; i < dbList.size(); i++)
			{
				String[] data = WMMisc.convertObjArrayToStringArray((Object[]) dbList.get(i));
				codeIdCodeDescMap.put(data[0], data[1]);
			}
		}
		log.logExit("getSysCodeIDAndDescMapping..");
		return codeIdCodeDescMap;
	}

	@Override
	public String getDeliveryOptionFromOrder(int distributionOrderId)
	{
		log.logEnter("getDeliveryOptionFromOrder");
		String retValue = Misc.EMPTY_STRING;
		StringBuilder query = new StringBuilder();

		query.append("SELECT O.DELIVERY_OPTIONS FROM ORDERS O WHERE O.ORDER_ID = :distributionOrderId");

		String[] nameList =
		{ "distributionOrderId" };
		Object[] parmValueList =
		{ distributionOrderId };

		List<String> columnNames = new ArrayList<String>();
		List<Type> columnTypes = new ArrayList<Type>();

		columnNames.add("DELIVERY_OPTIONS");
		columnTypes.add(Type.STRING);

		List returnList = directSQLQuery(query.toString(), nameList, parmValueList, columnNames, columnTypes);

		if (returnList != null && returnList.size() >= 1)
		{
			Iterator<String> itr = returnList.iterator();
			while (itr.hasNext())
			{
				retValue = itr.next();
			}
		}
		log.logExit("getDeliveryOptionFromOrder. retValue: " + retValue);
		return retValue;
	}

	/**
	 * Returns a String after casting the BigDecimal from Object.
	 * 
	 * @param value
	 * @return
	 */
	protected String getBigDecimalString(Object value)
	{
		log.logEnter("getBigDecimalString..");
		BigDecimal ret = null;
		String retVal = Misc.EMPTY_STRING;

		if (value != null)
		{
			if (value instanceof BigDecimal)
			{
				ret = (BigDecimal) value;
			}
			else if (value instanceof String)
			{
				ret = new BigDecimal((String) value);
			}
			else if (value instanceof BigInteger)
			{
				ret = new BigDecimal((BigInteger) value);
			}
			else if (value instanceof Number)
			{
				ret = new BigDecimal(((Number) value).doubleValue());
			}
			else
			{
				throw new ClassCastException("Not possible to convert [" + value + "] from class " + value.getClass() + " into a BigDecimal.");
			}
		}
		retVal = (null == ret) ? retVal : ret.toString();
		log.logExit("getBigDecimalString is returning : " + retVal);
		return retVal;
	}

}

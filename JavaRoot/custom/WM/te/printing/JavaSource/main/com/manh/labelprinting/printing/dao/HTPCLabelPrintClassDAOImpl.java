package com.manh.labelprinting.printing.dao;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.logistics.javalib.util.Misc;

//import com.manh.cbo.transactional.bd.shipment.ShipmentService;
import com.manh.ils.db.Type;

public class HTPCLabelPrintClassDAOImpl extends LabelPrintClassDAOImpl implements
      HTPCLabelPrintClassDAO {

	@Override
	public String getDynamicColValueFromOrderNote(int distributionOrderId,
			String noteType, String colName) {
		
		log.logEnter(" getDynamicColValueFromOrderNote ");
		String retValue = Misc.EMPTY_STRING;
		StringBuilder query = new StringBuilder();

		query.append("select orderNote.");
		query.append(colName);
		query.append(" from ORDER_NOTE orderNote where orderNote.order_id = :distributionOrderId and orderNote.note_type = :noteType  ");

		String[] nameList = { "distributionOrderId", "noteType" };
		Object[] parmValueList = { distributionOrderId, noteType };

		List<String> columnNames = new ArrayList<String>();
		List<Type> columnTypes = new ArrayList<Type>();

		columnNames.add(colName);
		columnTypes.add(Type.STRING);

		List returnList = directSQLQuery(query.toString(), nameList,
				parmValueList, columnNames, columnTypes);
		
		if (returnList != null && returnList.size() >= 1) {
			Iterator<String> itr = returnList.iterator();
			while(itr.hasNext()) {
				retValue = Misc.isNullTrimmedString(retValue) ? itr.next() : (retValue + " " + itr.next());
			}
		}
		log.logExit(" getDynamicColValueFromOrderNote retValue: " + retValue);
		return retValue;

	}

 


}


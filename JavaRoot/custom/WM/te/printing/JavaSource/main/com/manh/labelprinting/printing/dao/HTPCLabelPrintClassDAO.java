package com.manh.labelprinting.printing.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface HTPCLabelPrintClassDAO extends LabelPrintClassDAO{
	public String getDynamicColValueFromOrderNote(int distributionOrderId,
			String noteType, String colName);
			
	/* EX04*/
	public Map<String, List<String>> getOrderLineItemMap(String tc_lpn_id);

	/**
	 * This method returns a map of code_id mapped with code description.
	 */
	public HashMap<String, String> getSysCodeIDAndDescMapping(String rec_type, String code_type);
	
	public String getDeliveryOptionFromOrder(int distributionOrderId);


}

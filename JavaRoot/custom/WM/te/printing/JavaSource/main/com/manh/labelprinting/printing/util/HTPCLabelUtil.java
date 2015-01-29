package com.manh.labelprinting.printing.util;

import java.util.List;
import java.util.Map;

import com.logistics.javalib.util.Misc;
import com.manh.cbo.transactional.domain.enums.lpn.InboundOutboundIndicatorFlag;
import com.manh.labelprinting.printing.dao.HTPCLabelPrintClassDAO;
import com.manh.labelprinting.printing.processor.LabelPrintHelper;

public class HTPCLabelUtil extends LabelUtil
{
	public void setDynamicSubstitutions(List<LabelSubstitution> labelSubList,
			Map<String, Object> labelSubs)
	{
		for(int i = 0; i < labelSubList.size(); i++){
			String value = null;
			LabelSubstitution labelSub = labelSubList.get(i);
			if(labelSub.tableName.equalsIgnoreCase("Lpn"))
			{
				if(oLPN != null)
				{
					value = LabelPrintHelper.getLabelPrintClassDAO().
							getDynamicColValueFromLpn(oLPN.getLpnId(), 
									oLPN.getTcCompanyId(),
									oLPN.getCurrentFacilityId(),
									InboundOutboundIndicatorFlag.OUTBOUND,
									labelSub.colName);
				}
				else if(iLPN != null)
				{
					value = LabelPrintHelper.getLabelPrintClassDAO().
							getDynamicColValueFromLpn(iLPN.getLpnId(), 
									iLPN.getTcCompanyId(),
									iLPN.getCurrentFacilityId(),
									InboundOutboundIndicatorFlag.INBOUND,
									labelSub.colName);
				}
			}
			else if(labelSub.tableName.equalsIgnoreCase("LocnHdr")){
				if(locn != null){
					value = LabelPrintHelper.getLabelPrintClassDAO().
							getDynamicColValueFromLocnHdr(locn.getLocnId(),
							labelSub.colName);
				}
				}

			else if(labelSub.tableName.equalsIgnoreCase("Orders")){
				if(order != null){
					value = LabelPrintHelper.getLabelPrintClassDAO().
							getDynamicColValueFromOrder(order.getDistributionOrderIdString(),
							labelSub.colName);
				}
			}else if(labelSub.tableName.equalsIgnoreCase("ShipVia")){
				if(shipVia != null){
					value = LabelPrintHelper.getLabelPrintClassDAO().
							getDynamicColValueFromShipVia(String.valueOf(
									shipVia.getShipViaDataPK().getShipViaId()),
							labelSub.colName);
				}
			}else if(labelSub.tableName.equalsIgnoreCase("CarrierCode")){
				if(carrier != null){
					value = LabelPrintHelper.getLabelPrintClassDAO().
							getDynamicColValueFromCarrier(carrier.getCarrierId()
							, labelSub.colName);
				}
			}
			else if(labelSub.tableName.equalsIgnoreCase("OrderNote") || 	labelSub.tableName.equalsIgnoreCase("Order_Note") ){
				if(order != null)
				{					
					value = ((HTPCLabelPrintClassDAO)LabelPrintHelper.getLabelPrintClassDAO()).getDynamicColValueFromOrderNote(order.getDistributionOrderId(),
									labelSub.subString,
									labelSub.colName);
				}
			}
			if (!Misc.isNullTrimmedString(value)) {
				labelSubs.put(labelSub.subString, value);
			} else if (!labelSubs.containsKey(labelSub.subString)) {
				labelSubs.put(labelSub.subString, value);
			}
			
			//MACR00638478: done only for nordstorm.(shlbl14)			
			if(order != null){
				value = LabelPrintHelper.getLabelPrintClassDAO().
									getDynamicColValueFromOrder(order.getDistributionOrderIdString(),
									"STORE_NBR");
				labelSubs.put("nord_store_nbr", value);
				String formattedStoreNbr = null;
				if (!Misc.isNullTrimmedString(value))
				{
					formattedStoreNbr = "(91) ";
					formattedStoreNbr = formattedStoreNbr + value;
				}
				value = formattedStoreNbr;
				labelSubs.put("formatted_nord_store_nbr", value);
			}	
		}
	}
}

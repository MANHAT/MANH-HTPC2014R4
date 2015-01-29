package com.manh.labelprinting.printing.dao;

public interface HTPCLabelPrintClassDAO extends LabelPrintClassDAO{
	public String getDynamicColValueFromOrderNote(int distributionOrderId,
			String noteType, String colName);

}

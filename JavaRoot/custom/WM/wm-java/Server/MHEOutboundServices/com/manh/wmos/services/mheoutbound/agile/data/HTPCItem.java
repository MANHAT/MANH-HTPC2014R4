package com.manh.wmos.services.mheoutbound.agile.data;

import com.logistics.javalib.util.Misc;

public class HTPCItem {
	
	private String partNumber = "NA", description = "NA", quantity, 
			weight , unitPrice , unitOfMeasure = Misc.EMPTY_STRING;

	public String getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(String partNumber) {
		this.partNumber = Misc.replaceNullWithEmptyString(partNumber);
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = Misc.replaceNullWithEmptyString(description);
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = Misc.replaceNullWithEmptyString(quantity);
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = Misc.replaceNullWithEmptyString(weight);
	}

	public String getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(String unitPrice) {
		this.unitPrice = Misc.replaceNullWithEmptyString(unitPrice);
	}

	public String getUnitOfMeasure() {
		return unitOfMeasure;
	}

	public void setUnitOfMeasure(String unitOfMeasure) {
		this.unitOfMeasure = Misc.replaceNullWithEmptyString(unitOfMeasure);
	}
	
	

}

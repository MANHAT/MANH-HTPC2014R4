package com.manh.wmos.services.mheoutbound.agile.data;

import com.logistics.javalib.util.Misc;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;

public class HTPCHeaderData {
	
	private String orderNumber,deliveryBy = Misc.EMPTY_STRING;
	private HTPCShip ship;
	private HTPCOrder order;
	private HTPCRecord record;
	
	
	public String getOrderNumber() {
		return orderNumber;
	}
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = Misc.replaceNullWithEmptyString(orderNumber);
	}
	public String getDeliveryBy() {
		return deliveryBy;
	}
	public void setDeliveryBy(String deliveryBy) {
		if(null != deliveryBy) {
			HTPCWMAgileLogHelper.logDebug("Received delivery date : "+deliveryBy);
			deliveryBy = deliveryBy.substring(0, deliveryBy.indexOf(" "));
			HTPCWMAgileLogHelper.logDebug("Parsed delivery date : "+deliveryBy);
			this.deliveryBy = deliveryBy;
		}
	}
	public HTPCShip getShip() {
		return ship;
	}
	public void setShip(HTPCShip ship) {
		this.ship = ship;
	}
	public HTPCOrder getOrder() {
		return order;
	}
	public void setOrder(HTPCOrder order) {
		this.order = order;
	}
	public HTPCRecord getRecord() {
		return record;
	}
	public void setRecord(HTPCRecord record) {
		this.record = record;
	}
	
	

}

package com.manh.wmos.services.mheoutbound.agile.data;

import java.util.List;

import com.logistics.javalib.util.Misc;

public class HTPCPierbridgeOrderLoadRequestData {
	/**
	 * 	This bean holds the data fields required in Agile Communication.
	 */
	
	private String	orderHeaderId = Misc.EMPTY_STRING, username = Misc.EMPTY_STRING;
	private HTPCHeaderData header;
	private List<HTPCLineItems> lineItems;
	
	public String getOrderHeaderId() {
		return orderHeaderId;
	}
	public void setOrderHeaderId(String orderHeaderId) {
		this.orderHeaderId = Misc.replaceNullWithEmptyString(orderHeaderId);
	}
	public HTPCHeaderData getHeader() {
		return header;
	}
	public void setHeader(HTPCHeaderData header) {
		if(null != header)
			this.header = header;
	}
	public List<HTPCLineItems> getLineItems() {
		return lineItems;
	}
	public void setLineItems(List<HTPCLineItems> lineItems) {
		if(null != lineItems)
			this.lineItems = lineItems;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = Misc.replaceNullWithEmptyString(username);
	}
	
	
	

}

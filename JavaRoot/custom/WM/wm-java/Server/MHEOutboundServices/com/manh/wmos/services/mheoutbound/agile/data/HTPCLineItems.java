package com.manh.wmos.services.mheoutbound.agile.data;

public class HTPCLineItems {
	
	private HTPCLineItem lineItem;

	public HTPCLineItem getLineItem() {
		return lineItem;
	}

	public void setLineItem(HTPCLineItem lineItem) {
		if(null != lineItem)
			this.lineItem = lineItem;
	}
	
	

}

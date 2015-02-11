package com.manh.wmos.services.outbound.agile.dao;

import javax.servlet.http.HttpServletRequest;

import com.manh.wmos.services.mheoutbound.dao.IMHEOutboundDAO;

/**
 * This interface provides Data Base interaction level code for processing response XML messages received from Agile to WM
 * @author psindhu
 *
 */
public interface IHTPCAgileOrderLoadRespDAO {

	/**
	 * Fetch out LPN.LPN_FACILITY_STATUS corresponding tc_lpn_id
	 * @param lpnNumber
	 * @return
	 */
	public Short getLpnFacilityStatus(String lpnNumber);

	/**
	 * Update EVENT_MESSAGE.ERROR_SEQ_CODE = ‘999’,EVENT_MESSAGE.STAT_CODE = ‘96’ (Error) for invalid LPN status(>=90)
	 * @param eventMessageId
	 */
	public void updateEventMessageForFailure(String eventMessageId);

	/**
	 * Update EVENT_MESSAGE.ERROR_SEQ_CODE = ‘998’,EVENT_MESSAGE.STAT_CODE = ‘96’ (Error) for invalid LPN status(>=90)
	 * @param eventMessageId
	 */
	public void updateEventMessageForMissingFieldsFailure(String eventMessageId);

	/**
	 * update lpn corresponding order load message response
	 * @param trackingNumber
	 * @param surCharge
	 * @param freightCharge
	 * @param insauranceCharge
	 * @param codCharge
	 * @param shipVia
	 * @param actualWeight
	 * @param lpnRefField10
	 * @param lpnFacilityStatusToUpdate
	 * @param lpnNumber 
	 */
	public void updateLPNForOrderLoadResponse(String trackingNumber,
			String surCharge, String freightCharge, String insauranceCharge,
			String codCharge, String shipVia, String actualWeight,
			String lpnRefField10, Short lpnFacilityStatusToUpdate, String lpnNumber);

	/**
	 * Update EVENT_MESSAGE.STAT_CODE = 90
	 * @param eventMessageId
	 */
	public void updateEventMessageForSucess(String eventMessageId);

	/**
	 * If the last LPN holding items for a given order line gets weighed, update the order line item status (ORDER_LINE_ITEM.DO_DTL_STATUS) = ‘160’ (Weighed). 
	 * @param lpnNumber
	 */
	public void checkAndUpdateOrderLineItem(String lpnNumber);

	/**
	 * If the LPN is the only LPN for the order in less than weighed status, update ORDERS.DO_STATUS = ‘160’ (Weighed) 
	 * @param lpnNumber
	 */
	public void checkAndUpdateOrder(String lpnNumber);

	/**
	 * update lpn corresponding void message response
	 * @param lpnNumber
	 */
	public void updateLPNForVoidResponse(String lpnNumber);

	/**
	 * fetch for LPN.MISC_INSTR_CODE_1 
	 * @param lpnNumber
	 * @return
	 */
	public String getLpnMiscInstrCode(String lpnNumber);

	/**
	 * fetch LPN's cFacilityId
	 * @param lpnNumber
	 * @return
	 */
	public Long getLpncFacilityId(String lpnNumber);

	/**
	 * fetch LPN's tcCompanyId
	 * @param lpnNumber
	 * @return
	 */
	public int getTcCompanmyIdForLPN(String lpnNumber);

	/**
	 * fetch LPN's "lastUpdatedSource"
	 * @param lpnNumber
	 * @return
	 */
	public String getLpnLastUpdatedSource(String lpnNumber);

	/**
	 * If the order line associated with the LPN is in weighed status, the order line status needs to be updated back to packed status 
	 * @param lpnNumber
	 */
	public void checkAndUpdateOrderLineItemForVoidResponse(String lpnNumber);

	/**
	 * If the order for the LPN (LPN.TC_ORDER_ID) is in weighed status, the order needs be updated back to packed status (ORDERS.DO_STATUS = 150)
	 * @param lpnNumber
	 */
	public void checkAndUpdateOrderForVoidResponse(String lpnNumber);

	/**
	 * tran_log entry for void response
	 * @param request 
	 * @return 
	 */
	public int tranLogEntryForVoidResponse(HttpServletRequest request);

	/**
	 * tran_log entry for void response
	 * @param request 
	 * @return 
	 */
	public int tranLogEntryForOrderLoadRequest(HttpServletRequest request);

	/**
	 * tran_log entry for void response Acknowledgment from WM to Agile
	 * @param request 
	 */
	public void tranLogEntryForVoidResponseAck(HttpServletRequest request);

	/**
	 * tran_log entry for order load response Acknowledgment from WM to Agile
	 * @param request 
	 */
	public void tranLogEntryForOrderLoadRequestAck(HttpServletRequest request);
	
	public IMHEOutboundDAO getMheOutboundDao();

	/** Updates <code>RESULT_CODE</CODE> in TRAN_LOG table.
	 * @param tranLogId
	 * @param result_code
	 */
	public void updateTranLogEntry(int tranLogId, String result_code);

}

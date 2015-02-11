package com.manh.wmos.services.mheoutbound.agile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.context.ApplicationContext;

import com.logistics.javalib.util.Misc;
import com.manh.cboframework.syscode.domain.SysCode;
import com.manh.ils.db.AppContextUtil;
import com.manh.ils.wmservices.hibernate.EventMessage;
import com.manh.integration.core.router.response.IRouterResponse;
import com.manh.integration.core.router.response.IRouterResponseDetails;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;
import com.manh.wm.core.util.WMLogger;
import com.manh.wmos.services.mheoutbound.agile.dao.HTPCAgileDAOImpl;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCAgileEnum;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCHeaderData;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCInternational;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCItem;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCLineItem;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCLineItems;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCOrder;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCPierbridgeOrderLoadRequestData;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCRecord;
import com.manh.wmos.services.mheoutbound.agile.data.HTPCShip;
import com.manh.wmos.services.outbound.agile.data.IHTPCWMAgileCommunicationConstants;
import com.manh.wmos.services.systemctrl.helper.SystemCtrlServiceLocator;
import com.manh.wmos.services.wmcommon.helper.MheEventMsgHelper.MheMsgStatCode;

public class HTPCAgileOrderLoadRequest
{

	static WMLogger				log;
	private static EventMessage	eventMessage;

	public static EventMessage getEventMessage()
	{
		return eventMessage;
	}

	public static void setEventMessage(EventMessage eventMessage)
	{
		HTPCAgileOrderLoadRequest.eventMessage = eventMessage;
	}

	/**
	 * Here the whole process of sending order load request to Agile takes
	 * place.
	 * 
	 * @param eventId
	 * @param eventMsg
	 * @return
	 */
	public static boolean customAgileMessaging(int eventId, EventMessage eventMsg)
	{

		HTPCWMAgileLogHelper.logDebug("AgileOrderLoadRequest : in customAgileMessaging(" + eventId + ")");

		setEventMessage(eventMsg);
		String tc_lpn_id = eventMsg.getEkOlpnNbr();
		String whse = eventMsg.getWhse();

		IRouterResponse routerResp = null;

		// getting relevant LPN data from database.
		HashMap<String, String> headerRSdata = getAgileDAO().getHeaderData(tc_lpn_id, whse);
		String lpnFacilityStatus, lpnId, orderId = Misc.EMPTY_STRING;
		if (headerRSdata != null)
		{
			lpnId = headerRSdata.get("LPN_ID");
			lpnFacilityStatus = headerRSdata.get("LPN_FACILITY_STATUS");
			orderId = headerRSdata.get("ORDER_ID");
			HTPCWMAgileLogHelper.logDebug("LPN Id received : [" + lpnId + "], LPN Status is : [" + lpnFacilityStatus + "], ORDER Id is : [" + orderId + "]");

			if (Integer.valueOf(lpnFacilityStatus) == 20)
			{
				List<?> lineItemsRSdata = getAgileDAO().getLineItemsData(lpnId, orderId);

				// populating the Agile Data holder pojos.
				HTPCPierbridgeOrderLoadRequestData data = populateAgileDataBeans(headerRSdata, lineItemsRSdata, eventId);

				// send to velocity for xml generation and get the response from
				// Router.
				if (data != null)
				{
					String xmlRespFromAgile = Misc.EMPTY_STRING;
					HTPCWMAgileLogHelper.logEnter("HTPCPierbridgeOrderLoadRequestData data received for routing to Agile");
					routerResp = HTPCAgileTranslator.routeAgileRequestMessage(eventMsg, data);
					String respStatus = routerResp.getPersistenceState().getLiteral();
					String[] agileResponseCodeAndOrderId = null;
					if (respStatus.equalsIgnoreCase("COMPLETE"))
					{
						List<IRouterResponseDetails> routerRespDtlList = routerResp.getRouterResponseDetails();
						for (IRouterResponseDetails resp : routerRespDtlList)
						{
							xmlRespFromAgile = (String) resp.getCommResponse().getResponseDetails();
							HTPCWMAgileLogHelper.logDebug("Response from AGILE server : \n" + xmlRespFromAgile);
							agileResponseCodeAndOrderId = parseAgileRespose(xmlRespFromAgile);
						}
						if (agileResponseCodeAndOrderId[1].equals("1"))
						{
							getAgileDAO().updateLpnRefField2(agileResponseCodeAndOrderId[0], lpnId);

							// updates STAT_CODE in EVENT_MESSAGE to '50', which
							// indicates message has been sent to Agile.
							eventMsg.setErrorSeqNbr(HTPCAgileEnum.HTPCErrorSeqNbr.SUCCESS.value);
							eventMsg.setStatCode(50);
							getAgileDAO().getMheOutboundDao().updateEventMessage(eventMsg);
							// tran_log and tran_log_message entry.
							getAgileDAO().createTranLogAndTranLogMessageEntry(IHTPCWMAgileCommunicationConstants.OLR_SUCCESS, xmlRespFromAgile);
							HTPCWMAgileLogHelper.logDebug("Updated STAT_CODE to 50, and for LPN_ID : " + lpnId);
						}
						else
						{
							eventMsg.setErrorSeqNbr(HTPCAgileEnum.HTPCErrorSeqNbr.IMPROPER_OLR_RESPONSE.value); // 997.
							eventMsg.setStatCode(MheMsgStatCode.SEND_MSG_ERROR.stat); // 96.
							getAgileDAO().getMheOutboundDao().updateEventMessage(eventMsg);
							// tran_log and tran_log_message entry.
							getAgileDAO().createTranLogAndTranLogMessageEntry(IHTPCWMAgileCommunicationConstants.OLR_FAILURE, xmlRespFromAgile);
							HTPCWMAgileLogHelper.logDebug("Updated STAT_CODE to 96.");
						}
					}
				}
				else
				{
					HTPCWMAgileLogHelper.logDebug("HTPCPierbridgeOrderLoadRequestData data is NULL.");
				}
				return true;
			}
			else
			{
				eventMsg.setErrorSeqNbr(HTPCAgileEnum.HTPCErrorSeqNbr.INVALID_FACILITY_STATUS.value); // i.e.
																										// 996
				eventMsg.setStatCode(MheMsgStatCode.SEND_MSG_ERROR.stat); // i.e.
																			// 96.
				getAgileDAO().getMheOutboundDao().updateEventMessage(eventMsg); // setting
																				// the
																				// status
																				// to
																				// Failure.
				HTPCWMAgileLogHelper.logDebug("Error: LPN Status is NOT equal to 20");
				return false;
			}
		}
		HTPCWMAgileLogHelper.logDebug("Error: Header Data received is NULL.");
		return false;

	}

	/**
	 * This method takes the xml response from Agile server as input and parse
	 * extracts OrderHeaderID & Code elements from it. It then returns both of
	 * them in a String array of two elements, with OrderHeaderID at index 0 and
	 * Code at index 1.
	 * 
	 * @param xmlRespFromAgile
	 * @return
	 */
	private static String[] parseAgileRespose(String xmlRespFromAgile)
	{
		HTPCWMAgileLogHelper.logEnter("HTPCAgileOrderLoadRequest : Entering getAgileResposeCode()..");
		SAXBuilder builder = new SAXBuilder();
		Document document;
		String[] codeAndOrderId = new String[2];
		try
		{
			document = (Document) builder.build(new ByteArrayInputStream(xmlRespFromAgile.getBytes()));
			Element rootNode = document.getRootElement();
			codeAndOrderId[0] = rootNode.getChildText("OrderHeaderID");
			List<?> list = rootNode.getChildren("Status");
			Element node = (Element) list.get(0);
			codeAndOrderId[1] = node.getChildText("Code");
			return codeAndOrderId; // [0] is OrderHeaderID and [1] is Code.
		}
		catch (JDOMException | IOException e)
		{
			log.logException(e);
		}
		return codeAndOrderId;
	}

	/**
	 * Here the data is pushed into all the Agile Data holding pojo beans.
	 * 
	 * @param hdata
	 * @param lidata
	 * @param eventId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static HTPCPierbridgeOrderLoadRequestData populateAgileDataBeans(HashMap<String, String> hdata, List<?> lidata, int eventId)
	{

		HTPCWMAgileLogHelper.logDebug("In populateAgileDataBeans()...");

		HTPCPierbridgeOrderLoadRequestData pbolrDataBean = new HTPCPierbridgeOrderLoadRequestData();

		HTPCHeaderData headerBean = new HTPCHeaderData();
		HTPCShip shipdataBean = new HTPCShip();
		HTPCOrder orderdataBean = new HTPCOrder();
		HTPCRecord recorddataBean = new HTPCRecord();

		List<HTPCLineItems> lineItemsList = new ArrayList<HTPCLineItems>();
		HTPCLineItems lineitemsBean = null;
		HTPCLineItem lineitemdata = null;
		HTPCItem itemdata = null;
		HTPCInternational itnldata = null;

		// headerRSdata OR hdata cursor has already moved to first position,
		// therefore no next() call.
		shipdataBean.setTo(hdata.get("D_NAME"));
		shipdataBean.setCompany("12272");
		shipdataBean.setAddressOne(hdata.get("D_ADDRESS_1"));
		shipdataBean.setAddressTwo(hdata.get("D_ADDRESS_2"));
		shipdataBean.setAddressThree(hdata.get("D_ADDRESS_3"));
		shipdataBean.setCity(hdata.get("D_CITY"));
		shipdataBean.setState(hdata.get("D_STATE_PROV"));
		shipdataBean.setZip(hdata.get("D_POSTAL_CODE"));
		shipdataBean.setCountry(hdata.get("D_COUNTRY_CODE"));
		shipdataBean.setPhone(hdata.get("D_PHONE_NUMBER"));
		shipdataBean.setEmail(hdata.get("D_EMAIL"));

		orderdataBean.setShipViaCode(hdata.get("DSG_SHIP_VIA"));

		recorddataBean.setKeyThree(String.valueOf(getEventMessage().getEventMessageId()));

		headerBean.setDeliveryBy(hdata.get("DELIVERY_END_DTTM"));
		headerBean.setOrderNumber(hdata.get("TC_LPN_ID"));
		headerBean.setShip(shipdataBean);
		headerBean.setOrder(orderdataBean);
		headerBean.setRecord(recorddataBean);

		Iterator<?> itr = lidata.iterator();
		while (itr.hasNext())
		{
			HashMap<String, String> hm = (HashMap<String, String>) itr.next();
			lineitemsBean = new HTPCLineItems();
			lineitemdata = new HTPCLineItem();
			itemdata = new HTPCItem();
			itnldata = new HTPCInternational();

			itemdata.setPartNumber(hm.get("ITEM_NAME"));
			itemdata.setDescription(hm.get("DESCRIPTION"));
			itemdata.setQuantity(hm.get("SIZE_VALUE"));
			itemdata.setWeight(hm.get("ESTIMATED_WEIGHT"));
			itemdata.setUnitPrice(hm.get("PRICE"));
			itemdata.setUnitOfMeasure(hm.get("SIZE_UOM"));

			itnldata.setCountryOfOrigin(hm.get("CNTRY_OF_ORGN"));

			lineitemdata.setItem(itemdata);
			lineitemdata.setInternational(itnldata);

			lineitemsBean.setLineItem(lineitemdata);
			lineItemsList.add(lineitemsBean);
		}

		HTPCWMAgileLogHelper.logDebug("Line Items List is : " + lineItemsList);

		try
		{
			pbolrDataBean.setUsername(getMiscFlagValFromSysCode());
		}
		catch (Exception e)
		{
			log.logDebug("Got Exception while trying to get the sys_code from in populateAgileDataBeans()");
			log.logException(e);
		}

		if (eventId == 9002 || eventId == 9003)
			pbolrDataBean.setOrderHeaderId(hdata.get("REF_FIELD_2"));

		pbolrDataBean.setHeader(headerBean);
		pbolrDataBean.setLineItems(lineItemsList);

		HTPCWMAgileLogHelper.logDebug("Finished poulating Data Beans..");

		return pbolrDataBean;
	}

	/**
	 * Here, we get the DAO implementation class for EX01 i.e. WM
	 * <--Interfacing--> AGILE.
	 * 
	 * @return
	 */
	private static HTPCAgileDAOImpl getAgileDAO()
	{
		HTPCWMAgileLogHelper.logDebug("getting Agile DAO..");
		@SuppressWarnings("deprecation")
		ApplicationContext ctx = AppContextUtil.getAppCtx("wm.mheOutbound");
		HTPCAgileDAOImpl dao = (HTPCAgileDAOImpl) ctx.getBean("HTPCAgileDAOImpl");
		return dao;
	}

	/**
	 * Extracts and returns the Misc Flag value in form of a String.
	 * 
	 * @return
	 */
	private static String getMiscFlagValFromSysCode()
	{
		String miscFlag = Misc.EMPTY_STRING;
		SysCode sysCode = SystemCtrlServiceLocator.getSysCodeConfigManager().getSysCode("B", "780", "1");
		if (sysCode != null)
		{
			miscFlag = sysCode.getMiscFlags().trim();
			HTPCWMAgileLogHelper.logDebug("Misc Flag Value : " + miscFlag);
		}
		return miscFlag;
	}

}

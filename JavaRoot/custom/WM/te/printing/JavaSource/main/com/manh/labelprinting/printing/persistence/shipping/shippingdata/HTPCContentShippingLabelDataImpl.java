package com.manh.labelprinting.printing.persistence.shipping.shippingdata;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.logistics.javalib.util.FiniteValue.FiniteValueNotFoundException;
import com.logistics.javalib.util.Misc;
import com.manh.baseservices.service.CBOBaseLocalServiceLocator;
import com.manh.baseservices.service.CBOBaseServiceLocator;
import com.manh.cbo.application.item.IItemCBOService;
import com.manh.cbo.domain.item.Item;
import com.manh.cbo.transactional.domain.distributionorder.DistributionOrder;
import com.manh.cbo.transactional.domain.lpn.subobjects.LPNDetail;
import com.manh.cboframework.application.syscode.ISysCodeConfigService;
import com.manh.labelprinting.printing.dao.HTPCLabelPrintClassDAO;
import com.manh.labelprinting.printing.hibernate.LPAllocInvnDtl;
import com.manh.labelprinting.printing.hibernate.LPTaskDtl;
import com.manh.labelprinting.printing.processor.LabelPrintHelper;
import com.manh.labelprinting.printing.util.LabelUtil;
import com.manh.labelprinting.services.labelprinting.exception.LabelPrintingRuntimeException;
import com.manh.wmos.cbo.domain.item.ItemWmos;

public class HTPCContentShippingLabelDataImpl extends ContentShippingLabelDataImpl
{

	protected String		miscField			= Misc.EMPTY_STRING;
	boolean					ex04Enable			= false;
	protected String		orderBrand			= Misc.EMPTY_STRING;
	HashMap<String, String>	code_id_desc_map	= new HashMap<String, String>();

	public HTPCContentShippingLabelDataImpl()
	{
		super();
		log.logEnter("HTPCContentShippingLabelDataImpl");
		code_id_desc_map = ((HTPCLabelPrintClassDAO) LabelPrintHelper.getLabelPrintClassDAO()).getSysCodeIDAndDescMapping("S", "901");
	}

	/**
	 * Custom print method for HTPC Extension 04.
	 */
	@SuppressWarnings("unchecked")
	public void print() throws Exception
	{
		log.logEnter("HTPCContentShippingLabelDataImpl::print");

		orderBrand = getShippingData().getOrder().getRefField1String();
		log.logEnter("Order Brand: " + orderBrand);

		// gets the MiscFlag map for custom code id
		HashMap map = null;
		if (!Misc.isNullMap(getMiscCustomCode()))
			map = getMiscCustomCode();

		if (map != null)
		{
			miscField = (String) map.get(orderBrand);
			// check the Misc flag 1-1 is set to Y/1.
			if (miscField != null && (miscField.substring(0, 1).equalsIgnoreCase("Y") || miscField.substring(0, 1).equals("1")))
			{
				ex04Enable = true;
				log.logDebug("EX 04 is ENABLED !");
			}
			else
			{
				log.logDebug("EX 04 is NOT Enabled, misc field byte 1 is not 1 or Y");
			}
		}

		if (!ex04Enable)
		{
			log.logDebug("EX04 not enabled calling base version....");
			super.print();
			return;
		}

		setSubstitutions();

		/* EX04 Call Header substitution map starts */
		Map<String, Object> ex04HeaderSubMap = new HashMap<String, Object>();

		ex04HeaderSubMap = getHeaderSubstitutionMap();
		/* EX04 Call Header substitution map ends */

		String labelTemplateName = Misc.EMPTY_STRING;
		for (int i = 1; i <= m_labelSubsPageNbrMap.size(); i++)
		{
			if (i == 1 || i == 2)
				labelTemplateName = getLabelTemplateName(i);
			log.logDebug(" Label Template Name: " + labelTemplateName);

			if (Misc.isNullTrimmedString(labelTemplateName))
			{
				// if template is null , failing the transaction.
				log.logExit("custom print().. the template loaded is NULL.");
				return;
			}
			else
			{
				m_labelName = labelTemplateName;
			}

			if (m_labelSubsPageNbrMap.containsKey(Integer.toString(i)))
			{
				m_labelSubsTemp.clear();
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap = (Map<String, Object>) m_labelSubsPageNbrMap.get(Integer.toString(i));

				m_labelSubsTemp.putAll(tempMap);
				/* ex04 header map merge - Custom Change */
				if (Misc.isNullMap(ex04HeaderSubMap))
				{
					log.logExit("The map after Header Substitution is NULL. Exiting..");
					return;
				}
				else
				{
					m_labelSubsTemp.putAll(ex04HeaderSubMap);
					log.logDebug("Intiating label printing..");
					printLabel();
				}
			}
		}
	}

	/**
	 * Returns the number of details.
	 * 
	 * @param pageNbr
	 *            - number of pages
	 * @return
	 */
	public int getLinesOnLabel(int pageNbr)
	{
		log.logEnter("HTPCContentShippingLabelDataImpl::getLinesOnLabel");
		int numberOfDetails = 0;

		try
		{
			if (!Misc.isNullTrimmedString(miscField))
			{
				if (pageNbr == 1)
				{
					log.logDebug("For the first page....");
					String hauteCashOrder = getShippingData().getOrder().getRefField9String();
					if (!Misc.isNullTrimmedString(hauteCashOrder))
					{
						log.logDebug("Its a Haute Cash Order!");

						if (!Misc.isNullTrimmedString(miscField.substring(28, 37)))
						{
							numberOfDetails = Integer.parseInt(miscField.substring(25, 28).trim());
							log.logDebug("Number of Details from bytes 26-28 : " + numberOfDetails);
						}
						else
						{
							log.logDebug("Unable to get the bytes 26-28"); // From C-PCK.
						}
					}
					else
					{
						log.logDebug("Its NOT a Haute Cash Order!");

						if (!Misc.isNullTrimmedString(miscField.substring(4, 13)))
						{
							numberOfDetails = Integer.parseInt(miscField.substring(1, 4).trim());
							log.logDebug("Number of Details from bytes 2-4 : " + numberOfDetails);
						}
						else
						{
							log.logDebug("Unable to get the bytes 2-4"); // From C-PCK.
						}
					}
				}
				else
				{
					log.logDebug("Pages other than first page....");
					if (!Misc.isNullTrimmedString(miscField.substring(16, 25)))
					{
						numberOfDetails = Integer.parseInt(miscField.substring(13, 16).trim());
						log.logDebug("Number of Details bytes 14-16 : " + numberOfDetails);
					}
					else
					{
						log.logDebug("Unable to get the bytes 14-16"); // From C-PCK.
					}
				}
			}
		}
		catch (Exception ex)
		{
			log.logDebug("Exception in getLabelTemplateName..." + ex);
		}

		log.logExit("HTPCContentShippingLabelDataImpl::getLinesOnLabel");
		return numberOfDetails;
	}

	/**
	 * Fetchs mark magic custom shipping label printing template for specified page.
	 * 
	 * @param pageNbr
	 *            - Page number of the label.
	 * @return
	 */
	public String getLabelTemplateName(int pageNbr)
	{
		log.logEnter("HTPCContentShippingLabelDataImpl::getLabelTemplateName");
		String labelTemplateName = Misc.EMPTY_STRING;
		try
		{
			if (!Misc.isNullTrimmedString(miscField))
			{
				if (pageNbr == 1)
				{
					log.logDebug("For the first page....");
					String hauteCashOrder = getShippingData().getOrder().getRefField9String();
					if (!Misc.isNullTrimmedString(hauteCashOrder))
					{
						log.logDebug("Its a Haute Cash Order!");
						labelTemplateName = miscField.substring(28, 37).trim();
						log.logDebug("Template picked from bytes 29-37 for printing is : [" + labelTemplateName + "]");
					}
					else
					{
						log.logDebug("Its NOT a Haute Cash Order!");
						labelTemplateName = miscField.substring(4, 13).trim();
						log.logDebug("Template picked from bytes 5-13 for printing is : [" + labelTemplateName + "]");
					}

				}
				else
				{
					log.logDebug("Pages other than first page....");
					labelTemplateName = miscField.substring(16, 25).trim();
					log.logDebug("Template picked frin bytes 17-25 for printing is : [" + labelTemplateName + "]");
				}
			}
		}
		catch (Exception e)
		{
			log.logDebug("Exception in getLabelTemplateName..." + e);
		}

		log.logExit("HTPCContentShippingLabelDataImpl::getLabelTemplateName, returning Template Name : " + labelTemplateName);
		return labelTemplateName;
	}

	/* base copy paste */
	public void printLabelViaoLPNDetails() throws Exception
	{
		log.logEnter("HTPCContentShippingLabelDataImpl::printLabelViaoLPNDetails");

		String space = " ";

		Double totalQty = 0.0;
		Double totalPrice = 0.0;
		BigDecimal totalWtLb = new BigDecimal(0);
		BigDecimal totalWtOz = new BigDecimal(0);

		if (shippingData.getOLPN().getLpnDetailSet() == null)
			return;

		Iterator<?> it = shippingData.getOLPN().getLpnDetailSet().iterator();

		int lineNbr = 1;
		int lineNbrOnPage = 1;
		int pageNbr = 0;
		int numOfDetails = shippingData.getOLPN().getLpnDetailSet().size();

		boolean eanLoaded = false;

		// For ITEM* custom substitutions
		labelUtil.setLabelGroup("CL");

		labelUtil.setOrder(getShippingData().getOrder());
		labelUtil.setOLPN(getShippingData().getOLPN());
		labelUtil.setShipVia(getShippingData().getShipVia());
		labelUtil.setCarrier(getShippingData().getCarrier());
		labelUtil.loadDestCntry();

		String dspLocn = Misc.EMPTY_STRING;

		// Custom changes for HTPC EX04 - START.
		Map<String, List<String>> oliMap = (Map<String, List<String>>) ((HTPCLabelPrintClassDAO) LabelPrintHelper.getLabelPrintClassDAO()).getOrderLineItemMap(shippingData.getOLPN().getTcLpnId());
		int linesOnLabel = 0;
		// Custom changes for HTPC EX04 - END.

		while (numOfDetails > 0)
		{
			// first call commonSubs as it clears the previous substitutions
			commonSubs();

			if (!eanLoaded)
			{
				loadEanData();
				eanLoaded = true;
			}
			// Custom changes for HTPC EX04 - START
			if (pageNbr == 1 || pageNbr == 2)
				linesOnLabel = getLinesOnLabel(pageNbr);
			log.logDebug("Lines On Label: " + linesOnLabel);
			if (linesOnLabel != 0)
				m_linesOnlabel = linesOnLabel;
			else
			{
				// failing txn - either the label template or the number of lines on te, plates are not defined.
				log.logExit("Lines On Label is 0, exiting..");
				return;
			}
			// Custom changes for HTPC EX04 - END

			pageNbr++;
			String strPageNbr = String.valueOf(pageNbr);
			m_labelSubs.put(("page_nbr"), strPageNbr);

			String current_time = now();
			m_labelSubs.put(("current_time"), current_time);
			String cartonType = (getShippingData().getOLPN().getContainerType());
			m_labelSubs.put(("carton_type"), cartonType);
			String cartonSize = (getShippingData().getOLPN().getContainerSize());
			m_labelSubs.put(("carton_size"), cartonSize);
			String carton_type_size = (cartonType + cartonSize);
			m_labelSubs.put(("carton_type_size"), carton_type_size);

			lineNbr = 1;

			// while the current line number is not more than the number of lines on the label
			while (lineNbr <= m_linesOnlabel)
			{
				if (numOfDetails != 0)
				{
					// Putting detail info on label
					if (it.hasNext())
					{
						LPNDetail lpnDetail = (LPNDetail) it.next();

						String strLineNbr = String.valueOf(lineNbr);
						String strLineNbrOnPage = String.valueOf(lineNbrOnPage);
						String line = (("line_") + strLineNbr);

						m_labelSubs.put(line, strLineNbrOnPage);
						m_labelSubs.put(("location_") + strLineNbr, dspLocn);

						Item item = lpnDetail.getItem();
						if (item == null)
						{
							IItemCBOService cboBD = CBOBaseLocalServiceLocator.getItemCBOService();
							item = new Item();
							item.setItemId(lpnDetail.getItemId());
							item.persist(true);

							try
							{
								item = cboBD.getItem(null, item);
							}
							catch (Exception e)
							{
								log.logException(e);
								log.logHigh("Could not load item " + lpnDetail.getItemId());
								throw new LabelPrintingRuntimeException(e);
							}

							lpnDetail.setItem(item);
						}

						String dspSku = Misc.EMPTY_STRING;
						String itemCboDesc = Misc.EMPTY_STRING;
						double unitWeight = 0, unitPrice = 0;
						if (item != null)
						{
							dspSku = item.getItemName();
							itemCboDesc = item.getDescription();

							if (item.getUnitWeight() != null)
								unitWeight = item.getUnitWeight();

							ItemWmos itemWm = (ItemWmos) item.getItemWmos();
							if (itemWm != null && itemWm.getUnitPrice() != null)
							{
								unitPrice = itemWm.getUnitPrice();
							}
						}

						m_labelSubs.put(("dsp_sku_") + strLineNbr, dspSku);
						m_labelSubs.put(("Item_cbo_desc_") + strLineNbr, itemCboDesc);

						// Add ITEM specific custom substitutions
						labelUtil.setItemLabelSubstitutions(lpnDetail.getItemId(), shippingData.getWhse().getFacilityId(), strLineNbr, m_labelSubs);

						// Displaying pkt_dtl.cust_sku instead of xref.vendor_brcd per JP.
						double price = fetchRetailPrice(lpnDetail.getDistributionOrderDtlId());
						String custSkuVal = fetchCustomerItem(lpnDetail.getDistributionOrderDtlId());
						String refFieldValue = fetchRefValue(lpnDetail.getDistributionOrderDtlId());

						// comparing custSkuVal with null was giving false even when the value of custSkuVal was null, so comparison changed to !=null
						custSkuVal = (custSkuVal != null) ? custSkuVal : space;
						refFieldValue = (refFieldValue != null) ? refFieldValue : space;
						m_labelSubs.put(("ref_") + strLineNbr, refFieldValue);
						m_labelSubs.put(("cust_sku_") + strLineNbr, custSkuVal);

						double quantity = 0.0;
						long lpnStatus = 0L;

						try
						{
							lpnStatus = Long.parseLong(shippingData.getOLPN().getFacilityStatus().getCode());
						}
						catch (FiniteValueNotFoundException ex)
						{
							log.logException(ex);
						}

						if (lpnStatus >= 20)
						{
							quantity = lpnDetail.getSizeValue();
						}
						else
						{
							quantity = lpnDetail.getInitialQty();
						}

						totalQty += quantity;
						String qtyStr = labelUtil.qtyToString(quantity);
						qtyStr = LabelUtil.localizedBigDecimalString(qtyStr, getLabelPrintingData().getLocale());
						m_labelSubs.put(("qty_") + strLineNbr, qtyStr);
						m_labelSubs.put(("packed_qty_") + strLineNbr, qtyStr);

						Double itemPrice = (quantity * unitPrice);
						BigDecimal LbToOz = new BigDecimal(16);
						LbToOz = LbToOz.setScale(2, BigDecimal.ROUND_HALF_UP);
						BigDecimal itemWeight = new BigDecimal(quantity * unitWeight);
						itemWeight = itemWeight.setScale(2, BigDecimal.ROUND_HALF_UP);
						BigDecimal itemWeightLb = new BigDecimal(itemWeight.longValue());
						itemWeightLb = itemWeightLb.setScale(2, BigDecimal.ROUND_HALF_UP);
						BigDecimal itemWeightOz = (itemWeight.subtract(itemWeightLb)).multiply(LbToOz);
						itemWeightOz = itemWeightOz.setScale(2, BigDecimal.ROUND_HALF_UP);

						totalWtLb = totalWtLb.add(itemWeightLb);
						totalWtOz = totalWtOz.add(itemWeightOz);
						totalPrice += itemPrice;

						double val = itemWeightOz.doubleValue();
						val = val * 100;
						val = Math.round(val);
						val = val / 100;

						m_labelSubs.put(("item_weight_lb_") + strLineNbr, itemWeightLb.toString());
						m_labelSubs.put(("item_weight_oz_") + strLineNbr, itemWeightOz.toString());
						m_labelSubs.put(("ups_sp_value_") + strLineNbr, itemPrice.toString());

						String suggRetailPrice = null;
						if (price != 0.0)
							suggRetailPrice = priceToString(price);
						else
							suggRetailPrice = ("       ");

						suggRetailPrice = LabelUtil.localizedBigDecimalString(suggRetailPrice, getLabelPrintingData().getLocale());
						m_labelSubs.put(("sugg_rtl_price_") + strLineNbr, suggRetailPrice);

						// Custom code for HTPC EX 04 - START.
						if (ex04Enable)
						{
							log.logDebug("Calling setCustomSubstitutions for HTPC - EX04 with LPN_DETAIL Iteration");
							setCustomSubstitutions(lpnDetail, quantity, strLineNbr, oliMap);
						}
						// Custom changes for HTPC EX04 - END.

						numOfDetails--;
					}
				}

				lineNbr++;
				lineNbrOnPage++;
			}

			// while we have details set the other common fields set the ship from and consignee ship to fields for sure post content label
			setUPSSurePostData();

			int deptId = getShippingData().getOrder().getMerchandizingDepartmentId();
			String dept = null;
			if (deptId > 0)
				dept = fetchDept(deptId);

			if (dept == null)
				dept = space;

			m_labelSubs.put(("dept"), dept);

			if (getShippingData().getOLPN().getLpnDetailList().size() > (m_linesOnlabel * pageNbr))
			{
				m_labelSubs.put(("continued"), ("CONTINUED..."));
			}
			else
			{
				String totalQtyStr = labelUtil.qtyToString(totalQty);
				totalQtyStr = LabelUtil.localizedBigDecimalString(totalQtyStr, getLabelPrintingData().getLocale());

				m_labelSubs.put(("continued"), ("Total Qty : ") + totalQtyStr);
				m_labelSubs.put(("item_weight_total_lb"), totalWtLb.toString());
				m_labelSubs.put(("item_weight_total_oz"), totalWtOz.toString());
				m_labelSubs.put(("ups_sp_total_value"), totalPrice.toString());
			}

			log.logDebug("Exit:ContentShippingLabelDataImpl::printLabelViaoLPNDetails");
			// print label for each page
			// Perform Dynamic Substitution for each page
			getDynamicLabelSubs();
			if (!m_labelSubsPageNbrMap.containsKey(strPageNbr))
			{
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.putAll(m_labelSubs);
				m_labelSubsPageNbrMap.put(strPageNbr, tempMap);
			}
			log.logDebug("Intiating label printing..");
			printLabel();
		}

	}

	public void printLabelViaAllocations() throws Exception
	{
		// Commented since not used, if required fetch it from Documentation profile object as coded in ContentShippingLabelData

		log.logEnter("HTPCContentShippingLabelDataImpl::printLabelViaAllocations");

		String blank = Misc.EMPTY_STRING;
		String space = ((" "));

		double totalQty = 0.0;
		Double totalPrice = 0.0;
		BigDecimal totalWtLb = new BigDecimal(0);
		BigDecimal totalWtOz = new BigDecimal(0);

		List<LPAllocInvnDtl> allocList = null;
		List<LPTaskDtl> taskList = null;
		int numOfDetails;
		Iterator it;

		// load the task details
		taskList = loadTaskDetailList();

		if (Misc.isNullList(taskList))
		{
			// if task details dont exist load the allocation details
			allocList = loadAllocDetailList();

			if (Misc.isNullList(allocList))
			{
				return;
			}
			else
			{
				it = allocList.iterator();
				numOfDetails = allocList.size();
				m_labelSubs.put("ObjectList_ALLOC_INVN_DTL", allocList);
			}
		}
		else
		{
			numOfDetails = taskList.size();
			it = taskList.iterator();
		}
		int lineNbr = 1;
		int lineNbrOnPage = 1;
		int pageNbr = 0;

		boolean eanLoaded = false;

		// calculate total no of pages to be print for labels
		double totalNbrofPagesToPrint = Math.ceil(((double) numOfDetails / m_linesOnlabel));

		// For ITEM* custom substitutions
		labelUtil.setLabelGroup("CL");
		labelUtil.setOrder(getShippingData().getOrder());
		labelUtil.setOLPN(getShippingData().getOLPN());
		labelUtil.setShipVia(getShippingData().getShipVia());
		labelUtil.setCarrier(getShippingData().getCarrier());
		labelUtil.loadDestCntry();

		String dspLocn = Misc.EMPTY_STRING;
		String currentPullLocnId = Misc.EMPTY_STRING;

		Map<String, List<String>> oliMap = (Map<String, List<String>>) ((HTPCLabelPrintClassDAO) LabelPrintHelper.getLabelPrintClassDAO()).getOrderLineItemMap(shippingData.getOLPN().getTcLpnId());
		int linesOnLabel = 0;
		while (numOfDetails > 0)
		{
			// first call commonSubs as it clears the previous substitutions
			commonSubs();

			if (!eanLoaded)
			{
				loadEanData();
				eanLoaded = true;
			}
			pageNbr++;
			String strPageNbr = String.valueOf(pageNbr);
			m_labelSubs.put(("page_nbr"), strPageNbr);

			// custom code for HTPC EX04 - Start.
			if (pageNbr == 1 || pageNbr == 2)
				linesOnLabel = getLinesOnLabel(pageNbr);

			log.logDebug("Lines On Label: " + linesOnLabel);
			if (linesOnLabel != 0)
				m_linesOnlabel = linesOnLabel;
			else
			{
				log.logExit("linesOnLabel = 0, hence failing the transaction..");
				return;
			}
			// custom code for HTPC EX04 - End.

			String current_time = now();
			m_labelSubs.put(("current_time"), current_time);
			String cartonType = (getShippingData().getOLPN().getContainerType());
			m_labelSubs.put(("carton_type"), cartonType);
			String cartonSize = (getShippingData().getOLPN().getContainerSize());
			m_labelSubs.put(("carton_size"), cartonSize);
			String carton_type_size = (cartonType + cartonSize);
			m_labelSubs.put(("carton_type_size"), carton_type_size);

			// do not know what these fields are for, substituting empty strings for now
			m_labelSubs.put(("Text0128"), blank);
			m_labelSubs.put(("Text0039"), blank);
			m_labelSubs.put(("Text0061"), blank);

			lineNbr = 1;

			// while the current line number is not more than the number of lines on the label
			while (lineNbr <= m_linesOnlabel)
			{
				if (numOfDetails == 0)
				{
					// If we don't have a detail blank out subs
					String strLineNbr = String.valueOf(lineNbr);
					m_labelSubs.put(("cust_sku_") + strLineNbr, space);
				}
				else
				{ // Putting detail info on label
					if (!Misc.isNullList(taskList))
					{

						if (it.hasNext())
						{
							LPTaskDtl taskDetail = (LPTaskDtl) it.next();

							String strLineNbr = String.valueOf(lineNbr);
							String strLineNbrOnPage = String.valueOf(lineNbrOnPage);
							String line = (("line_") + strLineNbr);
							m_labelSubs.put(line, strLineNbrOnPage);

							boolean locnSet = false;
							if (Misc.isNullTrimmedString(currentPullLocnId) && !Misc.isNullTrimmedString(taskDetail.getPullLocnId()))
							{
								currentPullLocnId = taskDetail.getPullLocnId();
								locnSet = true;
							}
							if (locnSet || !currentPullLocnId.equals(taskDetail.getPullLocnId()))
							{
								dspLocn = LabelPrintHelper.getLabelPrintClassDAO().getDynamicColValueFromLocnHdr(taskDetail.getPullLocnId(), "DSP_LOCN");
							}
							currentPullLocnId = taskDetail.getPullLocnId();
							locnSet = false;

							m_labelSubs.put(("location_") + strLineNbr, dspLocn);

							IItemCBOService cboBD = CBOBaseLocalServiceLocator.getItemCBOService();
							Item item = new Item();
							item.setItemId(Long.valueOf(taskDetail.getItemId()));
							item.persist(true);

							try
							{
								item = cboBD.getItem(null, item);
							}
							catch (Exception e)
							{
								log.logException(e);
								log.logHigh("Could not load item " + taskDetail.getItemId());
								throw new LabelPrintingRuntimeException(e);
							}

							String dspSku = Misc.EMPTY_STRING;
							String itemCboDesc = Misc.EMPTY_STRING;
							double unitWeight = 0, unitPrice = 0;
							if (item != null)
							{
								dspSku = item.getItemName();
								itemCboDesc = item.getDescription();

								if (item.getUnitWeight() != null)
									unitWeight = item.getUnitWeight();

								ItemWmos itemWm = (ItemWmos) item.getItemWmos();
								if (itemWm != null && itemWm.getUnitPrice() != null)
								{
									unitPrice = itemWm.getUnitPrice();
								}
							}

							m_labelSubs.put(("dsp_sku_") + strLineNbr, dspSku);
							m_labelSubs.put(("Item_cbo_desc_") + strLineNbr, itemCboDesc);

							// Add ITEM specific custom substitutions
							labelUtil.setItemLabelSubstitutions(taskDetail.getItemId(), shippingData.getWhse().getFacilityId(), strLineNbr, m_labelSubs);

							// Displaying pkt_dtl.cust_sku instead of xref.vendor_brcd per JP.
							double price = fetchRetailPrice(taskDetail.getLineItemId());
							String custSkuVal = fetchCustomerItem(taskDetail.getLineItemId());
							String refFieldValue = fetchRefValue(taskDetail.getLineItemId());

							// comparing custSkuVal with null was giving false even when the value of custSkuVal was null, so comparison changed to !=null
							custSkuVal = (custSkuVal != null) ? custSkuVal : space;
							refFieldValue = (refFieldValue != null) ? refFieldValue : space;
							m_labelSubs.put(("ref_") + strLineNbr, refFieldValue);
							m_labelSubs.put(("cust_sku_") + strLineNbr, custSkuVal);

							double quantity = 0.0;
							long lpnStatus = 0L;

							try
							{
								lpnStatus = Long.parseLong(shippingData.getOLPN().getFacilityStatus().getCode());
							}
							catch (FiniteValueNotFoundException ex)
							{
								log.logException(ex);
							}

							if (lpnStatus >= 20)
							{
								quantity = taskDetail.getQtyPulld().doubleValue();
							}
							else
							{
								quantity = taskDetail.getQtyAlloc().doubleValue();
							}

							totalQty += quantity;
							String qtyStr = labelUtil.qtyToString(quantity);
							qtyStr = LabelUtil.localizedBigDecimalString(qtyStr, getLabelPrintingData().getLocale());
							m_labelSubs.put(("qty_") + strLineNbr, qtyStr);
							m_labelSubs.put(("packed_qty_") + strLineNbr, qtyStr);

							Double itemPrice = (quantity * unitPrice);
							BigDecimal LbToOz = new BigDecimal(16);
							LbToOz = LbToOz.setScale(2, BigDecimal.ROUND_HALF_UP);
							BigDecimal itemWeight = new BigDecimal(quantity * unitWeight);
							itemWeight = itemWeight.setScale(2, BigDecimal.ROUND_HALF_UP);
							BigDecimal itemWeightLb = new BigDecimal(itemWeight.longValue());
							itemWeightLb = itemWeightLb.setScale(2, BigDecimal.ROUND_HALF_UP);
							BigDecimal itemWeightOz = (itemWeight.subtract(itemWeightLb)).multiply(LbToOz);
							itemWeightOz = itemWeightOz.setScale(2, BigDecimal.ROUND_HALF_UP);

							totalWtLb = totalWtLb.add(itemWeightLb);
							totalWtOz = totalWtOz.add(itemWeightOz);
							totalPrice += itemPrice;

							double val = itemWeightOz.doubleValue();
							val = val * 100;
							val = Math.round(val);
							val = val / 100;

							m_labelSubs.put(("item_weight_lb_") + strLineNbr, itemWeightLb.toString());
							m_labelSubs.put(("item_weight_oz_") + strLineNbr, itemWeightOz.toString());
							m_labelSubs.put(("ups_sp_value_") + strLineNbr, itemPrice.toString());

							String suggRetailPrice = null;
							if (price != 0.0)
								suggRetailPrice = priceToString(price);
							else
								suggRetailPrice = ("       ");

							suggRetailPrice = LabelUtil.localizedBigDecimalString(suggRetailPrice, getLabelPrintingData().getLocale());

							m_labelSubs.put(("sugg_rtl_price_") + strLineNbr, suggRetailPrice);

							//Custom HTPC EX04 changes - Start.
							if (ex04Enable)
							{
								log.logDebug("Calling setCustomSubstitutions for HTPC-EX04 with LPN_DETAIL Iteration");
								Iterator<?> lpnDetailItr = shippingData.getOLPN().getLpnDetailSet().iterator();
								LPNDetail lpnDetail = null;
								while (lpnDetailItr.hasNext())
								{
									lpnDetail = (LPNDetail) lpnDetailItr.next();
									if (taskDetail.getCartonSeqNbr().longValue() == lpnDetail.getLpnDetailId())
									{
										log.logDebug("Setting custom substitutions for LPN, LPN Id : " + lpnDetail.getLpnDetailId());
										setCustomSubstitutions(lpnDetail, quantity, strLineNbr, oliMap);
										break;
									}
								}
							}
							//Custom HTPC EX04 changes - End.

							numOfDetails--;
						}
					}
					else
					{

						if (it.hasNext())
						{
							LPAllocInvnDtl allocDetail = (LPAllocInvnDtl) it.next();

							String strLineNbr = String.valueOf(lineNbr);
							String strLineNbrOnPage = String.valueOf(lineNbrOnPage);
							String line = (("line_") + strLineNbr);
							m_labelSubs.put(line, strLineNbrOnPage);

							boolean locnSet = false;
							if (Misc.isNullTrimmedString(currentPullLocnId) && !Misc.isNullTrimmedString(allocDetail.getPullLocnId()))
							{
								currentPullLocnId = allocDetail.getPullLocnId();
								locnSet = true;
							}

							if (locnSet || (!Misc.isNullTrimmedString(currentPullLocnId) && !currentPullLocnId.equals(allocDetail.getPullLocnId())))
							{
								dspLocn = LabelPrintHelper.getLabelPrintClassDAO().getDynamicColValueFromLocnHdr(allocDetail.getPullLocnId(), "DSP_LOCN");
							}

							currentPullLocnId = allocDetail.getPullLocnId();
							locnSet = false;

							m_labelSubs.put(("location_") + strLineNbr, dspLocn);

							Item item = allocDetail.getItem();
							if (item == null)
							{
								IItemCBOService cboBD = CBOBaseLocalServiceLocator.getItemCBOService();
								item = new Item();
								item.setItemId(allocDetail.getItemId());
								item.persist(true);

								try
								{
									item = cboBD.getItem(null, item);
								}
								catch (Exception e)
								{
									log.logException(e);
									log.logHigh("Could not load item " + allocDetail.getItemId());
									throw new LabelPrintingRuntimeException(e);
								}

								allocDetail.setItem(item);
							}

							String dspSku = Misc.EMPTY_STRING;
							String itemCboDesc = Misc.EMPTY_STRING;
							if (item != null)
							{
								dspSku = item.getItemName();
								itemCboDesc = item.getDescription();
							}

							m_labelSubs.put(("dsp_sku_") + strLineNbr, dspSku);
							m_labelSubs.put(("Item_cbo_desc_") + strLineNbr, itemCboDesc);

							// Add ITEM specific custom substitutions
							labelUtil.setItemLabelSubstitutions(allocDetail.getItemId(), shippingData.getWhse().getFacilityId(), strLineNbr, m_labelSubs);

							// Displaying pkt_dtl.cust_sku instead of xref.vendor_brcd per JP.
							double price = fetchRetailPrice(allocDetail.getLineItemId());
							String custSkuVal = fetchCustomerItem(allocDetail.getLineItemId());
							String refFieldVal = fetchRefValue(allocDetail.getLineItemId());

							// comparing custSkuVal with null was giving false even when the value of custSkuVal was null, so comparison changed to !=null
							custSkuVal = (custSkuVal != null) ? custSkuVal : space;
							refFieldVal = (refFieldVal != null) ? refFieldVal : space;
							m_labelSubs.put(("ref_") + strLineNbr, refFieldVal);
							m_labelSubs.put(("cust_sku_") + strLineNbr, custSkuVal);

							double quantity = 0.0;
							long lpnStatus = 0L;

							try
							{
								lpnStatus = Long.parseLong(shippingData.getOLPN().getFacilityStatus().getCode());
							}
							catch (FiniteValueNotFoundException ex)
							{
								log.logException(ex);
							}

							if (lpnStatus >= 20)
							{
								quantity = allocDetail.getQtyPulld().doubleValue();
							}
							else
							{
								quantity = allocDetail.getQtyAlloc().doubleValue();
							}

							totalQty += quantity;
							String qtyStr = labelUtil.qtyToString(quantity);
							qtyStr = LabelUtil.localizedBigDecimalString(qtyStr, getLabelPrintingData().getLocale());
							m_labelSubs.put(("qty_") + strLineNbr, qtyStr);
							m_labelSubs.put(("packed_qty_") + strLineNbr, qtyStr);

							String suggRetailPrice = null;
							if (price != 0.0)
								suggRetailPrice = priceToString(price);
							else
								suggRetailPrice = ("       ");

							suggRetailPrice = LabelUtil.localizedBigDecimalString(suggRetailPrice, getLabelPrintingData().getLocale());

							m_labelSubs.put(("sugg_rtl_price_") + strLineNbr, suggRetailPrice);

							//Custom HTPC EX04 changes - Start.
							if (ex04Enable)
							{
								log.logDebug("Calling setCustomSubstitutions for HTPC-EX04 with LPN_DETAIL Iteration");
								Iterator<?> lpnDetailItr = shippingData.getOLPN().getLpnDetailSet().iterator();
								LPNDetail lpnDetail = null;
								while (lpnDetailItr.hasNext())
								{
									lpnDetail = (LPNDetail) lpnDetailItr.next();
									if (allocDetail.getCartonSeqNbr().longValue() == lpnDetail.getLpnDetailId())
									{
										log.logDebug("Setting custom substitutions for LPN, LPN Id : " + lpnDetail.getLpnDetailId());
										setCustomSubstitutions(lpnDetail, quantity, strLineNbr, oliMap);
										break;
									}
								}
							}
							//Custom HTPC EX04 changes - End.

							numOfDetails--;
						}
					}
				}

				lineNbr++;
				lineNbrOnPage++;
			}

			// while we have details set the other common fields on the label
			// set the ship from and consignee ship to fields for sure post content label
			setUPSSurePostData();

			int deptId = getShippingData().getOrder().getMerchandizingDepartmentId();
			String dept = null;
			if (deptId > 0)
				dept = fetchDept(deptId);

			if (dept == null)
				dept = space;

			m_labelSubs.put(("dept"), dept);

			if (totalNbrofPagesToPrint > 1)
			{
				m_labelSubs.put(("continued"), ("CONTINUED..."));
				totalNbrofPagesToPrint--;
			}
			else
			{
				String totalQtyStr = labelUtil.qtyToString(totalQty);
				totalQtyStr = LabelUtil.localizedBigDecimalString(totalQtyStr, getLabelPrintingData().getLocale());

				m_labelSubs.put(("continued"), ("Total Qty : ") + totalQtyStr);
				m_labelSubs.put(("item_weight_total_lb"), totalWtLb.toString());
				m_labelSubs.put(("item_weight_total_oz"), totalWtOz.toString());
				m_labelSubs.put(("ups_sp_total_value"), totalPrice.toString());
			}

			log.logDebug("Exit:ContentShippingLabelDataImpl::printLabelViaAllocations");

			// print label for each page
			getDynamicLabelSubs();

			if (!m_labelSubsPageNbrMap.containsKey(strPageNbr))
			{
				Map<String, Object> tempMap = new HashMap<String, Object>();
				tempMap.putAll(m_labelSubs);
				m_labelSubsPageNbrMap.put(strPageNbr, tempMap);
			}
			log.logDebug("Intiating label printing..");
			printLabel();
		}

	}

	/**
	 * Performing custom substitutions for HTPC EX 04.
	 * 
	 * @param lpnDetail
	 *            - LPN Details.
	 * @param qty
	 *            - Quantity of Item.
	 * @param strLineNbr
	 *            - Line number on the label.
	 * @param orderLineDetailsMap
	 *            - Order Line Item data map.
	 */
	public void setCustomSubstitutions(LPNDetail lpnDetail, double qty, String strLineNbr, Map<String, List<String>> orderLineDetailsMap)
	{
		log.logEnter("HTPCContentShippingLabelDataImpl :: setCustomSubstitutions" + lpnDetail.getLpnDetailIdStr());

		DistributionOrder order = getShippingData().getOrder();

		/** If the order is for gift purposes, then giftWrapOrder = 1, otherwise 0. */
		Double giftWrapOrder = Double.valueOf(order.getRefNum4().getVal() == 1.0D ? 1.0D : 0.0D);

		Item item = lpnDetail.getItem();

		/* Packing Slip Details Section */
		if (item == null)
		{
			IItemCBOService cboBD = CBOBaseLocalServiceLocator.getItemCBOService();
			item = new Item();
			item.setItemId(lpnDetail.getItemId());
			item.persist(true);

			try
			{
				item = cboBD.getItem(null, item);
			}
			catch (Exception e)
			{
				log.logException(e);
				log.logHigh("Could not load item " + lpnDetail.getItemId());
				throw new LabelPrintingRuntimeException(e);
			}

			lpnDetail.setItem(item);
		}

		m_labelSubs.put(("htpc_ex04_size_") + strLineNbr, item.getItemSizeDesc());

		log.logDebug("ContentShippingLabelDataImpl :: setCustomSubstitutions");
		List<String> oliFields = orderLineDetailsMap.get(lpnDetail.getDistributionOrderDtlId().toString());

		if (oliFields.size() >= 1)
		{
			if (oliFields.get(0).equals("1"))
				m_labelSubs.put(("htpc_ex04_gw_") + strLineNbr, "Y");
			else
				m_labelSubs.put(("htpc_ex04_gw_") + strLineNbr, "");

			String htpcPrice = oliFields.get(1);
			htpcPrice = LabelUtil.localizedBigDecimalString(htpcPrice, getLabelPrintingData().getLocale());
			log.logDebug("htpcPrice : " + htpcPrice);

			if (giftWrapOrder.intValue() == 1)
			{
				m_labelSubs.put(("htpc_ex04_price_") + strLineNbr, encodedValue(htpcPrice));
			}
			else
			{
				m_labelSubs.put(("htpc_ex04_price_") + strLineNbr, htpcPrice);
			}
		}
		m_labelSubs.put(("htpc_ex04_item_color_") + strLineNbr, item.getItemColor());

		log.logDebug("Size Value : " + lpnDetail.getSizeValue());
		log.logDebug("Initial Quantity : " + lpnDetail.getInitialQty());

		Double htpcLpnDetailQty = 0.0;
		if (m_labelSubs.get(("qty_") + strLineNbr) != null && !Misc.isNullTrimmedString((m_labelSubs.get(("qty_") + strLineNbr)).toString()))
		{
			String htpcLpnDetailQtyStr = m_labelSubs.get(("qty_") + strLineNbr).toString();
			log.logDebug("Base Calculated Quantity : " + htpcLpnDetailQtyStr);
			htpcLpnDetailQty = Double.parseDouble(htpcLpnDetailQtyStr);
		}

		//checking for TAX.
		double htpc_ex04_tax = Misc.isNullTrimmedString(oliFields.get(2)) ? 0.0 : Double.parseDouble(oliFields.get(2).toString()) * htpcLpnDetailQty;
		//checking for Total Price i.e. Price + Tax. 
		double htpc_ex04_totalprice = Misc.isNullTrimmedString(oliFields.get(1)) ? htpc_ex04_tax : (htpc_ex04_tax + (Double.parseDouble(oliFields.get(1).toString()) * htpcLpnDetailQty));
		log.logDebug("htpc_ex04_tax: " + htpc_ex04_tax + " htpc_ex04_totalprice: " + htpc_ex04_totalprice);

		String strhtpc_ex04_tax = labelUtil.qtyToString(htpc_ex04_tax);
		strhtpc_ex04_tax = LabelUtil.localizedBigDecimalString(strhtpc_ex04_tax, getLabelPrintingData().getLocale());
		log.logDebug("strhtpc_ex04_tax : " + strhtpc_ex04_tax);

		String strhtpc_ex04_totalprice = labelUtil.qtyToString(htpc_ex04_totalprice);
		strhtpc_ex04_totalprice = LabelUtil.localizedBigDecimalString(strhtpc_ex04_totalprice, getLabelPrintingData().getLocale());
		log.logDebug("strhtpc_ex04_totalprice : " + strhtpc_ex04_totalprice);

		if (giftWrapOrder.intValue() == 1)
		{
			m_labelSubs.put(("htpc_ex04_tax_") + strLineNbr, encodedValue(strhtpc_ex04_tax));
			m_labelSubs.put(("htpc_ex04_totalprice_") + strLineNbr, encodedValue(strhtpc_ex04_totalprice));

			m_labelSubs.put(("htpc_ex04_paymethod"), "TBD"); // Yet To Be Decided.
		}
		else
		{
			m_labelSubs.put(("htpc_ex04_tax_") + strLineNbr, strhtpc_ex04_tax);
			m_labelSubs.put(("htpc_ex04_totalprice_") + strLineNbr, strhtpc_ex04_totalprice);

			m_labelSubs.put(("htpc_ex04_paymethod"), "TBD"); // Yet To Be Decided.
		}

		m_labelSubs.put(("htpc_ex04_item_desc_") + strLineNbr, oliFields.get(3));

	}

	/**
	 * This method encodes the numerical data provided into encoded string, using standard Manhattan
	 * encoding process.
	 * System Code used for encoding is <Code>S-901</code>
	 * 
	 * @param value
	 *            - String for of the numerical value.
	 * @return
	 */
	private String encodedValue(String value)
	{
		log.logEnter("Encoding : [" + value + "]");
		value = value.trim();

		if (Misc.isNullTrimmedString(value))
			return value;

		char[] data = value.toCharArray();
		StringBuffer output = new StringBuffer();
		String key = Misc.EMPTY_STRING;

		for (int x = 0; x < data.length; x++)
		{
			key = String.valueOf(data[x]);
			if (".".equals(key))
			{
				output.append(".");
			}
			else
			{
				output.append(code_id_desc_map.get(key));
			}
		}
		log.logExit("Encoded [" + value + "] as [" + output + "]");
		return output.toString();
	}

	/**
	 * Extracts data from the order and replaces it in the custom header sections.
	 * 
	 * @return
	 */
	protected Map<String, Object> getHeaderSubstitutionMap()
	{
		log.logEnter("Entering HTPCContentShippingLabelDataImpl :: setCustomSubstitutions");
		Map<String, Object> ex04HeaderSubMap = new HashMap<>();

		DistributionOrder order = shippingData.getOrder();

		//String sLPN = order.getDestinationFacilityAliasIdString() + shippingData.getOLPN().getTcASNId();

		/* oLPN label Section. */
		//ex04HeaderSubMap.put(("Store LPN"), sLPN);
		//ex04HeaderSubMap.put(("Customer Order Number"), order.getExtPurchaseOrderString());

		// check what this is for?
		//ex04HeaderSubMap.put(("bill_to_city"), order.getBillToCityString());

		/* Packing Slip Header Section. */
		Date htpcOrderDate = new Date(order.getOrderDate().getTime());
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy");
		String htpcOrderDateStr = sdf.format(htpcOrderDate);

		ex04HeaderSubMap.put(("htpc_ex04_order_date"), htpcOrderDateStr);
		ex04HeaderSubMap.put(("htpc_ex04_order_type"), order.getOrderTypeString());

		ex04HeaderSubMap.put(("htpc_ex04_cust_order"), order.getExtPurchaseOrderString());

		ex04HeaderSubMap.put(("htpc_ex04_insider_nbr"), order.getRefField2String());

		ex04HeaderSubMap.put(("htpc_ex04_total_page"), Integer.toString(m_labelSubsPageNbrMap.size()));

		String deliveryOption = ((HTPCLabelPrintClassDAO) LabelPrintHelper.getLabelPrintClassDAO()).getDeliveryOptionFromOrder(order.getDistributionOrderId());

		if ("02".equals(deliveryOption))
		{
			ex04HeaderSubMap.put(("htpc_ex04_sts"), "STS");
			String htpc_ex04_store = Misc.EMPTY_STRING;
			if (!Misc.isNullTrimmedString(order.getDestinationFacilityAliasIdString()))
				htpc_ex04_store = order.getDestinationFacilityAliasIdString() + shippingData.getOLPN().getTcLpnId();
			ex04HeaderSubMap.put(("htpc_ex04_store"), htpc_ex04_store);
		}
		else
		{
			ex04HeaderSubMap.put(("htpc_ex04_sts"), "");
			ex04HeaderSubMap.put(("htpc_ex04_store"), "");
		}

		if (orderBrand.equalsIgnoreCase("Hot Topic"))
		{
			ex04HeaderSubMap.put(("htpc_ex04_brandtitle"), "HOT TOPIC");
			ex04HeaderSubMap.put(("htpc_ex04_brand"), "Hot Topic");
		}
		else if (orderBrand.equalsIgnoreCase("Torrid"))
		{
			ex04HeaderSubMap.put(("htpc_ex04_brandtitle"), "TORRID");
			ex04HeaderSubMap.put(("htpc_ex04_brand"), "Torrid");
		}
		else if (orderBrand.equalsIgnoreCase("Blackheart"))
		{
			ex04HeaderSubMap.put(("htpc_ex04_brandtitle"), "BLACKHEART");
			ex04HeaderSubMap.put(("htpc_ex04_brand"), "Blackheart");
		}

		/* Haute Cash Section. */
		String refField9 = order.getRefField9String();
		String refField10 = order.getRefField10String();
		if (!Misc.isNullTrimmedString(refField9))
		{
			int delimiterCount = refField9.split("\\,").length - 1;
			log.logDebug("Number of values found in REF_FIELD_9 : " + (delimiterCount + 1));
			if (delimiterCount == 2)
			{
				ex04HeaderSubMap.put(("htpc_ex04_promo"), refField9.substring(0, refField9.indexOf(",")));
				refField9 = refField9.substring(refField9.indexOf(",") + 1, refField9.length());
				ex04HeaderSubMap.put(("htpc_ex04_min_amount"), refField9.substring(0, refField9.indexOf(",")));
				ex04HeaderSubMap.put(("htpc_ex04_hc_amount"), refField9.substring(refField9.indexOf(",") + 1, refField9.length()));
			}
			else
			{
				ex04HeaderSubMap.put(("htpc_ex04_promo"), Misc.EMPTY_STRING);
				ex04HeaderSubMap.put(("htpc_ex04_hc_amount"), Misc.EMPTY_STRING);
				ex04HeaderSubMap.put(("htpc_ex04_min_amount"), Misc.EMPTY_STRING);
			}
		}
		if (!Misc.isNullTrimmedString(refField10))
		{
			int delimiterCount = refField10.split("\\,").length - 1;
			log.logDebug("Number of values found in REF_FIELD_10 : " + (delimiterCount + 1));
			if (delimiterCount == 1)
			{
				ex04HeaderSubMap.put(("htpc_ex04_redeem_start"), refField10.substring(0, refField10.indexOf(",")));
				ex04HeaderSubMap.put(("htpc_ex04_redeem_end"), refField10.substring(refField10.indexOf(",") + 1, refField10.length()));
			}
			else
			{
				ex04HeaderSubMap.put(("htpc_ex04_redeem_start"), Misc.EMPTY_STRING);
				ex04HeaderSubMap.put(("htpc_ex04_redeem_end"), Misc.EMPTY_STRING);
			}
		}

		/* Below entry is there for both 'Haute Cash Section' as well as 'Returns Section'. */
		ex04HeaderSubMap.put(("htpc_ex04_cust_order"), order.getExtPurchaseOrderString());

		log.logExit("Exiting HTPCContentShippingLabelDataImpl :: setCustomSubstitutions");
		return ex04HeaderSubMap;
	}

	/**
	 * Getting a map of misc flags value for <code>C-PCK</code>
	 * 
	 * @return
	 */
	protected HashMap getMiscCustomCode()
	{
		log.logEnter("HTPCContentShippingLabelDataImpl :: getMiscCustomCode");
		HashMap MiscCustomCode = new HashMap<>();
		try
		{
			ISysCodeConfigService syscodeManager = getSysCodeConfigManagerBean();
			MiscCustomCode = (HashMap) syscodeManager.getMiscFlagsFromSysCode("C", "PCK");
			log.logDebug("getMiscCustomCode :: the map for system code is : " + MiscCustomCode);
			return MiscCustomCode;
		}
		catch (Exception ce)
		{
			log.logException(ce);
			log.logExit("Returning empty HashMap from getMiscCustomCode()..");
			return MiscCustomCode;
		}
	}

	protected ISysCodeConfigService getSysCodeConfigManagerBean()
	{
		log.logEnter("HTPCContentShippingLabelDataImpl :: getSysCodeConfigManagerBean");
		ISysCodeConfigService syscodeMgr = null;
		try
		{
			syscodeMgr = CBOBaseServiceLocator.getSysCodeConfigService();
		}
		catch (Exception ex)
		{
			log.logException(ex);
		}
		log.logExit("getSysCodeConfigManagerBean()");
		return syscodeMgr;
	}

}

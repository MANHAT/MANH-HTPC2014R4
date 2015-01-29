package com.manh.wmos.services.outbound.agile.service;
import javax.servlet.http.HttpServletRequest;

import com.manh.wmos.services.communication.core.WMCommunicationManagerException;
import com.manh.wmos.services.outbound.agile.processor.HTPCWMAgileResponseProcessor;


/**
 * This Interface provides code for processing response XML messages received from Agile to WM
 * @author psindhu
 *
 */
public interface IHTPCAgileOrderLoadRespService {

	/**
	 * this method is starting point for Parsing of order load response message received from Agile to WM
	 * @param in
	 * @param responseProcessor TODO
	 * @return
	 * @throws WMCommunicationManagerException
	 */
	public  String receiveAgileOrderLoadResponse( java.lang.String in, HTPCWMAgileResponseProcessor responseProcessor )throws WMCommunicationManagerException;

	/**
	 * this method is starting point for Parsing of void response message received from Agile to WM
	 * @param in
	 * @param responseProcessor
	 * @return
	 * @throws WMCommunicationManagerException
	 */
	public  String receiveAgileVoidResponse( java.lang.String in, HTPCWMAgileResponseProcessor responseProcessor )throws WMCommunicationManagerException;

	/**
	 * this method is starting point for response receiving of order load response message and void response messages
	 * @param in
	 * @param request 
	 * @return
	 * @throws WMCommunicationManagerException
	 */
	public  String receiveAgileResponse( java.lang.String in, HttpServletRequest request)throws WMCommunicationManagerException;
}

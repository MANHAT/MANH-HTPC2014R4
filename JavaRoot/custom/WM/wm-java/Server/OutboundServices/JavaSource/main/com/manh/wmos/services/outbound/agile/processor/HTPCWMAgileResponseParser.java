package com.manh.wmos.services.outbound.agile.processor;

import java.io.Reader;
import java.io.StringReader;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import com.manh.wm.core.util.HTPCWMAgileLogHelper;

/**
 * EX01 Agile to WM response message receiving parser
 * 
 * @author psindhu
 */
public class HTPCWMAgileResponseParser
{

	private final SAXBuilder	SAX	= sax();

	/**
	 * @return
	 */
	private SAXBuilder sax()
	{
		SAXBuilder sax = new SAXBuilder();
		sax.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		return sax;
	}

	/**
	 * Initializing response processor
	 * 
	 * @param requestXML
	 * @return
	 * @throws Exception
	 */
	public HTPCWMAgileResponseProcessor getAgileResponseProcessor(String requestXML) throws Exception
	{
		HTPCWMAgileLogHelper.logEnter();
		Document responseDocument;

		HTPCWMAgileResponseProcessor responseProcessor = new HTPCWMAgileResponseProcessor(requestXML);
		Reader sReader = new StringReader(requestXML);
		responseDocument = SAX.build(sReader);
		responseProcessor.setResponseDocument(responseDocument);

		HTPCWMAgileLogHelper.logExit();
		return responseProcessor;
	}

}

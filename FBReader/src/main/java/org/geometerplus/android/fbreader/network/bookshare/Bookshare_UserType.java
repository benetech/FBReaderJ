package org.geometerplus.android.fbreader.network.bookshare;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * 
 * Class for retrieving the download password
 *
 */
public class Bookshare_UserType {
	private String downloadPassword;
	private boolean isOM = false;
	private boolean isIM = false;
	
	/**
	 * Retrieve the download password for OM
	 * @param response A String containing the XML response
	 * @return String representing
	 */
	public boolean isOM(String response){
		InputSource is = new InputSource(new StringReader(response));

		try{
			/* Get a SAXParser from the SAXPArserFactory. */
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp;
			sp = spf.newSAXParser();

			/* Get the XMLReader of the SAXParser we created. */
			XMLReader parser = sp.getXMLReader();
			parser.setContentHandler(new SAXHandler());
			parser.parse(is);
		}
		catch(SAXException e){
			System.out.println(e);
		}
		catch (ParserConfigurationException e) {
			System.out.println(e);
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		return isOM;
	}

	public String getDownloadPassword(){
		return downloadPassword;
	}
	// Class containing the logic for parsing XML elements in the response and locating download password
	private class SAXHandler extends DefaultHandler{

		boolean result = false;
		boolean id = false;
		boolean isDownloadPassword = false;
		boolean value = false;
		boolean isUserType = false;


		public void startElement(String namespaceURI, String localName, String qName, Attributes atts){
			if(qName.equalsIgnoreCase("id")){
				id = true;
			}
			if(qName.equalsIgnoreCase("value")){
				value = true;
			}
		}

		public void endElement(String uri, String localName, String qName){

			if(qName.equalsIgnoreCase("id")){
				id = false;
			}
			if(qName.equalsIgnoreCase("value")){
				value = false;
			}
		}

		public void characters(char[] c, int start, int length){
			if(id){
				if(new String(c,start,length).equals("02")){
					isUserType = true;
				}

				if(new String(c,start,length).equals("11")){
					isDownloadPassword = true;
				}
			}
			if(value && isUserType){
				if(new String(c,start,length).equals("2")){
					isOM = true;
				}
				else if(new String(c,start,length).equals("1")){
					isIM = true;
				}
			}
			if(value && isDownloadPassword){
				downloadPassword = new String(c,start,length);
			}
		}
	}
}

package com.ahanlon.android;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

public class MapUtils {

	private static final HttpTransport transport = new ApacheHttpTransport();
	
	public static String buildGoogleMapsLink ( String latitude, String longitude ) throws IOException, XPathExpressionException, TransformerException, ParserConfigurationException, SAXException
	{

		String address;
		try {
			
			   HttpRequestFactory httpRequestFactory = createRequestFactory(transport);
			   HttpRequest request = httpRequestFactory.buildGetRequest(new GenericUrl(Constants.PLACES_SEARCH_URL));

			   //Setup the URL to hit the PLACES SEARCH URL
			   request.url.put("key", Constants.API_KEY);
			   request.url.put("location", latitude + "," + longitude);
			   request.url.put("radius", 500);
			   request.url.put("name", "McDonalds");
			   request.url.put("sensor", "false");

			   //Dump the whole XML so I can figure out the xPath query.
			   String xml = request.execute().parseAsString();
			   System.out.println( xml );
			   
			   
			   //Setup some XML parsing stuff
			   InputStream resultStream  = request.execute().getContent();
			   DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			   DocumentBuilder db = dbf.newDocumentBuilder();
			   Document doc = db.parse(resultStream);

			   XPathFactory factory = XPathFactory.newInstance();
			   XPath xpath = factory.newXPath();
			   
			   
			   //Take the parsed XML, and run and xPath query that brings back the first result
			   String foundRef = xpath.evaluate("/PlaceSearchResponse/result[1]/reference/text()", doc); 
			   System.out.println( foundRef );
			   
			   //Clear out the places query, now we'll use the top reference from the results and get some details about
			   //the closest McDonalds.
			   request.url.clear();
			   request = httpRequestFactory.buildGetRequest(new GenericUrl (Constants.PLACES_DETAILS_URL ) );
			   request.url.put("reference", foundRef );
			   request.url.put("sensor", "false");
			   request.url.put("key", Constants.API_KEY);
			   resultStream  = request.execute().getContent();
			   dbf = DocumentBuilderFactory.newInstance();
			   db = dbf.newDocumentBuilder();
			   doc = db.parse(resultStream);
			   
			   //Look for the formatted_address in the details result
			   address = xpath.evaluate("/PlaceDetailsResponse/result/formatted_address", doc);
			   System.out.println( address );
			   
			   if ( address == null || address.equals("")){
				   return "Unable to find the closest McDonald's";
			   }
			 
			  } catch (HttpResponseException e) {
				  e.printStackTrace();
			   throw e;
			  }
		
		return "The Closest McDonald's is located at: " + address ;
	}
	
	public static HttpRequestFactory createRequestFactory(final HttpTransport transport) {

		  return transport.createRequestFactory(new HttpRequestInitializer() {
			public void initialize(HttpRequest request) {
		    GoogleHeaders headers = new GoogleHeaders();
		    headers.setApplicationName("Google-Places-DemoApp");
		    request.headers=headers;
		    JsonHttpParser parser = new JsonHttpParser();
		    parser.jsonFactory = new JacksonFactory();
		    request.addParser(parser);
		   }
		});
	}

}

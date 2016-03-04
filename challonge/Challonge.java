package challonge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Challonge {

	// CONSTANTS - Parameters
	private static final String PARAM_KEY = "api_key=";
	private static final String PARAM_SUBDOMAIN = "subdomain=";
	
	// CONSTANTS - URLs
	public static final String URL_START = "https://api.challonge.com/v1/";
	private static final String URL_ENCODING = "UTF-8";
	
	// METHODS
	/**
	 * encodes a String in a format understandable in an HTTP request
	 * 
	 * @param s unencoded String
	 * @return encoded String
	 * @throws UnsupportedEncodingException
	 */
	public static String encodeString(String s) throws UnsupportedEncodingException
	{
		return URLEncoder.encode(s, URL_ENCODING);
	}
	
	/**
	 * returns whether or not a string can be used as a valid custom URL on Challonge
	 * 
	 * @param url the desired custom URL
	 * @return <b>true</b> if the URL is valid (contains only numbers, letters, and underscores),
	 * <b>false</b> otherwise
	 */
	public static boolean validURL(String url)
	{
		return url.matches("^[a-zA-Z0-9_]+$");
	}
	
	/**
	 * get the XML data from the user's tournament index
	 * 
	 * @param apiKey API key necessary for authentication
	 * @param subdomain subdomain associated with target organization
	 * @return ArrayList of tournaments from XML data retrieved from request
	 */
	public static ArrayList<Tournament> getTournamentIndex(String apiKey, String subdomain) throws ChallongeException
	{
		try
		{
			// Generate URL
			String urlString = URL_START + "tournaments.xml?" + PARAM_KEY + encodeString(apiKey);
			if(subdomain != null)
				urlString += "&" + PARAM_SUBDOMAIN + encodeString(subdomain);
			URL url = new URL(urlString);
			String xml = sendHttpRequest(url, "GET");
			
	        return Tournament.createTournamentListFromXML(apiKey, subdomain, xml);
		}
		catch(MalformedURLException mfe)
		{
			throw new ChallongeException(ChallongeException.REASON_ARGUMENTS);
		}
		catch(ChallongeException ce)
		{
			throw ce;
		}
		catch(IOException ioe)
		{
			throw new ChallongeException(ChallongeException.REASON_DEFAULT);
		}
	}
	
	/**
	 * sends an HTTP request to the Challonge server
	 * 
	 * @param url base URL of the request
	 * @param method request method
	 * @return the server's XML response
	 * @throws ChallongeException if request could not be processed properly
	 */
	/* package */ static String sendHttpRequest(URL url, String method) throws ChallongeException
	{
		return sendHttpRequest(url, method, null);
	}
	
	/**
	 * sends an HTTP request to the Challonge server
	 * 
	 * @param url base URL of the request
	 * @param method request method
	 * @param body body of request (can be <b>null</b>)
	 * @return the server's XML response
	 * @throws ChallongeException if request could not be processed properly
	 */
	/* package */ static String sendHttpRequest(URL url, String method, String body) throws ChallongeException
	{
		try
		{
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			
			// Send body, if applicable
			if(body != null)
			{
				// Write body parameters
				connection.setDoOutput(true);
				OutputStream output = connection.getOutputStream();
				output.write(body.toString().getBytes());
				output.flush();
			}
			connection.connect();
			
			// Check for errors
			int code = connection.getResponseCode();
			if(connection.getErrorStream() != null) // If error text exists...
				if(code == HttpURLConnection.HTTP_UNAUTHORIZED)
					throw new ChallongeException(ChallongeException.REASON_KEY);
				else
				{
					BufferedReader error = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
					String errorText = "";
					String errorLine;
					while((errorLine = error.readLine()) != null)
						errorText += errorLine + "\n";
					error.close();
					
					throw new ChallongeException(getConnectionErrorReason(errorText));
				}
			
			// If connection successful, read and return XML input
	        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String xml = "";
	        String inputLine;
	        while ((inputLine = input.readLine()) != null) 
	        	xml += inputLine + "\n";
	        input.close();
	        
	        return xml;
		}
		catch(ChallongeException ce)
		{
			throw ce;
		}
		catch(IOException ioe)
		{
			throw new ChallongeException(ChallongeException.REASON_DEFAULT);
		}
	}
	
	/**
	 * gets the reason for an error xml response
	 * 
	 * @param xml error xml response
	 * @return String containing description of error
	 */
	private static String getConnectionErrorReason(String xml)
	{
		return xml; // TODO: Actually extract error reason from xml
	}
}

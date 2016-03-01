package challonge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Challonge {

	// CONSTANTS - Error Messages
	private static final String ERR_ENCODE = "ERROR: Could not encode to ";
	private static final String ERR_INDEX_RETRIEVE = "ERROR: Could not retrieve index";
	private static final String ERR_MALFORMED_URL = "ERROR: Malformed URL";
	private static final String ERR_NAME_LENGTH = "ERROR: Name too long";
	private static final String ERR_PARTICIPANTS_RETRIEVE = "ERROR: Could not retrieve participants";
	private static final String ERR_TOURNAMENT_CREATE = "ERROR: Could not create tournament";
	
	// CONSTANTS - Limits
	private static final int NAME_MAX_LENGTH = 60;
	
	// CONSTANTS - Parameters
	private static final String PARAM_KEY = "api_key=";
	private static final String PARAM_SUBDOMAIN = "subdomain=";
	private static final String PARAM_TOURNAMENT_NAME = "tournament[name]=";
	private static final String PARAM_TOURNAMENT_SUBDOMAIN = "tournament[subdomain]=";
	private static final String PARAM_TOURNAMENT_TYPE = "tournament[tournament_type]=";
	private static final String PARAM_TOURNAMENT_URL = "tournament[url]=";
	
	// CONSTANTS - Tournament Types
	private static final String TYPE_SINGLE_ELIM = "single elimination";
	private static final String TYPE_DOUBLE_ELIM = "double elimination";
	private static final String TYPE_ROUND_ROBIN = "round robin";
	private static final String TYPE_SWISS = "swiss";
	
	// CONSTANTS - URLs
	public static final String URL_START = "https://api.challonge.com/v1/";
	private static final String URL_ENCODING = "UTF-8";
	
	// DATA MEMBERS
	private String apiKey;
	private String subdomain;
	
	// METHODS
	/**
	 * create a ChallongeConnector without a subdomain
	 * 
	 * @param key unique API key generated from Challonge settings
	 */
	public Challonge(String key)
	{
		try
		{
			apiKey = encodeString(key);
		}
		catch(UnsupportedEncodingException use)
		{
			System.err.println(ERR_ENCODE + URL_ENCODING);
		}
	}
	
	/**
	 * create a ChallongeConnector with a subdomain
	 * 
	 * @param key unique API key generated from Challonge settings
	 * @param sd subdomain for a specific organization
	 */
	public Challonge(String key, String sd)
	{
		try
		{
			apiKey = encodeString(key);
			subdomain = encodeString(sd);
		}
		catch(UnsupportedEncodingException use)
		{
			System.err.println(ERR_ENCODE + URL_ENCODING);
		}
	}
	
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
	 * @return ArrayList of tournaments from XML data retrieved from request
	 */
	public ArrayList<Tournament> getTournamentIndex() throws ChallongeException
	{
		try
		{
			// Generate URL
			String urlString = URL_START + "tournaments.xml?" + PARAM_KEY + apiKey;
			if(subdomain != null)
				urlString += "&" + PARAM_SUBDOMAIN + subdomain;
			URL url = new URL(urlString);
			
			// Establish connection
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			
			// Read input
	        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String xml = "";
	        String inputLine;
	        while ((inputLine = input.readLine()) != null) 
	        		xml += inputLine + "\n";
	        input.close();
	        
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
	
	public ArrayList<Participant> getParticipantList(Tournament t)
	{
		try
		{
			String urlString = URL_START + "tournaments/" + encodeString(Integer.toString(t.getID())) + "/participants.xml?"
					+ PARAM_KEY + apiKey;
			URL url = new URL(urlString);
			
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			
			// Read input
			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String xml = "";
			String inputLine;
			while((inputLine = input.readLine()) != null)
				xml += inputLine + "\n";
			input.close();
			
			return Participant.createParticipantListFromXML(xml);
		}
		catch(MalformedURLException mfe)
		{
			System.err.println(ERR_MALFORMED_URL);
			return null;
		}
		catch(IOException ioe)
		{
			System.err.println(ERR_PARTICIPANTS_RETRIEVE);
			return null;
		}	
	}
	
	public void setSubdomain(String sd)
	{
		try
		{
			subdomain = encodeString(sd);
		}
		catch(UnsupportedEncodingException uee)
		{
			System.err.println("ERROR: COULD NOT SET SUBDOMAIN");
			System.err.println("\tRequested Value: " + sd);
		}
	}
}

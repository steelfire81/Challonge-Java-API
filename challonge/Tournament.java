package challonge;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Tournament {

	// CONSTANTS - Limits
	private static final int NAME_MAX_LENGTH = 60;
	
	// CONSTANTS - Parameters
	private static final String PARAM_KEY = "api_key=";
	private static final String PARAM_TOURNAMENT_NAME = "tournament[name]=";
	private static final String PARAM_TOURNAMENT_SUBDOMAIN = "tournament[subdomain]=";
	private static final String PARAM_TOURNAMENT_TYPE = "tournament[tournament_type]=";
	private static final String PARAM_TOURNAMENT_URL = "tournament[url]=";
	
	// CONSTANTS - Tournament Types
	public static final String TYPE_SINGLE_ELIM = "single elimination";
	public static final String TYPE_DOUBLE_ELIM = "double elimination";
	public static final String TYPE_ROUND_ROBIN = "round robin";
	public static final String TYPE_SWISS = "swiss";
	public static final String[] TYPES = {TYPE_SINGLE_ELIM, TYPE_DOUBLE_ELIM, TYPE_ROUND_ROBIN, TYPE_SWISS};
	
	// CONSTANTS - XML Tags
	private static final String XML_DESCRIPTION = "description";
	private static final String XML_ID = "id";
	private static final String XML_NAME = "name";
	private static final String XML_TOURNAMENT = "tournament";
	private static final String XML_TYPE = "tournament-type";
	private static final String XML_URL = "url";
	
	// DATA MEMBERS
	String apiKey;
	String subdomain;
	int id;
	String name;
	String url;
	String description;
	String type;
	
	// METHODS
	// Constructors
	/**
	 * creates a tournament given an ID, name, URL, description, and type
	 * @param i ID
	 * @param n name
	 * @param u URL
	 * @param d description
	 * @param t type (TYPE_SINGLE_ELIM, TYPE_DOUBLE_ELIM, etc.)
	 */
	private Tournament(String key, String domain, int i, String n, String u, String d, String t)
	{
		apiKey = key;
		subdomain = domain;
		id = i;
		name = n;
		url = u;
		description = d;
		type = t;
	}
	
	// Static
	/**
	 * creates a new double elimination tournament on Challonge without a subdomain
	 * 
	 * @param name name of new tournament
	 * @param customURL url of new tournament
	 * @param apiKey key to connect to challonge service
	 * @return newly created tournament
	 * @throws ChallongeException if tournament could not be initialized
	 */
	public static Tournament createTournament(String name, String customURL, String apiKey, String type) throws ChallongeException
	{
		// Ensure valid arguments
		if(name.length() > NAME_MAX_LENGTH) // Ensure proper length name
			throw new ChallongeException(ChallongeException.REASON_NAME_LENGTH);
		if(!Challonge.validURL(customURL)) // Ensure desired custom URL is valid
			throw new ChallongeException(ChallongeException.REASON_INVALID_URL);
		if(!validTournamentType(type))
			throw new ChallongeException(ChallongeException.REASON_TOURNEY_TYPE);
		
		try
		{
			// Generate URL
			String urlString = Challonge.URL_START + "tournaments.xml?" + PARAM_KEY + apiKey;
			
			urlString += "&" + PARAM_TOURNAMENT_TYPE + TYPE_DOUBLE_ELIM;
			urlString += "&" + PARAM_TOURNAMENT_NAME + name;
			urlString += "&" + PARAM_TOURNAMENT_URL + customURL;
			urlString = Challonge.encodeString(urlString);
			URL url = new URL(urlString);
			
			// Establish connection
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.connect();
			
			// Check for errors
			connection.getResponseCode();
			if(connection.getErrorStream() != null) // If error text exists...
				if(connection.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
					throw new ChallongeException(ChallongeException.REASON_KEY);
				else // TODO: Parse error text and add to ChallongeException
					throw new ChallongeException(ChallongeException.REASON_ARGUMENTS);
			
			// Read input
	        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String xml = "";
	        String inputLine;
	        while ((inputLine = input.readLine()) != null) 
	        		xml += inputLine + "\n";
	        input.close();
	        
	        return createTournamentFromXML(apiKey, null, xml);
		}
		catch(UnsupportedEncodingException uee)
		{
			throw new ChallongeException(ChallongeException.REASON_ARGUMENTS);
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
	 * creates a tournament from XML data received from Challonge
	 * 
	 * @param apiKey the API key necessary to authenticate
	 * @param subdomain the desired subdomain of the tournament (or <b>null</b> if no subdomain)
	 * @param xml XML data as a String containing one tournament
	 * @return a fully initialized tournament, or <b>null</b> if initialization fails
	 */
	/* package */ static Tournament createTournamentFromXML(String apiKey, String subdomain, String xml) throws ChallongeException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
			Document doc = builder.parse(input);
			
			Element e = (Element) doc.getElementsByTagName(XML_TOURNAMENT).item(0);
			int id = Integer.parseInt(e.getElementsByTagName(XML_ID).item(0).getTextContent());
			String name = e.getElementsByTagName(XML_NAME).item(0).getTextContent();
			String url = e.getElementsByTagName(XML_URL).item(0).getTextContent();
			String description = e.getElementsByTagName(XML_DESCRIPTION).item(0).getTextContent();
			String type = e.getElementsByTagName(XML_TYPE).item(0).getTextContent();
			
			return new Tournament(apiKey, subdomain, id, name, url, description, type);	
		}
		catch(Exception e)
		{
			throw new ChallongeException(ChallongeException.REASON_XML);
		}
	}
	
	/**
	 * creates a list of Tournaments from XML data received from Challonge
	 * 
	 * @param apiKey the API key necessary to authenticate
	 * @param subdomain the desired subdomain of the tournaments (or <b>null</b> if no subdomain)
	 * @param xml XML data as a String containing a list of tournaments
	 * @return a list of Tournaments
	 */
	/* package */ static ArrayList<Tournament> createTournamentListFromXML(String apiKey, String subdomain, String xml) throws ChallongeException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
			Document doc = builder.parse(input);
			
			ArrayList<Tournament> tournamentList = new ArrayList<Tournament>();
			NodeList list = doc.getElementsByTagName(XML_TOURNAMENT);
			for(int i = 0; i < list.getLength(); i++)
			{
				Element e = (Element) list.item(i);
				int id = Integer.parseInt(e.getElementsByTagName(XML_ID).item(0).getTextContent());
				String name = e.getElementsByTagName(XML_NAME).item(0).getTextContent();
				String url = e.getElementsByTagName(XML_URL).item(0).getTextContent();
				String description = e.getElementsByTagName(XML_DESCRIPTION).item(0).getTextContent();
				String type = e.getElementsByTagName(XML_TYPE).item(0).getTextContent();
				
				tournamentList.add(new Tournament(apiKey, subdomain, id, name, url, description, type));
			}
			
			return tournamentList;
		}
		catch (Exception e)
		{
			throw new ChallongeException(ChallongeException.REASON_XML);
		}
	}
	
	public static boolean validTournamentType(String type)
	{
		for(int i = 0; i < TYPES.length; i++)
			if(type.equals(TYPES[i]))
				return true;
		
		return false;
	}
	
	// Instance methods
	public int getID()
	{
		return id;
	}
	
	@Override
	public String toString() // for testing purposes
	{
		return name + ": (ID: " + id + ") (URL: " + url + ")";
	}
}

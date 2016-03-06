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
	private static final String PARAM_PARTICIPANT_NAME = "participant[name]=";
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
	private String apiKey;
	private String subdomain;
	private int id;
	private String name;
	private String url;
	private String description;
	private String type;
	private ArrayList<Participant> participants;
	private ArrayList<Match> matches;
	
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
	private Tournament(String key, String domain, int i, String n, String u, String d, String t) throws ChallongeException
	{
		apiKey = key;
		subdomain = domain;
		id = i;
		name = n;
		url = u;
		description = d;
		type = t;
		
		updateParticipants();
		updateMatches();
	}
	
	// Static
	/**
	 * creates a new double elimination tournament on Challonge without a subdomain
	 * 
	 * @param apiKey key to connect to challonge service
	 * @param name name of new tournament
	 * @param customURL url of new tournament
	 * @param type tournament type (from <b>Tournament.TYPES</b>)
	 * @return newly created tournament
	 * @throws ChallongeException if tournament could not be initialized
	 * @see #createTournament(String, String, String, String, String)
	 */
	public static Tournament createTournament(String apiKey, String name, String customURL, String type) throws ChallongeException
	{
		return createTournament(apiKey, name, customURL, type, null);
	}
	
	/**
	 * creates a new double elimination tournament on Challonge with a subdomain (subdomain can be <b>null</b>)
	 * 
	 * @param apiKey key to connect to challonge service
	 * @param name name of new tournament
	 * @param customURL url of new tournament
	 * @param type tournament type (from <b>Tournament.TYPES</b>)
	 * @param subdomain subdomain associated with organization, can be <b>null</b>
	 * @return newly created tournament
	 * @throws ChallongeException if tournament could not be initialized
	 */
	public static Tournament createTournament(String apiKey, String name, String customURL, String type, String subdomain) throws ChallongeException
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
			
			urlString += "&" + PARAM_TOURNAMENT_TYPE + Challonge.encodeString(type);
			urlString += "&" + PARAM_TOURNAMENT_NAME + Challonge.encodeString(name);
			urlString += "&" + PARAM_TOURNAMENT_URL + Challonge.encodeString(customURL);
			// Subdomain may be null, check before adding it
			if(subdomain != null)
				urlString += "&" + PARAM_TOURNAMENT_SUBDOMAIN + Challonge.encodeString(subdomain);
			URL url = new URL(urlString);
			
			// Send request and process response
			String xml = Challonge.sendHttpRequest(url, "POST");
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
			e.printStackTrace();
			throw new ChallongeException(ChallongeException.REASON_XML);
		}
	}
	
	/**
	 * checks to see if the given String contains a valid tournament type
	 * 
	 * @param type String containing desired type of tournament to be created
	 * @return <b>true</b> if type is valid, <b>false</b> otherwise
	 * @see #TYPES
	 */
	public static boolean validTournamentType(String type)
	{
		for(int i = 0; i < TYPES.length; i++)
			if(type.equals(TYPES[i]))
				return true;
		
		return false;
	}
	
	// Instance methods
	/**
	 * adds a new participant to this Tournament's participant list from a username
	 * 
	 * @param name desired username to be added
	 * @throws ChallongeException if the user could not be added
	 */
	public void addParticipant(String name) throws ChallongeException
	{
		try
		{
			// Generate URL
			String urlString = Challonge.URL_START + "tournaments/" + id + "/participants.xml?" + PARAM_KEY + Challonge.encodeString(apiKey);
			urlString += "&" + PARAM_PARTICIPANT_NAME + Challonge.encodeString(name);
			URL url = new URL(urlString);
			String body = PARAM_PARTICIPANT_NAME + Challonge.encodeString(name);
			
			// Establish connection
			Challonge.sendHttpRequest(url, "POST", body);
			
			// Update participants list
			updateParticipants();
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
	 * changes the name of this Tournament
	 * 
	 * @param newName new tournament name 
	 */
	public void changeName(String newName) throws ChallongeException
	{
		try
		{
			// Ensure name is valid
			if(newName.length() > NAME_MAX_LENGTH)
				throw new ChallongeException(ChallongeException.REASON_NAME_LENGTH);
			
			// Generate and send request
			String urlString = Challonge.URL_START + "tournaments/" + id + ".xml?" + PARAM_KEY + Challonge.encodeString(apiKey);
			urlString += "&" + PARAM_TOURNAMENT_NAME + Challonge.encodeString(newName);
			URL url = new URL(urlString);
			String body = PARAM_TOURNAMENT_NAME + Challonge.encodeString(newName);
			Challonge.sendHttpRequest(url, "PUT", body);
			
			// If connection went through, change name
			name = newName;
		}
		catch(MalformedURLException mfe)
		{
			throw new ChallongeException(ChallongeException.REASON_INVALID_URL);
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
	 * deletes this tournament
	 * 
	 * @throws ChallongeException if tournament could not be deleted
	 */
	public void delete() throws ChallongeException
	{
		try
		{
			String urlString = Challonge.URL_START + "tournaments/" + id + ".xml?" + PARAM_KEY + Challonge.encodeString(apiKey);
			URL url = new URL(urlString);
			Challonge.sendHttpRequest(url, "DELETE");
		}
		catch(MalformedURLException mfe)
		{
			throw new ChallongeException(ChallongeException.REASON_INVALID_URL);
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
	 * returns the ID of this Tournament
	 * 
	 * @return unique tournament ID
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * returns the list of Matches in this Tournament
	 * 
	 * @return the list of Matches in this Tournament
	 */
	public ArrayList<Match> getMatches()
	{
		return matches;
	}
	
	/**
	 * returns the name of this Tournament
	 * 
	 * @return the name of this Tournament
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * gets a participant from this Tournament's participant list by ID number
	 * 
	 * @param id desired participant's ID number
	 * @return Participant with desired ID number, or <b>null</b> if not found
	 */
	public Participant getParticipantByID(int id)
	{
		for(int i = 0; i < participants.size(); i++)
			if(participants.get(i).getID() == id)
				return participants.get(i);
		
		// Not found
		return null;
	}
	
	/**
	 * returns the list of participants in this Tournament
	 * 
	 * @return the list of participants in this Tournament
	 */
	public ArrayList<Participant> getParticipants()
	{
		return participants;
	}
	
	/**
	 * start this tournament
	 * 
	 * @throws ChallongeException if tournament could not be started
	 */
	public void start() throws ChallongeException
	{
		try
		{
			String urlString = Challonge.URL_START + "tournaments/" + id + "/start.xml?" + PARAM_KEY + Challonge.encodeString(apiKey);
			URL url = new URL(urlString);
			Challonge.sendHttpRequest(url, "POST");
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
	 * returns a String representation of this Tournament
	 * 
	 * @return String representation of this Tournament
	 */
	@Override
	public String toString() // for testing purposes
	{
		return name + ": (ID: " + id + ") (URL: " + url + ")";
	}
	
	public ArrayList<Match> updateMatches() throws ChallongeException
	{
		System.out.println("Getting matches for " + name); // debug
		
		try
		{
			String urlString = Challonge.URL_START + "tournaments/" + id + "/matches.xml?" + PARAM_KEY + Challonge.encodeString(apiKey);
			URL url = new URL(urlString);
			String xml = Challonge.sendHttpRequest(url, "GET");
			System.out.println(xml);
			matches = Match.createMatchListFromXML(apiKey, this, xml);
			return matches;
		}
		catch(Exception e) // TODO: Actually handle exceptions
		{
			throw new ChallongeException(ChallongeException.REASON_XML);
		}
	}
	
	/**
	 * updates the participant list from the Challonge website
	 * 
	 * @return updated list of participants
	 * @throws ChallongeException if updated participant list could not be retrieved
	 */
	public ArrayList<Participant> updateParticipants() throws ChallongeException
	{
		try
		{
			String urlString = Challonge.URL_START + "tournaments/" + id + "/participants.xml?";
			urlString += PARAM_KEY + Challonge.encodeString(apiKey);
			URL url = new URL(urlString);
			String xml = Challonge.sendHttpRequest(url, "GET");
			participants = Participant.createParticipantListFromXML(apiKey, xml);
			return participants;
		}
		catch(MalformedURLException mfe)
		{
			throw new ChallongeException(ChallongeException.REASON_INVALID_URL);
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
}

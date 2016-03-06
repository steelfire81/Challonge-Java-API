package challonge;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Match {

	// CONSTANTS - Data
	public static final int RESULT_OPEN = -1;
	public static final int RESULT_DRAW = 0;
	public static final int RESULT_P1_WIN = 1;
	public static final int RESULT_P2_WIN= 2;
	
	// CONSTANTS - XML
	private static final String XML_ID = "id";
	private static final String XML_MATCH = "match";
	private static final String XML_P1_ID = "player1-id";
	private static final String XML_P2_ID = "player2-id";
	private static final String XML_STATE = "state";
	private static final String XML_STATE_OPEN = "open";
	private static final String XML_WINNER_ID = "winner_id";
	
	// DATA MEMBERS
	private String apiKey;
	private int id;
	private Tournament parentTournament;
	private Participant player1;
	private Participant player2;
	private int result;
	
	// METHODS
	// Constructors
	/**
	 * create a Match
	 * 
	 * @param key API key necessary for authentication
	 * @param i ID number of this Match
	 * @param parent Tournament to which this Match belongs
	 * @param p1 player 1
	 * @param p2 player 2
	 * @param r result (or RESULT_OPEN if the match is still going on)
	 */
	private Match(String key, int i, Tournament parent, Participant p1, Participant p2, int r)
	{
		apiKey = key;
		id = i;
		parentTournament = parent;
		player1 = p1;
		player2 = p2;
		result = r;
	}
	
	// Static
	private static Match createMatchFromElement(String apiKey, Tournament parent, Element e) throws ChallongeException
	{
		try
		{
			int id = Integer.parseInt(e.getElementsByTagName(XML_ID).item(0).getTextContent());
			int p1id = Integer.parseInt(e.getElementsByTagName(XML_P1_ID).item(0).getTextContent());
			int p2id = Integer.parseInt(e.getElementsByTagName(XML_P2_ID).item(0).getTextContent());
			String state = e.getElementsByTagName(XML_STATE).item(0).getTextContent();
			
			System.out.println(id + " " + p1id + " " + p2id + " " + state);
			
			// Determine winner (or that state is open if there is no winner)
			int result = RESULT_OPEN;
			if(!state.equals(XML_STATE_OPEN))
			{
				int winnerID = Integer.parseInt(e.getElementsByTagName(XML_WINNER_ID).item(0).getTextContent());
				if(winnerID == p1id)
					result = RESULT_P1_WIN;
				else if(winnerID == p2id)
					result = RESULT_P2_WIN;
				else
					result = RESULT_DRAW;
			}
			
			Participant p1 = parent.getParticipantByID(p1id);
			Participant p2 = parent.getParticipantByID(p2id);
			if((p1 == null) || (p2 == null))
				throw new ChallongeException(ChallongeException.REASON_PARTICIPANT_ID);
			
			return new Match(apiKey, id, parent, p1, p2, result);
		}
		catch(NumberFormatException nfe)
		{
			throw new ChallongeException(ChallongeException.REASON_XML);
		}
		catch(ChallongeException ce)
		{
			throw ce;
		}
	}
	
	/* package */ static Match createMatchFromXML(String apiKey, Tournament parent, String xml) throws ChallongeException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
			Document doc = builder.parse(input);
			
			Element e = (Element) doc.getElementsByTagName(XML_MATCH).item(0);
			return createMatchFromElement(apiKey, parent, e);
		}
		catch(ChallongeException ce)
		{
			throw ce;
		}
		catch(Exception ioe) // TODO: Actually handle exceptions
		{
			throw new ChallongeException(ChallongeException.REASON_XML);
		}
	}
	
	/**
	 * creates a list of Matches from a Challonge xml response
	 * 
	 * @param apiKey API key necessary for authentication
	 * @param parent Tournament to which these Matches belong
	 * @param xml Challonge xml response containing a list of Matches
	 * @return ArrayList of Matches
	 * @throws ChallongeException if Challonge response could not be parsed
	 */
	/* package */ static ArrayList<Match> createMatchListFromXML(String apiKey, Tournament parent, String xml) throws ChallongeException
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
			Document doc = builder.parse(input);
			
			ArrayList<Match> matchList = new ArrayList<Match>();
			NodeList list = doc.getElementsByTagName(XML_MATCH);
			for(int i = 0; i < list.getLength(); i++)
				matchList.add(createMatchFromElement(apiKey, parent, (Element) list.item(i)));
			
			return matchList;
		}
		catch(ChallongeException ce)
		{
			throw ce;
		}
		catch(Exception e) // TODO: Actually handle exceptions
		{
			throw new ChallongeException(ChallongeException.REASON_XML);
		}
	}
	
	// Instance Methods
	/**
	 * returns a String representation of this Match
	 * 
	 * @return a String representation of this Match
	 */
	@Override
	public String toString()
	{
		return player1.getName() + " vs. " + player2.getName();
	}
}

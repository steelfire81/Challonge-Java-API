package challonge;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Participant {

	// CONSTANTS - XML
	private static final String XML_ID = "id";
	private static final String XML_NAME = "name";
	private static final String XML_PARTICIPANT = "participant";
	
	// DATA MEMBERS
	private String name;
	private int id;
	
	// METHODS
	// Constructors
	/**
	 * create a participant given a name and id
	 * 
	 * @param n name
	 * @param i id
	 */
	private Participant(String n, int i)
	{
		name = n;
		id = i;
	}
	
	/**
	 * create a single participant from a Challonge xml response
	 * 
	 * @param xml String containing Challonge xml response
	 * @return a single Participant
	 */
	/* package */ static Participant createParticipantFromXML(String xml)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
			Document doc = builder.parse(input);
			
			Element e = (Element) doc.getElementsByTagName(XML_PARTICIPANT).item(0);
			String name = e.getElementsByTagName(XML_NAME).item(0).getTextContent();
			int id = Integer.parseInt(e.getElementsByTagName(XML_ID).item(0).getTextContent()); // TODO: Catch when not an int
			
			return new Participant(name, id);	
		}
		catch(Exception e) // TODO: Actually handle exceptions
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * creates a list of Participants from a Challonge xml response
	 * 
	 * @param xml String containing Challonge xml response
	 * @return a list of Participants
	 */
	/* package */ static ArrayList<Participant> createParticipantListFromXML(String xml)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
			Document doc = builder.parse(input);
			
			ArrayList<Participant> participantList = new ArrayList<Participant>();
			NodeList list = doc.getElementsByTagName(XML_PARTICIPANT);
			for(int i = 0; i < list.getLength(); i++)
			{
				Element e = (Element) list.item(i);
				String name = e.getElementsByTagName(XML_NAME).item(0).getTextContent();
				int id = Integer.parseInt(e.getElementsByTagName(XML_ID).item(0).getTextContent()); // TODO: Catch when not int
				
				participantList.add(new Participant(name, id));
			}
			
			return participantList;
		}
		catch (Exception e) // TODO: Actually handle exceptions
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * returns the unique ID number of this Participant
	 * 
	 * @return this Participant's ID number
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * returns the name of this Participant
	 * 
	 * @return this Participant's name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * returns a String representation of this Participant
	 * 
	 * @return a String representation of this Participant
	 */
	@Override
	public String toString() // for testing purposes
	{
		return name;
	}
}

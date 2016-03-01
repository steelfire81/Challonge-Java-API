package challonge;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Tournament {

	// CONSTANTS - Tournament Types
	public static final String TYPE_SINGLE_ELIM = "single elimination";
	public static final String TYPE_DOUBLE_ELIM = "double elimination";
	public static final String TYPE_ROUND_ROBIN = "round robin";
	public static final String TYPE_SWISS = "swiss";
	
	// CONSTANTS - XML Tags
	private static final String XML_DESCRIPTION = "description";
	private static final String XML_ID = "id";
	private static final String XML_NAME = "name";
	private static final String XML_TOURNAMENT = "tournament";
	private static final String XML_TYPE = "tournament-type";
	private static final String XML_URL = "url";
	
	// DATA MEMBERS
	int id;
	String name;
	String url;
	String description;
	String type;
	
	// METHODS
	/**
	 * creates a tournament given an ID, name, URL, description, and type
	 * @param i ID
	 * @param n name
	 * @param u URL
	 * @param d description
	 * @param t type (TYPE_SINGLE_ELIM, TYPE_DOUBLE_ELIM, etc.)
	 */
	private Tournament(int i, String n, String u, String d, String t)
	{
		id = i;
		name = n;
		url = u;
		description = d;
		type = t;
	}
	
	/**
	 * creates a tournament from XML data received from Challonge
	 * 
	 * @param xml XML data as a String containing one tournament
	 * @return a fully initialized tournament, or <b>null</b> if initialization fails
	 */
	public static Tournament createTournamentFromXML(String xml)
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
			
			return new Tournament(id, name, url, description, type);	
		}
		catch(Exception e) // TODO: Actually handle exceptions
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static ArrayList<Tournament> createTournamentListFromXML(String xml)
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
				
				tournamentList.add(new Tournament(id, name, url, description, type));
			}
			
			return tournamentList;
		}
		catch (Exception e) // TODO: Actually handle exceptions
		{
			e.printStackTrace();
			return null;
		}
	}
	
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

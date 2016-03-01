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
	private static final String XML_NAME = "name";
	private static final String XML_PARTICIPANT = "participant";
	
	// DATA MEMBERS
	private String name;
	
	// METHODS
	private Participant(String n)
	{
		name = n;
	}
	
	public static Participant createParticipantFromXML(String xml)
	{
		try
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes());
			Document doc = builder.parse(input);
			
			Element e = (Element) doc.getElementsByTagName(XML_PARTICIPANT).item(0);
			String name = e.getElementsByTagName(XML_NAME).item(0).getTextContent();
			
			return new Participant(name);	
		}
		catch(Exception e) // TODO: Actually handle exceptions
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public static ArrayList<Participant> createParticipantListFromXML(String xml)
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
				
				participantList.add(new Participant(name));
			}
			
			return participantList;
		}
		catch (Exception e) // TODO: Actually handle exceptions
		{
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public String toString() // for testing purposes
	{
		return name;
	}
}

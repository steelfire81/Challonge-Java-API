package test;

import java.util.ArrayList;
import challonge.*;

public class ChallongeTest {

	private static final String KEY = ""; // replace with your API key
	private static final String SUBDOMAIN = ""; // replace with your subdomain
	
	public static void main(String[] args) throws ChallongeException
	{
		ArrayList<Tournament> tournaments = Challonge.getTournamentIndex(KEY, SUBDOMAIN);
		for(int i = 0; i < tournaments.size(); i++)
		{
			System.out.println(tournaments.get(i));
			for(int j = 0; j < tournaments.get(i).getParticipants().size(); j++)
				System.out.println("\t- " + tournaments.get(i).getParticipants().get(j));
		}
		
		Tournament myNewTournament = Tournament.createTournament(KEY, "My New Tournament", "idslfhglsidfuh", Tournament.TYPE_DOUBLE_ELIM);
		System.out.println("NEW TOURNAMENT:");
		System.out.println(myNewTournament);
		
		Tournament mySubdomainTournament = Tournament.createTournament(KEY, "This Tournament Has A Subdomain", "uasdfhisudfhisdhuf", Tournament.TYPE_ROUND_ROBIN, SUBDOMAIN);
		System.out.println("NEW TOURNAMENT WITH SUBDOMAIN:");
		System.out.println(mySubdomainTournament);
		
		mySubdomainTournament.changeName("This Tournament Will Never Take Place");
		System.out.println("UPDATED NAME:");
		System.out.println(mySubdomainTournament);
		
		System.out.println("REGISTERING PARTICIPANTS FOR MY NEW TOURNAMENT");
		myNewTournament.addParticipant("Moe");
		myNewTournament.addParticipant("Larry");
		myNewTournament.addParticipant("Curly");
		System.out.println(myNewTournament);
		for(int i = 0; i < myNewTournament.getParticipants().size(); i++)
			System.out.println(myNewTournament.getParticipants().get(i));
		
		System.out.println("DELETING NEW TOURNAMENTS");
		mySubdomainTournament.delete();
		myNewTournament.delete();
		
		System.out.println("MATCH LIST FROM 1015");
		for(int i = 0; i < tournaments.size(); i++)
			if(tournaments.get(i).getName().contains("October"))
				for(int j = 0; j < tournaments.get(i).getMatches().size(); j++)
					System.out.println(tournaments.get(i).getMatches().get(j));
	}
}

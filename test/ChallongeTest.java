package test;

import java.util.ArrayList;
import challonge.*;

public class ChallongeTest {

	private static final String KEY = "";
	private static final String SUBDOMAIN = "";
	
	public static void main(String[] args) throws ChallongeException
	{
		ArrayList<Tournament> tournaments = Challonge.getTournamentIndex(KEY, SUBDOMAIN);
		for(int i = 0; i < tournaments.size(); i++)
			System.out.println(tournaments.get(i));
		
		Tournament myNewTournament = Tournament.createTournament(KEY, "My New Tournament", "idslfhglsidfuh", Tournament.TYPE_DOUBLE_ELIM);
		System.out.println("NEW TOURNAMENT:");
		System.out.println(myNewTournament);
		
		Tournament mySubdomainTournament = Tournament.createTournament(KEY, "This Tournament Has A Subdomain", "uasdfhisudfhisdhuf", Tournament.TYPE_ROUND_ROBIN, SUBDOMAIN);
		System.out.println("NEW TOURNAMENT WITH SUBDOMAIN:");
		System.out.println(mySubdomainTournament);
		
		mySubdomainTournament.updateName("This Tournament Will Never Take Place");
		System.out.println("UPDATED NAME:");
		System.out.println(mySubdomainTournament);
	}
}

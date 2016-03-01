package challonge;

import java.io.IOException;

public class ChallongeException extends IOException {

	// CONSTANTS - Messages
	public static final String REASON_DEFAULT = "Problem connecting with Challonge service";
	public static final String REASON_KEY = "Could not authenticate using given API key";
	public static final String REASON_ARGUMENTS = "Invalid arguments";
	public static final String REASON_NAME_LENGTH = "Name too long";
	public static final String REASON_INVALID_URL = "Invalid URL";
	public static final String REASON_XML = "Could not parse input XML";
	public static final String REASON_TOURNEY_TYPE = "Invalid tournament type";
	
	// METHODS
	public ChallongeException()
	{
		super(REASON_DEFAULT);
	}
	
	public ChallongeException(String message)
	{
		super(message);
	}
}

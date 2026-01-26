package botsserver;

public final class ChatCommandParser {
	private ChatCommandParser() {}

	public static String extractCommand(String message, String botName)
	{
		if (message == null)
			return null;
		String trimmed = message.trim();
		if (trimmed.isEmpty())
			return null;
		if (trimmed.startsWith("@"))
			return trimmed.substring(1).trim();
		int atIndex = trimmed.indexOf('@');
		if (atIndex != -1 && atIndex + 1 < trimmed.length())
			return trimmed.substring(atIndex + 1).trim();
		return null;
	}
}

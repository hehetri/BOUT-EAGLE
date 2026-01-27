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
		for (int i = 0; i < trimmed.length(); i++)
		{
			if (trimmed.charAt(i) != '@')
				continue;
			if (i > 0 && isWordChar(trimmed.charAt(i - 1)))
				continue;
			if (i + 1 >= trimmed.length())
				continue;
			if (!isWordChar(trimmed.charAt(i + 1)))
				continue;
			return trimmed.substring(i + 1).trim();
		}
		return null;
	}

	private static boolean isWordChar(char value)
	{
		return Character.isLetterOrDigit(value) || value == '_';
	}
}

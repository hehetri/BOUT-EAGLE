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
		int bracketIndex = trimmed.lastIndexOf(']');
		if (bracketIndex != -1 && bracketIndex + 1 < trimmed.length()){
			String after = trimmed.substring(bracketIndex + 1).trim();
			if (after.startsWith("@"))
				return after.substring(1).trim();
		}
		if (botName != null && !botName.isEmpty() && trimmed.startsWith(botName)){
			String after = trimmed.substring(botName.length()).trim();
			if (after.startsWith("@"))
				return after.substring(1).trim();
		}
		return null;
	}
}

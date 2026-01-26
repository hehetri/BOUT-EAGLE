package botsserver;

public class ChatCommandParserTest {
	public static void main(String[] args)
	{
		String command = ChatCommandParser.extractCommand("@help", "Player");
		if (!"help".equals(command))
			throw new RuntimeException("Expected to parse direct command.");
		command = ChatCommandParser.extractCommand("[Player] @exit", "Player");
		if (!"exit".equals(command))
			throw new RuntimeException("Expected to parse bracketed command.");
		command = ChatCommandParser.extractCommand("Player @win", "Player");
		if (!"win".equals(command))
			throw new RuntimeException("Expected to parse name-prefixed command.");
		command = ChatCommandParser.extractCommand("Hello world", "Player");
		if (command != null)
			throw new RuntimeException("Expected no command for non-command message.");
	}
}

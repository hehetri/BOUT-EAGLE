package botsserver;

public final class Game {
	private Game() {}

	public static void chatCommand(BotClass bot, Packet packet)
	{
		if (bot == null || packet == null || bot.room == null)
			return;
		packet.getInt(2);
		packet.getInt(2);
		String message = packet.getString(0, packet.getLen(), false).trim();
		if (message.isEmpty())
			return;
		if (message.startsWith("@"))
		{
			bot.lobby.standard.ParseChatCommand(bot, message.substring(1));
			return;
		}
		bot.room.SendMessage(true, 0, "["+bot.botname+"]"+message, 0);
	}
}

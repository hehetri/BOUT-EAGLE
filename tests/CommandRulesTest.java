package botsserver;

public class CommandRulesTest {
	public static void main(String[] args)
	{
		if (!CommandRules.canUseExit(CommandRules.ROOM_STATUS_ACTIVE))
			throw new RuntimeException("@exit should be allowed during active match.");
		if (CommandRules.canUseExit(0))
			throw new RuntimeException("@exit should be blocked outside active match.");
		if (!CommandRules.canUseWin(CommandRules.GM_LEVEL_GM, CommandRules.ROOM_STATUS_ACTIVE))
			throw new RuntimeException("@win should be allowed for GM in active match.");
		if (CommandRules.canUseWin(0, CommandRules.ROOM_STATUS_ACTIVE))
			throw new RuntimeException("@win should be blocked for normal players.");
		if (CommandRules.canUseWin(CommandRules.GM_LEVEL_GM, 0))
			throw new RuntimeException("@win should be blocked outside active match.");
	}
}

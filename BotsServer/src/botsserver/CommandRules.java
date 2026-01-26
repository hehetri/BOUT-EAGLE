package botsserver;

public final class CommandRules {
	public static final int GM_LEVEL_MOD = 100;
	public static final int GM_LEVEL_GM = 150;
	public static final int GM_LEVEL_SUPER = 200;
	public static final int GM_LEVEL_ADMIN = 250;
	public static final int ROOM_STATUS_ACTIVE = 3;

	private CommandRules() {}

	public static boolean hasPermission(int gmLevel, int requiredLevel)
	{
		return gmLevel >= requiredLevel;
	}

	public static boolean isActiveMatch(int roomStatus)
	{
		return roomStatus == ROOM_STATUS_ACTIVE;
	}

	public static boolean canUseExit(int roomStatus)
	{
		return isActiveMatch(roomStatus);
	}

	public static boolean canUseWin(int gmLevel, int roomStatus)
	{
		return hasPermission(gmLevel, GM_LEVEL_GM) && isActiveMatch(roomStatus);
	}
}

package botsserver;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Standard {
	/**
	 * @author BoutEagle
	 */
	public int[][] mapvalues = new int[42][];
	public String[][] drops = new String[15][];
	public int[][] SpMobs = new int[3][];
	//public int[][] rebirthspawn = new int[2][];
	public Map<Integer, Integer> rebirthspawn = new HashMap<>();
    public int[] droplist;
    public int[] explevels = {};
    public String[] tickets = new String[100];
    public String[] filterwords = new String[0];

    public Standard()
    {
    	MapValues();
    	Droplist();
    	ExpLevel();
    	drops();
    }
    
    public void debug(String msg)
    {
    	Main.debug("StandardActions:"+msg);
    }
    
    public void MapValues()
    {
    	//total mobs, difficulty, boss, elite, droplevel, time, minmobs
    	mapvalues[0]=new int[]{37, 1, 3, 0, 1, 8, 21};
    	mapvalues[1]=new int[]{41, 3, 11, 0, 1, 8, 15};
    	mapvalues[2]=new int[]{30, 5, 26, 0, 2, 9, 19};
    	mapvalues[3]=new int[]{37, 8, 163, 1, 2, 12, 28};
    	mapvalues[4]=new int[]{34, 10, 9, 0, 2, 9, 20};
    	mapvalues[5]=new int[]{43, 12, 56, 0, 2, 9, 25};
    	mapvalues[6]=new int[]{36, 15, 22, 0, 3, 9, 26};
    	mapvalues[7]=new int[]{43, 17, 34, 0, 3, 9, 21};
    	mapvalues[8]=new int[]{41, 18, 170, 1, 3, 21, 26};
    	mapvalues[9]=new int[]{66, 20, 41, 0, 3, 12, 52};
    	mapvalues[10]=new int[]{45, 22, 46, 0, 3, 12, 24};
    	mapvalues[11]=new int[]{26, 25, 67, 0, 4, 12, 15};
    	mapvalues[12]=new int[]{46, 28, 177, 1, 4, 24, 31};
    	mapvalues[13]=new int[]{46, 30, 91, 0, 4, 14, 36};
    	mapvalues[14]=new int[]{65, 32, 68, 0, 4, 14, 20};
    	mapvalues[15]=new int[]{40, 35, 96, 0, 5, 14, 17};
    	mapvalues[16]=new int[]{42, 38, 182, 1, 5, 27, 33};
    	mapvalues[17]=new int[]{61, 40, 108, 0, 5, 14, 42};
    	mapvalues[18]=new int[]{76, 45, 116, 0, 6, 14, 60};
    	mapvalues[19]=new int[]{57, 48, 190, 1, 6, 27, 35};
    	mapvalues[20]=new int[]{69, 50, 132, 0, 6, 14, 65};
    	mapvalues[21]=new int[]{71, 55, 122, 0, 6, 14, 31};
    	mapvalues[22]=new int[]{71, 58, 198, 1, 6, 27, 68};
    	mapvalues[23]=new int[]{65, 60, 158, 0, 7, 14, 50};
    	mapvalues[24]=new int[]{82, 68, 245, 1, 8, 27, 66};
    	mapvalues[25]=new int[]{87, 70, 221, 0, 8, 15, 50};//61
    	mapvalues[26]=new int[]{90, 78, 270, 1, 9, 29, 50};
    	mapvalues[27]=new int[]{78, 80, 226, 0, 9, 15, 50};
    	mapvalues[28]=new int[]{75, 88, 292, 1, 10, 30, 50};
    	mapvalues[29]=new int[]{81, 90, 235, 0, 10, 15, 40};
    	mapvalues[30]=new int[]{74, 95, 279, 0, 11, 17, 50};
    	mapvalues[31]=new int[]{76, 98, 301, 1, 14, 30, 52};
    	mapvalues[32]=new int[]{121, 100, 388, 0, 14, 22, 60};
    	mapvalues[33]=new int[]{107, 105, 393, 0, 14, 25, 60};
    	mapvalues[34]=new int[]{112, 108, 405, 1, 14, 24, 60};
    	mapvalues[35]=new int[]{123, 110, 318, 1, 27, 45, 90};//set droplevel for new sets! -> 27 and set lvl
    	mapvalues[36]=new int[]{126, 113, 331, 1, 27, 45, 90};
    	mapvalues[37]=new int[]{127, 115, 340, 1, 27, 45, 88};
    	mapvalues[38]=new int[]{127, 118, 355, 2, 27, 45, 73};
    	//mapvalues[39]=new int[]{55, 1, 362, 0, 1, 21, 45};//event start
    	//mapvalues[40]=new int[]{84, 50, 372, 0, 6, 23, 70};
    	//mapvalues[41]=new int[]{108, 100, 378, 0, 14, 23, 80}; //event end
    	SpMobs[0]=new int[]{38,94,93,135,207,230,239,282,291,304};
    	SpMobs[1]=new int[]{82,86};
    	int[][] replace = new int[2][];
    	//111-112-113-114-115 127-128-129-130 240-241-242-243-244
    	replace[0]=new int[]{81,85,111,127,142,150,155,193,194,195,196,240,255,274,296,313};
    	replace[1]=new int[]{164,28,115,131,142,147,147,115,115,115,115,244,255,273,295,295};
    	for (int i = 0; i<replace[0].length; i++)
    		rebirthspawn.put(replace[0][i],replace[1][i]);
    	/* 
    	 * Easter maps
    	 * 
    	mapvalues[36]=new int[]{89, 1, 395, 1, 1, 20, 50};
    	mapvalues[37]=new int[]{89, 50, 409, 1, 6, 20, 50};
    	mapvalues[38]=new int[]{89, 100, 423, 1, 14, 20, 50};
    	 * 
    	 * halloween maps
    	 *
    	mapvalues[39]=new int[]{82, 1, 416, 2, 1, 22, 21};
    	mapvalues[40]=new int[]{82, 50, 424, 2, 6, 22, 21};
    	mapvalues[41]=new int[]{82, 100, 432, 2, 14, 22, 21};
    	 *
    	 *Christmas maps
    	 *
    	mapvalues[39]=new int[]{55, 1, 362, 0, 1, 21, 45};
    	mapvalues[40]=new int[]{84, 50, 372, 0, 6, 23, 70};
    	mapvalues[41]=new int[]{108, 100, 378, 0, 14, 23, 80};
    	 */
    }
    
    public void ExpLevel()
    {
    	int[] level = new int[200];
    	String[] value = {};
    	int i = 1;
    	ResultSet rs = Main.sql.psquery("SELECT * FROM `bout_exptable`", value);
    	try{
    		while (rs.next()){
    			level[i] = rs.getInt("ReqExp");
    			i++;
    		}
    		rs.close();
    	}catch (Exception e){}
    	explevels = Arrays.copyOfRange(level, 0, i+1);
    }
    
    public void drops()
    {
		int[] i = new int[15];
		String[][] dropi = new String[15][500];
		try{
			String[] value= new String[0];
    	    ResultSet lvl = Main.sql.psquery("SELECT * FROM `drops` ORDER BY `minlevel` ASC", value);
    	    while (lvl.next()){
        	    String typ = lvl.getString("type");
        	    int sort = typ.equals("gun") ? 0 : (typ.equals("ef") ? 1 : (typ.equals("minibot") ? 2 : 
        	    	(typ.equals("shield") ? 3 : (typ.equals("shoulder") ? 4 : 5))));
        	    dropi[sort][i[sort]]=""+lvl.getInt("itemid")+" "+lvl.getInt("minlevel")+" "+lvl.getInt("maxlevel");
        	    i[sort]++;
    	    }
		}catch(Exception e){debug("Error in item selection![1] "+e);}
		try{
			String[] value= new String[0];
    	    ResultSet lvl = Main.sql.psquery("SELECT * FROM `coindrops`", value);
    	    while (lvl.next()){
    	    	String typ = lvl.getString("type");
    	    	int sort=typ.equals("head") ? 6 : (typ.equals("minibot") ? 7 : (typ.equals("wing") ? 8 : 
    	    		(typ.equals("flag") ? 9 : (typ.equals("gun") ? 10 : (typ.equals("trans") ? 11 : 
    	    			(typ.equals("merc") ? 12 : (typ.equals("gold") ? 13 : 14)))))));
    	    	dropi[sort][i[sort]]=""+lvl.getInt("itemid")+" 0 120";
    	    	i[sort]++;
			}
		}catch (Exception e){}
		for (int b = 0; b<15; b++)
			drops[b]=Arrays.copyOfRange(dropi[b],0,i[b]);
    }
	
	public void Droplist(){
		this.droplist=new int[147];
		/**5  hp 			- 15% - 6
		 * 6  rb 			- 10% - 4
		 * 7  recharge 		- 20% - 8
		 * 8  trans up		- 15% - 6
		 * 9  stun bomb		- 20% - 8
		 * 10 bomb			- 20% - 8
		 * 18 arms				  - 12
		 * 19 head				  - 12
		 * 20 body				  - 10
		 * 21 gun				  - 8
		 * 22 ef				  - 8
		 * 23 minibot			  - 8
		 * 35 shield			  - 6
		 * 36 shoulder			  - 6
		 * 38 gold				  - 10
		 * 39 event				  - 0
		 * 51 coin head			  - 4
		 * 52 coin minibot		  - 4 
		 * 53 coin wings		  - 4
		 * 54 coin flag			  - 4
		 * 55 coin gun			  - 4
		 * 56 coin trans		  - 4
		 * 57 coin merc			  - 4
		 */
		this.droplist[0]=5;
		this.droplist[1]=6;
		this.droplist[2]=7;
		this.droplist[3]=8;
		this.droplist[4]=9;
		this.droplist[5]=10;
		this.droplist[6]=5;
		this.droplist[7]=6;
		this.droplist[8]=7;
		this.droplist[9]=8;
		this.droplist[10]=9;
		this.droplist[11]=10;
		this.droplist[12]=5;
		this.droplist[13]=6;
		this.droplist[14]=7;
		this.droplist[15]=8;
		this.droplist[16]=9;
		this.droplist[17]=10;
		this.droplist[18]=5;
		this.droplist[19]=6;
		this.droplist[20]=7;
		this.droplist[21]=8;
		this.droplist[22]=9;
		this.droplist[23]=10;
		this.droplist[24]=5;
		this.droplist[25]=7;
		this.droplist[26]=8;
		this.droplist[27]=9;
		this.droplist[28]=10;
		this.droplist[29]=5;
		this.droplist[30]=7;
		this.droplist[31]=8;
		this.droplist[32]=9;
		this.droplist[33]=10;
		this.droplist[34]=7;
		this.droplist[35]=9;
		this.droplist[36]=10;
		this.droplist[37]=7;
		this.droplist[38]=9;
		this.droplist[39]=10;
		this.droplist[40]=18;
		this.droplist[41]=19;
		this.droplist[42]=20;
		this.droplist[43]=21;
		this.droplist[44]=22;
		this.droplist[45]=23;
		this.droplist[46]=35;
		this.droplist[47]=36;
		this.droplist[48]=38;
		this.droplist[49]=51;
		this.droplist[50]=52;
		this.droplist[51]=53;
		this.droplist[52]=54;
		this.droplist[53]=55;
		this.droplist[54]=56;
		this.droplist[55]=57;
		this.droplist[56]=18;
		this.droplist[57]=19;
		this.droplist[58]=20;
		this.droplist[59]=21;
		this.droplist[60]=22;
		this.droplist[61]=23;
		this.droplist[62]=35;
		this.droplist[63]=36;
		this.droplist[64]=38;
		this.droplist[65]=51;
		this.droplist[66]=52;
		this.droplist[67]=53;
		this.droplist[68]=54;
		this.droplist[69]=55;
		this.droplist[70]=56;
		this.droplist[71]=57;
		this.droplist[72]=18;
		this.droplist[73]=19;
		this.droplist[74]=20;
		this.droplist[75]=21;
		this.droplist[76]=23;
		this.droplist[77]=35;
		this.droplist[78]=36;
		this.droplist[79]=38;
		this.droplist[80]=51;
		this.droplist[81]=52;
		this.droplist[82]=53;
		this.droplist[83]=54;
		this.droplist[84]=55;
		this.droplist[85]=56;
		this.droplist[86]=57;
		this.droplist[87]=18;
		this.droplist[88]=19;
		this.droplist[89]=20;
		this.droplist[90]=21;
		this.droplist[91]=22;
		this.droplist[92]=23;
		this.droplist[93]=35;
		this.droplist[94]=36;
		this.droplist[95]=38;
		this.droplist[96]=51;
		this.droplist[97]=52;
		this.droplist[98]=53;
		this.droplist[99]=54;
		this.droplist[100]=55;
		this.droplist[101]=56;
		this.droplist[102]=57;
		this.droplist[103]=18;
		this.droplist[104]=19;
		this.droplist[105]=20;
		this.droplist[106]=21;
		this.droplist[107]=22;
		this.droplist[108]=23;
		this.droplist[109]=35;
		this.droplist[110]=36;
		this.droplist[111]=38;
		this.droplist[112]=18;
		this.droplist[113]=19;
		this.droplist[114]=20;
		this.droplist[115]=21;
		this.droplist[116]=22;
		this.droplist[117]=23;
		this.droplist[118]=35;
		this.droplist[119]=36;
		this.droplist[120]=38;
		this.droplist[121]=18;
		this.droplist[122]=19;
		this.droplist[123]=20;
		this.droplist[124]=21;
		this.droplist[125]=22;
		this.droplist[126]=23;
		this.droplist[127]=38;
		this.droplist[128]=18;
		this.droplist[129]=19;
		this.droplist[130]=20;
		this.droplist[131]=21;
		this.droplist[132]=22;
		this.droplist[133]=23;
		this.droplist[134]=38;
		this.droplist[135]=18;
		this.droplist[136]=19;
		this.droplist[137]=20;
		this.droplist[138]=38;
		this.droplist[139]=18;
		this.droplist[140]=19;
		this.droplist[141]=20;
		this.droplist[142]=38;
		this.droplist[143]=18;
		this.droplist[144]=19;
		this.droplist[145]=18;
		this.droplist[146]=19;
		/**
		 * 39 event		- inactive
		 */
	}
	
	public void ParseRoomCommands(BotClass bot, String command)
	{
		if (bot.roomnum==-1)
			return;
		String[] pcommand=command.split("\\s+");
		if (pcommand[0].equalsIgnoreCase("help"))
		{
			if(bot.roomnum==bot.room.roomowner){
				bot.sendChatMsg("@rm <name> transfer roommaster to specified botname.", 2, false, -1);
				bot.sendChatMsg("@kick <name> kick player from room.", 2, false, -1);
			}
			bot.sendChatMsg("@suicide kills your bot.", 2, false, -1);
			bot.sendChatMsg("@exit exit current room.", 2, false, -1);
		}
		if (pcommand[0].equalsIgnoreCase("exit"))
			if (bot.room.Exit(bot.roomnum, false))
            	bot.RemoveRoom(bot.room);
		if (pcommand[0].equalsIgnoreCase("suicide"))
			bot.room.Dead(bot.roomnum, 10);
		if (pcommand[0].equalsIgnoreCase("rm"))
			if(bot.roomnum==bot.room.roomowner)
				for (int i = 0; i<8; i++)
					if(bot.room.bot[i]!=null && bot.room.bot[i].botname.equalsIgnoreCase(pcommand[1])){
						bot.room.roomowner=i;
						bot.room.UserInfoPacket(-1);
						break;
					}
		if (pcommand[0].equalsIgnoreCase("kick"))
			if(bot.roomnum==bot.room.roomowner)
				for (int i = 0; i<8; i++)
					if(bot.room.bot[i]!=null && bot.room.bot[i].botname.equalsIgnoreCase(pcommand[1]))
						if (bot.room.Exit(i, true)){
                    		bot.RemoveRoom(bot.room);
                    		break;
						}
				
	}
	
	protected boolean isNumeric(String str)
	{
	  return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
	}
	
	public void ParseCommands(BotClass bot, String[] command)
	{
		command=command[0].split("\\s+");
		if (command[0].equals("help"))
		{
			if (command.length==1){
				bot.sendChatMsg("-------Showing all available commands-------", 1, false, -1);
				if (bot.gm>249) {
					bot.sendChatMsg("@dropreload              - need admin rights", 2, false, -1);
				}
				if (bot.gm>199){
            		bot.sendChatMsg("@coins <amount>          - need supergm rights", 2, false, -1);
            		bot.sendChatMsg("@item <itemid>           - need supergm rights", 2, false, -1);
            		bot.sendChatMsg("@gigas <amount>          - need supergm rights", 2, false, -1);
            		bot.sendChatMsg("@itemname <itemid>       - need supergm rights", 2, false, -1);
            		bot.sendChatMsg("@itemid <itemname> <page>- need supergm rights", 2, false, -1);
            		bot.sendChatMsg("@statboost <on|off>      - need supergm rights", 2, false, -1);
            		bot.sendChatMsg("@iplookup <name> <-u>    - need supergm rights", 2, false, -1);
            	}
            	if(bot.gm>149){
            		bot.sendChatMsg("@kick <charactername>       - need gm rights", 4, false, -1);
            		bot.sendChatMsg("@delinvent <part|0 for all> - need gm rights", 4, false, -1);
            		bot.sendChatMsg("@destroyroom <number> <mode>- need gm rights", 4, false, -1);
            		bot.sendChatMsg("@bruteforce <number> <mode> - need gm rights", 4, false, -1);
            		bot.sendChatMsg("@ban <player> <time> <reason>- need gm rights", 4, false, -1);
            		bot.sendChatMsg("@lookup <name> <-u>         - need gm rights", 4, false, -1);
            	}
            	if(bot.gm>99){
            		bot.sendChatMsg("@mute <player> <time>       - need mod rights", 1, false, -1);
            		bot.sendChatMsg("@getTickets <page>          - need mod rights", 1, false, -1);
            		bot.sendChatMsg("@solveTicket <number>       - need mod rights", 1, false, -1);
            		bot.sendChatMsg("@announce <on|off>          - need mod rights", 1, false, -1);
            	}
				bot.sendChatMsg("@help <command> gives more info about the command.", 2, false, -1);
            	bot.sendChatMsg("@exit - leave the current room.", 2, false, -1);
            	bot.sendChatMsg("@tags <tag> - all roomtags that are available for use.", 2, false, -1);
            	bot.sendChatMsg("@ticket <question info> creates a ticket to get help from staff.", 1, false, -1);
            	return;
			}
			bot.sendChatMsg("--------info for command: "+command[1]+"--------", 2, false, -1);
			if (command[1].equals("dropreload")) {
				bot.sendChatMsg("By using this command the droplist from the database,", 2, false, -1);
				bot.sendChatMsg("will be reloaded into memory, will return time spent reloading.", 2, false, -1);
			}
			else if (command[1].equals("coins") && bot.gm>199)
				bot.sendChatMsg("Adds specified amount to your coin balance.", 2, false, -1);
			else if (command[1].equals("gigas") && bot.gm>199)
				bot.sendChatMsg("Adds specified amount to your gigas balance.", 2, false, -1);
			else if (command[1].equals("item") && bot.gm>199)
				bot.sendChatMsg("Adds the specified item to your inventory.", 2, false, -1);
			else if (command[1].equals("itemname") && bot.gm>199)
				bot.sendChatMsg("Returns name of specified itemid if it excists.", 2, false, -1);
			else if (command[1].equals("item") && bot.gm>199){
				bot.sendChatMsg("Returns all found similair names to specified name.", 2, false, -1);
				bot.sendChatMsg("Specify a page name to see the next row of results.", 2, false, -1);}
			else if (command[1].equals("statboost") && bot.gm>199){
				bot.sendChatMsg("sets your stats to OP.", 2, false, -1);
				bot.sendChatMsg("if on or off is not specified the current setting will be inverted.", 2, false, -1);}
			else if (command[1].equals("kick") && bot.gm>149)
				bot.sendChatMsg("Kicks specified player from the server.", 2, false, -1);
			else if (command[1].equals("delinvent") && bot.gm>149){
				bot.sendChatMsg("Removes item from specified slot in inventory.", 2, false, -1);
				bot.sendChatMsg("if specified slot is 0 all items in inventory will be removed.", 2, false, -1);}
			else if (command[1].equals("destroyroom") && bot.gm>149){
				bot.sendChatMsg("Destroys specified room.", 2, false, -1);
				bot.sendChatMsg("Rooms can be specified by number or roomname", 2, false, -1);
				bot.sendChatMsg("if mode isn't specified sector will be used", 2, false, -1);
				bot.sendChatMsg("Modes: sector=1, pvp=0, base=2", 2, false, -1);}
			else if (command[1].equals("bruteforce") && bot.gm>149){
				bot.sendChatMsg("Joins a room regardless of password.", 2, false, -1);
				bot.sendChatMsg("Rooms can be specified by number or roomname", 2, false, -1);
				bot.sendChatMsg("if mode isn't specified sector will be used", 2, false, -1);
				bot.sendChatMsg("Modes: sector=1, pvp=0, base=2", 2, false, -1);}
			else if (command[1].equals("ban") && bot.gm>149) {
				bot.sendChatMsg("bans player for time if time=0 it will unban the player.", 2, false, -1);
				bot.sendChatMsg("Adding a reason for banning is optional but encouraged!", 2, false, -1);
			}
			else if (command[1].equals("mute") && bot.gm>99)
				bot.sendChatMsg("Mutes player for time if time=0 it will unmute the player.", 2, false, -1);
			else if (command[1].equalsIgnoreCase("gettickets") && bot.gm>99){
				bot.sendChatMsg("Gets all open tickets by page.", 2, false, -1);
				bot.sendChatMsg("Maximum page is 20.", 2, false, -1);}
			else if (command[1].equalsIgnoreCase("solveticket") && bot.gm>99)
				bot.sendChatMsg("Solves ticket with specified number.", 2, false, -1);
			else if ((command[1].equalsIgnoreCase("announce") || command[1].equalsIgnoreCase("a")) && bot.gm>99){
				bot.sendChatMsg("Sets chat color and broadcast to ON or OFF.", 2, false, -1);
				bot.sendChatMsg("if on or off is not specified the current setting will be inverted.", 2, false, -1);}
			else if (command[1].equals("help")){
				bot.sendChatMsg("Shows all available commands.", 2, false, -1);
				bot.sendChatMsg("if a command is specified it will show extra information about the command.", 2, false, -1);}
			else if (command[1].equals("tags")){
				bot.sendChatMsg("Shows all available roomtags with some info about them.", 2, false, -1);
				bot.sendChatMsg("if a roomtag is specified it will show extra information about the command.", 2, false, -1);}
			else if (command[1].equalsIgnoreCase("exit"))
				bot.sendChatMsg("Leaves the current room when used while inside one.", 2, false, -1);
			else if (command[1].equals("ticket"))
				bot.sendChatMsg("This opens a ticket which staff will try to answer as soon as possible.", 2, false, -1);
			else if (command[1].equals("lookup") && bot.gm>149) {
				bot.sendChatMsg("Returns information about the specified username or botname.", 2, false, -1);
				bot.sendChatMsg("When using lookup enter name first and when using an username add the tag -u.", 2, false, -1);
				bot.sendChatMsg("The information includes if user was banned and if a reason was given why.", 2, false, -1);
				bot.sendChatMsg("A list of alts is provided using the setup username(botname).", 2, false, -1);
			}
			else if (command[1].equals("iplookup") && bot.gm>199) {
				bot.sendChatMsg("Returns the ip adress for the specified username or botname.", 2, false, -1);
				bot.sendChatMsg("When using iplookup enter name first and when using an username add the tag -u.", 2, false, -1);
			}
			else
				bot.sendChatMsg("Specified command doesn't exist.", 2, false, -1);
			return;
		}
		if (command[0].equalsIgnoreCase("exit")) {
			if (bot.room!=null && bot.room.Exit(bot.roomnum, false))
				bot.RemoveRoom(bot.room);
			return;
		}
		if (command[0].equalsIgnoreCase("tags"))
        {
			if (command.length==1){
				bot.sendChatMsg("-------Showing all roomtags-------", 1, false, -1);
				return;
			}
			if (command[1].equals("e"))
				bot.sendChatMsg("sets room to extreme mode", 1, false, -1);
			else
				bot.sendChatMsg("Specified tag doesn't exist.", 2, false, -1);
			return;
        }
		if (command[0].equalsIgnoreCase("ticket"))
        {
			for (int i = 0; i<100; i++)
				if (tickets[i]==null){
					tickets[i]="<"+bot.botname+">";
					for (int a = 1; a<command.length; a++)
						tickets[i]+=command[a];
				}
			bot.sendChatMsg("Added ticket to the list.", 2, false, -1);
        }
		if (command[0].equalsIgnoreCase("gettickets") && bot.gm>99)
        {
			int number=Integer.parseInt(command[1])-1;
			if (number<20)
				bot.sendChatMsg("-------Showing tickets, Page "+(number+1)+"-------", 1, false, -1);
			else
				bot.sendChatMsg("number exceeds limit.",2 ,false, -1);
			for (int i = 5*number; i<(5*number+5); i++)
				if (number<100 && tickets[i]!=null)
					bot.sendChatMsg("["+i+"]:"+tickets[i], 2, false, -1);
        }
		if (command[0].equalsIgnoreCase("solveticket") && bot.gm>99)
        {
			int number=Integer.parseInt(command[1])-1;
			if (number>=0 && number<100)
				tickets[number]=null;
			int check=0;
			for (int i = 0; i<100; i++)
				if (tickets[number]!=null){
					tickets[check]=tickets[number];
					if (number!=check)
						tickets[number]=null;
					check++;
				}
        }
		if (command[0].equals("mute") && bot.gm>99)
        {
			int time=Integer.parseInt(command[2]);
			String[] value = new String[]{""+time, command[1]};
			bot.sql.psupdate("UPDATE `bout_characters` SET `muted`=?, `muteStime`=now() WHERE `name`=?", value);
			int num = bot.getNum(command[1]);
			if (num!=-1)
				bot.lobby.bots[num].muted=time;
			bot.sendChatMsg(command[1]+" has been "+(time==0 ? "unmuted" : "muted "+(time==-1 ? "permanently" : "temporarily")), 2, false, -1);
			return;
        }
		if ((command[0].equals("announce") || command[0].equals("a")) && bot.gm>99)
        {
			if (command.length==1)
				bot.announce=!bot.announce;
			else
				if (command[1].equals("on"))
					bot.announce=true;
				else if (command[1].equals("off"))
					bot.announce=false;
			bot.sendChatMsg("Announce has been set "+(bot.announce ? "on." : "off."), 2, false, -1);
			return;
        }
		if (command[0].equals("ban") && bot.gm>149)
        {
			try {
			int time=Integer.parseInt(command[2]);
			String banr ="Not specified";
			if (command.length>3) {
				banr = "";
				for (int i = 3; i<command.length; i++)
					banr += (i==3 ? "" : " ")+command[i];
			}
			String account = "";
			ResultSet rs = bot.sql.psquery("SELECT `username` FROM `bout_characters` WHERE `name`=?", new String[]{command[1]});
			try{
				if(rs.next())
					account=rs.getString("username");
				rs.close();
			}catch (Exception e){}
			String[] value = {""+(time==0 ? 0 : 1), ""+time, (time==0 ? "manual unban" : banr), account};
			bot.sql.psupdate("UPDATE `bout_users` SET `banned`=?, `bantime`=?, `banStime`=now(), `banreason`=? WHERE `username`=?", value);
			int num = bot.getNum(command[1]);
			if (num != -1)
				bot.lobby.bots[num].channel.closecon();
			bot.sendChatMsg(account+"("+command[1]+") has been "+(time==0 ? "unbanned" : "banned "+(time==-1 ? "permanently" : "temporarily")), 2, false, -1);
			}catch (Exception e){debug(e.getMessage());}
			return;
        }
		if (command[0].equals("kick") && bot.gm>149)
        {
			String name = command[1];
			int num = bot.getNum(name);
			if (num == -1){
				bot.sendChatMsg("Player "+name+" isn't online or doesn't exist", 2, false, -1);
				return;
			}
			bot.lobby.bots[num].channel.closecon();
			return;
        }
		if (command[0].equals("delinvent") && bot.gm>149)
        {
			int number=Integer.parseInt(command[1]);
			if (number==0)
				for (int i = 0; i<10; i++)
					bot.inventitems[i]=0;
			else
				bot.inventitems[number-1]=0;
			bot.UpdateInvent();
			return;
        }
		if (command[0].equals("bruteforce") && bot.gm>149)
        {
			int mode=1;
			int number=-1;
			if (command.length==3)
				mode=Integer.parseInt(command[2]);
			if(isNumeric(command[1]))
				number=Integer.parseInt(command[1])-1;
			else
				for (int i = mode*600; i<(mode*600+600); i++)
		    		if (bot.lobby.rooms[i].roomname.equals(command[1]))
		    			number=i;
			if (number==-1)
				return;
			Room room = bot.lobby.rooms[mode*600+number];
            if(room!=null)
            	room.Join(bot, room.password, bot.ip);
            return;
        }
		if (command[0].equals("destroyroom") && bot.gm>149)
        {
			int mode=1;
			int number=-1;
			if (command.length==3)
				mode=Integer.parseInt(command[2]);
			if(isNumeric(command[1]))
				number=Integer.parseInt(command[1])-1;
			else
				for (int i = mode*600; i<(mode*600+600); i++)
		    		if (bot.lobby.rooms[i].roomname.equals(command[1]))
		    			number=i;
			if (number==-1)
				return;
			Room room = bot.lobby.rooms[mode*600+number];
			bot.RemoveRoom(room);
			return;
        }
		if (command[0].equals("lookup") && bot.gm>149)
        {
			int user=1;
			if (command.length==3)
				user=command[2].equals("-u") ? 2 : 1;
			String username=command[1];
			if (user==1) {
				try {
					String[] value= new String[1];
		        	value[0]=command[1];
					ResultSet rs = bot.sql.psquery("SELECT * FROM bout_characters WHERE `name`=? LIMIT 1", value);
		            if(rs.next())
		            {
		            	username=rs.getString("username");
		            }
		            rs.close();
				} catch (Exception e) {}
			}
			try {
				String ip="";
				String charname=command[1];
				String banreason = "";
				int banned = 0;
				int bantime = 0;
				String bandate = "";
				int calctime = 0;
				String[] value= new String[1];
				value[0]=username;
				String[] users = new String[20];
				int[] banusers = new int[20];
				String[] charusers = new String[20];
				ResultSet rs = bot.sql.psquery("SELECT last_ip,banned,banreason,bantime,banStime FROM bout_users WHERE username=? LIMIT 1", value);
	            if(rs.next())
	            {
	            	ip=rs.getString("last_ip");
	            	banned=rs.getInt("banned");
	            	banreason=rs.getString("banreason");
	            	bantime=rs.getInt("bantime");
	            	bandate=rs.getString("banStime");
	            }
	            rs.close();
	            if (banned>0) {
	            	value[0]= bandate;
					rs = Main.sql.psquery("SELECT TIMESTAMPDIFF(SECOND, ?, now()) AS seconds", value);
					if(rs.next())
		            {
						calctime = rs.getInt("seconds")-bantime;
		            }
					rs.close();
	            }
	            value[0]=ip;
				rs = bot.sql.psquery("SELECT `banned`,`username` FROM `bout_users` WHERE `last_ip`=? LIMIT 20", value);
				int i = 0;
	            while(rs.next())
	            {
	            	users[i]=rs.getString("username");
	            	banusers[i]=rs.getInt("banned");
	            	try {
	            		ResultSet prs = bot.sql.psquery("SELECT `name` FROM bout_characters WHERE `username`=?", new String[] {users[i]});
	            		if (prs.next())
	            			charusers[i]=prs.getString("name");
	            		prs.close();
	            	} catch (Exception e){debug(e.getMessage());}
	            	i++;
	            	if (i == 20)
	            		break;
	            }
	            rs.close();
	            bot.sendChatMsg("---Showing info for " +username+"("+charname+")---", 2, false, -1);
	            bot.sendChatMsg(banned == 0 ? "User is not banned" : ("User is " + (bantime==-1 ? "permanently banned for " : 
	            	"banned for "+calctime+" seconds, reason ")+banreason), 2, false, -1);
	            String notbanned = "";
	            String isbanned = "";
	            for (i = 0; i<20; i++)
	            	if (users[i]==null || users[i].equals(""))
	            		break;
	            	else if (banusers[i]==0)
	            		notbanned += users[i]+"("+charusers[i]+"), ";
	            	else
	            		isbanned += users[i]+"("+charusers[i]+"), ";
	            String[] nban = notbanned.split(",");
	            for (i = 0; i<nban.length; i+=2)
	            	if (i==0 && nban.length>2)
	            		bot.sendChatMsg("Alts(not banned): "+nban[i+0]+nban[i+1], 4, false, -1);
	            	else if (i==0)
	            		bot.sendChatMsg("Alts(not banned): "+nban[i+0], 4, false, -1);
	            	else if (nban.length>2+i)
	            		bot.sendChatMsg(nban[i+0]+nban[i+1], 4, false, -1);
	            	else
	            		bot.sendChatMsg(nban[i+0], 4, false, -1);
	            String[] iban = isbanned.split(",");
	            for (i = 0; i<iban.length; i+=2)
	            	if (i==0 && iban.length>2)
	            		bot.sendChatMsg("Alts(banned): "+iban[i+0]+iban[i+1], 2, false, -1);
	            	else if (i==0)
	            		bot.sendChatMsg("Alts(banned): "+iban[i+0], 2, false, -1);
	            	else if (iban.length>2+i)
	            		bot.sendChatMsg(iban[i+0]+iban[i+1], 2, false, -1);
	            	else
	            		bot.sendChatMsg(iban[i+0], 2, false, -1);
			} catch (Exception e) {debug(e.getMessage());}
			return;
        }
		if (command[0].equals("iplookup") && bot.gm>199)
        {
			String ip = "";
			String username="";
			String charname="";
			int user=1;
			if (command.length==3)
				user=command[2].equals("-u") ? 2 : 1;
			if (user==1) {
				charname=command[1];
				try {
            		ResultSet rs = bot.sql.psquery("SELECT `username` FROM bout_characters WHERE `name`=?", new String[] {charname});
            		if (rs.next())
            			username=rs.getString("username");
            		rs.close();
            	} catch (Exception e){debug(e.getMessage());}
			}
			else
			{
				username=command[1];
				try {
	        		ResultSet rs = bot.sql.psquery("SELECT `name` FROM bout_characters WHERE `username`=?", new String[] {username});
	        		if (rs.next())
	        			charname=rs.getString("name");
	        		rs.close();
	        	} catch (Exception e){debug(e.getMessage());}
	        }
			try {
				ResultSet rs = bot.sql.psquery("SELECT `last_ip` FROM bout_users WHERE `username`=? LIMIT 1", new String[] {username});
	            if(rs.next())
	            {
	            	ip=rs.getString("last_ip");
	            }
        	}catch (Exception e) {debug(e.getMessage());}
			bot.sendChatMsg("Showing ip for "+username+"("+charname+"): "+ip, 1, false, -1);
            return;
        }
		if (command[0].equals("item") && bot.gm>199)
        {
			int itemid=Integer.parseInt(command[1]);
			int time=0;
			for (int i = 0; i<10; i++)
				if (bot.inventitems[i]==0){
					String[] value = {""+itemid};
					String name=null;
			    	ResultSet rs = bot.sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", value);
			        try{
			        	if (rs.next()){
			        		time = rs.getInt("days");
			        		name = rs.getString("name");
			        	}
			        	rs.close();
			        }catch (Exception e){}
					bot.inventitems[i]=itemid;
					if (time>0)
	    				bot.AddItemTime(itemid, i, "item", time);
					bot.UpdateInvent();
					bot.sendChatMsg("Added item: "+name+" at inventory slot "+(i+1), 2, false, -1);
					break;
				}
			return;
        }
		if (command[0].equals("itemname") && bot.gm>199)
        {
			int itemid=Integer.parseInt(command[1]);
			String name = "";
			ResultSet rs = bot.sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", new String[]{""+itemid});
			try{
				if (rs.next())
					name=rs.getString("name");
				rs.close();
			}catch(Exception e){}
			bot.sendChatMsg("Found item: "+name+" with id: "+itemid, 2, false, -1);
			return;
        }
		if (command[0].equals("statboost") && bot.gm>199)
        {
			boolean safed = bot.statboost;
			if (command.length==1)
				bot.statboost=!bot.statboost;
			else
				if (command[1].equals("on"))
					bot.statboost=true;
				else if (command[1].equals("off"))
					bot.statboost=false;
			if ((safed && bot.statboost) || (!safed && !bot.statboost))
				return;
			if (bot.statboost){
				bot.hpb+=30000;
			    bot.attminb+=3000;
			    bot.attmaxb+=4500;
			    bot.attmintransb+=4000;
			    bot.attmaxtransb+=6000;
			}
			if (!bot.statboost){
				bot.hpb-=30000;
			    bot.attminb-=3000;
			    bot.attmaxb-=4500;
			    bot.attmintransb-=4000;
			    bot.attmaxtransb-=6000;
			}
        }
		if (command[0].equals("itemid") && bot.gm>199)
        {
			int limit=0;
			if (command.length==2)
				limit=0;
			else
				limit=Integer.parseInt(command[2])-1;
			bot.sendChatMsg("showing results for page "+limit+".", 2, false, -1);
			ResultSet rs = bot.sql.psquery("SELECT * FROM bout_items WHERE UPPER(name) LIKE UPPER(?) LIMIT "+(limit*5)+",5", new String[]{"%"+command[1]+"%"});
			try{
				while (rs.next()){
					String name=rs.getString("name");
					int itemid=rs.getInt("id");
					bot.sendChatMsg(""+itemid+" - "+name, 2, false, -1);
				}
				rs.close();
			}catch(Exception e){}
			return;
        }
		if (command[0].equals("coins") && bot.gm>199)
        {
            bot.coins+=Integer.parseInt(command[1]);
            bot.sendChatMsg("Current coins: " + bot.coins, 2, false, -1);
            bot.UpdateCoins();
            return;
        }
		if (command[0].equals("gigas") && bot.gm>199)
        {
            bot.gigas+=Integer.parseInt(command[1]);
            bot.sendChatMsg("Current gigas: " + bot.gigas, 2, false, -1);
            bot.UpdateBot();
            return;
        }
		if (command[0].equals("reboot") && bot.gm>249)
        {
            int time=Integer.parseInt(command[1]);
            debug("rebooting in "+time+" seconds");
            Main.restartApplication();
            return;
        }
		if (command[0].equals("dropreload") && bot.gm>249)
        {
			Long start = System.currentTimeMillis();
            drops();
			int time = (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start);
            bot.sendChatMsg("Reloaded the drop list in: "+time+"s.", 2, false, -1);
            return;
        }
	}
	
	public int[][] moblist(int map)
	{
		int[] moblist = new int[0];
		int[] mobtype = new int[0];
		//for mobtype :: normal 0, trigger 1, rebirth 2, exceptional 3 
		//(kill all triggers to advance) (rebirth mobs that respawn) (exceptional mobs don't die when all triggers killed)
		switch (map)
		{
			case 0:
			{
				moblist=new int[]{2,2,2,2,4,4,-1,-1,0,0,0,0,2,2,4,4,4,2,82,2,2,2,2,0,0,-1,-1,-1,2,2,0,2,0,0,2,0,3,102,124};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,3,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,3,-1};
				break;
			}
			case 1:
			{
				moblist=new int[]{-1,-1,10,10,12,12,-1,-1,-1,-1,10,10,10,10,-1,10,12,12,12,12,12,12,12,12,-1,12,10,12,10,12,10,12,82,82,-1,14,14,14,14,14,10,12,14,10,12,14,10,12,14,10,11,12,12,10};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,1,1,1,1,3,3,3,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,0,1,0,0,0};
				break;
			}
			case 2:
			{
				moblist=new int[]{24,-1,24,-1,-1,-1,-1,-1,-1,-1,-1,24,-1,23,-1,24,25,25,21,-1,82,82,-1,82,23,24,-1,24,23,21,23,24,24,25,-1,-1,23,23,21,-1,82,-1,-1,21,24,24,21,82,-1,26,-1,-1,-1,-1};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,3,3,1,3,1,1,1,3,1,1,1,1,1,1,1,1,1,1,1,1,3,1,0,0,0,0,0,3,0,1,0,0,0,0};
				break;
			}
			case 3:
			{
				moblist=new int[]{159,-1,159,-1,159,159,-1,160,-1,-1,-1,159,-1,159,-1,160,159,159,162,-1,-1,159,160,81,159,160,-1,160,159,162,159,160,160,161,159,160,161,161,161,161,161,-1,-1,162,160,160,162,159,-1,163,-1,-1,160,-1,164};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,3,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,4,0,0,0,0,-1};
				break;
			}
			case 4:
			{
				moblist=new int[]{24,24,5,5,6,6,5,6,24,5,-1,82,5,6,83,5,24,-1,85,24,-1,8,24,-1,-1,5,6,5,5,24,5,6,24,5,5,6,5,83,-1,-1,-1,-1,-1,-1,9,28};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,3,1,1,3,1,1,0,3,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,3,0,0,0,-1,0,0,1,-1};
				break;
			}
			case 5:
			{
				moblist=new int[]{16,16,16,16,16,16,6,6,6,16,16,6,83,16,5,6,5,6,5,5,6,6,19,82,82,83,-1,6,6,85,83,-1,6,6,82,83,-1,6,6,5,5,6,6,19,5,56,82,83,83};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,3,1,1,1,1,1,1,1,1,1,1,3,3,3,1,1,1,3,3,1,1,1,3,3,0,0,0,0,0,0,0,0,0,4,3,3,0};
				break;
			}
			case 6:
			{
				moblist=new int[]{17,15,15,15,15,19,15,15,15,15,15,15,18,18,20,15,15,15,15,17,17,17,15,15,17,17,18,17,19,18,20,17,22,82,84,83,-1,-1,28,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,1,3,3,3,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
				break;
			}
			case 7:
			{
				moblist=new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,15,15,17,17,18,20,20,33,18,18,17,17,15,18,18,18,33,18,17,17,17,-1,20,34,19,33,18,18,18,17,15,15,15,15,15,15,82,83,83,82,82,15,-1};
				mobtype=new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,0,1,1,1,1,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,3,0,3,3,3,-1,-1};
				break;
			}
			case 8:
			{
				moblist=new int[]{166,164,165,164,165,168,164,164,165,165,164,164,167,167,168,164,165,164,165,166,166,166,164,165,166,166,167,166,168,167,168,166,170,166,166,166,167,167,86,86,86,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,4,0,0,0,0,0,3,3,3,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
				break;
			}
			case 9:
			{
				moblist=new int[]{37,35,-1,35,-1,39,6,6,35,35,82,83,35,35,35,6,6,36,36,39,82,85,6,37,39,35,35,36,36,37,39,38,35,35,6,6,35,35,36,6,35,6,37,39,6,6,37,39,-1,-1,-1,35,36,39,37,41,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,35,35,6,6,35,35,-1,6,6,6,-1,-1,-1,-1,82,85,6,6,35,6,82,84,83,28};
				mobtype=new int[]{1,1,1,1,1,0,0,0,0,0,3,3,0,0,0,0,0,0,0,0,3,3,0,1,1,0,0,0,0,1,3,3,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,-1,0,0,0,0,2,0,4,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,3,3,0,0,0,0,-1,-1,-1,-1};
				break;
			}
			case 10:
			{
				moblist=new int[]{-1,18,18,35,35,18,18,35,35,37,20,-1,18,18,48,18,18,39,39,38,20,18,18,18,35,36,35,45,36,-1,49,18,18,82,85,-1,46,45,36,35,35,-1,82,83,85,82,84,82,83,83,28};
				mobtype=new int[]{-1,1,1,1,1,0,0,1,1,1,1,0,0,0,1,0,0,1,1,3,3,3,3,3,1,1,1,1,0,0,1,0,0,3,3,0,4,0,0,0,0,0,3,3,3,3,3,3,3,3,-1};
				break;
			}
			case 11:
			{
				moblist=new int[]{51,51,52,53,-1,-1,-1,-1,-1,-1,82,83,51,51,55,60,-1,-1,-1,-1,-1,-1,-1,-1,55,55,54,61,-1,-1,-1,-1,-1,-1,-1,-1,82,83,85,83,62,57,58,58,58,59,58,67,-1,-1,-1,28};
				mobtype=new int[]{1,1,1,1,-1,-1,-1,-1,-1,-1,3,3,1,1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,3,3,3,3,1,1,1,1,0,0,0,4,-1,-1,-1,-1};
				break;
			}
			case 12:
			{
				moblist=new int[]{173,171,-1,171,-1,-1,171,171,172,-1,171,171,86,171,-1,-1,-1,-1,86,81,86,-1,-1,173,176,171,171,-1,172,173,-1,174,171,172,-1,172,-1,86,-1,-1,-1,172,173,176,-1,172,173,176,164,-1,172,86,172,173,176,177,-1,-1,-1,-1,-1,171,-1,172,172,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,171,-1,171,172,-1,-1,-1,-1,171,-1,-1,-1,-1,-1,172,-1,-1,171,172,-1,-1,-1,-1};
				mobtype=new int[]{1,1,1,1,1,0,0,0,0,0,0,0,3,0,0,0,0,0,3,3,3,0,0,1,1,0,0,0,0,1,3,3,0,0,0,0,0,3,0,0,0,0,1,1,1,1,1,1,-1,0,0,3,0,2,0,4,-1,-1,-1,-1,0,0,0,0,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,-1,-1};
				break;
			}
			case 13:
			{
				moblist=new int[]{87,87,87,87,-1,87,-1,87,-1,82,87,83,88,89,87,-1,90,88,87,-1,88,-1,87,88,92,87,87,89,-1,83,82,89,88,85,87,87,-1,92,87,92,-1,87,87,84,89,87,-1,88,88,87,-1,87,87,-1,-1,-1,-1,-1,-1,92,90,91,-1,28,93};
				mobtype=new int[]{1,1,1,0,0,0,0,0,0,3,0,3,0,1,0,0,1,1,0,0,0,0,0,0,1,0,0,1,1,3,3,1,1,3,1,1,1,1,1,1,1,1,1,3,1,0,0,1,1,1,0,1,1,0,0,0,0,0,0,0,0,4,0,0,3};
				break;
			}
			case 14:
			{
				moblist=new int[]{17,17,17,-1,17,17,17,17,17,17,99,102,98,98,82,83,83,-1,-1,17,17,17,17,98,99,17,100,17,100,83,84,85,95,98,94,-1,17,17,98,98,98,98,99,17,17,97,98,98,82,84,83,17,-1,17,17,17,97,98,99,98,68,102,17,17,17,98,99,17,95,83,83,84,94,-1,28};
				mobtype=new int[]{1,1,1,-1,0,0,0,0,0,0,0,1,0,0,3,3,0,-1,-1,1,1,0,0,1,1,0,1,0,3,3,3,3,3,0,3,-1,0,0,1,1,1,1,1,1,0,0,1,1,3,3,3,0,-1,0,0,0,0,0,2,0,4,0,0,0,0,0,2,0,0,0,0,0,0,-1,-1};
				break;
			}
			case 15:
			{
				moblist=new int[]{-1,-1,17,97,98,17,98,99,94,99,97,95,17,17,98,98,83,63,98,84,97,85,-1,17,-1,99,-1,-1,99,-1,-1,83,-1,-1,95,-1,95,102,-1,98,-1,99,82,-1,97,-1,98,83,98,101,97,100,100,17,100,100,100,95,17,100,96,17,17,99,99,83,-1,-1,98,-1,99,-1,98,98,-1,-1,-1,-1,94,28};
				mobtype=new int[]{1,1,1,1,1,1,1,1,3,1,1,3,-1,0,0,0,3,0,0,3,1,3,0,0,0,0,0,0,0,0,0,3,0,0,3,3,0,0,0,0,0,0,3,0,0,0,0,3,0,3,1,3,3,3,3,3,3,3,3,0,4,0,0,0,0,3,0,0,2,0,0,-1,-1,-1,-1,-1,-1,-1,-1,-1};
				break;
			}
			/*case 13: unused i-worm map
			case 14:
			{
				moblist=new int[]{70,70,71,71,-1,-1,-1,-1,-1,-1,-1,-1,73,73,73,73,-1,-1,-1,-1,-1,-1,-1,-1,77,79,-1,80,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,74,84,74,77,78,77,86,-1,-1,-1,-1};
				mobtype=new int[]{1,1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,1,1,1,1,1,1,1,1,-1,-1,-1,-1};
				break;
			}
			*/
			case 16:
			{
				moblist=new int[]{179,179,179,179,179,-1,-1,-1,-1,86,179,81,180,179,178,-1,181,179,178,178,179,179,180,-1,-1,179,179,180,86,180,180,-1,-1,-1,-1,178,178,178,179,-1,-1,179,179,-1,181,178,81,179,179,178,-1,178,178,94,-1,-1,178,178,179,180,181,182,-1,-1,184,164};
				mobtype=new int[]{1,1,1,0,0,0,0,0,0,3,0,3,0,1,0,0,1,1,0,0,0,0,1,0,0,0,1,1,3,1,1,0,0,0,0,1,1,1,1,0,0,1,1,0,1,0,3,1,1,1,0,1,1,3,0,0,0,0,0,0,0,4,0,0,3,-1};
				break;
			}
			case 17:
			{
				moblist=new int[]{95,105,97,17,17,17,17,17,17,84,97,-1,-1,103,104,104,104,104,105,105,107,94,104,82,104,-1,105,-1,104,83,104,105,105,105,103,104,104,105,97,104,104,97,-1,82,82,-1,-1,17,17,104,104,105,105,94,95,94,104,104,104,107,106,106,108,109,109,109,17,28};
				mobtype=new int[]{2,1,1,1,1,1,1,1,1,3,1,1,1,3,1,1,1,1,1,1,1,3,1,3,1,1,1,1,1,3,1,1,1,0,3,1,1,1,1,1,1,1,0,3,3,1,1,1,1,1,1,1,1,3,3,3,1,1,1,1,1,1,1,0,0,0,0,-1};
				break;
			}
			case 18:
			{
				moblist=new int[]{111,112,113,114,115,117,117,117,117,117,111,117,117,117,117,117,117,118,118,118,111,117,117,117,117,83,82,85,121,121,111,121,105,109,-1,119,119,121,119,119,111,120,120,117,-1,117,137,-1,121,121,111,120,120,105,119,119,105,121,121,121,111,120,121,120,121,121,121,109,109,121,111,123,105,84,121,84,121,121,121,105,121,28,-1,-1,-1,-1,-1,-1,-1,-1,-1,125,125,125,125,125,-1,-1,-1,-1,116,117,117,117,117,117,117,118,118,118,118,118,109,82,82,-1,-1,82,83,109,109,109};
				mobtype=new int[]{-1,-1,-1,-1,-1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,3,3,3,3,3,1,2,0,0,0,0,0,3,0,0,1,0,0,0,0,0,0,0,2,3,1,0,0,0,0,0,2,0,0,0,1,0,3,0,3,3,3,2,3,3,1,0,0,3,0,3,0,2,2,0,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,3,3,3,0,0,-1,-1,-1,-1,1,0,0,0,0,0,0,0,0,0,0,0,0,3,3,0,0,3,3,0,0,0};
				break;
			}
			case 19:
			{
				moblist=new int[]{95,187,186,186,192,86,192,192,192,81,187,-1,-1,185,186,186,186,186,187,187,189,94,-1,-1,-1,-1,187,-1,81,187,186,187,187,187,185,186,186,187,187,-1,-1,192,187,187,-1,-1,-1,-1,192,192,86,-1,-1,94,95,94,186,186,186,189,188,188,190,191,191,191,186,164};
				mobtype=new int[]{2,1,1,1,1,3,1,1,1,3,1,1,1,3,1,1,1,1,1,1,1,3,1,1,1,1,1,1,3,1,1,1,1,0,3,1,1,1,1,1,1,1,0,0,0,1,1,1,1,1,3,1,1,3,3,3,1,1,1,1,1,1,4,0,0,0,0,-1};
				break;
			}
			case 20:
			{
				moblist=new int[]{127,128,129,130,131,-1,127,127,127,127,127,127,127,-1,132,109,109,133,133,105,133,84,141,141,141,109,137,135,134,139,109,82,136,136,136,136,136,136,109,133,139,133,139,139,139,139,135,133,133,134,138,138,137,134,133,133,136,136,136,136,133,133,134,134,134,140,135,82,141,141,105,141,109,141,83,141,141,141,141,141,141,141,141,141,-1,109,142,139,143,105,143,143,143,143,143,143,143,143,28};
				mobtype=new int[]{1,-1,-1,-1,-1,-1,1,1,1,1,1,1,1,-1,1,0,0,0,0,0,0,3,0,0,0,0,0,3,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,4,0,3,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,-1,0,3,3,0,0,0,0,0,0,0,0,0,0,-1};
				break;
			}
			case 21:
			{
				moblist=new int[]{147,148,148,148,148,151,137,28,-1,127,148,148,148,133,133,151,151,82,83,83,-1,127,147,147,152,135,-1,-1,-1,-1,82,83,84,83,85,82,83,133,133,148,137,148,154,147,137,152,-1,-1,127,82,85,133,133,147,84,148,148,137,133,133,122,137,154,94,128,129,130,131,137};
				mobtype=new int[]{2,1,1,1,1,1,1,-1,-1,1,0,0,0,0,0,0,0,3,3,3,-1,1,0,0,0,3,-1,-1,-1,-1,3,3,3,3,3,3,3,1,1,1,1,0,0,0,0,0,-1,-1,1,3,3,0,0,0,1,0,0,0,0,0,4,0,0,0,-1,-1,-1,-1,2};
				break;
			}
			case 22:
			{
				moblist=new int[]{193,194,195,196,197,199,199,199,199,199,193,199,199,199,199,199,199,200,200,200,193,199,199,199,199,81,-1,86,203,203,193,203,201,201,-1,201,201,203,201,201,193,202,202,199,199,199,86,202,203,203,193,202,202,202,201,201,201,203,203,203,193,202,203,202,203,250,-1,203,203,203,193,205,250,86,203,81,203,203,203,203,203,164,-1,-1,-1,-1,-1,-1,-1,-1,-1,207,207,207,207,207,-1,-1,-1,-1,198,199,199,199,199,199,199,200,200,200,200,200,200,199,199,199,199,199,199,200,200,200};
				mobtype=new int[]{-1,-1,-1,-1,-1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,3,0,3,2,3,1,2,0,0,0,0,0,3,0,0,1,0,0,0,0,0,3,0,2,3,1,0,0,0,0,0,2,0,0,0,1,0,3,0,3,3,3,2,3,3,1,0,0,3,0,3,2,2,2,2,2,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,3,3,3,0,0,-1,-1,-1,-1,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
				break;
			}
			case 23:
			{
				moblist=new int[]{144,147,148,150,151,144,144,148,149,148,148,150,148,148,148,144,144,144,149,150,149,150,-1,148,148,148,148,-1,84,156,148,152,148,148,149,149,148,148,145,145,145,145,145,145,145,145,83,153,153,85,82,144,-1,144,144,149,148,148,153,153,153,155,155,156,-1,145,145,145,-1,157,154,155,153,153,82,82,84,85,-1,-1,-1,-1,151,150,155,155,156,-1,158,28};
				mobtype=new int[]{1,0,0,0,0,1,1,0,0,3,0,0,3,0,0,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,0,3,3,0,1,0,0,0,0,0,0,1,1,1,1,1,1,1,1,3,3,3,3,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,0,4,0,0,0,0,3,3,3,0,0,0,0,0,0,0,0,0,0,0,1,-1};
				break;
			}
			case 24:
			{
				moblist=new int[]{240,241,242,243,244,86,240,240,240,240,240,240,240,-1,245,191,191,191,191,191,250,251,254,254,254,191,191,248,247,252,250,249,249,249,249,249,86,81,250,246,252,246,252,252,252,252,248,191,191,247,251,251,191,250,246,247,249,249,249,249,246,246,247,247,247,253,248,86,254,254,191,254,250,254,254,254,254,254,254,254,254,254,254,254,81,191,255,252,256,191,256,256,256,256,256,256,256,256,164};
				mobtype=new int[]{1,-1,-1,-1,-1,3,1,1,1,1,1,1,1,-1,1,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,3,3,0,0,0,0,0,0,0,0,3,3,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,4,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,3,3,0,0,0,0,0,0,0,0,0,0,-1};
				break;
			}
			case 25:
			{
				moblist=new int[]{213,215,215,216,-1,209,209,209,209,209,213,216,-1,-1,215,215,217,209,216,82,83,211,209,209,-1,-1,-1,-1,215,215,215,217,217,220,-1,-1,209,-1,209,209,213,216,-1,209,219,219,216,-1,217,85,84,82,82,208,217,-1,213,216,216,220,-1,-1,-1,-1,208,209,209,-1,-1,210,210,217,217,216,216,82,83,-1,213,213,212,83,209,82,218,219,215,215,215,215,-1,216,-1,220,216,-1,82,82,85,82,-1,-1,221,28};
				mobtype=new int[]{2,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,3,0,1,1,0,0,0,0,0,0,0,0,0,0,0,-1,-1,0,0,0,0,0,0,0,0,0,1,0,0,0,3,3,3,0,1,1,1,0,0,0,0,-1,-1,-1,-1,1,1,0,0,0,0,1,0,0,0,0,3,3,0,0,0,1,3,0,3,4,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,0,0,4,-1};
				break;
			}
			case 26:
			{
				moblist=new int[]{262,264,264,265,265,258,258,258,258,258,262,265,265,265,264,264,266,258,265,265,265,265,258,258,265,265,86,81,264,264,264,266,266,269,-1,-1,258,-1,258,258,262,265,265,258,268,268,265,265,266,86,86,164,-1,257,266,265,262,265,265,269,264,264,-1,-1,257,258,258,265,265,259,259,266,266,265,265,86,86,81,262,262,261,-1,258,-1,267,268,264,264,264,264,265,265,266,269,265,265,264,266,265,265,-1,-1,270};
				mobtype=new int[]{2,0,0,0,0,1,1,1,1,1,0,0,0,0,0,0,0,0,0,-1,-1,1,1,0,0,0,3,0,0,0,0,0,0,0,-1,-1,0,0,0,0,0,0,0,0,0,1,0,0,0,3,3,-1,-1,1,1,1,0,0,0,0,0,0,-1,-1,1,1,0,0,0,0,1,0,0,0,0,3,3,3,0,0,1,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4};
				break;
			}
			case 27:
			{
				moblist=new int[]{222,222,222,222,222,222,224,227,230,228,222,222,224,224,230,222,222,222,224,222,223,225,224,222,222,-1,222,225,228,225,227,229,227,82,227,230,222,227,229,230,222,224,-1,229,222,230,222,222,222,224,225,225,225,225,224,224,227,222,222,229,84,83,85,83,84,82,230,226,225,225,225,225,225,223,224,224,85,229,229,82,84,82,83,28};
				mobtype=new int[]{1,1,1,0,3,1,1,3,0,1,0,0,1,3,0,1,1,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,3,0,0,0,3,0,0,0,0,0,0,0,0,0,0,1,1,1,1,3,3,3,1,1,3,3,3,0,0,0,0,0,4,0,0,0,0,2,0,0,0,0,0,0,3,3,3,0,-1};
				break;
			}
			case 28:
			{
				moblist=new int[]{283,283,283,283,283,283,285,288,291,289,283,283,285,285,291,283,283,283,285,283,284,286,285,283,283,-1,283,286,289,286,288,290,288,-1,288,291,283,288,290,291,283,285,-1,290,283,291,283,283,283,285,286,286,286,286,285,285,288,-1,-1,286,286,286,286,290,286,286,291,292,286,286,286,286,286,284,285,285,-1,290,290,86,81,81,86,86,86,86,164};
				mobtype=new int[]{1,1,1,0,3,1,1,3,0,1,0,0,1,3,0,1,1,0,0,0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,3,0,0,0,3,0,0,0,0,0,0,0,0,0,0,1,1,1,1,3,3,3,0,-1,1,1,1,1,0,3,3,0,4,0,0,0,0,0,0,0,0,0,0,0,3,3,3,3,0,3,0,-1};
				break;
			}
			case 29:
			{
				moblist=new int[]{231,231,223,236,232,83,229,229,229,231,233,231,231,82,231,231,231,84,233,236,231,233,233,82,231,239,234,234,234,228,-1,-1,236,231,231,233,223,231,231,239,233,233,233,234,234,234,234,228,231,231,239,231,83,236,233,233,234,234,234,234,232,229,229,236,239,235,234,234,-1,-1,231,231,228,234,234,-1,-1,233,231,231,233,231,231,239,229,239,83,85,84,82,83,82,84,84,28};
				mobtype=new int[]{1,1,1,3,1,3,0,0,3,0,0,0,0,3,0,0,1,3,0,3,1,0,1,3,1,3,1,1,1,1,0,0,3,0,0,1,1,1,1,0,0,0,0,1,1,1,1,0,0,0,0,0,3,0,2,0,1,1,1,1,3,0,3,3,0,4,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,3,3,0,3,3,0,-1};
				break;
			}
			case 30:
			{
				moblist=new int[]{271,272,271,271,271,272,271,272,271,271,271,-1,-1,83,276,276,271,271,271,272,271,82,82,280,280,280,280,237,237,272,272,237,237,275,82,83,282,277,275,275,272,272,237,237,273,273,274,274,275,84,85,282,273,273,276,278,274,273,274,274,273,273,272,272,276,273,85,84,82,84,85,82,275,275,280,280,280,280,279,280,280,280,280,273,273,28};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,3,1,0,0,0,0,0,0,3,0,0,0,0,0,1,1,1,1,1,1,0,3,3,3,1,0,0,0,0,0,0,1,1,1,1,1,3,3,3,3,3,3,1,0,0,0,0,0,0,0,0,0,0,3,3,3,3,3,3,1,1,0,0,0,0,4,0,0,0,0,2,2,-1};
				break;
			}
			case 31:
			{
				moblist=new int[]{293,294,293,293,293,294,-1,293,293,294,293,293,293,86,298,293,298,293,293,293,294,86,86,302,302,302,302,287,287,294,294,287,287,297,81,86,304,299,297,297,294,294,287,287,295,295,296,296,297,86,86,304,295,295,298,300,295,295,296,296,295,295,294,294,287,287,86,81,86,86,86,86,297,297,302,302,302,302,301,302,302,302,302,295,295,164};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,3,1,0,0,0,0,0,0,3,0,0,0,0,0,1,1,1,1,1,1,0,3,3,3,1,0,0,0,0,0,0,1,1,1,1,1,3,3,3,3,3,3,1,0,0,0,0,0,0,0,0,0,0,3,3,3,3,3,3,1,1,0,0,0,0,4,0,0,0,0,2,2,-1};
				break;
			}
			case 32:
			{
				moblist=new int[]{386,386,386,252,408,409,-1,385,407,407,-1,387,409,-1,407,-1,-1,384,252,252,387,387,387,252,407,407,252,386,386,386,409,385,-1,-1,-1,385,407,407,409,409,387,387,-1,407,383,-1,-1,407,407,383,-1,385,-1,383,-1,252,407,407,408,408,-1,386,386,-1,-1,387,384,384,-1,407,408,409,83,82,387,252,383,386,407,385,-1,252,252,387,387,386,-1,407,252,385,407,407,407,-1,409,-1,-1,388,409,408,383,-1,387,387,386,386,386,386,386,82,82,252,407,407,-1,407,407,407,-1,-1,-1};
				mobtype=new int[]{1,1,1,0,1,1,0,1,1,1,0,1,1,0,1,0,0,1,0,0,1,1,1,0,1,1,0,1,1,1,1,1,0,0,0,1,1,1,1,1,0,0,0,1,1,0,0,0,0,1,0,1,0,1,0,0,0,0,0,0,0,1,1,0,0,0,1,1,0,1,1,1,0,0,0,0,0,1,1,1,0,0,0,1,1,1,0,0,0,0,0,0,1,0,1,0,0,1,0,0,0,0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
				break;
			}
			case 33:
			{
				moblist=new int[]{389,389,390,390,390,389,397,397,392,392,390,389,390,391,392,392,-1,-1,-1,392,392,-1,396,392,395,395,-1,397,397,82,396,-1,396,-1,-1,392,392,392,390,397,397,392,392,295,394,389,389,-1,295,392,392,396,-1,82,82,392,392,392,392,392,392,392,392,396,396,397,389,389,396,390,391,-1,396,-1,390,390,390,82,-1,392,392,392,392,392,392,392,392,392,392,392,392,392,390,390,396,397,392,392,392,392,393,392,392,389,389,390,390};
				mobtype=new int[]{1,1,1,1,1,1,1,1,0,0,1,1,1,1,0,0,0,0,0,0,0,0,1,0,1,1,1,1,1,0,1,0,1,-1,-1,0,0,0,1,1,1,0,0,1,1,1,1,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,0,1,-1,1,1,1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,2,0};
				break;
			}
			case 34:
			{
				moblist=new int[]{399,399,399,399,403,403,403,-1,403,403,401,-1,-1,402,402,401,-1,399,403,403,403,399,400,400,-1,399,399,399,399,402,402,399,403,401,399,403,-1,406,-1,399,399,399,399,403,403,406,401,403,403,403,-1,401,403,400,400,-1,-1,399,399,399,402,401,403,401,401,403,403,401,400,400,404,402,399,399,402,-1,402,402,399,399,401,402,-1,401,399,399,403,403,404,406,-1,403,-1,402,401,403,401,399,399,403,401,-1,-1,404,403,405,402,82,82,403,401,399};
				mobtype=new int[]{1,1,1,1,1,1,1,0,1,1,1,0,0,1,1,1,0,1,1,1,1,1,0,0,0,1,1,1,1,1,1,1,1,1,1,1,0,1,0,1,1,1,1,1,1,1,1,1,1,1,0,1,1,0,0,0,0,-1,1,1,1,1,1,1,1,1,1,1,0,0,1,1,1,1,0,0,1,2,2,1,1,1,0,1,1,0,0,0,0,0,0,1,0,0,0,0,0,0,2,0,0,0,0,0,0,4,0,0,0,2,0,0};
				break;
			}
			case 35:
			{
				moblist=new int[]{310,311,310,310,310,311,-1,310,310,311,310,310,310,310,315,310,315,310,310,86,86,86,311,319,319,319,319,287,287,311,311,287,287,314,319,312,304,316,314,314,311,311,287,287,312,312,314,314,314,312,314,311,312,311,312,314,312,312,314,319,86,315,315,312,86,314,314,312,312,312,319,319,320,320,320,315,312,312,319,311,311,311,319,317,312,312,311,312,304,312,312,315,312,312,312,312,312,312,312,311,311,287,287,314,81,320,315,310,86,314,314,319,319,319,319,318,319,319,319,319,312,312,164,164};
				mobtype=new int[]{1,1,0,0,1,1,1,1,1,0,0,1,1,1,1,1,1,1,1,3,3,3,0,0,1,1,1,1,1,1,1,1,1,0,1,0,0,0,0,0,0,0,0,0,1,1,0,0,0,1,1,1,1,0,0,0,1,0,0,0,3,1,1,1,3,0,0,1,1,0,0,0,0,0,0,0,1,0,0,-1,-1,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,0,3,1,0,0,3,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0};
				break;
			}
			case 36:
			{
				moblist=new int[]{-1,-1,333,-1,335,333,325,-1,-1,-1,333,-1,333,332,327,325,333,-1,335,335,334,-1,335,328,-1,-1,335,326,325,86,86,333,333,336,336,-1,326,327,-1,-1,325,-1,325,332,-1,-1,-1,333,333,333,-1,-1,328,336,336,-1,-1,335,325,332,86,86,334,335,335,327,327,-1,328,325,-1,-1,333,333,335,335,336,329,335,335,335,-1,334,332,333,-1,-1,336,327,325,337,-1,335,329,334,325,325,86,332,333,333,337,337,335,-1,333,333,333,327,325,325,337,337,337,336,337,337,337,337,337,333,333,331,327,327,-1};
				mobtype=new int[]{0,1,1,1,1,1,1,1,0,0,1,0,0,0,0,0,1,1,1,1,1,1,0,0,0,1,1,1,1,3,3,0,0,0,0,1,1,1,0,0,1,1,1,0,0,0,1,1,1,1,0,1,1,1,0,0,0,0,0,0,3,3,0,0,0,0,0,0,1,1,0,0,0,0,0,0,0,1,0,-1,1,0,1,0,1,1,1,1,0,1,0,1,2,1,2,0,0,3,0,0,0,0,0,1,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0};
				break;
			}
			case 37:
			{
				moblist=new int[]{341,350,351,-1,-1,350,351,-1,-1,-1,-1,-1,352,-1,-1,-1,350,-1,350,351,349,343,-1,-1,351,347,350,351,-1,350,351,-1,341,-1,352,347,350,-1,349,-1,341,350,351,347,347,347,352,352,347,348,-1,348,348,348,-1,-1,348,347,347,350,351,-1,-1,-1,352,-1,-1,-1,-1,-1,350,-1,343,347,-1,351,86,352,347,86,342,342,-1,-1,-1,-1,352,351,341,343,350,86,352,352,-1,-1,-1,348,-1,348,-1,348,344,344,347,342,342,341,352,-1,-1,348,-1,348,350,351,341,347,86,86,347,344,350,340,351,347,349};
				mobtype=new int[]{1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,1,1,1,1,1,0,0,1,1,0,1,1,0,0,1,0,1,1,0,1,1,1,0,1,1,1,1,0,0,0,0,0,0,1,1,0,1,1,1,0,0,0,1,1,1,1,0,0,0,0,0,0,0,0,1,0,1,1,0,1,3,0,0,3,0,0,0,1,1,1,1,1,1,1,2,3,0,0,0,0,0,2,2,2,2,0,1,1,0,0,0,1,0,0,0,2,0,2,1,1,1,0,3,3,0,0,0,4,0,0,2};
				break;
			}
			case 38:
			{
				moblist=new int[]{359,359,-1,360,360,-1,-1,360,359,-1,359,-1,360,359,-1,-1,-1,-1,-1,358,-1,358,-1,358,358,86,-1,-1,-1,360,360,360,356,359,-1,-1,360,360,-1,-1,-1,359,360,360,359,359,360,360,-1,-1,-1,-1,-1,-1,-1,359,-1,359,358,-1,358,-1,358,359,-1,-1,-1,-1,360,360,360,86,-1,-1,-1,-1,360,360,-1,356,360,357,360,356,359,359,360,360,360,360,-1,-1,-1,-1,-1,-1,-1,360,360,360,360,359,359,86,-1,358,360,-1,86,360,356,360,-1,-1,360,360,359,356,359,-1,-1,355,357,360,360,359,360};
				mobtype=new int[]{1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,1,1,1,1,1,1,3,0,0,0,1,1,1,1,1,0,1,1,1,0,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,0,1,1,1,1,1,1,1,1,0,0,0,0,0,1,1,1,3,0,0,0,0,1,1,1,1,1,1,1,1,1,0,0,0,3,0,0,0,0,0,0,0,0,1,1,1,1,2,0,3,0,0,1,0,3,2,1,0,0,0,1,1,1,1,0,0,0,4,2,2,0,0,0};
				break;
			}
			//event start
			/*case 39:
			{
				moblist=new int[]{413,-1,-1,-1,-1,410,410,410,410,410,410,-1,415,415,412,414,415,415,415,415,415,415,415,415,414,86,414,-1,411,411,415,-1,-1,415,415,415,415,415,415,411,411,414,415,415,414,415,415,-1,-1,412,-1,-1,415,415,410,410,411,411,413,414,415,-1,-1,-1,-1,410,411,412,412,415,416,86,414,410,411,412,415,415,417,86,414,413};
				mobtype=new int[]{1,-1,-1,-1,0,1,1,1,1,0,0,0,0,0,1,1,2,2,1,1,1,1,1,1,0,3,0,0,1,1,0,0,-1,0,0,0,0,0,0,1,1,1,1,1,1,2,2,-1,-1,1,0,0,0,0,1,1,1,1,1,0,0,0,0,-1,-1,0,0,2,2,2,4,3,0,0,0,0,0,0,0,3,0,-1};
				break;
			}
			case 40:
			{
				moblist=new int[]{421,-1,-1,-1,-1,418,418,418,418,418,418,-1,423,423,420,422,423,423,423,423,423,423,423,423,422,86,422,-1,419,419,423,-1,-1,423,423,423,423,423,423,419,419,422,423,423,422,423,423,-1,-1,420,-1,-1,423,423,418,418,419,419,421,422,423,-1,-1,-1,-1,418,419,420,420,423,424,86,422,418,419,420,423,423,425,86,422,421};
				mobtype=new int[]{1,-1,-1,-1,0,1,1,1,1,0,0,0,0,0,1,1,2,2,1,1,1,1,1,1,0,3,0,0,1,1,0,0,-1,0,0,0,0,0,0,1,1,1,1,1,1,2,2,-1,-1,1,0,0,0,0,1,1,1,1,1,0,0,0,0,-1,-1,0,0,2,2,2,4,3,0,0,0,0,0,0,0,3,0,-1};
				break;
			}
			case 41:
			{
				moblist=new int[]{429,-1,-1,-1,-1,426,426,426,426,426,426,-1,431,431,428,430,431,431,431,431,431,431,431,431,430,86,430,-1,427,427,431,-1,-1,431,431,431,431,431,431,427,427,430,431,431,430,431,431,-1,-1,428,-1,-1,431,431,426,426,427,427,429,430,431,-1,-1,-1,-1,426,427,428,428,431,432,86,430,426,427,428,431,431,433,86,430,429};
				mobtype=new int[]{1,-1,-1,-1,0,1,1,1,1,0,0,0,0,0,1,1,2,2,1,1,1,1,1,1,0,3,0,0,1,1,0,0,-1,0,0,0,0,0,0,1,1,1,1,1,1,2,2,-1,-1,1,0,0,0,0,1,1,1,1,1,0,0,0,0,-1,-1,0,0,2,2,2,4,3,0,0,0,0,0,0,0,3,0,-1};
				break;
			}*/
			/*case 39:
			{
				moblist=new int[]{364,364,302,361,-1,-1,364,365,-1,367,383,364,-1,383,366,-1,364,364,383,367,361,367,302,-1,367,366,-1,383,364,364,361,366,365,82,82,365,-1,-1,364,361,383,366,-1,-1,367,383,364,364,366,367,362,-1,82,-1,-1,-1};
				mobtype=new int[]{1,1,0,1,0,1,1,1,1,1,1,1,0,1,1,0,1,1,1,1,1,0,0,0,1,1,0,1,1,1,1,1,2,3,3,0,0,1,1,1,1,1,0,0,0,0,2,0,2,0,4,-1,3,-1,-1,-1};
				break;
			}
			case 40:
			{
				moblist=new int[]{368,368,370,302,-1,-1,-1,368,368,370,-1,-1,-1,-1,368,371,-1,-1,374,368,369,374,-1,-1,371,-1,374,302,371,302,374,-1,-1,302,302,-1,368,-1,368,-1,-1,369,-1,373,368,368,371,-1,302,-1,-1,-1,-1,-1,369,373,371,82,82,371,-1,-1,302,374,302,-1,374,374,-1,371,368,368,82,368,369,370,372,373,370,371,368,369,370,302,-1};
				mobtype=new int[]{1,1,1,0,1,0,-1,1,1,1,0,0,-1,0,1,1,0,0,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,1,1,1,1,0,1,1,1,1,1,1,-1,0,1,0,1,0,1,1,1,1,3,3,0,0,1,0,0,0,1,1,0,0,0,0,0,3,0,0,0,4,2,2,2,0,0,0,0,-1};
				break;
			}
			case 41:
			{
				moblist=new int[]{381,379,379,381,379,381,-1,-1,375,376,377,302,302,376,-1,376,-1,-1,-1,376,-1,375,302,381,379,379,381,381,82,381,377,380,-1,377,302,380,-1,-1,379,-1,379,380,302,381,302,302,376,-1,375,376,379,-1,380,380,375,-1,-1,-1,-1,-1,-1,375,376,379,380,379,379,-1,-1,302,381,302,381,375,380,-1,-1,380,302,302,379,376,-1,-1,379,-1,379,-1,378,382,381,381,381,382,382,82,82,380,-1,380,380,-1,302,302,302,-1,-1,-1};
				mobtype=new int[]{1,1,1,1,1,1,0,1,1,1,1,0,0,1,0,1,0,0,0,1,0,1,0,1,1,1,1,1,3,0,1,1,0,1,0,1,0,0,1,0,1,1,0,0,0,0,1,0,1,1,1,0,1,1,1,0,0,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,1,1,0,0,1,0,0,1,1,0,0,1,0,1,0,4,2,2,0,0,2,2,3,3,0,0,0,0,0,0,0,0,0,0,0};
				break;
			}*/
			//event end
		}
		int[][] returnvalues=new int[2][];
		returnvalues[0]=moblist;
		returnvalues[1]=mobtype;
		return returnvalues;
	}
}

package botsserver;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Room {
	protected BotClass[] bot = new BotClass[8];
	protected int[] port = new int[8];
	protected boolean[] closedslot = new boolean[8];
	protected String roomname = "";
	protected String password = "";
	protected int roommode=0;//2 sector //0 pvp //1 teampvp //3 bvb
	protected int status=0;
	protected int[] map={0,0};
	protected int roomowner=0;
	protected int[] color = new int[8];
	protected int roomlevel=1;
	protected int roomnum=0;
	protected long starttime=0;
	protected int[] roomposition = new int[8];
	protected boolean[][] roomready = new boolean[8][2];
	protected int[] statmod = {1, 1, 1, 1, 1};
	protected int[][] ips = new int[8][4];
	protected int[] deadSp = {4,4,4,4,4,4,4,4};
	protected boolean[] dead = new boolean[8];
	protected int[] MobKill = new int[10]; //8 pushed 9 total
	protected int[] drops = new int[100];
	protected int[] MapValues = {};//total mobs, difficulty, boss, elite, droplevel, time, minmobs
	protected int RuleBonus = 1;
	protected int[] PlayerKill = {0,0,0,0,0,0,0,0};
	protected long[] rbuse = new long[8];
	protected boolean event = false;
	protected int[][] Moblist = new int[2][]; //1 id, 2 type
	protected boolean sectorclear = false;
	//protected int[][] rspawn = null;
	protected int[] Mobkilled = {};
	protected int moblistMap = -1;
	protected Packet packet = new Packet();
	protected ScheduledFuture<?> clearstage = null;
	protected ScheduledFuture<?> timeover = null;
	protected ScheduledFuture<?> PvpDrop = null;
	
	public Room(BotClass bot, String roomname, String password, int mode, String ip, int roomnum)
	{
		this.bot[0]=bot;
		this.roomname=roomname;
		this.password=password;
		this.roommode=mode;
		this.roomlevel=bot.level;
		this.roomposition[0] = 0x70;
		this.roomowner=0;
        this.roomready[0] = new boolean[]{true, false};
        this.roomnum = roomnum;
        this.color[0]= roommode==1 || roommode==3 ? 1 : 0;
        String[] rip = ip.split("\\.");
        for (int i = 0; i < 4; i++)
            this.ips[0][i] = (byte) Integer.parseInt(rip[i]);
        if (roommode==3)
        	bot.autocheckrun=false;
        RoomPacket();
        statusPacket(0);
        bot.lobby.sendAll(bot.PlayerAddPacket(0), bot.lobbynum, false);
        bot.packet.clean();
	}
	
	public void debug(String msg)
	{
		Main.debug("RoomThread: "+msg);
	}
	
	public void RoomPacket()
	{
        packet.addHeader((byte) 0xEE, (byte) 0x2E);
        packet.addByte2((byte) 0x01, (byte) 0x00);
        packet.addInt(roomnum+1, 2, false);
        packet.addByte4((byte) this.ips[0][0], (byte) this.ips[0][1], (byte) this.ips[0][2], (byte) this.ips[0][3]);
        bot[roomowner].send(packet,false);
	}
	
	public void SendMessage(boolean all, int num, String message, int color)
	{
		packet.addHeader((byte) 0x1A, (byte) 0x27);
        packet.addByte2((byte) 0x01, (byte) 0x00);
        packet.addInt(message.length(), 2, false);
        packet.addInt(color, 2, false);
        packet.addString(message);
        packet.addByte((byte)0x00);
        if(all)
    		for (int i = 0; i<8; i++)
    			if(bot[i]!=null)
    				bot[i].send(packet,true);
        if(!all)
        	bot[num].send(packet,true);
        packet.clean();
	}
	
	public void ChangePass(String password, int num)
	{
		if (this.roomowner!=num)
			return;
		this.password=password;
		bot[roomowner].PopUp(false, "Room password succesfully changed to "+this.password +"!");
	}
	
	public void setMap(int map, int num)
	{
		if (this.roomowner!=num)
			return;
		if (this.roommode!=2){
    		if(map==0)
    			this.map[0] = (int)(Math.random()*5)+1;
    		else
    			this.map[0] = map-1;}
    	else
    		this.map[0] = map;
    	this.map[1]=map;
    	MapPacket();
    	if (this.roommode==2)
    		refreshSectorMoblist(this.map[1]);
    	//if (this.roommode==2)
    	//	event(1);
	}
	
	public void event(int typ)
	{
		if (typ==1) {
			if((this.map[0]==39 || this.map[0]==40 || this.map[0]==41) && this.roommode==2)
	        {
	        	int diff = this.map[0]==39 ? 1 : this.map[0]==40 ? 50 : this.map[0]==41 ? 100 : 200;
	        	if (!event){
		            SendMessage(true, 0, "!!To be able to receive candy your level must be within 25 levels difference up or down!!", 2);
		            SendMessage(true, 0, "Trade candy for rewards on the event page of our website.", 1);
	        	}
	        	for (int i =0; i<8; i++)
	        		if(bot[i]!=null)
	        			if(diff+25<bot[i].level || diff-25>bot[i].level) {
	        	            SendMessage(true, 0, bot[i].botname+" will not be able to get candy from this map.",1);
	        				break;
	        			}
	        	this.event=true;
	        }
			else {
				this.event=false;
			}
		}
	}
	
	public void SlotStatus(int num, int task, int slot)
	{
		if (task==0 && num==roomowner)
			closedslot[slot] = !closedslot[slot];
		if (task==1)
			roomready[num][0] = !roomready[num][0];
		if (task==2)
			color[num] = (color[num]==1 ? 2 : 1);
		statusPacket(task==0 ? slot : num);
	}
	
	public void statusPacket(int slot)
	{
		packet.addHeader((byte) 0x20, (byte) 0x2F);
		packet.addInt(slot, 2, false);
		packet.addByte4((byte)0x00, (byte) 0x00, (byte) (closedslot[slot] ? 1 : 0), (byte) 0x00);
		packet.addByte4((byte) (roomready[slot][0] ? 1 : 0), (byte) 0x00, (byte) color[slot], (byte) 0x00);
		for (int i = 0; i<8; i++)
			if(bot[i]!=null)
				bot[i].send(packet,true);
		packet.clean();
	}
	
	public void MapPacket()
	{
        packet.addHeader((byte) 0x4A, (byte) 0x2F);
        packet.addByte2((byte) 0x01, (byte) 0x00);
        packet.addInt(this.map[1], 2, false);
		for (int i = 0; i<8; i++)
			if(bot[i]!=null)
				bot[i].send(packet,true);
		packet.clean();
	}

	private void refreshSectorMoblist(int mapId)
	{
		this.map[0]=mapId;
		MapValues=bot[roomowner].lobby.standard.mapvalues[mapId];
		//prep
		int[][] mobtemp=bot[roomowner].lobby.standard.moblist(mapId);
		Map<Integer, Integer> rspawn=bot[roomowner].lobby.standard.rebirthspawn;
		int[] mobwork=mobtemp[0].clone();
		for (int i = 0; i<mobwork.length; i++)
			if(rspawn.containsKey(mobwork[i]))
				mobwork[i] = rspawn.get(mobwork[i]);
		//prep end
		sectorclear=false;
		Moblist[0]=mobwork;
		Moblist[1]=mobtemp[1];
		Mobkilled=Moblist[0].clone();
		moblistMap=mapId;
	}

	private void ensureSectorMoblist()
	{
		int mapId=this.map[1]!=0 ? this.map[1] : this.map[0];
		if (moblistMap!=mapId)
			refreshSectorMoblist(mapId);
	}
	
	public int GhostRoomCheck()
	{
    	int empty = 0;
    	int temp = 0;
    	for (int i = 0; i<8; i++)
    		if (this.bot[i]==null || (temp=this.bot[i].getNum(this.bot[i].botname))==-1 || this.bot[i].lobbynum!=temp){
    			empty++;
    			this.bot[i]=null;
    		}
    	return empty;
	}
	
    public int Join(BotClass bot, String password, String ip)
    {
    	int num = -1;
    	int error = 0;
    	int empty = GhostRoomCheck();
    	for (int i = 0; i<8; i++)
    		if (this.bot[i]==null && !this.closedslot[i]){
    			num = i;
    			break;
    		}
    	if (!this.password.equals(password))
    		error=0x3E;
    	if (this.status == 3)
    		error=0x3B;
    	if (num==-1)
    		error=0x51;
    	if (empty==8){
    		error=0x51;
    		bot.RemoveRoom(this);
    	}
    	if (error != 0){
    		packet.addHeader((byte) 0x28, (byte) 0x2F);
    		packet.addByte2((byte) 0x00, (byte) error);
    		bot.send(packet,false);
    		return -1;
    	}
    	else{
    		String[] rip = ip.split("\\.");
            for (int i = 0; i < 4; i++)
                this.ips[num][i] = (byte) Integer.parseInt(rip[i]);
            this.roomready[num] = new boolean[]{false, false};
            this.roomposition[num]=0x50;
            this.bot[num]=bot;
            this.color[num] = (roommode==1 || roommode==3) ? 1 : 0;
            bot.roomnum=num;
    		bot.room=this;
    		bot.lobbyroomnum=roomnum;
    		bot.lobby.sendAll(bot.PlayerAddPacket(0), bot.lobbynum, false);
    		bot.packet.clean();
    		if (roommode==3)
    			bot.autocheckrun=false;
            packet.addHeader((byte) 0x29, (byte) 0x27);
            packet.addByteArray(generateUserPack(num));
            for (int i = 0; i<8; i++)
    			if(this.bot[i]!=null)
    				this.bot[i].send(packet,true);
            packet.clean();
            UserInfoPacket(-1);
            for (int i = 0; i<8; i++)
            	if (this.bot[i]!=null)
            		statusPacket(i);
            MapPacket();
    	}
    	return num;
    }
    
    public boolean Exit(int num, boolean kick)
    {
    	for (int i = 0; i < 4; i++)
            this.ips[num][i] = (byte) 0;
    	if (status==3)
    	{
    		if(!roomready[num][1])
    			for (int i = 0; i<8; i++)
    				if (roomowner!=i && bot[i]!=null)
    					readyToPlay(num);
    		if (roommode==3)
    			deadSp[num]=1;
            Dead(num, 10);
    	}
        this.roomready[num] = new boolean[]{false, false};
        this.roomposition[num]=0;
        if (roomowner == num)
        	for (int i = 0; i<8; i++)
        		if(bot[i]!=null && num!=i){
        			roomowner=i;
        			roomposition[i]=0x70;
        			this.roomlevel=bot[i].level;
        			roomready[i][0]=true;
        			break;
        		}
        /*obsolete double check for id removal from relay.
         * int[] roomrnum=Main.getRelayStore().roomids.clone();
        for (int i = 0; i<roomrnum.length; i++)
        	try {
	        	if (roomrnum[i]==roomrnum[bot[num].relaynum])
	        		Main.getRelayStore().removeByID(i, bot[num].relaynum);
        	}catch (Exception e){}*/
        Main.getRelayStore().roomids[bot[num].relaynum]=-1;
        bot[num].roomnum=0;
        bot[num].lobbyroomnum = -1;
        bot[num].autocheckrun=true;
        try {
	        ExitPacket(num, kick);
	        bot[num].lobby.sendAll(bot[num].PlayerAddPacket(1), bot[num].lobbynum, false);
	        bot[num].packet.clean();
        }catch (Exception e){}
        this.bot[num]=null;
        return roomowner==num;
    }
    
    public void PreDead(int num)
    {
    	packet.addHeader((byte)0x54, (byte)0x2F);
    	packet.addByte4((byte)0x01, (byte)0x00, (byte)num, (byte)0x00);
    	for (int i = 0; i<8; i++)
        	if (bot[i]!=null)
        		bot[i].send(packet,true);
    	packet.clean();
    }
    
    public void DeadPacket(int id, int head, int typ, boolean player)
    {
    	try{
    	int dir = (int)(Math.random()*6);
    	int tdrop = 0;
        boolean[] spdrop = {false, false};
        int[][] spmobs;
        int[] drop = new int[tdrop];
        if ((this.roommode==3 || this.roommode<=1) && TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()-starttime)>10){
        	drop = new int[tdrop=(int)(this.roommode==3 ? Math.random()*3 : (this.roommode<=1 ? (Math.random()*5) : 0))];
        	for (int i = 0; i<tdrop; i++)
        		drop[i] = this.roommode==3 ? ((int)(Math.random()*3)==0 ? 5 : 10) : ((int)(Math.random()*3)+1);
        }
        //event start
		if(event)
    	{
    		if (typ == 82 || typ==86){
    			tdrop=(int)(Math.random()*10)==1 ? 1 : 0;
    			spdrop[1]=false;
    		}
    		if(typ==MapValues[2]){
    			int playercount=0;
    			for (int i = 0; i<8; i++)
    				if (bot[i]!=null)
    					playercount++;
    			int eventdrop=(int)(Math.random()*100);//set 1 to gaurentee event drop
    			if (eventdrop>=30 && eventdrop<=(65+(2*playercount>=15 ? 15 : 2*playercount))){
    				tdrop=2;
    			}
    			else{
    				tdrop=1;
    			}
    		}
    		drop = new int[tdrop];
    	}
		//event end
		if (roommode==2 && (spmobs=bot[roomowner].lobby.standard.SpMobs)!=null && !player && !event){
			tdrop=((tdrop = (int)(Math.random()*7))<3 ? (MapValues[2]==typ ? tdrop+1 : tdrop) : (MapValues[2]==typ ? 1 : 0));
			for (int i = 0; i<spmobs[0].length; i++)
        		if(spdrop[0]=(typ==spmobs[0][i]))
        			break;
        	for (int i = 0; i<spmobs[1].length; i++)
        		if(spdrop[1]=(typ==spmobs[1][i]) || (spdrop[1]=(MapValues[2]==typ)))
        			break;
        	if(!spdrop[0] && !spdrop[1] && MapValues[2]!=typ)
        		tdrop=(int)(Math.random()*10)==1 ? 1 : 0;
        	if(spdrop[0])
        		tdrop+=(int)(Math.random()*3);
        	if (this.Mobkilled[id]==-1) {
        		String [] value = {""+MapValues[1], ""+id+" removing drop", "already killed",};
            	Main.sql.psupdate("INSERT INTO `bout_mob_log` (`level`, `mobinfo`, `error`, `date`)VALUES (?, ?, ?, now())", value);
        		tdrop=0;
        	}
        	drop = new int[tdrop];
		}
        for (int i = 0; i<tdrop; i++)
        	if (roommode==2 && MapValues[2]==typ && i == 0)
        		drop[i]=6;
        	else if (roommode==2)
        		drop[i]=bot[roomowner].lobby.standard.droplist[(int)(Math.random()*(spdrop[1] ? 106 : 40))+(spdrop[1] ? 40 : 0)];
        //event override
        if (roommode==2 && event && MapValues[2]==typ && tdrop>1)
        	drop[1]=39;
    	packet.addHeader((byte)head, (byte)0x2F);
        packet.addPacketHead((byte)0x01, (byte)0x00);
        packet.addInt(id, 2, false);
        packet.addByte2((byte)0x00, (byte)0x00);
        packet.addByte2((byte)(dir+tdrop), (byte) 0x00);
        for (int i = 0; i<dir; i++)
    		packet.addByte2((byte)0x00, (byte)0x00);
        for (int i = 0; i<tdrop; i++){
        	packet.addByte2((byte)drops[0], (byte)drop[i]);
        	drops[1+drops[0]]=drop[i];
        	drops[0]++;
        }
        for (int i = 0; i<8; i++)
        	if (bot[i]!=null)
        		bot[i].send(packet,true);
        packet.clean();
    	}catch (Exception e){debug("error: "+ e);}
    }
    
    public void Dead(int id, int killer)
    {
    	DeadPacket(id, 0x22, 0, true);
    	if (dead[id])
    		return;
    	dead[id]=true;
    	int[] alive = new int[3];
    	if (0<=killer && 8>killer)
    		PlayerKill[killer]++;
    	if (dead[id] && roommode==3)
    		deadSp[id]--;
    	if(roommode==3 && deadSp[id]>=0)
    		dead[id]=false;
    	for (int i = 0; i<8; i++)
    		if (bot[i]!=null && roommode==3 && deadSp[i]>=0)
    			return;
    		else if (bot[i]!=null && roommode==2 && !dead[i])
    			return;
    		else if (bot[i]!=null && roommode<=1 && !dead[i])
    			alive[roommode==1 ? (color[i]==1 ? 0 : 1) : 2]++;
    	if ((roommode==1 && alive[0]>0 && alive[1]>0) || (alive[2]>1 && roommode==0))
    		return;
    	if (roommode==2){
    		EndRoom(new int[]{0,0,0,0,0,0,0,0},new int[]{0,0,0,0,0,0,0,0},0,new int[]{0,0,0,0,0,0,0,0}, false);
    		return;
    	}
    	int time = (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.starttime);
    	int[] exp = new int[8];
    	int[] winner = new int[8];
    	int[] gigas = new int[8];
    	for (int i = 0; i<8; i++){
    		exp[i] = time<60 ? time*PlayerKill[i]+(time>30 ? 13 : 0) : 60*PlayerKill[i];
    		winner[i] = roommode==1 ? (alive[(color[i]==0 ? 1 : color[i])-1]==0 ? 0 : 1) : (dead[i] ? 0 : 1);
    		gigas[i] = exp[i]*13+100;
    	}
    	EndRoom(exp, gigas, 0, winner, false);
    }
    
    public void MobKill(int typ, int num, int killedby, int pushed, int id)
    {
    	try {
    		if (roommode==2) {
    			ensureSectorMoblist();
		    	if (this.Moblist[0][num]==typ) {
		    		if (this.Mobkilled[num]==-1 && this.Moblist[1][num]!=2) {
		    			String [] value = {""+MapValues[1], ""+num+": supected "+this.Moblist[0][num]+" - actual "+typ, "already killed",};
		            	Main.sql.psupdate("INSERT INTO `bout_mob_log` (`level`, `mobinfo`, `error`, `date`)VALUES (?, ?, ?, now())", value);
		    		}
		    	}
		    	else {
		    		String [] value = {""+MapValues[1], ""+num+": supected "+this.Moblist[0][num]+" - actual "+typ, "Incorrect ID",};
	            	Main.sql.psupdate("INSERT INTO `bout_mob_log` (`level`, `mobinfo`, `error`, `date`)VALUES (?, ?, ?, now())", value);
	            	hackdetected(id, "[AutoBan]: Wrong mob id received!");
	            	return;
		    	}
    		}
    	}catch (Exception e){debug(e.getMessage());}
    	if (0<=killedby && killedby<8)
        	this.MobKill[killedby]++;
    	if (pushed==1)
    		this.MobKill[8]++;
    	if (this.Mobkilled[num]!=-1 || this.Moblist[1][num]==2)
    		this.MobKill[9]++;
    	DeadPacket(num, 0x25, typ, false);
    	try {
	    	if (roommode==2)
	    		this.Mobkilled[num]=-1;
    	}catch (Exception e){debug(e.getMessage());}
    	if (roommode==3 && num>15){
    		int time = (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.starttime);
        	int[] exp = new int[8];
        	int[] winner = new int[8];
        	int[] gigas = new int[8];
        	for (int i = 0; i<8; i++){
        		if (bot[i]!=null){
	        		exp[i] = time<60 ? time*PlayerKill[i]+(time>30 ? 13 : 0) : 60*PlayerKill[i];
	        		winner[i] = color[i]==(num==16 ? 1 : 2) ? 1 : 0;
	        		gigas[i] = exp[i]*13+100;
        		}
        	}
    		EndRoom(exp, gigas, 10, winner, false);
    	}
        if (MapValues[2]==typ){
        	if (sectorclear)
        		return;
        	sectorclear=true;
        	int[] expp = new int[8];
        	int[] winner = new int[8];
            Calendar c = Calendar.getInstance();
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
            double bonusweekend=0.5;
            if (dayOfWeek==1 || dayOfWeek==7)
            	bonusweekend=1;
        	double exp = (MapValues[0]/4*Math.sqrt(MapValues[1])*(1+MapValues[3])*bonusweekend*RuleBonus);
        	int gigas = (int)100*MapValues[1]*(1+MapValues[3]);
        	for (int i = 0; i<8; i++)
        		if (bot[i]!=null){
        			expp[i] += MapValues[1]<(bot[i].level-10) ? (int)exp*0.25 : (MapValues[1]<(bot[i].level-4) ? (int)exp*0.5 : exp);
        			winner[i]=1;
        		}
        	boolean triggerkill=true;
            for (int i = 0; i<Moblist.length; i++)
            	if (this.Moblist[1][num]==1 && this.Mobkilled[num]!=-1)
            		triggerkill=false;
            
            if (roommode==2 && !triggerkill) {
            	hackdetected(id, "[AutoBan]: Not all triggers were killed!");
            	return;
        	}
            
            /*if (roommode==2 && MapValues[6]>this.MobKill[9]) {
            	hackdetected(id, "[AutoBan]: MinmobCount not met!");
            	return;
        	}*/
        	EndRoom(expp, new int[]{gigas,gigas,gigas,gigas,gigas,gigas,gigas,gigas}, 10, winner, false);
        }
    }
    
    protected void hackdetected(int id, String reason) {
    	

    	for (int i = 0; i<8; i++)
    		if(bot[i]!=null){
    			
    			// Make sure that if an admin is in the room we don't ban anyone
//    			if (bot[i].) {
    				
//    			}
    			
    			packet.clean();
    			packet.addHeader((byte)0x2A, (byte)0x27);
    	        packet.addByte2((byte)0x00, (byte)0x00);
    			bot[i].send(packet,false);
    		}
    	SendMessage(true, 0, "hack attempt detected", 2);
    	String playerlist = "";
        for (int i = 0; i<8; i++)
        	if(bot[i]!=null)
        		try{
        			playerlist+=bot[i].botname+"["+bot[i].level+"], ";
        		}catch (Exception e){}
    	int time = (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.starttime);
    	String [] value = {""+MapValues[1], bot[roomowner].botname, playerlist, ""+time, ""+MobKill[9]};
    	Main.sql.psupdate("INSERT INTO `sector_log` (`level`, `roommaster`, `roomplayers`, `time`, `kills`, `date`)VALUES (?, ?, ?, ?, ?, now())", value);
    	value = new String[]{"1", "-1",reason, bot[id].account};
		Main.sql.psupdate("UPDATE `bout_users` SET `banned`=?, `bantime`=?, `banStime`=now(), `banreason`=? WHERE `username`=?", value);
		bot[id].channel.closecon();
    	clearstage=bot[roomowner].executorp.schedule(SectorClear(new int[]{0,0,0,0,0,0,0,0},new boolean[] {false,false,false,false,false,false,false,false},bot,1,1,MobKill,PlayerKill,new int[]{0,0,0,0,0,0,0,0},new int[]{0,0,0,0,0,0,0,0}), 5, TimeUnit.SECONDS);
    }
    
    public void EndRoom(int[] exp, int[] gigas, int coins, int[] winner, boolean timeover)
    {
        boolean[] leveledup = new boolean[8];
        int[] points = new int[8];
		packet.clean();
        for (int i = 0; i<8; i++)
    		if(bot[i]!=null){
    			packet.addHeader((byte)0x2A, (byte)0x27);
    	        packet.addByte2((byte)(timeover ? 2 : winner[i]), (byte)0x00);
    			bot[i].send(packet,false);
    		}
        int highestlvl=1;
        for (int i = 0; i<8; i++)
        	if(bot[i]!=null){
        		if(bot[i].level>highestlvl)
        			highestlvl=bot[i].level;
        	}
        double multiplier = this.roommode == 2 ? (MapValues[1]<(highestlvl-30) ? 0.7 : (MapValues[1]<(highestlvl-10) ? 1 : 3)) : 0;
        for (int i = 0; i<8; i++)
        	if(bot[i]!=null){
        		try{
		        	bot[i].exp += exp[i];
		        	bot[i].gigas += gigas[i];
		        	bot[i].coins += coins;
		        	points[i]=(int)(exp[i]*(this.roommode == 2 ? 0.50 : 0.75))+ (this.roommode == 2 ? (int)(this.MobKill[i]*multiplier) : 0);
		        	if(bot[i].exp>bot[i].lobby.standard.explevels[bot[i].level])
		        		for (int b = bot[i].level+1; b<=120; b++)
		        			if (bot[i].exp>bot[i].lobby.standard.explevels[b]){
		        				bot[i].level = b;
		        				leveledup[i]=true;
		        			}
		        	bot[i].UpdateBot();
		        	bot[i].UpdateCoins();
		        	BotClass[] bottemp = bot[roomowner].lobby.bots;
		        	if (bot[i].guildnum!=-1){
			        	bot[i].guildMemberpoints[bot[i].guildnum]+=points[i];
			        	bot[i].guildtotalpoints+=points[i];
			        	bot[i].UpdateGuild();
			    		for (int a = 0; a<600; a++)
			    			if (bottemp[a]!=null && bottemp[a].guildname.equals(bot[i].guildname))
			    				if(bottemp[a].guildmembers[bot[i].guildnum].equals(bot[i].botname) && !bottemp[a].botname.equals(bot[i].botname)) {
			    					bottemp[a].guildMemberpoints[bot[i].guildnum]+=points[i];
			    					bottemp[a].guildtotalpoints+=points[i];
			    				}
		        	}
        		}catch (Exception e){}
	        }
        String playerlist = "";
        for (int i = 0; i<8; i++)
        	if(bot[i]!=null)
        		try{
        			playerlist+=bot[i].botname+"["+bot[i].level+"], ";
        		}catch (Exception e){}
        if (roommode==2){
        	int time = (int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - this.starttime);
        	String [] value = {""+MapValues[1], bot[roomowner].botname, playerlist, ""+time, ""+MobKill[9]};
        	Main.sql.psupdate("INSERT INTO `sector_log` (`level`, `roommaster`, `roomplayers`, `time`, `kills`, `date`)VALUES (?, ?, ?, ?, ?, now())", value);
        }
        clearstage=bot[roomowner].executorp.schedule(SectorClear(winner,leveledup,bot,1,1,MobKill,PlayerKill,points,exp), 5, TimeUnit.SECONDS);
    }
    
    private static Runnable TimeOver(final Room room) {
    	return new Runnable() {
    		@Override
    		public void run() {
    			room.EndRoom(new int[]{0,0,0,0,0,0,0,0},new int[]{0,0,0,0,0,0,0,0},0,new int[]{0,0,0,0,0,0,0,0}, true);
    		}
    	};
    }
    
    private static Runnable SectorClear(final int[] winner, final boolean[] leveledup, final BotClass[] bot,final int statmod
    		, final int statmod2, final int[] Mobkills, final int[] Playerkills, final int[] points, final int[] exp) {
    	return new Runnable() {
    		@Override
    		public void run() {
    			Packet packet = null;
    			for (int i = 0; i<8; i++)
                	if (bot[i]!=null) {
                		packet = bot[i].packet;
                		break;
                	}
    			packet.clean();
                for (int i = 0; i<8; i++)
                	packet.addByte((byte)winner[i]);
                for (int i = 0; i<8; i++)
                	packet.addByte((byte)(leveledup[i] ? 1 : 0));
                for (int i = 0; i<8; i++)
                	packet.addByte((byte)(bot[i]!=null ? bot[i].level : 0));
                for (int i = 0; i<8; i++)
                	packet.addInt((int)(bot[i]!=null ? exp[i] : 0),2,false);
                for (int i = 0; i<8; i++)
                	packet.addInt((bot[i]!=null ? (int)((bot[i].hp+bot[i].hpb)*statmod2) : 0),2,false);
                for (int i = 0; i<8; i++)
                	packet.addInt(points[i], 2, false);
                for (int i = 0; i<4; i++)
                	packet.addInt(0, 4, false);
                for (int i = 0; i<8; i++)
                	packet.addInt(Playerkills[i],2,false);
                for (int i = 0; i<8; i++)
                	packet.addInt(Mobkills[i],2,false);
                int amount=0;
                for (int i = 0; i<8; i++)
                	amount+=bot[i]!=null ? 1 : 0;
                packet.addInt(amount>=2 ? 1 : 0, 2, false);
                packet.addInt(amount>=3 ? 1 : 0, 2, false);
                for (int i = 0; i<19; i++)
                	packet.addInt(0, 4, false);
                packet.addInt(0, 2, false);
                packet.addByte((byte)0x00);
                byte[] constant = packet.getPacket();
                packet.clean();
                for (int i = 0; i<8; i++){
                	if (bot[i]!=null){
		    			packet.addHeader((byte)0x1F, (byte)0x2F);
		                packet.addByte4((byte)0x01, (byte)0x00,(byte)0x00, (byte)0x00);
		                packet.addInt(bot[i].gigas,4,false);
		                packet.addInt(bot[i].level,2,false);
		                packet.addInt(bot[i].exp,4,false);
		                packet.addInt(bot[i].botstract,4,false);
		                packet.addByte4((byte)0x00, (byte)0x00,(byte)0x00, (byte)0x00);
						for (int a = 0; a<19; a++)
							if (bot[i].itemdurationsort[a]==3)
								bot[i].UpdateItemUses(1, a);
		                bot[i].TimeCalc();
		                packet.addInt(bot[i].itemdurationcur[17],4,false);
		                packet.addInt(bot[i].itemdurationcur[18],4,false);
		                packet.addInt(bot[i].itemdurationcur[11],4,false);
		                packet.addInt(bot[i].itemdurationcur[12],4,false);
		                packet.addInt(bot[i].itemdurationcur[13],4,false);
		                packet.addInt(bot[i].itemdurationcur[14],4,false);
		                packet.addInt(bot[i].itemdurationcur[15],4,false);
		                packet.addInt(bot[i].itemdurationcur[16],4,false);
		                packet.addInt(bot[i].itemdurationcur[3],4,false);
		                packet.addInt(bot[i].itemdurationcur[4],4,false);
		                packet.addInt(bot[i].itemdurationcur[5],4,false);
		                packet.addInt(bot[i].itemdurationcur[6],4,false);
		                packet.addInt(bot[i].itemdurationcur[7],4,false);
		                packet.addInt(bot[i].itemdurationcur[8],4,false);
		                packet.addInt(bot[i].itemdurationcur[9],4,false);
		                packet.addInt(bot[i].itemdurationcur[10],4,false);
		                packet.addInt((int)((bot[i].attmin+bot[i].attminb)*statmod), 2, false);
		                packet.addInt((int)((bot[i].attmax+bot[i].attmaxb)*statmod), 2, false);
		                packet.addInt((int)((bot[i].attmintrans+bot[i].attmintransb)*statmod), 2, false);
		                packet.addInt((int)((bot[i].attmaxtrans+bot[i].attmaxtransb)*statmod), 2, false);
		                packet.addByteArray(constant);
		                bot[i].send(packet,false);
                	}
                }
                try{
                	Thread.sleep(6000);
                }catch (Exception e){}
                packet.clean();
                packet.addHeader((byte)0x2A, (byte)0x2F);
        		packet.addByte((byte) 0x00);
        		for (int i = 0; i<8; i++)
                	if (bot[i]!=null)
                		bot[i].send(packet,true);
        		packet.clean();
        		for (int i = 0; i<8; i++)
                	if (bot[i]!=null){
                		bot[i].room.waitroom(false);
                		break;
                	}
    		}
    	};
    }
    
    private static Runnable PvpDrops(final Room room, final BotClass[] bot) {
    	return new Runnable() {
    		@Override
    		public void run() {
    			Packet packet=bot[room.roomowner].packet;
    			int[] drops = new int[10];
    			int drop = 0;
    			if (room.roommode==3)
    				for (int i = (int)(Math.random()*3+1); i>0; i--)
    					drops[(i-1)*3+(int)(Math.random()*3)]=((drop=(int)(Math.random()*1))==0 ? drop+5 : drop+6);
    			else
    				for (int i = (int)(Math.random()*3+1); i>0; i--)
        				drops[(i-1)*3+(int)(Math.random()*3)]=((drop=(int)(Math.random()*2))==0 ? drop+5 : drop+6);
    			packet.addHeader((byte)0x2B, (byte)0x27);
    			packet.addInt(drops.length, 2, false);
    			for (int i =0; i<drops.length; i++)
    			{
    				packet.addInt(i, 2, false);
    				packet.addByte2((byte)(drops[i]==0 ? 0 : room.drops[0]), (byte)drops[i]);
    				if (drops[i]!=0){
    					room.drops[1+room.drops[0]]=drops[i];
    					room.drops[0]++;
    				}
    			}
    			for (int i=0; i<8; i++)
    				if (bot[i]!=null){
                		bot[i].send(packet,true);
    				}
    			packet.clean();
    			room.PvpDropRerun();
    		}
    	};
    }
    
    protected void PvpDropRerun()
    {
    	try{
    		PvpDrop.cancel(true);
    	}catch (Exception e){}
    	PvpDrop=bot[roomowner].executorp.schedule(PvpDrops(this, bot), (int)(Math.random()*15+15), TimeUnit.SECONDS);
    }

    public void waitroom(boolean statupdate)
    {
    	stopAllSchedules();
	    this.status=0;
		this.MobKill=new int[]{0,0,0,0,0,0,0,0,0,0};
		this.dead = new boolean[]{false,false,false,false,false,false,false,false};
		this.PlayerKill = new int[]{0,0,0,0,0,0,0,0};
		this.deadSp=new int[]{4,4,4,4,4,4,4,4};
		this.drops=new int[100];
		bot[roomowner].RoomsPacket(true, new int[]{roommode==0 ? 0 : roommode-1, roommode==0 ? (int)(roomnum/6) : (int)((roomnum-600*(roommode-1))/6)});
		for (int i = 0; i<8; i++){
			if (i==roomowner)
				this.roomready[i]=new boolean[]{true, false};
			else
				this.roomready[i]=new boolean[]{false, false};
		}
    }
    
    public void stopAllSchedules()
    {
		if (status!=3)
			return;
    	try{
    		if (this.roommode==2)
        		timeover.cancel(true);
    		else
    			PvpDrop.cancel(true);
    		clearstage.cancel(true);
    	}catch (Exception e){}
    }
    
    public void PickupPacket(int slot, int typ, int num, int drop, int invslot)
    {
    	packet.addHeader((byte)0x23, (byte)0x2F);
        packet.addInt(slot, 2, false);
        packet.addByte((byte)num);
        packet.addByte((byte)typ);
        for (int i = 0; i<8; i++)
        	if (bot[i]!=null)
        		bot[i].send(packet,true);
        packet.clean();
        if (drop==0)
        	return;
        if(drop!=0){
        	packet.addHeader((byte)0x2C, (byte)0x2F);
        	if (invslot==-1)
        		packet.addByte2((byte)0x00, (byte)0x44);
        	else
        		packet.addByte2((byte)0x01, (byte)0x00);
        	packet.addInt(drop, 4, false);
        	packet.addInt(invslot, 2, false);
        	packet.addInt(invslot, 2, false);
        	packet.addInt(invslot, 2, false);
        	bot[slot].send(packet,false);
        }
    }
    
    public void DropPickup(int slot, int typ, int num)
    {
    	int drop = 0;
    	int invslot = -1;
    	if(drops[num+1]!=typ)
    		return;
    	drops[num+1]=0;
    	if (typ > 0 && typ <= 3){
    		bot[slot].botstract+=(typ==1 ? 5 : (typ==2 ? 15 : 30));
    		bot[slot].UpdateBot();
    	}
    	if(typ==6)
    		for (int i = 0; i<8; i++)
    			this.dead[i]=false;
    	if(typ==18 || typ==19 || typ==20)
			drop=1000000+bot[slot].bottype*100000+(typ==18 ? 3 : (typ==19 ? 1 : 2))*10000+MapValues[4]*100+(int)(Math.random()*3+1);
    	else if (typ>20 && typ!=38 && typ!=39)
    	{
    		int base=0;
    		String[] droptemp=bot[slot].lobby.standard.drops[typ>50 ? typ-45 : (typ>34 ? typ-32 : (typ>20 ? typ-21 : 13))];
    		for (int i = 0; i<droptemp.length; i++)
    			if(MapValues[1]>=Integer.parseInt(droptemp[i].split(" ")[1]) && MapValues[1]<=Integer.parseInt(droptemp[i].split(" ")[2]))
    				drop++;
    			else if (drop==0)
    				base++;
    		drop=Integer.parseInt(droptemp[((int)(drop*Math.random()))+base].split(" ")[0]);
    	}
    	else if (typ==38) {
    		int a=((int)(Math.random()*4)+1);
    		drop=a>4 ? 6000004 : 6000000+a;
    	}
    	//event box handler
    	else if (typ==39) {
    		drop=5041500;//((int)(Math.random()*2))==0 ? 4042507 : 5041502;
	    	if ((MapValues[1]-25)>=bot[slot].level || (MapValues[1]+25)<bot[slot].level) {
				drop=0;
				SendMessage(true, 0, "[Room] "+bot[slot].botname+" is unable to receive rewards on this difficulty.",1);
	    	}
	    	else {
		    	ResultSet rs = Main.sql.psquery("SELECT * FROM Event WHERE account=? LIMIT 1", new String[]{bot[slot].account});
	            try{
	            	if (rs.next()) {
	            		SendMessage(false, slot, "[Room] You received the reward and have a total of: "+(rs.getInt("christmas2020")+1)+" fireworks.",1);
	            		Main.sql.psupdate("UPDATE `Event` SET `name`=?, `christmas2020`=(`christmas2020`+1) WHERE `account`=?", new String[]{bot[slot].botname,bot[slot].account});
	            	}
	            	else {
	            		SendMessage(false, slot, "[Room] You received the reward and have a total of: 1 firework.",1);
	            		Main.sql.psupdate("INSERT INTO `Event` (`name`,`account`,`christmas2020`)VALUES (?,?,1)", new String[]{bot[slot].botname,bot[slot].account});
	            	}
	                rs.close();
	            }catch(Exception e){}
	    	}
    	}
    	//event box handler end
    	if(drop!=0)
    		for (int i=0; i<10; i++)
    			if(bot[slot].inventitems[i]==0){
    				invslot=i;
    				break;
    			}
    	if(invslot!=-1 && drop!=0 && !event){
    		bot[slot].inventitems[invslot]=drop;
    		int time=0;
    		ResultSet rs = Main.sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", new String[]{""+drop});
            try{
            	if (rs.next())
            		time = rs.getInt("days");
                rs.close();
            }catch(Exception e){}
            if(time!=0)
            	bot[slot].AddItemTime(drop, invslot, "item", time);
            bot[slot].UpdateInvent();
    	}
    	PickupPacket(slot, typ, num, drop, invslot);
    }
    
    public void EquipPackUse(int slot)
    {
    	boolean use = false;
    	if(bot[slot].equipitemspack[2]>=4030100 && bot[slot].equipitemspack[2]<=4030103 && 
    			TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()-rbuse[slot])>3){
    		packet.addHeader((byte)0x3A, (byte)0x27);
    		packet.addByte4((byte)slot, (byte)0x00,(byte)0x01, (byte)0x00);
    		for (int i = 0; i<8; i++)
            	if (this.bot[i]!=null)
            		bot[i].send(packet,true);
    		packet.clean();
        	for (int i = 0; i<8; i++)
    			this.dead[i]=false;
        	rbuse[slot]=System.currentTimeMillis();
        	use=true;
    	}
    	else
    		use=true;
    	packet.addHeader((byte)0x1D, (byte)0x2F);
    	packet.addInt(1, 2, false);
    	if (use)
    		bot[slot].UpdateItemUses(1, 13);
    	for (int i = 0; i<5; i++){
    		packet.addInt(bot[slot].equipitemspack[i], 4, false);
    		packet.addInt(bot[slot].itemdurationcur[11+i], 4, false);
    		packet.addByte((byte)bot[slot].itemdurationsort[11+i]);
    	}
    	bot[slot].send(packet,false);
    	packet.clean();
    }
    
    private void ExitPacket(int slot, boolean kicked)
    {
    	/**
    	 * 0x01 exited
    	 * 0x02 kicked
    	 * 0x03 system overload
    	 * 0x04 overlapping login
    	 * 0x05 disconnected from server
    	 * 0x06 forced to logout
    	 */
        packet.addHeader((byte) 0x2E, (byte) 0x27);
        if(kicked)
        	packet.addByte4((byte) 0x01, (byte) 0x00, (byte) slot, (byte) 0x02);
        else if(this.status==3)
        	packet.addByte4((byte) 0x01, (byte) 0x00, (byte) slot, (byte) 0x06);
        else
        	packet.addByte4((byte) 0x01, (byte) 0x00, (byte) slot, (byte) 0x01);
        for (int i = 0; i < 8; i++)
        	packet.addByte((byte)roomposition[i]);
        for (int i = 0; i<8; i++)
        	if(bot[i]!=null)
        		bot[i].send(packet,true);
        packet.clean();
    }
    
    public void Start()
    {
    	boolean ready = true;
    	boolean[] enuff = {false, false};
    	int error = 0;
    	for (int i = 0; i<8; i++)
    		if(bot[i]!=null)
    			if (!roomready[i][0])
    				ready=false;
    			else if(this.roommode==1 || this.roommode==3){
    				if(color[i]==1)
    					enuff[0]=true;
    				else if (color[i]==2)
    					enuff[1]=true;
    			}
    			else if(this.roommode==0){
    				error+=1;
    				if (error<=1)
    					ready = false;
    				else
    					ready = true;
    			}
    	error=0;
    	if(!ready)
    		error=0x50;
    	if(this.roommode==1 || this.roommode==3)
    		if(!enuff[0] || !enuff[1])
    			error=0x6F;
    	if(error!=0){
    		packet.addHeader((byte) 0xF3, (byte) 0x2E);
    		packet.addByte2((byte) 0x00, (byte)error);
    		bot[roomowner].send(packet,false);
    		return;
    	}
    	if (roommode==2){
    		int mapId=this.map[1]!=0 ? this.map[1] : this.map[0];
    		refreshSectorMoblist(mapId);
    		timeover=bot[roomowner].executorp.schedule(TimeOver(this), MapValues[5], TimeUnit.MINUTES);
    	}
    	if (roommode!=2)
    		PvpDropRerun();
    	this.status = 3;
        this.starttime  = System.currentTimeMillis();
        bot[roomowner].RoomsPacket(true, new int[]{roommode==0 ? 0 : roommode-1, roommode==0 ? (int)(roomnum/6) : (int)((roomnum-600*(roommode-1))/6)});
		StartPackets();
    }
    
    protected void StartPackets()
    {
		int spectrans=(int)(Math.random()*5);
        if(spectrans>4)
        	spectrans=4;
        packet.addHeader((byte)0x67, (byte)0x66);
    	packet.addByte2((byte)0x74, (byte)0x68);
    	packet.addByte((byte)0x03);
    	for (int i = 0; i<8; i++)
			if(bot[i]!=null)
				bot[i].send(packet,true);
    	packet.clean();
    	packet.addHeader((byte)0x67, (byte)0x66);
    	packet.addByte4((byte)0x73, (byte)0x68, (byte)0x6C, (byte)0x02);
    	for(int b = 0; b<7; b++)
    		packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
    	packet.addByte2((byte)0x00, (byte)0x00);
    	for (int i = 0; i<8; i++)
			if(bot[i]!=null)
				bot[i].send(packet,true);
    	packet.clean();
		packet.addHeader((byte) 0xF3, (byte) 0x2E);
        packet.addPacketHead((byte) 0x01, (byte) 0x00);
        packet.addInt(roomnum, 2, false);
        packet.addInt(this.map[0], 2, false);
        packet.addByte2((byte) 0x03, (byte) 0x00);// soort van beginpositie mod 0x50 en ^ is spectator
        packet.addByte2((byte) 0x01, (byte) 0x00);
        packet.addByte2((byte) 0x00, (byte) 0x00);
        packet.addByte2((byte) spectrans, (byte) 0x01);
		for (int i = 0; i<8; i++)
			if(bot[i]!=null)
				bot[i].send(packet,true);
		packet.clean();
        packet.addHeader((byte)0x67, (byte)0x66);
        packet.addByte2((byte)0x65, (byte)0x68);
        packet.addString(roomname);
        for (int i = 0; i<27-roomname.length(); i++)
        	packet.addByte((byte)0x00);
        for (int i = 0; i<8; i++)
			if(bot[i]!=null)
				bot[i].send(packet,true);
        packet.clean();
    }
    
    public void readyToPlay(int num)
    {
    	roomready[num][1]=true;
        for (int i = 0; i<8; i++)
            if(bot[i]!=null && !roomready[i][1])
            	return;
        packet.addHeader((byte) 0x24, (byte) 0x2F);
        packet.addByte((byte) 0xCC);
        for (int i = 0; i<8; i++)
			if(bot[i]!=null)
				bot[i].send(packet,true);
        packet.clean();
    }
    
    protected void UserInfoPacket(int num)
    {
        packet.addHeader((byte) 0x28, (byte) 0x2F);
        packet.addPacketHead((byte) 0x01, (byte) 0x00);
        for (int i = 0; i < 8; i++)
        {
            if (this.bot[i]!=null)
            {
                packet.addByteArray(this.generateUserPack(i));
            }
            else
            {
                for (int z = 0; z < 52; z++)
                {
                    packet.addByte4((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
                }
            }
        }
        packet.addInt(roomnum+1, 2, false);
        packet.addString(this.roomname);
        for (int i = this.roomname.length(); i<38; i++)
        	packet.addByte((byte) 0x00);
        packet.addByte2((byte) this.roommode, (byte) 0x08);
        packet.addByte2((byte) 0x01, (byte) 0x00);
        packet.addByte2((byte) 0x00, (byte) 0x00);
        packet.addByte((byte) 0x00);
        packet.addByte2((byte) 0x00, (byte) this.roomowner);
        if(num==-1){
        	for (int i = 0; i<8; i++)
        		if(bot[i]!=null)
        			bot[i].send(packet,true);
        	packet.clean();
        }
        else
        	bot[num].send(packet,false);
    }
    
    private byte[] generateUserPack(int num)
    {
    	Packet packet = bot[num].packet;
        packet.addInt(this.bot[num].level, 2, false);
        for (int i = 0; i<3; i++)
            packet.addInt(bot[num].equipitemspart[i], 4, false);
        for (int i = 0; i<8; i++)
        	packet.addInt(bot[num].equipitemsgear[i], 4, false);
        for (int i = 0; i<6; i++)
        	packet.addInt(bot[num].equipitemspack[i], 4, false);
        for (int i = 0; i<2; i++)
        	packet.addInt(bot[num].equipitemscoin[i], 4, false);
        packet.addByte2((byte) 0x02, (byte) 0x00);
        packet.addInt(roomnum, 2, false);
        for (int i = 0; i < 4; i++)
            packet.addByte((byte)ips[num][i]);
        for (int i = 0; i < 3; i++)
            packet.addByte4((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        // Instead of sending player IP send 1.1.1.1 to force relay
        packet.addByte4((byte)1, (byte)1, (byte)1, (byte)1);
        //for (int i = 0; i < 4; i++)
        //	packet.addByte((byte)ips[num][i]);
        
        for (int i = 0; i < 2; i++)
            packet.addByte2((byte) 0x00, (byte) 0x00);
        if(color[num]==1)
        	packet.addByte2((byte)0x74, (byte)0x00);
        else if(color[num]==2)
        	packet.addByte2((byte)0x78, (byte)0x00);
        else
        	packet.addByte2((byte) 0x00, (byte) 0x00);
        for (int i = 0; i < 2; i++)
            packet.addByte2((byte) 0x00, (byte) 0x00);
        packet.addByte((byte)this.roomposition[num]);
        for (int i = 0; i < 5; i++)
            packet.addByte4((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
        packet.addInt((int)((bot[num].attmin+bot[num].attminb)*statmod[0]), 2, false);
        packet.addInt((int)((bot[num].attmax+bot[num].attmaxb)*statmod[0]), 2, false);
        packet.addInt((int)((bot[num].attmintrans+bot[num].attmintransb)*statmod[0]), 2, false);
        packet.addInt((int)((bot[num].attmaxtrans+bot[num].attmaxtransb)*statmod[0]), 2, false);
        packet.addInt((int)((bot[num].hp+bot[num].hpb)*statmod[2]), 2, false);
        packet.addInt((int)((bot[num].defense+bot[num].defenseb)*statmod[2]), 2, false);
        packet.addInt((int)((bot[num].transgauge+bot[num].transgaugeb)*statmod[1]), 2, false);
        packet.addInt((int)((bot[num].crit+bot[num].critb)*statmod[0]), 2, false);
        packet.addInt((int)((bot[num].evade+bot[num].evadeb)*statmod[0]), 2, false);
        packet.addInt((int)((bot[num].spectrans+bot[num].spectransb)*statmod[4]), 2, false);
        packet.addInt((int)((bot[num].speed+bot[num].speedb)*statmod[3]), 2, false);
        packet.addInt((int)((bot[num].transdef+bot[num].transdefb)*statmod[0]), 2, false);
        packet.addInt((int)((bot[num].transbotatt+bot[num].transbotattb)*statmod[0]), 2, false);
        packet.addInt((int)((bot[num].transspeed+bot[num].transspeedb)*statmod[3]), 2, false);
        packet.addInt((int)((bot[num].rangeatt+bot[num].rangeattb)*statmod[4]), 2, false);
        packet.addInt(bot[num].luk+bot[num].lukb, 2, false);
        packet.addInt(bot[num].bottype, 2, false);
        packet.addByte((byte) num);
        packet.addByte4((byte) 0x03, (byte) 0x00, (byte) 0x06, (byte) 0x00);
        packet.addString(bot[num].botname);
        for (int a = bot[num].botname.length(); a<15; a++)
        	packet.addByte((byte)0x00);
        packet.addString(bot[num].guildname);
        for (int a = bot[num].guildname.length(); a<21; a++)
        	packet.addByte((byte)0x00);
        byte[] temp = packet.getPacket();
        packet.clean();
        return temp;
    }
    
    public void setport(int port, int num)
    {
    	this.port[num]=port;
    	//Packet packet = bot[num].packet;
        packet.addHeader((byte) 0x39, (byte) 0x27);
        for (int i = 0; i < 8; i++)
            if (bot[i] != null)
                packet.addInt(this.port[i], 2, true);
            else
                packet.addByte2((byte) 0x00, (byte) 0x00);
        bot[num].send(packet,false);
    }
}

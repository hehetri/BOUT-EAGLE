package botsserver;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
* @author Bouteagle
*/
public class BotClass {
	private static final int DAILY_REWARD_ITEM_ID = 5010300;
	private static final int DAILY_REWARD_CHAT_COLOR = 2;
	private static volatile boolean dailyRewardTableReady = false;
	private boolean firstlog = true;
	protected Long lastreply = System.currentTimeMillis();
    protected ScheduledExecutorService executorp = Executors.newScheduledThreadPool(10);
    protected boolean autocheckrun = true;
    private ScheduledFuture<?> autocheckrunc = null;
    protected ChannelServerConnection channel;
    protected SQLDatabase sql;
    protected OutputStream socketOut;
    protected Room room;
    protected String account;
    protected String ip;
    protected int roomnum = -1;
    protected int lobbynum = 0;
    protected int relaynum = -1;
    protected String botname = "";
    protected int bottype = 0;
    protected int exp = 0;
    protected int level = 0;
    protected int hp = 0, hpb = 0;
    protected int attmin = 0, attminb = 0;
    protected int attmax = 0, attmaxb = 0;
    protected int attmintrans = 0, attmintransb = 0;
    protected int attmaxtrans = 0, attmaxtransb = 0;
    protected int transgauge = 0, transgaugeb = 0;
    protected int crit = 0, critb = 0;
    protected int evade = 0, evadeb = 0;
    protected int spectrans = 0, spectransb = 0;
    protected int speed = 0, speedb = 0;
    protected int transdef = 0, transdefb = 0;
    protected int transbotatt = 0, transbotattb = 0;
    protected int transspeed = 0, transspeedb = 0;
    protected int rangeatt = 0, rangeattb = 0;
    protected int luk = 0, lukb = 0;
    protected int defense = 0, defenseb = 0;
    protected int stash = 0;
    protected int botstract = 0;
    protected int[] equipitemspart = new int[3];
    protected int[] equipitemsgear = new int[8];
    protected int[] equipitemspack = new int[6];
    protected int[] equipitemscoin = new int[2];
    protected int[] inventitems = new int[10];
    private int[] stasheditems = new int[50];
    protected int[] itemduration = new int[79];
    protected int[] itemdurationcur = new int[79];
    protected Timestamp[] itemdurationdate = new Timestamp[79];
    protected int[] itemdurationsort = new int[79];
    private String[] blockedusers = new String[15];
    private String[] friends = new String[20];
    private int guildid = -1;
    private int guildmember = 0;
    protected int guildnum = -1;
    protected String[] guildmembers = new String[100];
    protected int[] guildMemberpoints = new int[100];
    private int guildmembermax = 0;
    protected String guildname = "";
    private String Guildnotice = "";
    private String GuildCreatedate = "";
    protected int guildtotalpoints = 0;
    private int guildstate = 0;
    private String[] lobbymsg = new String[10];
    private int[] lobbymsgshow = new int[10];
    private int[] lobbymsgcol = new int[10];
    protected int lobbyroomnum = -1;
    protected int[] page = {1,0};
    protected int gigas = 0;
    protected int coins = 0;
    protected Lobby lobby;
    private int[] statmod = {1,1,1,1,1};
	protected int gm = 0;
	protected int muted = 0;
	protected Long mutedate = null;
	protected int[] trade = new int[] {0,-1,-1,-1,0,0,0};
	protected String[] Gifter = new String[20];
	protected String[] Giftdate = new String[20];
	protected String[] GiftMsg = new String[20];
	protected int[] gift = new int[18];
	protected int[] Giftkind = new int[18];
	protected boolean announce = false;
	protected boolean statboost = false;
	protected String[] chathistory = new String[10];
	private Long lastmsg = System.currentTimeMillis();
    private int msgcount = 0;
    protected boolean finalize = false;
    protected long LastCalc = 0;
    protected Packet packet = new Packet();
    
    public BotClass(String account, String ip, SQLDatabase sql, OutputStream socketOut, ChannelServerConnection channel, Lobby lobby)
    {
    	this.account=account;
    	this.ip=ip;
    	this.sql=sql;
    	this.socketOut=socketOut;
    	this.channel=channel;
    	this.lobby=lobby;
        runautocheck();
    }
    
    private static void debug(String msg)
    {
    	System.out.println("Channel[11002]: "+msg);
    }
    
    public int addplayer()
    {
    	String[] arr = new String[0];
    	int num = 1;
        ResultSet rs = sql.psquery("SELECT * FROM `lobbylist` WHERE online = '1' ORDER BY num",arr);
        try{
        	int i=0;
        	while (rs.next()){
        		i++;
        		num = rs.getInt("num");
        		if (i!=num && i<num){
        			num=i;
        			break;
        		}
        		else{
        			num++;
        		}
        	}
        	rs.close();
        }catch (Exception e){debug("11004: "+e);}
        lobbynum=num;
        arr = new String[]{""+num, channel.Channelname, account};
        sql.psupdate("UPDATE `lobbylist` SET `num`=?, Channel=? WHERE `username`=?", arr);
        arr = new String[] {account};
        rs = sql.psquery("SELECT * FROM `lobbylist` WHERE `username`=?",arr);
        try{
        	if (rs.next())
        		relaynum = rs.getInt("Rnum");
        	rs.close();
        } catch (Exception e) {}
        addplayerPack();
        return num;
    }
    
    public void addplayerPack()
    {
    	packet.addHeader((byte)0xE0, (byte)0x2E);
    	packet.addInt(lobbynum, 2, false);
    	packet.addByte2((byte)0x01, (byte)0x00);
    	send(packet,false);
    }
    
    public void loadchar()
    {
        try {
        	String[] value= new String[1];
        	value[0]=this.account;
            ResultSet rs = sql.psquery("SELECT * FROM bout_characters WHERE username=? LIMIT 1", value);
            if(rs.next())
            {
                this.botname = rs.getString("name");
                this.bottype = rs.getInt("bot");
                this.exp = rs.getInt("exp");
                this.level = rs.getInt("level");
                this.hp = rs.getInt("hp");
                this.gigas = rs.getInt("gigas");
                this.attmin = rs.getInt("attmin");
                this.attmax = rs.getInt("attmax");
                this.attmintrans = rs.getInt("attmintrans");
                this.attmaxtrans = rs.getInt("attmaxtrans");
                this.transgauge = rs.getInt("transgauge");
                this.crit = rs.getInt("crit");
                this.evade = rs.getInt("evade");
                this.spectrans = rs.getInt("specialtrans");
                this.speed = rs.getInt("speed");
                this.transdef = rs.getInt("transdef");
                this.transbotatt = rs.getInt("transbotatt");
                this.transspeed = rs.getInt("transspeed");
                this.rangeatt = rs.getInt("rangeatt");
                this.luk = rs.getInt("luk");
                this.botstract = rs.getInt("botstract");
                this.defense = rs.getInt("defense");

                this.equipitemspart[0] = rs.getInt("equiphead");
                this.equipitemspart[1] = rs.getInt("equipbody");
                this.equipitemspart[2] = rs.getInt("equiparm");
                this.equipitemsgear[0] = rs.getInt("equipminibot");
                this.equipitemsgear[1] = rs.getInt("equipgun");
                this.equipitemsgear[2] = rs.getInt("equipefield");
                this.equipitemsgear[3] = rs.getInt("equipwing");
                this.equipitemsgear[4] = rs.getInt("equipshield");
                this.equipitemsgear[5] = rs.getInt("equiparmpart");
                this.equipitemsgear[6] = rs.getInt("equipflag1");
                this.equipitemsgear[7] = rs.getInt("equipflag2");
                this.equipitemspack[0] = rs.getInt("equippassivskill");
                this.equipitemspack[1] = rs.getInt("equipaktivskill");
                this.equipitemspack[2] = rs.getInt("equippack");
                this.equipitemspack[3] = rs.getInt("equiptransbot");
                this.equipitemspack[4] = rs.getInt("equipmerc");
                this.equipitemspack[5] = rs.getInt("equipmerc2");
                this.equipitemscoin[0] = rs.getInt("equipheadcoin");
                this.equipitemscoin[1] = rs.getInt("equipminibotcoin");
            }
            rs.close();
            value[0]=this.botname;
            rs = sql.psquery("SELECT * FROM bout_inventory WHERE name=? LIMIT 1", value);
            if(rs.next())
            {
                for(int i=2; i<12; i++)
                {
                    this.inventitems[i-2] = rs.getInt(i);
                }
            }
            rs.close();
            value[0]=this.account;
            rs = sql.psquery("SELECT coins,position FROM bout_users WHERE username=? LIMIT 1", value);
            if(rs.next())
            {
                this.coins = rs.getInt("coins");
                this.gm = rs.getInt("position");
            }
            rs.close();
            int a=0;
    		this.stash=0;
        	value= new String[1];
        	value[0]=this.account;
	        rs = sql.psquery("SELECT * FROM stashes WHERE name=?", value);
	        while(rs.next())
	        {
	        	this.stash++;
	        	this.stasheditems[0+a]=rs.getInt("stas1");
	        	this.stasheditems[1+a]=rs.getInt("stas2");
	        	this.stasheditems[2+a]=rs.getInt("stas3");
	        	this.stasheditems[3+a]=rs.getInt("stas4");
	        	this.stasheditems[4+a]=rs.getInt("stas5");
	        	this.stasheditems[5+a]=rs.getInt("stas6");
	        	this.stasheditems[6+a]=rs.getInt("stas7");
	        	this.stasheditems[7+a]=rs.getInt("stas8");
	        	this.stasheditems[8+a]=rs.getInt("stas9");
	        	this.stasheditems[9+a]=rs.getInt("stas10");
	        	a=a+10;
	        }
	        rs.close();
        	String loc = "";
        	int itemid=0;
	        for(int i = 0; i<79; i++)
	        {
	        	if (i<3){
	        		loc = "part"+(i+1);
	        		itemid=equipitemspart[i];}
	        	else if (i<11){
	        		loc = "gear"+(i-2);
	        		itemid=equipitemsgear[i-3];}
	        	else if (i<17){
	        		loc = "pack"+(i-10);
	        		itemid=equipitemspack[i-11];}
	        	else if (i<19){
	        		loc = "coin"+(i-16);
	        		itemid=equipitemscoin[i-17];}
	        	else if (i<29){
	        		loc = "item"+(i-18);
	        		itemid=inventitems[i-19];}
	        	else if (i<79){
	        		loc = "stas"+(i-28);
	        		itemid=stasheditems[i-29];}
	        	try{
	        		value= new String[]{this.botname,loc,""+itemid};
	        		ResultSet lvl = sql.psquery("SELECT * FROM `item_times` WHERE name=? and location=? and itemid=?", value);
	        		if(lvl.next()){
	        			itemduration[i]=lvl.getInt("time");
	        			itemdurationsort[i]=lvl.getInt("sort");
	        			itemdurationdate[i]=lvl.getTimestamp("date");
	        		}
	        		else{
	        			itemduration[i]=0;
	        			itemdurationsort[i]=0;
	        			itemdurationdate[i]=null;
	        		}
	        		lvl.close();
	        	}catch (Exception e){}
	        }
	        TimeCalc();
            loadEquipBonus();
	        value= new String[1];
    		value[0]=this.botname;
    		int i = 0;
    		rs = sql.psquery("SELECT * FROM blocklist WHERE name=?", value);
        	while (rs.next())
        	{
        		blockedusers[i]=rs.getString("blocked");
        		i++;
        	}
        	rs.close();
	        CharPacket(new byte[]{(byte)0xE1, (byte)0x2E});
	        i = 0;
	        rs = sql.psquery("SELECT * FROM `friends` WHERE `name`=?", value);
	    	try{
	    		while(rs.next())
	    		{
	    			friends[i] = rs.getString("name2");
	    			i++;
	    		}
	    		rs.close();
	    	}catch(Exception e){}
	    	rs = sql.psquery("SELECT * FROM `guilds` WHERE `leader`=?", value);
	    	if(rs.next()){
				this.guildname=rs.getString("Guildname");
				this.guildstate=2;
			}
	    	rs.close();
	    	rs = sql.psquery("SELECT * FROM `guildmembers` WHERE `player`=?", value);
			if(rs.next()){
				this.guildname=rs.getString("guild");
				this.guildstate=2;
			}
			rs.close();
			value = new String[]{guildname};
			rs = sql.psquery("SELECT * FROM `guilds` WHERE `Guildname`=?", value);
	    	if(rs.next()){
	    		this.guildtotalpoints=rs.getInt("total_points");
				this.guildmembermax=rs.getInt("maxmemb");
				this.GuildCreatedate=rs.getString("date");
				this.Guildnotice=rs.getString("notice");
				this.guildmembers[0]=rs.getString("leader");
				this.guildMemberpoints[0]=rs.getInt("leader_points");
				this.guildid=rs.getInt("number");
				if (this.guildmembers[0].equals(botname))
					guildnum=0;
				this.guildmember++;
	    	}
	    	rs.close();
	    	i=1;
	    	rs = sql.psquery("SELECT * FROM `guildmembers` WHERE `guild`=?", value);
			while(rs.next()){
				this.guildmembers[i]=rs.getString("player");
				this.guildMemberpoints[i]=rs.getInt("points");
				if (this.guildmembers[i].equals(botname))
					guildnum=i;
				this.guildmember++;
				this.guildid=rs.getInt("guildid");
				i++;
			}
			rs.close();
			value = new String[]{botname};
			rs = sql.psquery("SELECT * FROM `guildapp` WHERE `name`=?",value);
			if(rs.next()){
				this.guildstate=1;
				this.guildname=rs.getString("guildname");
			}
			value = new String[]{};
			i = 0;
			rs = sql.psquery("SELECT * FROM `lobbymsg`", value);
            while (rs.next()){
            	lobbymsg[i]=rs.getString("message");
            	lobbymsgcol[i]=rs.getInt("color");
            	lobbymsgshow[i]=rs.getInt("showonce");
            	i++;
            }
            rs.close();
            muted(true);
	        value = new String[]{botname+" ", ""+bottype, account};
	        sql.psupdate("UPDATE `lobbylist` SET `name`=?, `bottype`=? WHERE `username`=?", value);
        } catch (Exception e)
        {
        	debug(""+e);
        }
    }
    
    protected int muted(boolean load)
    {
    	if (load){
    		ResultSet rs = sql.psquery("SELECT * FROM `bout_characters` WHERE `name`=?", new String[]{botname});
    		try{
    			if(rs.next()){
    				muted=rs.getInt("muted");
    				mutedate=rs.getTimestamp("muteStime").getTime();
    			}
    			rs.close();
    		}catch (Exception e){}
    	}
        if (muted>0)
        {
        	Long endTime = System.currentTimeMillis();
			Long totTime = (endTime-mutedate);
			int secs=(int)TimeUnit.MILLISECONDS.toSeconds(totTime);
			if (secs>muted)
				sql.psupdate("UPDATE `bout_characters` SET `muted`=0, `muteStime`=NULL WHERE `name`=?", new String[]{botname});
        }
        return muted;
    }
    
    protected void loadEquipBonus()
    {
        hpb = 0;
        attminb = 0;
        attmaxb = 0;
        attmintransb = 0;
        attmaxtransb = 0;
        transgaugeb = 0;
        critb = 0;
        evadeb = 0;
        spectransb = 0;
        speedb = 0;
        transdefb = 0;
        transbotattb = 0;
        transspeedb = 0;
        rangeattb = 0;
        lukb = 0;
        defenseb = 0;
        statboost=false;
        for (int i = 0; i<3; i++)
            if (this.equipitemspart[i] != 0)
            	DoBonus(equipitemspart[i]);
        for (int i = 0; i<8; i++)
            if (this.equipitemsgear[i] != 0)
            	DoBonus(equipitemsgear[i]);
        for (int i = 0; i<6; i++)
            if (this.equipitemspack[i] != 0)
            	DoBonus(equipitemspack[i]);
        for (int i = 0; i<2; i++)
            if (this.equipitemscoin[i] != 0)
            	DoBonus(equipitemscoin[i]);
    }
    
    protected void DoBonus(int itemid)
    {
    	String script = "";
    	ResultSet rs = sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", new String[]{""+itemid});
        try{
        	if (rs.next())
        		script=rs.getString("script");
        	rs.close();
        }catch (Exception e){}
        if (script.equals(""))
        	return;
        String[] scripts = script.split("; ");
        for (int i = 0; i<scripts.length; i++)
        {
        	String[] subscript=scripts[i].split(",");
        	int value = Integer.parseInt(subscript[1]);
        	if (subscript[0].equals("hpp"))
                this.hpb += value;
            else if (subscript[0].equals("attmin"))
                this.attminb += value;
            else if (subscript[0].equals("attmax"))
                this.attmaxb += value;
            else if (subscript[0].equals("atttransmin"))
                this.attmintransb += value;
            else if (subscript[0].equals("atttransmax"))
                this.attmaxtransb += value;
            else if (subscript[0].equals("transgauge"))
                this.transgaugeb += value;
            else if (subscript[0].equals("crit"))
                this.critb += value;
            else if (subscript[0].equals("evade"))
                this.evadeb += value;
            else if (subscript[0].equals("spectrans"))
                this.spectransb += value;
            else if (subscript[0].equals("speed"))
                this.speedb += value;
            else if (subscript[0].equals("transbotdef"))
                this.transdefb += value;
            else if (subscript[0].equals("transbotatt"))
                this.transbotattb += value;
            else if (subscript[0].equals("transspeed"))
                this.transspeedb += value;
            else if (subscript[0].equals("luk"))
                this.lukb += value;
            else if (subscript[0].equals("rangeatt"))
                this.rangeattb += value;
            else if (subscript[0].equals("defense"))
                this.defenseb += value;
        }
    }
    
    protected void TimeCalc()
    {
        long now = (new Timestamp(System.currentTimeMillis())).getTime();
        if (LastCalc!=0 && (int)(TimeUnit.MILLISECONDS.toHours(now-LastCalc))<1)
        	return;
        LastCalc=now;
    	for(int i = 0; i<79; i++)
        {
    		try{
	    		String loc="";
	    		int itemid=0;
	    		boolean remove = false;
    			if (i<3){
	        		loc = "part"+(i+1);
	        		itemid = equipitemspart[i];
				}
	        	else if (i<11){
	        		loc = "gear"+(i-2);
	        		itemid = equipitemsgear[i-3];
	        	}
	        	else if (i<17){
	        		loc = "pack"+(i-10);
	        		itemid = equipitemspack[i-11];
	        	}
	        	else if (i<19){
	        		loc = "coin"+(i-16);
	        		itemid = equipitemscoin[i-17];
	        	}
	        	else if (i<29){
	        		loc = "item"+(i-18);
	        		itemid = inventitems[i-19];
	        	}
	        	else if (i<79){
	        		loc = "stas"+(i-28);
	        		itemid = stasheditems[i-29];
	        	}
    	    	if (itemdurationsort[i]==1){
    				long equipdate = itemdurationdate[i].getTime();
    				itemdurationcur[i]=itemduration[i]*24-(int)TimeUnit.MILLISECONDS.toHours(now - equipdate);
    				String[] value = {botname, loc, ""+itemid};
					sql.psupdate("DELETE FROM `item_times` WHERE `name`=? AND `location`=? AND `itemid`!=?", value);
    				if (itemdurationcur[i]<=0){
    					remove=true;
    					sql.psupdate("DELETE FROM `item_times` WHERE `name`=? AND `location`=? AND `itemid`=?", value);
    				}
    			}
    			else
    			{
    				String[] value = {botname, loc, ""+itemid};
					sql.psupdate("DELETE FROM `item_times` WHERE `name`=? AND `location`=? AND `itemid`!=?", value);
    				if (itemdurationsort[i]==0 && itemid!=0){
    					int time=0;
        				ResultSet rs = sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", new String[]{""+itemid});
        		        try{
        		        	if (rs.next())
        		        		time = rs.getInt("days");
        		        }catch (Exception e){}
    		        	rs.close();
    		        	if (time!=0)
    		        		remove=true;
    				}
    				itemdurationcur[i]=itemduration[i];
    			}
    	    	if (remove){
	        		if (i<3)
	        			equipitemspart[i]=0;
		        	else if (i<11)
		        		equipitemsgear[i-3]=0;
		        	else if (i<17)
		        		equipitemspack[i-11]=0;
		        	else if (i<19)
		        		equipitemscoin[i-17]=0;
		        	else if (i<29)
		        		inventitems[i-19]=0;
		        	else if (i<79)
		        		stasheditems[i-29]=0;
	        		UpdateBot();
	        		UpdateInvent();
	        		UpdateStash((int)(i/10));
    	    	}
    		}catch (Exception e){}
        }
    }
    
    protected boolean checkbot()
    {
        try {
        	String[] value= new String[1];
    		value[0]=this.account;
        ResultSet rs = sql.psquery("SELECT * FROM bout_characters WHERE username=? LIMIT 1", value);
        if (rs.next())
        {
        	rs.close();
            return true;
        }
        rs.close();
        return false;
        } catch (Exception e){

        }
        return false;
    }
    
    public void CharPacket(byte[] head)
    {
        try {
        	TimeCalc();
	        
	        packet.addHeader(head[0], head[1]);
	        packet.addByte2((byte)0x01, (byte)0x00);
	        packet.addString(this.botname);
	        for (int i = botname.length(); i<15; i++){
	            packet.addByte((byte)0x00);
	        }
	        packet.addInt(this.bottype, 2,false);
	        packet.addInt(this.exp, 4,false);
	        packet.addInt(this.level, 2,false);
	        packet.addInt((int)((this.hp+this.hpb)*statmod[2]), 2,false);
	        packet.addInt((int)((this.attmin+this.attminb)*statmod[0]),2,false);
	        packet.addInt((int)((this.attmax+this.attmaxb)*statmod[0]),2,false);
	        packet.addInt((int)((this.attmintrans+this.attmintransb)*statmod[0]),2,false);
	        packet.addInt((int)((this.attmaxtrans+this.attmaxtransb)*statmod[0]),2,false);
	        packet.addInt((int)((this.defense+this.defenseb)*statmod[0]),2,false);
	        packet.addInt((int)((this.transgauge+this.transgaugeb)*statmod[1]),2,false);
	        packet.addInt((int)((this.crit+this.critb)*statmod[0]),2,false);
	        packet.addInt((int)((this.evade+this.evadeb)*statmod[0]),2,false);
	        packet.addInt((int)((this.spectrans+this.spectransb)*statmod[4]),2,false);
	        packet.addInt((int)((this.speed+this.speedb)*statmod[3]),2,false);
	        packet.addInt((int)((this.transdef+this.transdefb)*statmod[0]),2,false);
	        packet.addInt((int)((this.transbotatt+this.transbotattb)*statmod[0]),2,false);
	        packet.addInt((int)((this.transspeed+this.transspeedb)*statmod[3]),2,false);
	        packet.addInt((int)((this.rangeatt+this.rangeattb)*statmod[4]),2,false);
	        packet.addInt(this.luk+this.lukb,2,false);
	        packet.addInt(this.botstract,4,false);
	
	        for(int i = 0; i<16;i++)
	        	packet.addByte((byte)0x00);
	
	        for (int i = 0; i<3; i++)
	        {
	            packet.addInt(this.equipitemspart[i], 4, false);
	            packet.addInt((this.equipitemspart[i] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
	            packet.addByte((this.equipitemspart[i] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);
	        }
	        packet.addByte((byte)0x01);
	        for (int i = 19; i<29; i++)
	        {
	            packet.addInt(this.inventitems[i-19], 4, false);
	            packet.addInt((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
	            packet.addByte((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);
	        }
	
	        packet.addInt(this.gigas, 4,false);
	        
	        for(int i = 0; i<60; i++)
	        	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
	        packet.addByte2((byte)0x00, (byte)0x00);
	        
	        for (int i = 3; i<11; i++)
	        {
	            packet.addInt(this.equipitemsgear[i-3], 4, false);
	            packet.addInt((this.equipitemsgear[i-3] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
	            packet.addByte((this.equipitemsgear[i-3] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);
	        }
	
	        for (int i = 11; i<17; i++)
	        {
	            packet.addInt(this.equipitemspack[i-11], 4, false);
	            packet.addInt((this.equipitemspack[i-11] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
	            packet.addByte((this.equipitemspack[i-11] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);
	        }
	        for (int i = 0; i<50; i++)
	        	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
	
	        for (int i = 17; i<19; i++)
	        {
	            packet.addInt(this.equipitemscoin[i-17], 4, false);
	            packet.addInt((this.equipitemscoin[i-17] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
	            packet.addByte((this.equipitemscoin[i-17] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);
	        }
	        
	        for(int i = 0; i<7; i++)
	        	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
	        packet.addByte2((byte)0x00, (byte)0x00);
	        
	        packet.addByte((byte)this.stash);
	        
	        for (int i = 29; i<79; i++)
	        {
	            packet.addInt(this.stasheditems[i-29], 4, false);
	            packet.addInt((this.stasheditems[i-29] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
	            packet.addByte((this.stasheditems[i-29] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);            	
	        }
	        
	        for(int i = packet.getPacket().length; i < 1374; i++){
	        	packet.addByte((byte)0x00);}
	
	        send(packet,false);
	        } catch(Exception e){
	        	debug(""+e);
	        }
	    }
    
    public void extraloadPackets()
    {
    	packet.addHeader((byte) 0x28, (byte) 0x27);
        packet.addInt(1, 2, false);
        send(packet,false);
        packet.addHeader((byte) 0x53, (byte) 0x2F);
        packet.addByteArray(new byte[]{(byte) 0x4E, (byte) 0x95, (byte) 0xDD, (byte) 0x29, (byte) 0xCE,
                (byte) 0x3A, (byte) 0x55, (byte) 0xDB, (byte) 0x20, (byte) 0xB6, (byte) 0xAD,
                (byte) 0x97, (byte) 0xA6, (byte) 0x5C, (byte) 0xC0, (byte) 0x1C});
        send(packet,false);
        packet.addHeader((byte) 0x27, (byte) 0x27);
        packet.addInt(1, 2, false);
        packet.addString(botname);
        packet.addByte((byte) 0x00);
        packet.addByte4((byte) 0xCC, (byte) 0xCC, (byte) 0x01, (byte) 0x01);
        send(packet,false);
        blockedlistPacket();
    }
    
    public void blockedlistPacket()
    {
        packet.addHeader((byte) 0x4F, (byte) 0x2F);
        packet.addInt(1, 2, false);
        packet.addByte2((byte) 0x00, (byte) 0x00);
        int blockednum=0;
        for(int i = 0; i<blockedusers.length; i++)
        	if (blockedusers[i]!=null && !blockedusers[i].equals(""))
        		blockednum++;
        packet.addInt(blockednum, 2, false);
        for(int i = 0; i<blockednum; i++)
        {
        	if (blockedusers[i]!=null)
        	{
        		String namee = blockedusers[i];
        		packet.addString(namee);
        		for(int a = 0; a<15-namee.length(); a++)
        			packet.addByte((byte)0x00);
        	}
        }
        send(packet,false);
    }
    
    public void nobotPacket()
    {
    	packet.addHeader((byte)0xE1, (byte)0x2E);
    	packet.addByte2((byte)0x00, (byte)0x35);
    	send(packet,false);
    }
    
    protected boolean parsemessage(String message)
    {
		int secs=(int)TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastmsg);
		if (secs<5)
			msgcount++;
		else
		{
			msgcount=0;
			lastmsg = System.currentTimeMillis();
		}
		int samemessage = 0;
		for(int i=0; i<10; i++)
		{
			if(chathistory[i]!=null)
				if (chathistory[i].equalsIgnoreCase(message))
					samemessage++;
		}
		for (int i = 1; i<10; i++)
			chathistory[i]=chathistory[i-1];
		chathistory[0]=message;
		if (samemessage > 2 || msgcount > 2)
		{
			String[] value = new String[]{"60", botname};
			sql.psupdate("UPDATE `bout_characters` SET `muted`=?, `muteStime`=now() WHERE `name`=?", value);
			return false;
		}
		return true;
	}
    
    protected boolean filterchat(String message)
    {
    	for (int i = 0; i<lobby.standard.filterwords.length; i++)
    		if((message.toLowerCase()).matches(lobby.standard.filterwords[i]))
    			return true;
    	return false;
    }
    
    public boolean checkhasbot()
    {
        try
        {
        	String[] arr = {account};
            ResultSet rs = Main.sql.psquery("SELECT name FROM bout_characters WHERE username=? LIMIT 1", arr);
            if (rs.next()){
            	rs.close();
            	return true;
            }
            else{
                rs.close();
            	return false;
            }
        }catch(Exception e){debug(""+e);}
        return true;
    }
    
    protected void createbot(String username, String name, int bottype)
    {
    	if (checkhasbot()){
    		CreateBotPacket(new byte[]{(byte)0x01, (byte)0x00});
    		return;
    	}
    	String[] value= new String[3];
		value[0]=username;
		value[1]=name;
		value[2]=Integer.toString(bottype);
        sql.psupdate("INSERT INTO `bout_characters` (`username`, `name`, `bot`)VALUES (?, ?, ?)", value);
        value= new String[1];
		value[0]=name;
        sql.psupdate("INSERT INTO `bout_inventory` (`name`) VALUES (?)", value);
        value= new String[2];
		value[0]=name;
    	value[1]=username;
    	sql.psupdate("UPDATE `item_times` SET `name`=? WHERE `accountname`=?", value);
    	CreateBotPacket(new byte[]{(byte)0x01, (byte)0x00});
    }
    
    public void lobbypacket()
    {
    	String[] player= new String[600];
    	int[] bottyp= new int[600];
    	int[] state= new int[600];
    	int[] num= new int[600];
    	int i = 1;
    	String[] value = {"1",this.channel.Channelname};
        ResultSet rs = sql.psquery("SELECT * FROM `lobbylist` WHERE online=? AND Channel=?", value);
        try{
        	while (rs.next()){
        		player[i] = rs.getString("name");
        		bottyp[i] = rs.getInt("bottype");
        		state[i]=rs.getInt("status");
        		num[i]=rs.getInt("num");
        		i++;
        	}
        	rs.close();
        }catch(Exception e){}
        packet.addHeader((byte)0xF2, (byte)0x2E);
        packet.addByte2((byte) 0x01, (byte) 0x00);
        packet.addInt(i, 2, false);
        try{
            for (int a = 1; a<i; a++){
            	packet.addString(player[a]);
            	for (int b = player[a].length(); b<15; b++)
            		packet.addByte((byte)0x00);
            	packet.addByte2((byte) (bottyp[a] & 0xff), (byte) 0x01);
            }
            for (int a = i; a<10; a++)
            	packet.addByteArray(new byte[] {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
            			(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00});
            packet.addByte2((byte)0x02, (byte)0x01);
            send(packet,false);
            for (int a = 1; a<i; a++)
            	if (state[a]==0)
            		send(lobby.bots[num[a]].PlayerAddPacket(0),false);
    	} catch (Exception e) {
    		debug(""+e);
    	}
        for(int a = 0; a<lobbymsg.length; a++)
        	if (firstlog && lobbymsg[a]!=null)
        		sendChatMsg(lobbymsg[a], lobbymsgcol[a], false, -1);
        	else
        		if(lobbymsgshow[a]==0 && lobbymsg[a]!=null)
        			sendChatMsg(lobbymsg[a], lobbymsgcol[a], false, -1);
        if(firstlog)
        	firstlog=false;
        //event start
        /**rs = Main.sql.psquery("SELECT * FROM Event WHERE account=? LIMIT 1", new String[]{account});
        try{
        	if (rs.next()) 
        		sendChatMsg("The halloween event is live collect candy, current amount: "+rs.getInt("halloween2020"), 2, false, -1);
        	else
        		sendChatMsg("The halloween event is live collect candy, current amount: 0", 2, false, -1);
        }catch (Exception e){}*/
        /**rs = Main.sql.psquery("SELECT * FROM Event WHERE account=? LIMIT 1", new String[]{account});
        try{
        	if (rs.next()) 
        		sendChatMsg("The Christmas event is live collect tickets, current amount: "+rs.getInt("christmas2020"), 2, false, -1);
        	else
        		sendChatMsg("The Christmas event is live collect tickets, current amount: 0", 2, false, -1);
        }catch (Exception e){}*/
        //event end
        grantDailyRewardIfEligible();
    }

    private void grantDailyRewardIfEligible()
    {
    	ensureDailyRewardTable();
    	LocalDate today = LocalDate.now(ZoneOffset.UTC);
    	LocalDate lastRewardDate = null;
    	ResultSet rs = sql.psquery("SELECT reward_date FROM `bout_daily_rewards` WHERE `username`=? LIMIT 1", new String[]{account});
    	try{
    		if (rs.next()){
    			java.sql.Date rewardDate = rs.getDate("reward_date");
    			if (rewardDate != null)
    				lastRewardDate = rewardDate.toLocalDate();
    		}
    		rs.close();
    	}catch (Exception e){debug("Daily reward lookup failed: "+e);}
    	if (lastRewardDate != null && !lastRewardDate.isBefore(today))
    		return;
    	DailyRewardItem reward = fetchDailyRewardItem(DAILY_REWARD_ITEM_ID);
    	if (!placeDailyRewardItem(reward)){
    		sendChatMsg("[Daily Reward] Inventory and stash are full. Clear space and reconnect to claim your reward.", DAILY_REWARD_CHAT_COLOR, false, -1);
    		return;
    	}
    	sql.psupdate("REPLACE INTO `bout_daily_rewards` (`username`, `reward_date`, `updated_at`) VALUES (?, ?, now())",
    			new String[]{account, today.toString()});
    	sendChatMsg("[Daily Reward] You received "+reward.name+"!", DAILY_REWARD_CHAT_COLOR, false, -1);
    }

    private void ensureDailyRewardTable()
    {
    	if (dailyRewardTableReady)
    		return;
    	synchronized (BotClass.class){
    		if (dailyRewardTableReady)
    			return;
    		sql.psupdate("CREATE TABLE IF NOT EXISTS `bout_daily_rewards` (" +
    				"`username` VARCHAR(64) NOT NULL PRIMARY KEY, " +
    				"`reward_date` DATE NOT NULL, " +
    				"`updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
    				")", new String[]{});
    		dailyRewardTableReady = true;
    	}
    }

    private DailyRewardItem fetchDailyRewardItem(int itemId)
    {
    	int time = 0;
    	String name = "Daily Reward Item";
    	ResultSet rs = sql.psquery("SELECT `name`, `days` FROM `bout_items` WHERE `id`=? LIMIT 1", new String[]{""+itemId});
    	try{
    		if (rs.next()){
    			time = rs.getInt("days");
    			name = rs.getString("name");
    		}
    		rs.close();
    	}catch (Exception e){debug("Daily reward item lookup failed: "+e);}
    	return new DailyRewardItem(itemId, time, name);
    }

    private boolean placeDailyRewardItem(DailyRewardItem reward)
    {
    	for (int i = 0; i<10; i++)
    		if (inventitems[i]==0){
    			inventitems[i]=reward.itemId;
    			if (reward.durationDays>0)
    				AddItemTime(reward.itemId, i, "item", reward.durationDays);
    			UpdateInvent();
    			return true;
    		}
    	int stashSlots = stash * 10;
    	for (int i = 0; i<stashSlots; i++)
    		if (stasheditems[i]==0){
    			stasheditems[i]=reward.itemId;
    			if (reward.durationDays>0)
    				AddItemTime(reward.itemId, i, "stas", reward.durationDays);
    			UpdateStash(i / 10);
    			return true;
    		}
    	return false;
    }

    private static class DailyRewardItem
    {
    	private final int itemId;
    	private final int durationDays;
    	private final String name;

    	private DailyRewardItem(int itemId, int durationDays, String name)
    	{
    		this.itemId = itemId;
    		this.durationDays = durationDays;
    		this.name = name;
    	}
    }
    
    public void WhisperPacket(String Receive, String message, int len)
    {
    	packet.addHeader((byte) 0x2B, (byte) 0x2F);
        int num = getNum(Receive);
        if(num == -1)
        	packet.addByte4((byte) 0x00, (byte) 0x6B, (byte) 0x00, (byte) 0x00);
        else
        {
        	packet.addByte2((byte) 0x01, (byte) 0x00);
        	packet.addInt(len, 2, false);
        	packet.addString(message);
        	packet.addByte((byte) 0x00);
        	boolean notinblock=true;
        	Receive=lobby.bots[num].botname;
        	for (int i = 0; i<15; i++)
        		if (blockedusers[i]!=null && blockedusers[i].equalsIgnoreCase(Receive))
        			notinblock=false;
        	if (notinblock)
        		lobby.bots[num].send(packet,true);
        }
        send(packet,false);
    }
    
    public void RoomsPacket(boolean global, int[] page)
    {
    	if (page[0]==-1)
    		page=this.page;
    	packet.addHeader((byte) 0xEF, (byte) 0x2E);
        packet.addByte2((byte) 0x01, (byte) 0x00);
    	Room[] rooms = lobby.getRoomsbyPage(page[0], page[1]);
    	for (int i = 0; i<6; i++)
    	{
	    	if (rooms[i]!=null)
	        {
	    		if (rooms[i].GhostRoomCheck()==8)
	    			RemoveRoom(rooms[i]);
	        	packet.addInt(page[0]*600+page[1]*6+i+1, 2, false);
	            String cname, cpass;
	            cname=rooms[i].roomname;
	            packet.addString(cname);
	            for (int a = cname.length(); a<27; a++)
	            	packet.addByte((byte)0x00);
	            
	            cpass=rooms[i].password;
	            if(cpass!=null && !cpass.equals(""))
	                packet.addString("password");
	            for (int a = (cpass!=null && !cpass.equals("") ? 8 : 0); a<11; a++)
	                packet.addByte((byte)0x00);

	            packet.addByte((byte) rooms[i].roommode);
	            packet.addByte((byte) 0x08);
	            packet.addByte((byte) rooms[i].status);
	            packet.addByte((byte) rooms[i].roomlevel);
	            packet.addByte4((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
	        }
	        else
	        {
	            for (int z = 0; z < 48; z++)
	            {
	                packet.addByte((byte) 0x00);
	            }
	        }
    	}
    	if (!global)
    		send(packet,false);
    	else
    		lobby.sendAllroom(packet, lobbynum, page);
    }
    
    public void addRoom(int mode, String roomname, String password)
    {
    	int rnum = mode;
    	if (mode==0)
    		rnum = mode+1;
    	rnum = (rnum-1)*600;
    	rnum = lobby.getEmptyRoom(rnum);
    	if (rnum==-1)
    		channel.closecon();
    	this.lobbyroomnum=rnum;
    	this.roomnum=0;
    	Room room = new Room(this, roomname, password, mode, ip, rnum);
    	lobby.setRoom(rnum, room);
    	RoomsPacket(true, new int[]{room.roommode==0 ? 0 : room.roommode-1, room.roommode==0 ? (int)(room.roomnum/6) : (int)((room.roomnum-600*(room.roommode-1))/6)});
    	this.room=room;
    }
    
    public void quickJoin(int roommode)
    {
    	if (roommode==0)
    		roommode+=1;
    	roommode = (roommode-1)*600;
    	for (int i = roommode; i<(roommode+600); i++)
    		if (lobby.rooms[i]!=null)
    		{
    			int rnum=lobby.rooms[i].Join(this, "", ip);
    			if (rnum!=-1)
    				break;
    		}
    	if (room==null){
    		packet.addHeader((byte) 0x28, (byte) 0x2F);
            packet.addByte2((byte) 0x00, (byte) 0x3A);
            send(packet,false);
    	}
    }
    
    public void RemoveRoom(Room room)
    {
    	try {
	    	int lobbyrnum=room.roomnum;
	    	room.stopAllSchedules();
	    	int roommode=room.roommode;
	    	try {
	    		room.clearstage.cancel(true);
	    		room.timeover.cancel(true);
	    	}catch (Exception e) {debug("error shutting down scheduled events");}
	    	for (int i = 0; i<8; i++)
	    		if (room.bot[i]!=null){
	    			room.bot[i].roomnum=0;
	    			room.bot[i].lobbyroomnum=-1;
	    			room.bot[i].room=null;
	    			room.bot[i].autocheckrun=true;
	    		}
	    	if (this.room==room){
	    		roomnum=0;
				lobbyroomnum=-1;
				this.room=null;
				autocheckrun=true;
	    	}
	    	room.bot=new BotClass[8];
	    	lobby.rooms[lobbyrnum]=null;
	    	RoomsPacket(true, new int[]{roommode==0 ? 0 : roommode-1, roommode==0 ? (int)(lobbyrnum/6) : (int)((lobbyrnum-600*(roommode-1))/6)});
    	}catch (Exception e){debug("Error occured while removing room: "+e);}
    }
    
    public void FriendlistPacket()
    {
        packet.addHeader((byte)0x0C, (byte)0x2F);
        packet.addPacketHead((byte)0x01, (byte)0x00);
        try{
	        for (int i = 0; i<friends.length; i++){  
	            i++;
	            packet.addString(friends[i]);
	            for (int a = friends[i].length(); a<15; a++)
	            	packet.addByte((byte)0x00);
	            String[] value = {friends[i]};
	            ResultSet rs = sql.psquery("SELECT * FROM `bout_characters` WHERE `name`=?", value);
	            if (rs.next()){
	            	packet.addByte4((byte)rs.getInt("level"),(byte)0x00,(byte)0x00,(byte)0x00);}
	            else
	            	packet.addByte4((byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00);
	            packet.addByte4((byte)(getNum(friends[i])!=-1 ? 0x01 : 0x00),(byte)0x00,(byte)0x00,(byte)0x00);
	            rs.close();
	        }
        } catch (Exception e){}
        for (int i = 0; i<16; i++){
        	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        	packet.addByte2((byte)0x00, (byte)0x00);
        	packet.addByte((byte)0x00);
        }
        send(packet,false);
    }
    
    
    public void FriendRequest(String friend)
    {
    	friend=friend.replaceAll("\\s+","");
    	friend=friend.replaceAll("(?i)mod","Mod ");
    	friend=friend.replaceAll("(?i)gm","Gm ");
    	friend=friend.replaceAll("(?i)admin","Admin ");
    	int num = getNum(friend);
    	if(friend.equalsIgnoreCase(botname) && num ==-1){
    		PopUpFixed(new byte[]{(byte)0x00, (byte)0x53}, -1);
    		return;
    	}
    	for (int i = 0; i<20; i++)
    		if(friends[i]!=null && friends[i].equalsIgnoreCase(friend)){
    			PopUpFixed(new byte[]{(byte)0x00, (byte)0x55}, -1);
    			return;
    		}
		packet.addHeader((byte)0x0F, (byte)0x2F);
		packet.addPacketHead((byte)0x01, (byte)0x00);
		packet.addString(botname);
		for (int i = botname.length(); i<15; i++)
			packet.addByte((byte)0x00);
		packet.addString(friend);
		for (int i = botname.length(); i<15; i++)
			packet.addByte((byte)0x00);
		lobby.bots[num].send(packet,false);
    }
    
    public void FriendReply(String friend, boolean accept)
    {
    	int num = getNum(friend);
    	if (!accept){
    		PopUpFixed(new byte[]{(byte)0x53, (byte)0x00}, num);
    		return;
    	}
    	friend=friend.replaceAll("\\s+","");
    	friend=friend.replaceAll("(?i)mod","Mod ");
    	friend=friend.replaceAll("(?i)gm","Gm ");
    	friend=friend.replaceAll("(?i)admin","Admin ");
    	for (int i = 0; i<20; i++)
    		if(friends[i]!=null && friends[i].equalsIgnoreCase(friend))
    			return;
    	sql.psupdate("INSERT INTO `friends` (`name`, `name2`)VALUES (?, ?)", new String[]{botname, friend});
		sql.psupdate("INSERT INTO `friends` (`name2`, `name`)VALUES (?, ?)", new String[]{botname, friend});
		FriendReplyPacket(accept);
		FriendlistPacket();
		if(num!=-1) {
			lobby.bots[num].FriendlistPacket();
		}
    }
    
    public void RemoveFriend(String friend)
    {
    	int num = 0;
    	sql.psupdate("DELETE FROM `friends` WHERE `name`=? AND `name2`=?", new String[]{botname,friend});
    	sql.psupdate("DELETE FROM `friends` WHERE `name`=? AND `name2`=?", new String[]{friend,botname});
    	FriendlistPacket();
    	if ((num=getNum(friend))!=-1)
    		lobby.bots[num].FriendlistPacket();
    }
    
    public void FriendReplyPacket(boolean accept)
    {
		packet.addHeader((byte)0x28, (byte)0x2F);
		packet.addByte2((byte)(accept ? 0x52 : 0x53), (byte)0x00);
		send(packet,false);
    }
    
    public void FnineOK()
    {
    	packet.addHeader((byte)0x46, (byte)0x2F);
    	packet.addByte4((byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00);
    	for (int i = 0; i<7; i++)
    		packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
    	send(packet,false);
    }
    
    public void DoGuildPackets()
    {
    	if(guildstate==2){
    		GuildStatePacket();
    		GuildInfoPacket();
    		GuildMemberPacket();
    		sendChatMsg("[Guild Notice] "+Guildnotice, 8, false, -1);
    	}
    	if(guildstate==1)
    		GuildStatePacket();
    }
    
    public void GuildStatePacket()
    {
    	packet.addHeader((byte)0x46, (byte)0x2F);
    	packet.addPacketHead((byte)0x01, (byte)0x00);
    	packet.addByte4((byte)0xAA, (byte)0x0C, (byte)0x01, (byte)0x00); 
    	packet.addByte((byte)guildstate);//0 no guild, 1 applying state, 2 member, 
    	packet.addString(guildname);
    	for(int i = guildname.length(); i<17; i++){
    		packet.addByte((byte)0x00);
    	}
    	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        if(guildmembers[0]!=null && guildmembers[0].equals(botname))
        	packet.addByte4((byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00); //01 1ste byte voor leader        
        else
        	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        send(packet,false);
    }
    
    public void GuildMemberPacket()
    {
    	packet.addHeader((byte)0x48, (byte)0x2F);
    	packet.addByte2((byte)0x01, (byte)0x00);
    	for (int i = 0; i<guildmember; i++)
    	{
    		if(guildmembers[i]!=null){
    			packet.addString(guildmembers[i]);
    			for(int a = guildmembers[i].length(); a<15; a++){
    				packet.addByte((byte)0x00);
    			}
    			packet.addInt(guildMemberpoints[i], 4, false);
    		}
    	}
    	for(int i = guildmember; i<101; i++){
    		for(int a = 0; a<4; a++)
    			packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
    		packet.addByte2((byte)0x00, (byte)0x00);
    		packet.addByte((byte)0x00);
    	}
    	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
    	send(packet,false);
    }
    
    public void GuildInfoPacket()
    {
    	packet.addHeader((byte)0x45, (byte)0x2F);
    	packet.addPacketHead((byte)0x01, (byte)0x00);
    	packet.addString(guildname);
    	for(int i = guildname.length(); i<17; i++){
    		packet.addByte((byte)0x00);
    	}
    	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
    	packet.addString(guildmembers[0]);
    	for(int i = guildmembers[0].length(); i<15; i++){
    		packet.addByte((byte)0x00);
    	}
    	packet.addByte2((byte)0x00, (byte)0x00);
    	packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
    	packet.addString(GuildCreatedate.substring(0, 19));
    	packet.addByte((byte)0x00);
    	packet.addInt(guildmember, 4, false); //members
    	packet.addByte4((byte)guildmembermax, (byte)0x00, (byte)0x00, (byte)0x00); //max members
    	packet.addInt(guildtotalpoints, 4, false); //points
    	packet.addByte4((byte)0x01, (byte)0x00,(byte)0x00, (byte)0x00);
    	send(packet,false);
    }
    
    protected void sendChatMsg(String msg, int color, boolean all, int who)
    {
        packet.addHeader((byte) 0x1A, (byte) 0x27);
        packet.addByte2((byte) 0x01, (byte) 0x00);
        packet.addInt(msg.length(), 2, false);
        packet.addInt(color, 2, false);
        packet.addString(msg);
        packet.addByte((byte) 0x00);
        if(all)
        	lobby.sendAll(packet, lobbynum, announce);
        if (who==-1)
        	send(packet,false);
        else
        	lobby.bots[who].send(packet,false);
    }
    
    public void logout()
    {
    	String[] value = {ip};
    	sql.psupdate("UPDATE bout_users SET current_ip='', online='0' WHERE last_ip=?", value);
    	value = new String[]{ip, account};
    	sql.psupdate("UPDATE bout_users SET current_ip=? WHERE username=?", value);
    	packet.addHeader((byte) 0x0A, (byte) 0x2F);
        packet.addInt(1, 2, false);
        send(packet,false);
    }
    
    public void GuildRemove(String player)
    {
    	if (guildmembers[0].equals(player) && botname.equalsIgnoreCase(player))
    	{
    		String name2 = "";
    		int points2 = 0;
    		String[] value= {this.guildname};
    		ResultSet rs = sql.psquery("SELECT * FROM `guildmembers` WHERE `guild`=?", value);
        	try{
    		if(rs.next()){
    			name2 = rs.getString("player");
    			points2 = rs.getInt("points");
    		}rs.close();}catch(Exception e){}
        	value= new String[]{name2, player, this.guildname};
    		sql.psupdate("UPDATE `guilds` SET `leader`=? WHERE `leader`=? AND `Guildname`=?", value);
    		value= new String[]{""+points2, name2, this.guildname};
    		sql.psupdate("UPDATE `guilds` SET `leader_points`=? WHERE `leader`=? AND `Guildname`=?", value);
    		if(name2==null || name2.equals("")){
    			value= new String[]{this.guildname};
    			sql.psupdate("DELETE FROM `guilds` WHERE `Guildname`=?", value);
    		}
    		else
    			sql.psupdate("DELETE FROM `guildmembers` WHERE `player`=? and `guild`=?", new String[]{name2, this.guildname});
    		guildmembermax = guildmember = guildtotalpoints = guildstate = 0;
    		guildnum = -1;
    		guildmembers = new String[100];
    		guildMemberpoints = new int[100];
    		guildname = Guildnotice = GuildCreatedate = "";
    		guildid=-1;
    		DoGuildPackets();
    		int num = -1;
    		if ((num=getNum(name2))!=-1)
    			lobby.bots[num].DoGuildPackets();
    	}
    	else
    	{
    		String[] value= new String[]{player, this.guildname};
    		sql.psupdate("DELETE FROM `guildmembers` WHERE `player`=? and `guild`=?", value);
    		try {
	    		BotClass bot = null;
	    		if((bot=botname.equalsIgnoreCase(player) ? this : getNum(player)!=-1 ? lobby.bots[getNum(player)] : null)!=null) {
		    		bot.guildmembermax = bot.guildmember = bot.guildtotalpoints = bot.guildstate = 0;
		    		bot.guildnum = -1;
		    		bot.guildid=-1;
		    		bot.guildmembers = new String[100];
		    		bot.guildMemberpoints = new int[100];
		    		bot.guildname = bot.Guildnotice = bot.GuildCreatedate = "";
		    		bot.DoGuildPackets();
	    		}
    		} catch (Exception e) {debug("Exception occured during removal from guild: "+e);}
    		this.guildmember--;
    		for (int i = 0; i<guildmembermax; i++)
    			if(this.guildmembers[i]!=null && this.guildmembers[i].equalsIgnoreCase(player)) {
    				String[] temparr = new String[guildmembermax];
    				int a = 0;
    				for (int b = 0; b<this.guildmembers.length; b++) {
    					if (b==i)
    						continue;
    					temparr[a++]=this.guildmembers[b];
    				}
    				this.guildmembers=temparr;
    			}
    		DoGuildPackets();
    	}
    }
    
    public void GuildExtend()
    {
    	this.guildmembermax+=5;
    	String[] value= {""+(this.guildmembermax), this.botname, this.guildname};
    	sql.psupdate("UPDATE `guilds` SET `maxmemb`=? WHERE `leader`=? AND `Guildname`=?", value);
    	for(int i = 0; i<10; i++)
    	{
    		if(inventitems[i]==5041300 || inventitems[i]==5041400)
    		{
    			inventitems[i]=0;
    			break;
    		}
    	}
    }
    
    public void GuildApp(String guildname)
    {   
    	boolean excists = false;
    	ResultSet rs = sql.psquery("SELECT * FROM `guilds` WHERE `Guildname`=?", new String[]{guildname});
    	try{
    	if (rs.next())
    		excists = true;
    	rs.close();}catch(Exception e){}
    	if(excists)
    		sql.psupdate("INSERT INTO `guildapp` (`name`, `guildname`)VALUES (?, ?)", new String[]{botname, guildname});
    	GuildStatePacket();
    }
    
    public void GuildAppAction(String player, boolean approve)
    {
    	boolean excist=false;
    	ResultSet rs = sql.psquery("SELECT * FROM `bout_characters` WHERE `name`=?", new String[]{player});
    	try{
    		if (rs.next())
    			excist=true;
    		rs.close();
    	}catch (Exception e){}
		String[] value = new String[]{guildname, player, ""+this.guildid};
		try {
	    	if(approve && guildmember+1<guildmembermax && excist){
	        	guildmember++;
	        	guildmembers[guildmember-1]=player;
	    		sql.psupdate("INSERT INTO `guildmembers` (`guild`, `player`, `guildid`, `points`)VALUES (?, ?, ?, '0')", value);
	    		int num = -1;
	    		if((num = getNum(player))!=-1) {
	    			lobby.bots[num].guildnum=this.guildmember-1;
	    			lobby.bots[num].guildmember=this.guildmember;
	    			lobby.bots[num].guildMemberpoints=this.guildMemberpoints;
	    			lobby.bots[num].guildtotalpoints=this.guildtotalpoints;
	    			lobby.bots[num].guildmembermax=this.guildmembermax;
	    			lobby.bots[num].GuildCreatedate=this.GuildCreatedate;
	    			lobby.bots[num].Guildnotice=this.Guildnotice;
	    			lobby.bots[num].guildmembers=this.guildmembers;
	    			lobby.bots[num].guildstate=2;
	    			lobby.bots[num].guildname=this.guildname;
	    			lobby.bots[num].guildid=this.guildid;
	    			lobby.bots[num].DoGuildPackets();
	    		}
	    	}
	    }catch (Exception e){}
    	sql.psupdate("DELETE FROM `guildapp` WHERE `name`=?", new String[]{player});
    	GuildMemberPacket();
		GuildAppList();
    }
    
    public void GuildAppList()
    {
    	ResultSet rs = sql.psquery("SELECT * FROM `guildapp` WHERE `guildname`=? LIMIT 10", new String[]{guildname});
    	packet.addHeader((byte)0x49, (byte)0x2F);
    	packet.addByte2((byte)0x01, (byte)0x00);
    	int noapp=0;
    	try{
    	while (rs.next())
    	{
    		String name = "";
    		packet.addString(name=rs.getString("name"));
        	for (int i = name.length(); i<15; i++)
        		packet.addByte((byte)0x00);
        	ResultSet res = sql.psquery("SELECT * FROM `bout_characters` WHERE `name`=?", new String[]{name});
        	if (res.next())
        		packet.addInt(res.getInt("level"), 4, false);
        	else
        		packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        	res.close();
        	noapp+=1;
    	}rs.close();}catch (Exception e){}
    	for (int a = noapp; a<10; a++) {
			for (int i = 0; i<15; i++)
				packet.addByte((byte)0x00);
			packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
		}
    	send(packet,false);
    }
    
    public void CreateGuild(String guildname)
    {
    	boolean taken = false;
		String[] str = new String[1];
		str[0] = guildname;
    	ResultSet rs = sql.psquery("SELECT * FROM `guilds` WHERE `Guildname`=?", str);
    	try{
    		if(rs.next())
    		{
    			taken=true;
    		}
    		rs.close();
    	}catch (Exception e){}
    	if(!taken && gigas>=30000)
    	{
    		String[] value = {guildname, botname, "Set a guild notice."};
    		sql.psupdate("INSERT INTO `guilds` (`Guildname`, `leader`, `date`, `notice`, `maxmemb`)VALUES (?, ?, now(), ?, '20')", value);
    		value = new String[]{guildname};
			rs = sql.psquery("SELECT * FROM `guilds` WHERE `Guildname`=?", value);
			try {
	    	if(rs.next())
				this.guildid=rs.getInt("number");
	    	} catch (Exception e) {}
	    	this.guildnum=this.guildMemberpoints[0]=this.guildtotalpoints=0;
			this.guildmembermax=20;
			this.GuildCreatedate=""+new java.sql.Timestamp(System.currentTimeMillis());
			this.Guildnotice="Set a guild notice.";
			this.guildmembers[0]=botname;
			this.guildmember++;
			this.guildstate=2;
			this.guildname=guildname;
			this.gigas-=30000;
			UpdateBot();
    	}
		DoGuildPackets();
    }
    
    public void InvitetoGuild(String player)
    {
    	int num = getNum(player);
    	if (num!=-1 && (lobby.bots[num].guildname==null || lobby.bots[num].guildname.equals("")))
    	{
	        //49 2F is application list
	        packet.addHeader((byte)0x62, (byte)0x2B);
	        packet.addPacketHead((byte)0x01, (byte)0x00);
	        packet.addString(botname);
	        for (int i = botname.length(); i<15; i++)
	        	packet.addByte((byte)0x00);
	        packet.addString(player);
	        for (int i = player.length(); i<15; i++)
	        	packet.addByte((byte)0x00);
	        lobby.bots[num].send(packet,false);
    	}
    	else
    		PopUp(false, player+" is already in a guild");
    }
    
    public void GuildInviteReply(String player, boolean accept)
    {
    	int num = getNum(player);
    	if (num==-1)
    		PopUp(false, "Error");
    	String[] value = {lobby.bots[num].guildname, botname, ""+lobby.bots[num].guildid};
    	if (accept && (lobby.bots[num].guildmember+1)<=lobby.bots[num].guildmembermax){
    		sql.psupdate("INSERT INTO `guildmembers` (`guild`, `player`, `guildid`, `points`)VALUES (?, ? ,? ,'0')", value);
    		this.guildnum=lobby.bots[num].guildmember;
    		lobby.bots[num].guildmember++;guildmember=lobby.bots[num].guildmember;
    		this.guildMemberpoints=lobby.bots[num].guildMemberpoints;
    		this.guildtotalpoints=lobby.bots[num].guildtotalpoints;
    		this.guildmembermax=lobby.bots[num].guildmembermax;
    		this.GuildCreatedate=lobby.bots[num].GuildCreatedate;
    		this.Guildnotice=lobby.bots[num].Guildnotice;
    		lobby.bots[num].guildmembers[guildnum]=botname;
    		this.guildmembers=lobby.bots[num].guildmembers;
    		this.guildstate=2;
    		this.guildname=lobby.bots[num].guildname;
    		this.guildid=lobby.bots[num].guildid;
    		lobby.bots[num].PopUp(false, botname+" succesfully joined the guild: " + guildname);
    		DoGuildPackets();
    		lobby.bots[num].DoGuildPackets();
    	}
    	else {
			lobby.bots[num].PopUp(false, this.botname+" has declined the guildinvite");
			DoGuildPackets();
    	}
    }
    
    public void SetGuildNotice(String notice)
    {
    	sql.psupdate("UPDATE guilds SET notice=? WHERE Guildname=?", new String[]{notice, guildname});
    	Guildnotice=notice;
    	sendChatMsg("[Guild Notice]"+Guildnotice, 8, false, -1);
    }
    
    public void UpdateGuild()
    {
    	if (guildname==null || guildname.equals(""))
    		return;
    	String[] value = {""+guildtotalpoints, ""+guildMemberpoints[0], guildname};
    	sql.psupdate("UPDATE `guilds` SET `total_points`=?, `leader_points`=? WHERE `Guildname`=?", value);
    	if (guildnum!=0){
    		value = new String[]{""+guildMemberpoints[guildnum], guildmembers[guildnum], guildname};
    		sql.psupdate("UPDATE `guildmembers` SET `points`=? WHERE `player`=? AND `guild`=?", value);
    	}
    }
    
    public void UpdateInvent()
    {
    	String[] value = new String[11];
    	for (int i = 0; i<10; i++)
    		value[i]=Integer.toString(this.inventitems[i]);
    	value[10]=this.botname;
        sql.psupdate("UPDATE `bout_inventory` SET `item1`=?, `item2`=?, `item3`=?, `item4`=?, `item5`=?, `item6`=?, `item7`=?, `item8`=?, `item9`=?, `item10`=? WHERE `name`=?", value);
    }
    
    public void UpdateBot()
    {
    	String[] value = {""+this.bottype,""+this.exp,""+this.level,""+this.hp,""+this.gigas,""+this.attmin,""+this.attmax,
    			""+this.attmintrans,""+this.attmaxtrans,""+this.spectrans,""+this.rangeatt,""+this.botstract,this.botname};
        sql.psupdate("UPDATE `bout_characters` SET `bot`=?, `exp`=?, `level`=?, `hp`=?, `gigas`=?, `attmin`=?, `attmax`=?,"+
    			" `attmintrans`=?, `attmaxtrans`=?, `specialtrans`=?, `rangeatt`=?, `botstract`=? WHERE name=?", value);
        value = new String[20];
        for (int i = 0; i<3; i++)
        	value[i]=""+this.equipitemspart[i];
		for (int i = 0; i<8; i++)
			value[3+i]=""+this.equipitemsgear[i];
		for (int i = 0; i<6; i++)
			value[i+11]=""+this.equipitemspack[i];
		for (int i = 0; i<2; i++)
			value[17+i]=""+this.equipitemscoin[i];
		value[19]=this.botname;
        sql.psupdate("UPDATE `bout_characters` SET `equiphead`=?, `equipbody`=?, `equiparm`=?, `equipminibot`=?, `equipgun`=?," +
        		" `equipefield`=?, `equipwing`=?, `equipshield`=?, `equiparmpart`=?, `equipflag1`=?, `equipflag2`=?, `equippassivskill`=?," +
        		" `equipaktivskill`=?, `equippack`=?, `equiptransbot`=?, `equipmerc`=?, `equipmerc2`=?, `equipheadcoin`=?, `equipminibotcoin`=? WHERE `name`=?", value);
    }
    
    public void UpdateCoins()
    {
    	String[] value= {""+this.coins,this.account};
        sql.psupdate("UPDATE `bout_users` SET `coins`=? WHERE username=?", value);
    }
    
    public void AddItemTime(int itemid, int slot, String sort, int time)
    {
    	if (time > 0){
        	String[] value = {this.botname,this.account, ""+time, sort+(slot+1), ""+itemid};
    		sql.psupdate("INSERT INTO `item_times` (`name`, `accountname`, `time`, `location`, `itemid`, `sort`)VALUES (?, ?, ?, ?, ?, '4')", value);
    	}
    	if (sort.equals("gear"))
    		slot+=3;
    	else if (sort.equals("pack"))
    		slot+=11;
    	else if (sort.equals("coin"))
    		slot+=17;
    	else if (sort.equals("item"))
    		slot+=19;
    	else if (sort.equals("stas"))
    		slot+=29;
    	itemduration[slot]=time;
    	itemdurationcur[slot]=0;
		itemdurationsort[slot]=4;
		itemdurationdate[slot]=new Timestamp(System.currentTimeMillis());
    }
    
    public void UpdateItemLocation(int itemid, String oldsort, String newsort, int oldslot, int newslot)
    {
    	if (oldslot>18 && oldslot<29 && itemdurationsort[newslot]==4){
    		String[] value = {""+itemid};
    		int sort = 1;
    		ResultSet rs = sql.psquery("SELECT * FROM `bout_items` WHERE `id`=?", value);
    		try{
        		if(rs.next())
        			sort=rs.getInt("duration");
    		rs.close();}catch (Exception e){debug(""+e);}
    		/*if (itemdurationsort[newslot] == 4){
        		if (newslot == 4 || newslot == 12 || newslot == 11)
        			sort=3;
        		if(newslot == 13)
        			sort=2;
    		}*/
    		itemdurationsort[newslot] = sort;
    		itemdurationdate[newslot] = new Timestamp(System.currentTimeMillis());
    		if (sort==1)
    			itemdurationcur[newslot]=itemduration[newslot]*24;
    		else
    			itemdurationcur[newslot]=itemduration[newslot];
    	}
    	String[] value = {""+itemdurationsort[newslot], newsort, ""+itemdurationdate[newslot], botname, oldsort, ""+itemid};
		sql.psupdate("UPDATE `item_times` SET `sort`=?, `location`=?, `date`=? WHERE `name`=? and `location`=? and `itemid`=? LIMIT 1", value);
    }
    
    public void UpdateItemUses(int used, int part)
    {
    	int itemid=0;
    	int slot=0;
    	String sort = "";
    	if (part<3){
    		sort="part"+(part+1);
    		itemid = this.equipitemspart[part];
    		slot=part;
    	}
    	else if (part<11){
    		sort="gear"+(part-2);
    		itemid = this.equipitemsgear[part-3];
    		slot=part-3;
    	}
    	else if (part<17){
    		sort="pack"+(part-10);
    		itemid = this.equipitemspack[part-11];
    		slot=part-11;
    	}
    	else if (part<19){
    		sort="coin"+(part-16);
    		itemid = this.equipitemscoin[part-17];
    		slot=part-17;
    	}
    	else if (part<29){
    		sort="item"+(part-18);
    		itemid = this.inventitems[part-19];
    		slot=part-19;
    	}
    	else if (part<79){
    		sort="stas"+(part-28);
    		itemid = this.stasheditems[part-29];
    		slot=part-29;
    	}
    	if (itemdurationcur[part]-1>0){
    		itemdurationcur[part]-=used;
    		itemduration[part]-=used;
    		String[] value = {""+itemdurationcur[part], botname, sort, ""+itemid};
    		sql.psupdate("UPDATE `item_times` SET `time`=? WHERE `name`=? and `location`=? and `itemid`=?", value);
    	}
    	else{
    		RemoveItemTime(itemid, slot, sort.substring(0, 4));
    	}
    }
    
    public void RemoveItemTime(int itemid, int slot, String sort)
    {
    	String sort2=sort+(slot+1);
    	String[] value = {this.botname, sort2, ""+itemid};
    	sql.psupdate("DELETE FROM `item_times` WHERE `name`=? AND `location`=? AND `itemid`=?", value);
    	int part = 0;
    	if (sort.equals("part")){
    		this.equipitemspart[slot]=0;
    		part=slot;
    		UpdateBot();
    	}
    	else if (sort.equals("gear")){
    		this.equipitemsgear[slot]=0;
    		part=slot+3;
    		UpdateBot();
    	}
    	else if (sort.equals("pack")){
    		this.equipitemspack[slot]=0;
    		part=slot+11;
    		UpdateBot();
    	}
    	else if (sort.equals("coin")){
    		this.equipitemscoin[slot]=0;
    		part=slot+17;
    		UpdateBot();
    	}
    	else if (sort.equals("item")){
    		this.inventitems[slot]=0;
    		part=slot+19;
    		UpdateInvent();
    	}
    	else if (sort.equals("stas")){
    		this.stasheditems[slot]=0;
    		part=slot+29;
    		UpdateStash((int)(slot/10));
    	}
    	itemduration[part]=0;
    	itemdurationsort[part]=0;
    	itemdurationcur[part]=0;
    	itemdurationdate[part]=null;
    }
    
    public void UpdateStash(int stash)
    {
    	String[] value = new String[12];
    	for (int i = 0; i<10; i++)
    		value[i]=""+(this.stasheditems[i+stash*10]);
		value[10]=""+(1+stash);
		value[11]=this.account;
		sql.psupdate("UPDATE `stashes` SET `stas1`=?, `stas2`=?, `stas3`=?, `stas4`=?, `stas5`=?, `stas6`=?, `stas7`=?, "+
		"`stas8`=?, `stas9`=?, `stas10`=? WHERE `stashnr`=? AND `name`=?", value);
    }
    
    public void BuyStash()
    {
    	int price = 0;
    	ResultSet rs = sql.psquery("SELECT * FROM `bout_items` WHERE `id`=? LIMIT 1", new String[]{""+5010300});
        try{
        	if (rs.next())
        		price = rs.getInt("coins");
        	rs.close();
        }catch (Exception e){debug("Error in buystash: "+e);}
    	if(coins-price<0 || stash>=5) {
            packet.addHeader((byte)0xEC, (byte)0x2E);
            packet.addByte2((byte)0x00, (byte)0x41);
            send(packet,false);
    		return;
    	}
    	stash++;
    	coins-=price;
    	String[] value = {account, "0","0","0","0","0","0","0","0","0","0", ""+stash};
    	sql.psupdate("INSERT INTO `stashes` (`name`, `stas1`, `stas2`, `stas3`, `stas4`, `stas5`, `stas6`, `stas7`, `stas8`, `stas9`, `stas10` , " + 
    			"`stashnr`)VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", value);
    	UpdateCoins();
    	packet.addHeader((byte)0x4C, (byte)0x2F);
    	packet.addInt(1, 2, false);
    	packet.addInt(stash, 2, false);
    	send(packet,false);
    }
    
    public void StashPacket(int stash)
    {
    	packet.addHeader((byte)0x4D, (byte)0x2F);
    	packet.addByte2((byte)0x01, (byte)0x00);
    	packet.addByte((byte)0x00);
    	for (int i = 19; i<29; i++)
        {
            packet.addInt(this.inventitems[i-19], 4, false);
            packet.addInt((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
            packet.addByte((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);
        }
        packet.addByte((byte)stash);
        for (int i = 29+10*stash; i<79; i++)
        {
            packet.addInt(this.stasheditems[i-29], 4, false);
            packet.addInt((this.stasheditems[i-29] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
            packet.addByte((this.stasheditems[i-29] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);            	
        }
        send(packet,false);
    }
    
    public void MoveToStash(int stash, int slot,  boolean out)
    {
    	String location = "";
    	String locationnew = "";
    	int itemslot = -1;
    	int itemid = 0;
    	if (out){
    		for(int i = 0; i<10; i++)
    			if(this.inventitems[i]==0){
    				itemslot=i;
    				break;
    			}
    		inventitems[itemslot]=itemid=this.stasheditems[slot+stash*10];
    		this.stasheditems[slot+stash*10]=0;
    		locationnew="stas"+(slot+stash*10+1);
    		location="item"+(itemslot+1);
    	}
    	else{
    		for(int i = 0; i<10; i++)
    			if(this.stasheditems[i+stash*10]==0){
    				itemslot=i;
    				break;
    			}
    		itemid=this.stasheditems[itemslot+stash*10]=inventitems[slot];
    		inventitems[slot]=0;
    		locationnew="item"+(slot+1);
    		location="stas"+(itemslot+stash*10+1);
    	}
    	int tempdur = itemduration[29+10*stash + (out ? slot : itemslot)];
        int tempcur = itemdurationcur[29+10*stash + (out ? slot : itemslot)];
        Timestamp tempdate = itemdurationdate[29+10*stash + (out ? slot : itemslot)];
        int tempsort = itemdurationsort[29+10*stash + (out ? slot : itemslot)];
        itemduration[29+10*stash + (out ? slot : itemslot)] = itemduration[19 + (!out ? slot : itemslot)];
        itemdurationcur[29+10*stash + (out ? slot : itemslot)] = itemdurationcur[19 + (!out ? slot : itemslot)];
        itemdurationdate[29+10*stash + (out ? slot : itemslot)] = itemdurationdate[19 + (!out ? slot : itemslot)];
        itemdurationsort[29+10*stash + (out ? slot : itemslot)] = itemdurationsort[19 + (!out ? slot : itemslot)];
        itemduration[19 + (!out ? slot : itemslot)]=tempdur;
        itemdurationcur[19 + (!out ? slot : itemslot)]=tempcur;
        itemdurationdate[19 + (!out ? slot : itemslot)]=tempdate;
        itemdurationsort[19 + (!out ? slot : itemslot)]=tempsort;
		String[] value = {location, botname, ""+itemid, locationnew};
    	sql.psupdate("UPDATE `item_times` SET `location`=? WHERE `name`=? AND `itemid`=? AND `location`=?", value);
    	UpdateStash(stash);
    	UpdateInvent();
    	StashPacket(stash);
    }
    
    public void TransCoupon(int botchange)
    {
    	int a = -1;
    	for (int i = 0; i<10; i++){
    		if(this.inventitems[i]==5010200){
    			this.inventitems[i]=0;
    			RemoveItemTime(5010200, i, "item");
    			a=i;
    			break;
    		}
    	}
    	if(a==-1)
    		return;
    	int numchange=0;
    	numchange=botchange-this.bottype;
    	this.bottype=botchange;
    	for(int i = 0; i<3; i++){
    		int changeto=this.equipitemspart[i]+numchange*100000;
    		String[] value = {""+changeto};
    		ResultSet rs = sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", value);
    		try{
				boolean found = false;
    			if(rs.next()){
    				this.equipitemspart[i]=changeto;
    				found=true;
    			}
	    		else {
	    			for(int b=0; b<10; b++){
	    				if(this.inventitems[b]==0){
	    					this.inventitems[b]=this.equipitemspart[i];
	    					this.equipitemspart[i]=0;
	    					found=true;
	    					break;
	    				}
	    			}
    			}
    			if(!found && this.equipitemspart[i]!=0){
					long time = System.currentTimeMillis();
			    	java.sql.Timestamp timestamp = new java.sql.Timestamp(time);
					value = new String[]{"Server", botname, "Transcoupon sendback", ""+timestamp, "0", ""+this.equipitemspart[i]};
					sql.psupdate("INSERT INTO `gifts` (`from`, `to`, `message`, `date`, `sort`, `gift`)VALUES (?, ?, ?, ?, ?, ?)", value);
				}
    		rs.close();}catch (Exception e){debug(""+e);}
    	}
    	UpdateBot();
    	loadEquipBonus();
    	UpdateInvent();
    	InventPacket(0xEB);
    	CharPacket(new byte[]{(byte) 0xE4, (byte) 0x2E});
    	PopUp(false, "You have successfully been transformed to ("+(botchange==1 ? "patch" : botchange==2 ? "surge" : "ram")+").");
    }
    
    public void CombiCoupon(int card, int itemslot1, int itemslot2, int itemslot3)
    {
    	if(this.inventitems[card]==5041200)
    	{
    		if(this.inventitems[itemslot1]==this.inventitems[itemslot2] && this.inventitems[itemslot2]==this.inventitems[itemslot3])
    		{
    			String[] value = {""+(this.inventitems[itemslot1]+1)};
    	    	ResultSet rs = sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", value);
    	        try{
    	        	if (rs.next()){
    	    			this.inventitems[itemslot1]++;
    	    			this.inventitems[card]=0;
    	    			this.inventitems[itemslot2]=0;
    	    			this.inventitems[itemslot3]=0;
    	        	}
    	        rs.close();}catch (Exception e){}
    	        UpdateInvent();
    	        InventPacket(0xEB);
    	        CharPacket(new byte[]{(byte)0xE4, (byte)0x2E});
    		}
    	}
    }
    
    public void deEquipItem(int head, int slot)
    {
    	byte[] header = null;
    	if(head==1)
        	header= new byte[]{(byte) 0xE5, (byte) 0x2E};
        if(head==2)
        	header= new byte[]{(byte) 0x1A, (byte) 0x2F};
        if(head==3)
        	header= new byte[]{(byte) 0x1C, (byte) 0x2F};
        if (head == 1 && slot == 0 && this.equipitemscoin[0] != 0)
            head = 4;
        else if (head == 2 && slot == 0 && this.equipitemscoin[1] != 0){
        	head = 4;
        	slot = 1;
        }
        int newslot = -1;
        for (int i = 0; i<10; i++){
        	if (this.inventitems[i]==0){
        		newslot=i;
        		break;
        	}
        }
        if (newslot==-1)
        {
    		packet.addHeader(header[0], header[1]);
    		packet.addByte2((byte) 0x00, (byte) 0x61);
    		send(packet,false);
        	return;
        }
        int equip = 0;
        int oldslot = 0;
        String oldsort = "";
        if(head==1){
        	equip = this.equipitemspart[slot];
        	if(equip == 0){
        		packet.addHeader(header[0], header[1]);
        		packet.addByte2((byte) 0x00, (byte) 0x60);
        		send(packet,false);
        		return;
        	}
        	oldslot=slot;
        	oldsort = "part"+(slot+1);
        	this.equipitemspart[slot]=0;
        	this.inventitems[newslot]=equip;
        }
        if(head==2){
        	equip = this.equipitemsgear[slot];
        	if(equip == 0){
        		packet.addHeader(header[0], header[1]);
        		packet.addByte2((byte) 0x00, (byte) 0x60);
        		send(packet,false);
        		return;
        	}
        	oldslot=slot+3;
        	oldsort = "gear"+(slot+1);
        	this.equipitemsgear[slot]=0;
        	this.inventitems[newslot]=equip;
        }
        if(head==3){
        	equip = this.equipitemspack[slot];
        	if(equip == 0){
        		packet.addHeader(header[0], header[1]);
        		packet.addByte2((byte) 0x00, (byte) 0x60);
        		send(packet,false);
        		return;
        	}
        	oldslot=slot+11;
        	oldsort = "pack"+(slot+1);
        	this.equipitemspack[slot]=0;
        	this.inventitems[newslot]=equip;
        }
        if(head==4){
        	equip = this.equipitemscoin[slot];
        	if(equip == 0){
        		packet.addHeader(header[0], header[1]);
        		packet.addByte2((byte) 0x00, (byte) 0x60);
        		send(packet,false);
        		return;
        	}
        	oldslot=slot+17;
        	oldsort = "coin"+(slot+1);
        	this.equipitemscoin[slot]=0;
        	this.inventitems[newslot]=equip;
        }
        itemduration[newslot+19] = itemduration[oldslot];
        itemdurationcur[newslot+19] = itemdurationcur[oldslot];
        itemdurationsort[newslot+19] = itemdurationsort[oldslot];
        itemdurationdate[newslot+19] = itemdurationdate[oldslot];
        itemduration[oldslot]=0;
        itemdurationcur[oldslot]=0;
        itemdurationsort[oldslot]=0;
        itemdurationdate[oldslot]=null;
        loadEquipBonus();
        UpdateItemLocation(equip, oldsort, "item"+(newslot+1), oldslot, newslot+19);
        UpdateInvent();
        UpdateBot();
        CharPacket(header);
    }
    
    public void RequestTrade(String player)
    {
        packet.addHeader((byte)0x53, (byte)0x2B);
        packet.addByte2((byte)0x0C, (byte)0x00);
        packet.addString(botname);
        for (int i = botname.length(); i<15; i++)
        	packet.addByte((byte)0x00);
        packet.addString(player);
        for (int i = player.length(); i<15; i++)
        	packet.addByte((byte)0x00);
        int num = getNum(player);
        if ((this.gm!=0 && this.gm!=255 && this.gm!=-1) || (lobby.bots[num].gm!=0 && lobby.bots[num].gm!=255 && lobby.bots[num].gm!=-1))
        	return;
        player=player.replaceAll("\\s+","");
        player=player.replaceAll("(?i)mod","Mod ");
    	player=player.replaceAll("(?i)gm","Gm ");
    	player=player.replaceAll("(?i)admin","Admin ");
    	if (num!=-1 && !player.equalsIgnoreCase(botname) && lobby.bots[num].lobbyroomnum==-1)
    		lobby.bots[num].send(packet,false);
    }
    
    public void TradeRequestAccept(String player, int accept)
    {
        packet.addHeader((byte)0x39, (byte)0x2F);
        packet.addInt(accept, 2, false);
        packet.addByte2((byte)0x01, (byte)0x00);
        packet.addString(botname);
        for (int i = botname.length(); i<15; i++)
        	packet.addByte((byte)0x00);
        packet.addString(player);
        for (int i = player.length(); i<15; i++)
        	packet.addByte((byte)0x00);
        trade[0] = getNum(player);
        if (trade[0]!=-1 && !player.equalsIgnoreCase(botname) && lobby.bots[trade[0]].lobbyroomnum==-1){
        	lobby.bots[trade[0]].trade[0]=lobbynum;
    		lobby.bots[trade[0]].send(packet,false);
        }
    }
    
    public void TradeMessage(String message, String name)
    {
        packet.addHeader((byte)0x37, (byte)0x27);
        boolean nuse=name!=null && !name.equals("");
        packet.addString(nuse ? name : botname);
        for (int i = (nuse ? name.length() : botname.length()); i<15; i++)
        	packet.addByte((byte)0x00);
        packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        packet.addByte2((byte)0x37, (byte)0x00);
        packet.addString(message);
        for (int i = message.length(); i<128; i++)
        	packet.addByte((byte)0x00);
        lobby.bots[trade[0]].send(packet,true);
        send(packet,false);
    }
    
    public void TradeItems(int[] item, int gigas, int botstract)
    {
    	try {
    	for (int i = 0; i<3; i++)
    		trade[i+1]=item[i]>=0 && item[i]<=10 && trade[6]!=1 ? item[i] : -1;
    	trade[4]=gigas<=this.gigas ? gigas : 0;
    	trade[5]=botstract<=this.botstract ? botstract : 0;
    	if (trade[6]==0)
    		trade[6]=1;
    	else {
    		trade[6]=0;
	    	lobby.bots[trade[0]].trade[6]=0;
	    	for (int i = 0; i<3; i++)
	    		lobby.bots[trade[0]].trade[i+1]=-1;
    	}
    	TradeItemPacket();
    	TradePlayerStates(new int[]{-1, trade[0]});
    	lobby.bots[trade[0]].TradeItemPacket();
    	TradePlayerStates(new int[]{trade[0], -1});
    	}catch(Exception e) {debug("Error sending items "+e);}
    }
    
    public void TradeItemPacket()
    {
    	packet.addHeader((byte)0x35, (byte)0x27);
    	for (int i = 1; i<4; i++){
    		if (trade[i]!=-1){
    		packet.addInt(inventitems[trade[i]],4,false);
    		packet.addInt(itemdurationcur[19+trade[i]], 4, false);
    		packet.addByte((byte)itemdurationsort[19+trade[i]]);
    		}
    		else
    			packet.addByteArray(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00});
    	}
    	packet.addInt(trade[4], 4, false);
    	packet.addInt(trade[5], 4, false);
    	for (int i = 1; i<4; i++){
    		if (lobby.bots[trade[0]].trade[i]!=-1){
    		packet.addInt(lobby.bots[trade[0]].inventitems[lobby.bots[trade[0]].trade[i]],4,false);
    		packet.addInt(lobby.bots[trade[0]].itemdurationcur[19+lobby.bots[trade[0]].trade[i]], 4, false);
    		packet.addByte((byte)lobby.bots[trade[0]].itemdurationsort[19+lobby.bots[trade[0]].trade[i]]);
    		}
    		else
    			packet.addByteArray(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00});
    	}
    	packet.addInt(lobby.bots[trade[0]].trade[4], 4, false);
    	packet.addInt(lobby.bots[trade[0]].trade[5], 4, false);
    	send(packet,false);
    }
    
    public void TradePlayerStates(int[] num)
    {
    	packet.addHeader((byte)0x36, (byte)0x27);
    	if((num[0]==-1 ? trade[6] : lobby.bots[num[0]].trade[6])==2)
    		packet.addByte2((byte)0x01, (byte)0x01);
    	else if((num[0]==-1 ? trade[6] : lobby.bots[num[0]].trade[6])==1)
    		packet.addByte2((byte)0x01, (byte)0x00);
    	else
    		packet.addByte2((byte)0x00, (byte)0x00);
    	if((num[1]==-1 ? trade[6] : lobby.bots[num[1]].trade[6])==2)
    		packet.addByte2((byte)0x01, (byte)0x01);
    	else if((num[1]==-1 ? trade[6] : lobby.bots[num[1]].trade[6])==1)
    		packet.addByte2((byte)0x01, (byte)0x00);
    	else
    		packet.addByte2((byte)0x00, (byte)0x00);
    	if (num[0]==-1)
    		send(packet,false);
    	else
    		lobby.bots[num[0]].send(packet,false);
    }
    
    public void TradeAccept()
    {
    	if (trade[6]==2){
    		 lobby.bots[trade[0]].trade=new int[] {lobbynum,-1,-1,-1,0,0,0};
    		 trade=new int[] {trade[0],-1,-1,-1,0,0,0};
    		 TradePlayerStates(new int[]{-1, trade[0]});
    		 TradePlayerStates(new int[]{trade[0], -1});
    		 return;
    	}
    	if (lobby.bots[trade[0]].trade[6]==1){
    		trade[6]=2;
    		TradePlayerStates(new int[]{-1, trade[0]});
   		 	TradePlayerStates(new int[]{trade[0], -1});
    		return;
    	}
    	if (lobby.bots[trade[0]].trade[6]==2){
    		int[][] nums = new int[2][5];
    		int nr1 = 0, nr2 = 0;
    		for (int i = 0; i<10; i++) {
    			if (nr1<4 && inventitems[i]==0)
    				nums[0][nr1+=1]=i;
    			if (nr2<4 && lobby.bots[trade[0]].inventitems[i]==0)
    				nums[1][nr2+=1]=i;
    		}
    		for (int i = 0; i<3; i++) {
    			if (nr1<4 && trade[i+1]!=-1) 
    				nums[0][nr1+=1]=trade[i+1];
    			if (nr2<4 && lobby.bots[trade[0]].trade[i+1]!=-1)
    				nums[1][nr2+=1]=lobby.bots[trade[0]].trade[i+1];
    		}
    		for (int i = 1; i<4; i++){
    			if (trade[i]!=-1)
    				nr2--;
    			if (lobby.bots[trade[0]].trade[i]!=-1)
    				nr1--;
    		}
    		if (nr1<0 || nr2<0){
    			trade=new int[] {trade[0],-1,-1,-1,0,0,0};//trade[7]=-1;
    			lobby.bots[trade[0]].trade=new int[] {lobbynum,-1,-1,-1,0,0,0};//lobby.bots[trade[0]].trade[7]=-1;
    			TradePlayerStates(new int[]{-1, trade[0]});
            	TradePlayerStates(new int[]{trade[0], -1});
    			TradeMessage("Something went wrong! Check how many inventory spaces you have available.", "SERVER");
    			return;
    		}
    		Timestamp[][] temp2=new Timestamp[2][3];
    		int[][] temp=new int[2][3], temp1=new int[2][3], temp3=new int[2][3], temp4=new int[2][3];
    		temp4[0]= new int[]{-1,-1,-1};
    		temp4[1]= new int[]{-1,-1,-1};
    		try {
	    		for (int i = 1; i<4; i++)
	    			if (lobby.bots[trade[0]].trade[i]!=-1){
	    				temp4[0][i-1]=lobby.bots[trade[0]].inventitems[lobby.bots[trade[0]].trade[i]];
	    				temp[0][i-1]=lobby.bots[trade[0]].itemduration[19+lobby.bots[trade[0]].trade[i]];
	    				temp1[0][i-1]=lobby.bots[trade[0]].itemdurationcur[19+lobby.bots[trade[0]].trade[i]];
	    				temp2[0][i-1]=lobby.bots[trade[0]].itemdurationdate[19+lobby.bots[trade[0]].trade[i]];
	    				temp3[0][i-1]=lobby.bots[trade[0]].itemdurationsort[19+lobby.bots[trade[0]].trade[i]];
	    				lobby.bots[trade[0]].inventitems[lobby.bots[trade[0]].trade[i]]=0;
	    				lobby.bots[trade[0]].itemduration[19+lobby.bots[trade[0]].trade[i]]=0;
	    				lobby.bots[trade[0]].itemdurationcur[19+lobby.bots[trade[0]].trade[i]]=0;
	    				lobby.bots[trade[0]].itemdurationdate[19+lobby.bots[trade[0]].trade[i]]=null;
	    				lobby.bots[trade[0]].itemdurationsort[19+lobby.bots[trade[0]].trade[i]]=0;
	    			}
	    		gigas+=lobby.bots[trade[0]].trade[4];
	    		botstract+=lobby.bots[trade[0]].trade[5];
	    		gigas-=trade[4];
	    		botstract-=trade[5];
	    		for (int i = 1; i<4; i++)
	    			if (trade[i]!=-1){
	    				temp4[1][i-1]=inventitems[trade[i]];
	    				temp[1][i-1]=itemduration[19+trade[i]];
	    				temp1[1][i-1]=itemdurationcur[19+trade[i]];
	    				temp2[1][i-1]=itemdurationdate[19+trade[i]];
	    				temp3[1][i-1]=itemdurationsort[19+trade[i]];
	    				inventitems[trade[i]]=0;
	    				itemduration[19+trade[i]]=0;
	    				itemdurationcur[19+trade[i]]=0;
	    				itemdurationdate[19+trade[i]]=null;
	    				itemdurationsort[19+trade[i]]=0;
	    			}
	    		for (int i = 1; i<4; i++) {
	    			if (temp4[0][i-1]!=-1){
	    				inventitems[nums[0][i]]=temp4[0][i-1];
	    				itemduration[19+nums[0][i]]=temp[0][i-1];
	    				itemdurationcur[19+nums[0][i]]=temp1[0][i-1];
	    				itemdurationdate[19+nums[0][i]]=temp2[0][i-1];
	    				itemdurationsort[19+nums[0][i]]=temp3[0][i-1];
	    			} if (temp4[1][i-1]!=-1) {
	    				lobby.bots[trade[0]].inventitems[nums[1][i]]=temp4[1][i-1];
	    				lobby.bots[trade[0]].itemduration[19+nums[1][i]]=temp[1][i-1];
	    				lobby.bots[trade[0]].itemdurationcur[19+nums[1][i]]=temp1[1][i-1];
	    				lobby.bots[trade[0]].itemdurationdate[19+nums[1][i]]=temp2[1][i-1];
	    				lobby.bots[trade[0]].itemdurationsort[19+nums[1][i]]=temp3[1][i-1];
	    			}
	    		}	
	    		lobby.bots[trade[0]].gigas+=trade[4];
	    		lobby.bots[trade[0]].botstract+=trade[5];
	    		lobby.bots[trade[0]].gigas-=lobby.bots[trade[0]].trade[4];
	    		lobby.bots[trade[0]].botstract-=lobby.bots[trade[0]].trade[5];
    		}catch(Exception e){debug("failed replacing traded items "+e);}
    		try {
	    		for (int i = 1; i<4; i++)
	    			if (trade[i]!=-1){
		    			String[] value = {lobby.bots[trade[0]].botname,"item"+(nums[0][i]+1),"item"+(trade[i]+1),botname};
		    			sql.psupdate("UPDATE `item_times` SET `name`=?, `location`=? WHERE `location`=? AND `name`=?", value);
		    		}
    		}catch (Exception e){debug("Error while trading: "+e);}
    		try {
    		for (int i = 1; i<4; i++)
	    			if (lobby.bots[trade[0]].trade[i]!=-1){
		    			String[] value = {botname,"item"+(nums[0][i]+1),"item"+(lobby.bots[trade[0]].trade[i]+1),lobby.bots[trade[0]].botname};
		    			sql.psupdate("UPDATE `item_times` SET `name`=?, `location`=? WHERE `location`=? AND `name`=?", value);
		    		}
    		}catch (Exception e){debug("Error while trading: "+e);}
    		UpdateInvent();
    		lobby.bots[trade[0]].UpdateInvent();
    		UpdateBot();
    		lobby.bots[trade[0]].UpdateBot();
    		TradePacket();
    		lobby.bots[trade[0]].TradePacket();
        	trade=new int[] {trade[0],-1,-1,-1,0,0,0};//trade[7]=-1;
        	lobby.bots[trade[0]].trade=new int[] {lobbynum,-1,-1,-1,0,0,0};//lobby.bots[trade[0]].trade[7]=-1;
    		TradePlayerStates(new int[]{-1, trade[0]});
        	TradePlayerStates(new int[]{trade[0], -1});
    	}
    }
    
    public void TradePacket()
    {
        packet.addHeader((byte)0x33, (byte)0x27);
        packet.addPacketHead((byte)0x01, (byte)0x00);
        for (int i = 19; i<29; i++)
        {
            packet.addInt(this.inventitems[i-19], 4, false);
            packet.addInt((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
            packet.addByte((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);
        }
        packet.addInt(gigas, 4, false);
    	packet.addInt(botstract, 4, false);
    	send(packet,false);
    }
    
    public void ExitTrade()
    {
        packet.addHeader((byte)0x34, (byte)0x27);
        packet.addString(botname);
        for (int i = botname.length(); i<15; i++)
        	packet.addByte((byte)0x00);
        packet.addByte2((byte)0x88, (byte)0x00);
        for (int i = 0; i<15; i++)
        	packet.addByte((byte)0x00);
        lobby.bots[trade[0]].send(packet,true);
        trade=new int[] {0,-1,-1,-1,0,0,0};
        send(packet,false);
    }
    
    public void MessagePacket()
    {
    	int num=0;
    	Gifter= new String[20];
    	Giftdate= new String[20];
        packet.addHeader((byte)0x10, (byte)0x2F);
        packet.addPacketHead((byte)0x01, (byte)0x00);
        String[] value = {botname};
        ResultSet rs = sql.psquery("SELECT * FROM `gifts` WHERE `to`=? and `sort`='3' LIMIT 20", value);
        try{
        	while (rs.next()){
        		Gifter[num] = rs.getString("from");
        		Giftdate[num] = rs.getString("date");
        		GiftMsg[num] = rs.getString("message");
        		num++;
        }rs.close();} catch (Exception e){}
        packet.addByte2((byte)(num>18 ? 18 : num), (byte)0x00);
        for (int i = 0; i<20; i++)
        	if (Gifter[i]!=null){
        		packet.addByte4((byte)(i+1), (byte)0x00, (byte)0x00, (byte)0x00);
        		packet.addString(Gifter[i]);
        		for (int b = (Gifter[i].length()); b<15; b++)
        			packet.addByte((byte)0x00);
        		packet.addString(Giftdate[i].substring(0, 16));
        		packet.addByte((byte)0x00);
        	}
        	else
        		for (int a = 0; a<9; a++)
        			packet.addByte4((byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00);
        send(packet,false);
    }
    
    public void MessageReadPacket(int num)
    {
    	packet.addHeader((byte)0x12, (byte)0x2F);
        packet.addPacketHead((byte)0x01, (byte)0x00);
        packet.addString(Gifter[num]);
        for (int i = (Gifter[num].length()); i<15; i++)
        	packet.addByte((byte)0x00);
        packet.addString(GiftMsg[num]);
        for (int i = (GiftMsg[num].length()); i<15; i++)
        	packet.addByte((byte)0x00);
    	packet.addString(Giftdate[num].substring(0, 16));
    	packet.addByte((byte)0x00);
    	send(packet,false);
    }
    
    public void messagesend(String recepient, String message)
    {
    	boolean excist=false;
    	ResultSet rs = sql.psquery("SELECT * FROM `bout_characters` WHERE `name`=?", new String[]{recepient});
    	try{
    		if (rs.next())
    			excist=true;
    	rs.close();}catch (Exception e){}
    	recepient = recepient.replaceAll("\\s+","");
    	recepient=recepient.replaceAll("(?i)mod","Mod ");
    	recepient=recepient.replaceAll("(?i)gm","Gm ");
    	recepient=recepient.replaceAll("(?i)admin","Admin ");
    	if(excist && !botname.equalsIgnoreCase(recepient)){
    		String[] value = {botname, recepient, message, ""+new java.sql.Timestamp(System.currentTimeMillis())};
    		sql.psupdate("INSERT INTO `gifts` (`from`, `to`, `message`, `date`, `sort`)VALUES (?, ?, ?, ?, '3')", value);
    	}
    }
    
    public void DeleteMessage(int nr)
    {
    	String[] value = {botname, Gifter[nr], GiftMsg[nr]};
        sql.psupdate("DELETE FROM `gifts` WHERE `to`=? and `from`=? and `message`=?", value);
    	MessagePacket();
    }
    public void gifts()
    {
    	Gifter = new String[20];
    	Giftdate = new String[20];
    	GiftMsg = new String[20];
    	gift = new int[20];
    	Giftkind = new int[20];
    	int[] sort = new int[20];
    	int num = 0;
        ResultSet rs = sql.psquery("SELECT * FROM `gifts` WHERE `to`=? and `sort` NOT LIKE '3' LIMIT 18", new String[]{botname});
        try{
        while (rs.next())
        	if((Giftkind[num] = rs.getInt("sort"))!=3){
            	Gifter[num] = rs.getString("from");
            	Giftdate[num] = rs.getString("date");
            	GiftMsg[num] = rs.getString("message");
            	gift[num] = rs.getInt("gift");
            	num++;
            }
        rs.close();
        } catch (Exception e){}
        for (int i = 0; i<18; i++)
        	if (Giftkind[i]==0 && gift[i]!=0){
        		String[] value = new String[]{botname, "gift1", ""+gift[num]};
        		rs = sql.psquery("SELECT * FROM `item_times` WHERE `name`=? and `location`=? and `itemid`=?", value);
        		try{
        			if(rs.next()){
        				sort[i]=rs.getInt("sort");
        }rs.close();}catch (Exception e){}}
        packet.addHeader((byte)0x15, (byte)0x2F);
        packet.addPacketHead((byte)0x01, (byte)0x00);
        for (int i = 0; i<18; i++)// && !name2[nr].equals("")
        	if (Gifter[i]!=null){
        		packet.addString(Gifter[i]);
        		for (int a = Gifter[i].length(); a<15; a++)
        			packet.addByte((byte)0x00);
        		packet.addString(GiftMsg[i]);
        		for (int a = GiftMsg[i].length(); a<31; a++)
        			packet.addByte((byte)0x00);
        		packet.addInt(Giftkind[i]==1 ? gift[i] : 0, 4, false);
        		packet.addInt(Giftkind[i]==0 ? gift[i] : 0, 4, false);
        		packet.addInt(0, 4, false);
        		packet.addByte((byte)(Giftkind[i]==0 ? sort[i] : 0x00));
        		packet.addInt(Giftkind[i]==2 ? gift[i] : 0, 4, false);
        		for (int a = 0; a<3; a++)
        			packet.addInt(0, 4, false);
        		packet.addString(Giftdate[i].substring(0, 4)+Giftdate[i].substring(5, 7)+
            			Giftdate[i].substring(8, 10)+Giftdate[i].substring(11, 13)+Giftdate[i].substring(14, 16));
            	packet.addByte((byte)0x00);
        	}
        	else
        		for (int c = 0; c<21; c++)
            		packet.addInt(0, 4, false);
        send(packet,false);
    }
    
    public void SendGift(String to, String message, int kind, int gift)
    {
    	if ((this.gm!=0 && this.gm!=255 && this.gm!=-1) || (gift==0 && kind !=0))
    		return;
    	boolean excist=false;
    	String username="";
    	ResultSet rs = sql.psquery("SELECT * FROM `bout_characters` WHERE `name`=?", new String[]{to});
    	try{
    		if (rs.next()) {
    			excist=true;
    			username=rs.getString("username");
    		}
    		rs.close();}catch (Exception e){}
    	to=to.replaceAll("\\s+","");
    	to=to.replaceAll("(?i)mod","Mod ");
    	to=to.replaceAll("(?i)gm","Gm ");
    	to=to.replaceAll("(?i)admin","Admin ");
    	if(excist && !botname.equalsIgnoreCase(to)){
    		int[] giftitem = new int[2];
    		gigas-=(kind == 1 ? (gift<gigas ? gift : (gift=0)) : 0);
    		botstract-=(kind == 2 ? (gift<botstract ? gift : (gift=0)) : 0);
    		giftitem[0]=kind == 0 ? inventitems[gift] : 0;
    		giftitem[1]=kind == 0 ? itemdurationcur[19+gift] : 0;
    		if (kind == 0)
    			inventitems[gift]=itemduration[gift+19]=itemdurationsort[gift+19]=itemdurationcur[gift+19]=0;
    		if (giftitem[1]!=0){
    			String[] value = {to,"gift1",username,"item"+(gift+1), botname};
    			sql.psupdate("UPDATE `item_times` SET `name`=?, `location`=?, `accountname`=? WHERE `location`=? AND `name`=?", value);
    		}
    		gift=kind==0 ? giftitem[0] : gift;
    		String[] value = {botname,to,message,""+new java.sql.Timestamp(System.currentTimeMillis()),""+kind,""+gift};
    		sql.psupdate("INSERT INTO `gifts` (`from`,`to`,`message`,`date`,`sort`,`gift`)VALUES (?,?,?,?,?,?)", value);
    	}
    	else
    		return;
    	UpdateBot();
    	UpdateInvent();
    	StractPacket();
    	int num = getNum(to);
    	if (!excist && botname.equalsIgnoreCase(to) && num!=-1)
    		sendChatMsg("[Server] "+botname+" has sent you a gift.", 2, false, num);
    }
    
    public void ShopGift(String to, String message, int kind, int gift)
    {
    	if ((this.gm!=0 && this.gm!=255 && this.gm!=-1) || (gift==0 && kind !=0))
    		return;
    	boolean excist=false;
    	String username="";
    	to=to.replaceAll("\\s+","");
    	to=to.replaceAll("(?i)mod","Mod ");
    	to=to.replaceAll("(?i)gm","Gm ");
    	to=to.replaceAll("(?i)admin","Admin ");
    	ResultSet rs = sql.psquery("SELECT * FROM `bout_characters` WHERE `name`=?", new String[]{to});
    	try{
    		if (rs.next()) {
    			excist=true;
    			username=rs.getString("username");
    		}
    		rs.close();}catch (Exception e){}
    	if (!excist)
    		return;
    	int time=0, pricec=0, priceg=0;
    	rs = sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", new String[]{""+gift});
        try{
        	if (rs.next()){
        		if(rs.getInt("buyable")==1)
        		{
        			time = rs.getInt("days");
        			pricec = rs.getInt("coins");
        			priceg = rs.getInt("buy");
        		}
        		else
        			pricec=-1;
        	}
        	rs.close();
        }catch (Exception e){}
    	if(pricec==-1)
        	return;
        if (this.coins < pricec || this.gigas < priceg)
        	return;
        this.coins-=pricec;
        this.gigas-=priceg;
    	String[] value = {to,username, ""+time, "gift1", ""+gift};
    	if (time>0)
    		sql.psupdate("INSERT INTO `item_times` (`name`, `accountname`, `time`, `location`, `itemid`, `sort`)VALUES (?,?,?,?,?,'4')", value);
    	value = new String[] {botname,to,message,""+new java.sql.Timestamp(System.currentTimeMillis()),""+kind,""+gift};
		sql.psupdate("INSERT INTO `gifts` (`from`,`to`,`message`,`date`,`sort`,`gift`)VALUES (?,?,?,?,?,?)", value);
		UpdateBot();
		UpdateCoins();
		CoinPacket();
    }
    
    public void ReceiveGift(int num)
    {
    	int nr = -1;
    	if (Giftkind[num]==0){
        	for(int i = 0; i<10; i++)
        		if(inventitems[i]==0){
        			nr=i;
        			break;
        		}
        	if(nr!=-1){
        		inventitems[nr]=gift[num];
        		String[] value = {botname, Gifter[num], GiftMsg[num], ""+gift[num]};
        		sql.psupdate("DELETE FROM `gifts` WHERE `to`=? and `from`=? and `message`=? and `gift`=? LIMIT 1", value);
        		value = new String[]{"item"+(nr+1), botname, "gift1", ""+gift[num]};
                sql.psupdate("UPDATE `item_times` SET `location`=? WHERE `name`=? and `location`=? and `itemid`=? LIMIT 1", value);
                value = new String[]{botname, "item"+(nr+1), ""+gift[num]};
                ResultSet rs = sql.psquery("SELECT * FROM `item_times` WHERE `name`=? and `location`=? and `itemid`=?", value);
        		try{
                if(rs.next()){
                	itemdurationcur[nr+19]=itemduration[nr+19]=rs.getInt("time");
        			itemdurationsort[nr+19]=rs.getInt("sort");
        			itemdurationdate[nr+19]=rs.getTimestamp("date");
        		}rs.close();}catch (Exception e){}
        		UpdateBot();
        	}
        }
        if (Giftkind[num]==1){
        	gigas+=gift[num];
        	String[] value = {botname, Gifter[num], GiftMsg[num], ""+gift[num]};
    		sql.psupdate("DELETE FROM `gifts` WHERE `to`=? and `from`=? and `message`=? and `gift`=? LIMIT 1", value);
    		UpdateBot();
    		StractPacket();
    	}
        if (Giftkind[num]==2){
        	botstract+=gift[num];
        	String[] value = {botname, Gifter[num], GiftMsg[num], ""+gift[num]};
    		sql.psupdate("DELETE FROM `gifts` WHERE `to`=? and `from`=? and `message`=? and `gift`=? LIMIT 1", value);
    		UpdateBot();
    		StractPacket();
        }
        gifts();
    }
    
    public void PopUpFixed(byte[] info, int num)
    {
    	packet.addHeader((byte) 0x28, (byte) 0x2F);
    	packet.addByteArray(info);
    	if (num==-1)
    		send(packet,false);
    	else
    		lobby.bots[num].send(packet,false);
    }
    
    public void StractPacket()
    {
    	packet.addHeader((byte)0x16, (byte)0x2F);
    	packet.addByte2((byte)0x01, (byte)0x00);
    	packet.addInt(gigas, 4, false);
    	packet.addInt(botstract, 4, false);
    	for (int i = 0; i<3; i++)
    		packet.addInt(0, 4, false);
    	packet.addByte((byte)0x00);
    	for (int i = 19; i<29; i++)
        {
            packet.addInt(this.inventitems[i-19], 4, false);
            packet.addInt((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
            packet.addByte((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);
        }
    	send(packet,false);
    }
    
    public void EquipItem(int head, int slot)
    {
    	byte[] header = null;
        if(head==1)
        	header= new byte[]{(byte) 0xE4, (byte) 0x2E};
        if(head==2)
        	header= new byte[]{(byte) 0x19, (byte) 0x2F};
        if(head==3)
        	header= new byte[]{(byte) 0x1B, (byte) 0x2F};
        if(this.inventitems[slot]==0){
        	packet.addHeader(header[0], header[1]);
        	packet.addByte2((byte) 0x00, (byte) 0x60);
        	send(packet,false);
        	return;
        }
        int theEquip=this.inventitems[slot];
        int level = 0;
		int bottyp = 0;
		int part = 0;
        int thedeEquip=0;
		String[] value = {""+theEquip};
        ResultSet rs = sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", value);
        try{
        	if (rs.next()){
        		level = rs.getInt("reqlevel");
        		bottyp = rs.getInt("bot");
        		part = rs.getInt("part")-1;
        	}
        	rs.close();
        }catch (Exception e){debug(""+e);}
        if (level > this.level){
        	packet.addHeader(header[0], header[1]);
        	packet.addByte2((byte) 0x00, (byte) 0x65);
        	send(packet,false);
        	return;
        }
        if (bottyp!=this.bottype && bottyp!=0){
        	packet.addHeader(header[0], header[1]);
        	packet.addByte2((byte) 0x00, (byte) 0x60);
        	send(packet,false);
    		return;
    	}
        int DurDeequip = itemduration[part];
        int DurEquip = itemduration[slot+19];
        int DurCurDeequip = itemdurationcur[part];
        int DurCurEquip = itemdurationcur[slot+19];
        int SortDeequip = itemdurationsort[part];
        int SortEquip = itemdurationsort[slot+19];
        Timestamp DateDeequip = itemdurationdate[part];
        Timestamp DateEquip = itemdurationdate[slot+19];
        String sort = "";
        /*if (this.inventitems[slot]>=3072300 && this.inventitems[slot]<=3072309 && this.equipitemsgear[7]!=0) {
        	int flagslot=-1;
        	for (int i = 0; i<10; i++)
        		if (inventitems[i]==0)
        			flagslot=i;
        	if (flagslot==-1) {
        		packet.addHeader(header[0], header[1]);
            	packet.addByte2((byte) 0x00, (byte) 0x60);
            	send(packet,false);
        		return;
        	}
        	this.inventitems[flagslot]=this.equipitemsgear[7];
            itemduration[flagslot+19]=itemduration[10];
            itemdurationcur[flagslot+19]=itemdurationcur[10];
            itemdurationsort[flagslot+19]=itemdurationsort[10];
            itemdurationdate[flagslot+19]=itemdurationdate[10];
            this.equipitemsgear[7]=0;
            itemduration[10]=0;
            itemdurationcur[10]=0;
            itemdurationsort[10]=0;
            itemdurationdate[10]=null;
            UpdateItemLocation(inventitems[flagslot], "gear8", "item"+(slot+1), 10, flagslot+19);
        }*/
        if (part<3){
        	thedeEquip=this.equipitemspart[part];
        	this.equipitemspart[part]=theEquip;
        	this.inventitems[slot]=thedeEquip;
        	sort="part"+(part+1);
        }
        else if (part<11){
        	thedeEquip=this.equipitemsgear[part-3];
        	this.equipitemsgear[part-3]=theEquip;
        	this.inventitems[slot]=thedeEquip;
        	sort="gear"+(part-2);
        }
    	else if (part<17){
    		if (part == 15 && this.equipitemspack[4]!=0 && this.equipitemspack[5]==0)
    			part++;
    		thedeEquip=this.equipitemspack[part-11];
    		this.equipitemspack[part-11]=theEquip;
    		this.inventitems[slot]=thedeEquip;
    		sort="pack"+(part-10);
    	}
    	else if (part<19){
    		thedeEquip=this.equipitemscoin[part-17];
    		this.equipitemscoin[part-17]=theEquip;
    		this.inventitems[slot]=thedeEquip;
    		sort="coin"+(part-16);
    	}
        itemduration[part]=DurEquip;
        itemduration[slot+19]=DurDeequip;
        itemdurationcur[part]=DurCurEquip;
        itemdurationcur[slot+19]=DurCurDeequip;
        itemdurationsort[part]=SortEquip;
        itemdurationsort[slot+19]=SortDeequip;
        itemdurationdate[part]=DateEquip;
        itemdurationdate[slot+19]=DateDeequip;
        loadEquipBonus();
        UpdateItemLocation(theEquip, "item"+(slot+1), sort, slot+19, part);
        if(thedeEquip!=0)
            UpdateItemLocation(thedeEquip, sort, "item"+(slot+1), part, slot+19);
        UpdateInvent();
        UpdateBot();
        CharPacket(header);
    }
    
    public void BuySellItem(boolean coins, boolean sell, int itemid)
    {
    	int head = 0;
    	int error = 0;
    	int time = 0;
    	int price = -1;
    	String[] value = {""+(!coins && sell ? inventitems[itemid] : itemid)};
    	ResultSet rs = sql.psquery("SELECT * FROM bout_items WHERE id=? LIMIT 1", value);
        try{
        	if (rs.next()){
        		if(rs.getInt("buyable")==1)
        		{
        			time = rs.getInt("days");
        			if(coins)
        				price = rs.getInt("coins");
        			if(!coins && !sell)
        				price = rs.getInt("buy");
        			if(!coins && sell)
        				price = rs.getInt("sell");
        		}
        		else
        			if(!coins && sell)
        				price = rs.getInt("sell");
        	}
        	rs.close();
        }catch(Exception e){debug("Sql exception"+e);}
    	if(!coins && !sell)
    	{
    		head = 0xEA;
    		int slot=-1;
    		for(int i = 0; i<10; i++)
    			if(inventitems[i]==0){
    				slot=i;
    				break;
    			}
    		if (price == -1)
                error=0x42;
    		else if (this.gigas < price)
                error=0x41;
    		else if(slot == -1)
                error=0x44;
    		else{
    			this.gigas-=price;
    			this.inventitems[slot]=itemid;
    			if(itemid==5041400)
    				if(guildname!=null && !guildname.equals("") && guildmembers[0].equals(botname) && guildmembermax<40)
    					GuildExtend();
    				else{
    					this.gigas+=price;
    	    			this.inventitems[slot]=0;
    	    			error=0x42;
    				}
    			UpdateInvent();
    			UpdateBot();
    			if (time>0)
    				AddItemTime(itemid, slot, "item", time);
    		}
    	}
    	if(!coins && sell)
    	{
    		head = 0xEB;
    		int slot = itemid;
    		itemid=this.inventitems[slot];
    		if (itemid==6000001 || itemid==6000002 || itemid==6000003 || itemid==6000004) {
    			this.coins+=price;
    			UpdateCoins();
    			packet.addHeader((byte) 0x37, (byte) 0x2F);
                packet.addByte2((byte) 0x01, (byte) 0x00);
                packet.addInt(this.coins, 4, false);
                send(packet,false);
    		}
            else {
            	this.gigas+=price;
            	UpdateBot();
            }
    		this.inventitems[slot]=0;
    		UpdateInvent();
    		RemoveItemTime(itemid, slot, "item");
    	}
    	if(coins)
    	{
    		head = 0xEC;
    		int slot=-1;
    		for(int i = 0; i<10; i++)
    			if(inventitems[i]==0){
    				slot=i;
    				break;
    			}
    		if (price == -1)
                error=0x42;
    		else if (this.coins < price)
                error=0x41;
    		else if(slot == -1)
                error=0x44;
    		else{
    			this.coins-=price;
    			this.inventitems[slot]=itemid;
    			if(itemid==5041300)
    				if(guildname!=null && guildname.equals("") && guildmembers[0].equals(botname) && guildmembermax<40)
    					GuildExtend();
    				else{
    					this.coins+=price;
    	    			this.inventitems[slot]=0;
    	    			error=0x42;
    				}
    			UpdateInvent();
    			UpdateBot();
    			UpdateCoins();
    			if (time>0)
    				AddItemTime(itemid, slot, "item", time);
    		}
    	}
    	if(error != 0){
        	packet.addHeader((byte)head, (byte)0x2E);
        	packet.addPacketHead((byte)0x00, (byte)error);
       		for (int i = 0; i<95; i++)
       			packet.addByte((byte)0xCC);
       		send(packet,false);
    	}
    	else
    		InventPacket(head);
    }
    
    public void sendGuildMsg(String msg)
    {
        packet.addHeader((byte) 0x1A, (byte) 0x27);
        packet.addByte2((byte) 0x01, (byte) 0x00);
        packet.addInt(msg.length(), 2, false);
        packet.addInt(5, 2, false);
        packet.addString(msg);
        packet.addByte((byte) 0x00);
        int num=-1;
        for (int i = 0; i<guildmembers.length; i++)
        	if (guildmembers[i]!=null && (num=getNum(guildmembers[i]))!=-1)
        		if (lobby.bots[num]!=null)
        			lobby.bots[num].send(packet,true);
        packet.clean();
    }
    
    public void PopUp(boolean all, String message)
    {
    	packet.addHeader((byte)0x38, (byte)0x2F);
    	packet.addByte2((byte) 0x01, (byte) 0x00);
    	packet.addString(message);
    	packet.addByte((byte)0x00);
    	if(all)
        	lobby.sendAll(packet, lobbynum, false);
    	send(packet,false);
    }
    
    public void Examine(String charname)
    {
        try
        {
    		int clevel = 0;
        	int cbot = 0;
        	int num = getNum(charname);
        	int roomn = -1;
        	if (num!=-1)
	        	roomn = lobby.bots[num].lobbyroomnum;
            String[] value= new String[1];
    		value[0]=charname;
            ResultSet rs = sql.psquery("SELECT * FROM bout_characters WHERE name=? LIMIT 1", value);
            if (rs.next()){
            	clevel = rs.getInt("level");
            	cbot = rs.getInt("bot");
            }
            packet.addHeader((byte)0x27, (byte)0x2F);
            packet.addInt(1, 4, false);
            packet.addInt(clevel, 2, false);
            if (roomn!=-1)
            	packet.addInt(roomn+1, 2, false);
            else
            	packet.addInt(0, 2, false);
            for (int i = 0; i < 11; i++)
            {
                packet.addInt(rs.getInt(i + 25), 4, false);
            }
            packet.addInt(rs.getInt("equipheadcoin"), 4, false);
            packet.addInt(rs.getInt("equipminibotcoin"), 4, false);
            rs.close();
            if(roomn!=-1)
            	packet.addByte((byte) 0x02);
            else
            	packet.addByte((byte)0x00);
            packet.addByte((byte)cbot);
            packet.addByte((byte)0x01);
            packet.addByte((byte)0x00);
            packet.addString(charname);
            for (int i = 0; i<15-charname.length(); i++)
            	packet.addByte((byte)0x00);
            send(packet,false);
        } catch (Exception e)
        {
        	debug("error in examine packet "+e);
        }
    }
    
    public void InventPacket(int head)
    {
    	TimeCalc();
        packet.addHeader((byte)head, (byte)0x2E);
        packet.addPacketHead((byte)0x01, (byte)0x00);
        packet.addByte((byte)0x01);
        for (int i = 19; i<29; i++)
        {
            packet.addInt(this.inventitems[i-19], 4, false);
            packet.addInt((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : itemdurationcur[i], 4, false);
            packet.addByte((this.inventitems[i-19] == 0 || itemdurationsort[i]==0) ? 0 : (byte)itemdurationsort[i]);
        }
        packet.addInt(this.gigas, 4, false);
        send(packet,false);
    }
    
    public void CoinPacket()
    {
    	TimeCalc();
    	packet.addHeader((byte) 0x37, (byte) 0x2F);
        packet.addPacketHead((byte) 0x01, (byte) 0x00);
        packet.addInt(coins, 4, false);
        send(packet,false);
        InventPacket(0xEB);
    }
    
    public void deleteBot()
    {
    	String[] value= {botname};
		sql.psupdate("DELETE FROM `item_times` WHERE `name`=? AND `location` NOT LIKE 'stas__'  AND `location` NOT LIKE 'stas_'", value);
		value= new String[]{account};
    	sql.psupdate("UPDATE `bout_users` SET `online`=0 WHERE `username`=?", value);
    	sql.psupdate("UPDATE `item_times` SET `name`=? WHERE `accountname`=?", value);
    	value= new String[]{botname};
    	sql.psupdate("DELETE FROM `lobbylist` WHERE `name`=?", value);
		sql.psupdate("DELETE FROM `bout_inventory` WHERE `name`=?", value);
		sql.psupdate("DELETE FROM `friends` WHERE `name`=?", value);
		sql.psupdate("DELETE FROM `friends` WHERE `name2`=?", value);
		sql.psupdate("DELETE FROM `gifts` WHERE `to`=?", value);
		sql.psupdate("DELETE FROM `blocklist` WHERE `name`=?", value);
		value= new String[]{account, botname};
        sql.psupdate("DELETE FROM `bout_characters` WHERE `username`=? and `name`=?", value);
        sql.psupdate("INSERT INTO `bout_deleted`  (`username`,`name`,`date`) , (?,?,now())", value);
    	packet.addHeader((byte) 0xE3, (byte) 0x2E);
        packet.addInt(1, 2, false);
        send(packet,false);
    }
    
    public void BlockUser(String player)
    {
    	boolean correct = true;
    	String account = "";
    	int num = -1;
    	for (int i = 0; i<15; i++)
    		if (blockedusers[i]!=null && blockedusers[i].equals(player))
    			correct=false;
    		else if (num==-1 && blockedusers[i]==null)
    			num=i;
    	if(num==-1 || player.equalsIgnoreCase(this.botname))
    		correct=false;
    	ResultSet rs = sql.psquery("SELECT * FROM `bout_characters` WHERE `name`=?", new String[]{player});
    	try{
		if(rs.next())
			account=rs.getString("username");
		rs.close();
		rs = sql.psquery("SELECT * FROM `bout_users` WHERE `username`=?", new String[]{account});
		if(rs.next())
			if(rs.getInt("Position")!=0)
				correct=false;
		rs.close();}catch (Exception e){}
    	if (correct && player!=null && !player.equals("")){
    		packet.addHeader((byte)0x50, (byte)0x2F);
    		packet.addInt(1, 2, false);
    		packet.addString(player);
    		for(int i = 0; i<15-player.length(); i++)
    			packet.addByte((byte)0x00);
    		sql.psupdate("INSERT INTO `blocklist` (`name`, `blocked`)VALUES (?, ?)", new String[]{botname, player});
    		blockedusers[num]=player;
    	}
    	else{
    		packet.addHeader((byte)0x50, (byte)0x2F);
        	packet.addByte2((byte)0x00, (byte)0x33);
        	for(int i = 0; i<15; i++)
        		packet.addByte((byte)0x00);
    	}
		send(packet,false);
    }
    
    public void UnBlockUser(String player)
    {
    	sql.psupdate("DELETE FROM `blocklist` WHERE `name`=? and `blocked`=?", new String[]{botname, player});
    	for (int i = 0; i<15; i++)
    		if (blockedusers[i]!=null && blockedusers[i].equalsIgnoreCase(player))
    			blockedusers[i]=null;
		packet.addHeader((byte)0x51, (byte)0x2F);
        packet.addInt(1, 2, false);
        packet.addString(player);
        for (int i = 0; i<15-player.length(); i++)
        	packet.addByte((byte)0x00);
        send(packet,false);
    }
    
    public boolean checkexist(String charname, String account)
    {
        try
        {
        	String[] arr = {charname};
            ResultSet rs = Main.sql.psquery("SELECT username FROM bout_characters WHERE name=? LIMIT 1", arr);
            if (rs.next()){
            	rs.close();
            	return true;
            }
            else
            	return false;
        }catch(Exception e){debug(""+e);}
        return true;
    }
    
    public void CreateBotPacket(byte[] value)
    {
    	packet.addHeader((byte)0xE2, (byte)0x2E);
    	packet.addByte2(value[0], value[1]);
    	send(packet,false);
    }
    
    public Packet PlayerAddPacket(int state)
    {
        packet.addHeader((byte) 0x27, (byte) 0x27);
        packet.addPacketHead((byte) 0x01, (byte) 0x00);
        packet.addString(botname+" ");
        packet.addByte((byte) 0x00);
        for (int i = packet.getLen(); i<17; i++)
        {
            packet.addByte((byte) 0xCC);
        }
        packet.addByte2((byte) (bottype & 0xff), (byte) state);
        if (state<5){
        	String [] value = new String[]{""+state, account, botname};
        	sql.psupdate("UPDATE `lobbylist` SET `status`=? WHERE `username`=? AND `name`=?", value);
        }
        return packet;
    }
    
    public int getNum(String player)
    {
    	try{
    		String[] arr = {player, channel.Channelname};
    		ResultSet rs = sql.psquery("SELECT * FROM `lobbylist` WHERE name=? AND Channel=?", arr);
    		while(rs.next()){
	        	int clientnum=rs.getInt("num");
	        		return clientnum;
	        }
    		rs.close();
    	}catch(Exception e){debug(""+e);}
        return -1;
    }
    
    public void setroomport(String ip, int port)
    {
    	if((ip.substring(1)).equals(this.ip))
    		if(room!=null)
    			room.setport(port, roomnum);
    }
    
	private static Runnable autocheck(final BotClass bot, final ChannelServerConnection channel, final OutputStream socketOut) {
        return new Runnable() {
          @Override
          public void run() {
        	  try{
        		  byte[] packand = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
        		  byte[] packy = {(byte)0x01, (byte)0x00, (byte)0x01, (byte)0x00};
        		  byte[] packx = {(byte)0xCC};
        		  socketOut.write(bot.encrypt(packand));
        		  socketOut.flush();
        		  socketOut.write(bot.encrypt(packy));
        		  socketOut.flush();
        		  socketOut.write(bot.encrypt(packx));
        		  socketOut.flush();
        		  bot.runautocheck();
        	  }catch(Exception e){
        		  debug("channel[11002]: runautosend error "+e);
        		  channel.closecon();
        	  }
          }
        };
	}
    
	protected void runautocheck()
	{
		try{
			if (finalize)
				return;
			if (autocheckrun)
				autocheckrunc = executorp.schedule(autocheck(this, channel, socketOut), 3, TimeUnit.SECONDS);
			else
				autocheckrunc = executorp.schedule(autocheck(this, channel, socketOut), 1, TimeUnit.SECONDS);
			Long endTime = System.currentTimeMillis();
			Long totTime = (endTime - lastreply);
			int secs=(int)TimeUnit.MILLISECONDS.toSeconds(totTime);
			if(secs>20){ // ping timeout
				// @TODO: check playername
//				String[] value = new String[]{"", account};
//		        sql.psupdate("UPDATE `lobbylist` SET Channel=? WHERE `username`=?", value);
//				channel.closecon();
			}
		}catch(Exception e){debug("runauto error "+e);}
	}
	
	protected byte[] encrypt(byte[] a)
	{
		/*byte[] b = new byte[a.length];
		for (int i = 0; i<a.length; i++)
			b[i]=Main.encrypt[a[i] & 0xFF];*/
		return a;
	}
	
	public void send(Packet packet, boolean keep)
	{
		try {
        	byte[] header = packet.getHeader();
        	byte[] data = packet.getPacket();
        	this.socketOut.write(encrypt(new byte[]{(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00}));
            this.socketOut.flush();
			this.socketOut.write(encrypt(header));
	        this.socketOut.flush();
	        this.socketOut.write(encrypt(data));
	        this.socketOut.flush();
	        if (!keep)
	        	packet.clean();
		} catch (Exception e) {
			debug("Error in packetsend: "+e);
			if (!finalize) {
				channel.closecon();
			}
		}
	}
	
	public void closeThread()
	{
		try{
        	autocheckrunc.cancel(true);
        }catch (Exception e){debug("Error while canceling autocheck: "+e);}
		try {
        	executorp.shutdown();
		}catch (Exception e){debug("Error while shutting down executor: "+e);}
		try{
			lobby.sendAll(PlayerAddPacket(255), lobbynum, false);
			packet.clean();
		}catch (Exception e){debug("Error while removing user from lobby: "+e);}
		try{
			lobby.removeuser(lobbynum);
			String[] value = new String[]{"0", account};
			sql.psupdate("UPDATE bout_users SET online=? WHERE username=?", value);
			value = new String[]{"", account};
	        sql.psupdate("UPDATE `lobbylist` SET Channel=? WHERE `username`=?", value);
	        sql=null;
	        channel=null;
		}catch (Exception e){debug("Error while removing user from lobby: "+e);}
		try {
        	socketOut.close();
		} catch (IOException e) {debug("Error while closing socket: "+e);}
	}
}

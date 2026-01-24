package botsserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.ResultSet;

public class ChannelServerConnection extends Thread{
    protected ChannelServer server;
    protected Lobby lobby;
	protected Socket socket;
    protected SQLDatabase sql;
    protected InputStream socketIn;
    protected OutputStream socketOut;
    protected String ip;
    protected BotClass bot;
    protected String Channelname;
    protected String account;
    protected String charname;
    protected Packet pack = new Packet();
    
    public ChannelServerConnection(Socket socket, ChannelServer server, Lobby _lobby, SQLDatabase _sql, String channelname)//, RelayServer Relayserver)
    {
        this.socket = socket;
        this.server = server;
        this.sql = _sql;
        this.ip = Main.getip(socket);
        this.lobby = _lobby;
        this.Channelname=channelname;
    }
    
    private void debug(String msg)
    {
    	Main.debug("ChannelServer[11002]"+msg);
    }
    
    protected int isbanned(String account)
    {
        try
        {
        	int ban=0;
        	String[] arr = {account};
            ResultSet rs = Main.sql.psquery("SELECT banned FROM bout_users WHERE username=? LIMIT 1", arr);
            if (rs.next())
            {
                ban = rs.getInt("banned");
            }
            rs.close();
            return ban;
        } catch (Exception e)
        {
        }
        return 0;
    }
    
    public void checkAccount()
    {
        try
        {
        	String[] arr = {Main.getip(socket)};
            ResultSet rs = Main.sql.psquery("SELECT username FROM bout_users WHERE current_ip=? LIMIT 1", arr);
            if (rs.next())
            {
                this.account = rs.getString("username");
            }
            rs.close();
            if (this.account != null && isbanned(this.account) == 0)
            {
            	arr[0]=this.account;
                Main.sql.psupdate("UPDATE bout_users SET current_ip='', online='1' WHERE username=?", arr);
            }
            else
            {
            	if(isbanned(this.account)!=0)
            	{
            		debug("banned account tried logging in.");
            		arr[0]=this.account;
            		Main.sql.psupdate("UPDATE bout_users SET current_ip='' WHERE username=?", arr);
            		closecon();
            	}
                account = "";
            }
        } catch (Exception e)
        {
            debug("Error :" + e);
        }
    }
    
    protected void parsecmd(int cmd, byte[] packet)
    {
        try
        {
            pack.setPacket(packet);
            pack.removeHeader();
            switch (cmd)
            {
            	case 0xF82A:
            	{
            		bot.addplayer();
            		break;
            	}
                case 0xF92A:
                {
                	if(bot.checkbot())
                	{
                		bot.loadchar();
                		try{
                		bot.extraloadPackets();
                		lobby.adduser(bot, bot.lobbynum, bot.PlayerAddPacket(1));
                		} catch (Exception e){debug(""+e);}
                	}
                	else
                		bot.nobotPacket();
                	break;
                }
                case 0xFA2A:
                {
                	 pack.getInt(2);
                     int bottype = pack.getInt(2);
                     pack.getInt(2);
                     String accountname = pack.getString(1, 23, false);
                     String charname = pack.getString(0, 15, false);
                     if(!accountname.equals(account))
                    	 this.closecon();
                     if (bot.checkexist(charname, accountname))
                    	 bot.CreateBotPacket(new byte[]{(byte)0x00, (byte)0x33});
                     else if (charname.matches("[a-zA-Z0-9.~_!\\x2d]+"))
                    	 bot.createbot(accountname, charname, bottype);
                     else
                    	 bot.CreateBotPacket(new byte[]{(byte)0x00, (byte)0x36});
                     break;
                }
                case 0x742B:
                {
                    pack.getInt(2);
                    pack.getInt(2);
                    @SuppressWarnings("unused")String name=pack.getString(0,15,false);
                    bot.lobbypacket();
                    bot.RoomsPacket(false, new int[]{-1,-1});
                    bot.FnineOK();
                    bot.DoGuildPackets();
                    bot.FriendlistPacket();
                    break;
                }
                case 0x1A27:
                {
                	pack.getInt(2);
                	@SuppressWarnings("unused")int length=pack.getInt(2);
                    boolean guild = pack.getInt(2)==5;
                    String msg = pack.getString(0, pack.getLen(), false);
            		String[] parts = msg.split("]", 2);
            		String content = parts.length>1 ? parts[1] : msg;
            		String trimmed = content.trim();
            		if (trimmed.startsWith("@")) {
            			String commandText = trimmed.substring(1);
            			if (bot.room!=null)
            				lobby.standard.ParseRoomCommands(bot, commandText);
            			else
            				lobby.standard.ParseCommands(bot, new String[]{commandText});
            		}
            		else if (bot.room!=null) {
            			if (bot.muted(false)==0 && bot.parsemessage(msg.substring(2+bot.botname.length())))
            				bot.room.SendMessage(true, bot.roomnum, "["+bot.botname+"]"+content, !bot.announce ? 0 : bot.gm==100 ? 4 : bot.gm==150 ? 1 : bot.gm>199 ? 2 : 0);
            			else
            				bot.sendChatMsg("[Server] you are muted for "+(bot.muted==-1 ? "forever" : bot.muted+" seconds."),2,false,-1);
            		}
            		else if (guild)
            			bot.sendGuildMsg("["+bot.botname+"]"+msg.substring(2+bot.botname.length()));
            		else if (bot.muted(false)==0 && bot.parsemessage(msg.substring(2+bot.botname.length())))
            			bot.sendChatMsg("["+bot.botname+"]"+content, !bot.announce ? 0 : bot.gm==100 ? 4 : bot.gm==150 ? 1 : bot.gm>199 ? 2 : 0, true, -1);
            		else
            			bot.sendChatMsg("[Server] you are muted for "+(bot.muted==-1 ? "forever" : bot.muted+" seconds."),2,false,-1);
                    break;
                }
                case 0x442B:
                {
                	int len = pack.getInt(2);
                    String recvUser = pack.getString(0, 15, false);
                    String message = pack.getString(0, pack.getLen(), false);
                    bot.WhisperPacket(recvUser, "["+bot.botname+"]Whisper\r:"+message.split(":", 2)[1], len);
                    break;
                }
                case 0x222B:
                {
                	bot.logout();
                	this.closecon();
                	break;
                }
                case 0xFB2A:
                {
                	bot.deleteBot();
                	this.closecon();
                	break;
                }
                case 0x512B:
                {
                	bot.CoinPacket();
                	break;
                }
                case 0x022B:
                {
                	pack.getString(0, 38, true);
                    int itemid = pack.getInt(4);
                    bot.BuySellItem(false, false, itemid);
                    break;
                }
                case 0x042B:
                {
                    pack.getString(0, 38, true);
                    pack.getInt(2);
                    int itemid = pack.getInt(4);
                    bot.BuySellItem(true, false, itemid);
                    break;
                }
                case 0x032B:
                {
                    pack.getString(0, 38, true);
                    int slotnum = pack.getInt(2);
                    pack.getInt(2);
                    int itemid = pack.getInt(4);
                    if (bot.inventitems[slotnum]==itemid)
                    	bot.BuySellItem(false, true, slotnum);
                    break;
                }
                case 0x702B:
                {
                    pack.getInt(2);
                    int card = pack.getInt(1);
                    int itemslot1 = pack.getInt(1);
                    int itemslot2 = pack.getInt(1);
                    int itemslot3 = pack.getInt(1);
                    bot.CombiCoupon(card, itemslot1, itemslot2, itemslot3);
                    break;
                }
                case 0x722B:
                {
                	pack.getInt(2);
                    int botchange = pack.getInt(2);
                    pack.getInt(2);
                    @SuppressWarnings("unused")String charname = pack.getString(0, 15, false);
                    bot.TransCoupon(botchange);
                    break;
                }
                case 0xFC2A:
                {
                    pack.getString(0, 25, false);
                    int slot = pack.getInt(2);
                    pack.getInt(2);
                    bot.EquipItem(1, slot);
                    break;
                }
                case 0xFD2A:
                {
                    pack.getString(0, 25, false);
                    int slot = pack.getInt(2);
                    pack.getInt(2);
                    bot.deEquipItem(1, slot);
                    break;
                }
                case 0x322B:
                {
                	pack.getString(0, 2, false);
                    int slot = pack.getInt(2);
                	bot.EquipItem(2, slot);
                	break;
                }
                case 0x332B:
                {
                	pack.getString(0, 2, false);
                    int slot = pack.getInt(2);
                    bot.deEquipItem(2, slot);
                    break;
                }
                case 0x342B:
                {
                	pack.getString(0, 2, false);
                    int slot = pack.getInt(2);
                    bot.EquipItem(3, slot);
                    break;
                }
                case 0x352B:
                {
                	pack.getString(0, 2, false);
                    int slot = pack.getInt(2);
                    bot.deEquipItem(3, slot);
                    break;
                }
                case 0x412B:
                {
                	String name = pack.getString(0, pack.getLen(), false);
                	bot.Examine(name);
                	break;
                }
                case 0x0A2B:
                {
                	pack.getInt(2);
                	int page = pack.getInt(2);
                    int mode = pack.getInt(2);
                    bot.page = new int[]{mode, page};
                    bot.RoomsPacket(false,new int[]{-1,-1});
                    break;
                }
                case 0x092B:
                {
                    if(bot.room!=null)
                    	return;
                	String roomname = pack.getString(0, 27, false);
                    String roompassword = pack.getString(0, 10, false);
                    pack.getInt(2);
                    int roommode = pack.getInt(1);
                    bot.addRoom(roommode, roomname, roompassword);
                    break;
                }
                case 0x652B:
                {
                	if(bot.room==null)
                    	return;
                	pack.getInt(2);
                	int map = pack.getInt(2);
                	bot.room.setMap(map, bot.roomnum);
                	break;
                }
                case 0x522B:
                {
                	if(bot.room==null)
                    	return;
                	pack.getInt(3);
                	@SuppressWarnings("unused")String name = pack.getString(0, 15, false);
                    pack.getInt(4);
                    pack.getInt(3);
                    String newpass = pack.getString(0, 11, false);
                    bot.room.ChangePass(newpass, bot.roomnum);
                    break;
                }
                case 0x0E2B:
                {
                	pack.getInt(8);
                	int roommode=pack.getInt(1);
                	bot.quickJoin(roommode);
                    break;
                }
                case 0x062B:
                {
                	int roomnum = pack.getInt(2)-1;
                    String rname = pack.getString(0, 27, false);
                    String rpass = pack.getString(0, 10, false);
                    if(bot.lobbyroomnum!=-1)
                    	return;
                    Room room = lobby.rooms[roomnum];
                    if(rname.equals(room.roomname)){
                    	room.Join(bot, rpass, ip);
                    }
                    break;
                }
                case 0x402B:
                {
                	pack.getInt(2);
                    pack.getString(0, 15, false);
                    int slot = pack.getInt(2);
                    if (bot.roomnum==bot.room.roomowner) {
                    	if (bot.room.Exit(slot, true))
                    		bot.RemoveRoom(bot.room);
	                    bot.lobby.bots[slot].room=null;
                    }
                    break;
                }
                case 0x422B:
                {
                	if (bot.room.Exit(bot.roomnum, false))
                    	bot.RemoveRoom(bot.room);
                	bot.room=null;
                	break;
                }
                case 0x0B2B:
                {
                	if (bot.roomnum==bot.room.roomowner)
                		bot.room.Start();
                    break;
                }
                case 0x3E2B:
                {
                	bot.room.readyToPlay(bot.roomnum);
                	break;
                }
                case 0x392B:
                {
                	pack.getInt(2);
                    int slot = pack.getInt(2);
                    pack.getInt(2);
                    int what = pack.getInt(2);
                    bot.room.SlotStatus(bot.roomnum, what, slot);
                    break;
                }
                case 0x6F2B:
                {
                	bot.room.PreDead(bot.roomnum);
                	break;
                }
                case 0x362B:
                {
                	bot.room.EquipPackUse(bot.roomnum);
                	break;
                }
                case 0x3A2B:
                {
                	int num = pack.getInt(2);
                    int typ = pack.getInt(2);
                    int killedby = pack.getInt(2);
                    int pushed = pack.getInt(2);
                    bot.room.MobKill(typ, num, killedby, pushed, bot.roomnum);
                    break;
                }
                case 0x3B2B:
                {
                	pack.getInt(2);
                    pack.getInt(2);
                    int bywho = pack.getInt(2);
                    pack.getInt(2);
                    bot.room.Dead(bot.roomnum, bywho);
                    break;
                }
                case 0x3C2B:
                {
                	pack.getInt(2);
                    int num = pack.getInt(1);
                    int typ = pack.getInt(1);
                    bot.room.DropPickup(bot.roomnum, typ, num);
                    break;
                }
                case 0x282B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    if (name.equals(bot.botname))
                    	bot.MessagePacket();
                    break;
                }
                case 0x2A2B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    int nr=pack.getInt(1);
                    if (name.equals(bot.botname))
                    	bot.MessageReadPacket(nr-1);
                    break;
                }
                case 0x292B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String recepient = pack.getString(0, 15, false);
                    String message = pack.getString(0, 96, false);
                    if (name.equals(bot.botname))
                    	bot.messagesend(recepient, message);
                    break;
                }
                case 0x2B2B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    int num=pack.getInt(1);
                    if(name.equals(bot.botname))
                    	bot.DeleteMessage(num-1);
                    break;
                }
                case 0x2D2B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    if(name.equals(bot.botname))
                    	bot.gifts();
                    break;
                }
                case 0x2E2B:
                {
                	pack.getInt(2);
                    int kind=pack.getInt(2);
                    String from = pack.getString(0, 15, false);
                    String to = pack.getString(0, 15, false);
                    String message = pack.getString(0, 30, false);
                    int gift=pack.getInt(4);
                    gift=gift==0 ? pack.getInt(4) : gift;
                    gift=gift==0 ? pack.getInt(4) : gift;
                    if (from.equals(bot.botname))
                    	bot.SendGift(to, message, kind, gift);
                    break;
                }
                case 0x312B:
                {
                	pack.getInt(2);
                    pack.getInt(2);
                    int nr=pack.getInt(4);
                    String from = pack.getString(0, 15, false);
                    if (from.equals(bot.botname))
                    	bot.ReceiveGift(nr);
                    break;
                }
                case 0x662B:
                {
                	pack.getInt(2);
                    pack.getInt(1);
                    String from = pack.getString(0, 15, false);
                    pack.getInt(7);
                    String to = pack.getString(0, 15, false);
                    String message = pack.getString(0, 30, false);
                    int item = pack.getInt(4);
                    if (from.equalsIgnoreCase(bot.account))
                    	bot.ShopGift(to, message, 0, item);
                    break;
                }
                case 0x272B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String friend = pack.getString(0, 15, false);
                    if (name.equals(bot.botname))
                    	bot.FriendRequest(friend);
                    break;
                }
                case 0x242B:
                {
                	pack.getInt(2);
                    boolean accept = pack.getInt(2)==1;
                    pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String friend = pack.getString(0, 15, false);
                    if (name.equals(bot.botname))
                    	bot.FriendReply(friend, accept);
                    break;
                }
                case 0x252B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String friend = pack.getString(0, 15, false);
                    if (name.equals(bot.botname))
                    	bot.RemoveFriend(friend);
                    break;
                }
                case 0x642B:
                {
                	pack.getInt(2);
                    String player = pack.getString(0, 15, false);
                    if (bot.botname.equals(player))
                    	bot.GuildAppList();
                }
                case 0x562B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String guildname = pack.getString(0, 20, false);
                    if (bot.botname.equals(name))
                    	bot.GuildApp(guildname);
                    break;
                }
                case 0x5A2B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    pack.getInt(4);
                    String player = pack.getString(0, 15, false);
                    if (bot.guildmembers[0].equals(name))
                    	bot.GuildAppAction(player, true);
                    break;
                }
                case 0x5B2B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    pack.getInt(4);
                    String player = pack.getString(0, 15, false);
                    if (bot.guildmembers[0].equals(name))
                    	bot.GuildAppAction(player, false);
                    break;
                }
                case 0x572B:
                {
                	pack.getInt(2);
                    String player = pack.getString(0, 15, false);
                    if (bot.botname.equals(player))
                    	bot.GuildAppAction(bot.botname, false);
                    break;
                }
                case 0x552B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String guildname = pack.getString(0, 16, false);
                    if (bot.botname.equals(name))
                    	bot.CreateGuild(guildname);
                    break;
                }
                case 0x622B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String player = pack.getString(0, 15, false);
                    if (bot.botname.equals(name))
                    	bot.InvitetoGuild(player);
                    break;
                }
                case 0x472F:
                {
                	boolean accept=pack.getInt(2)==1;
                    pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String player = pack.getString(0, 15, false);
                    if (bot.botname.equals(name))
                    	bot.GuildInviteReply(player, accept);
                    break;
                }
                case 0x732B:
                {
                	pack.getInt(4);
                    String name = pack.getString(0, 15, false);
                    String notice = pack.getString(0, 80, false);
                    if (name.equals(bot.botname) && bot.botname.equals(bot.guildmembers[0]))
                    	bot.SetGuildNotice(notice);
                    break;
                }
                case 0x592B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    pack.getInt(4);
                    String kick = pack.getString(0, 15, false);
                    if(name.equals(bot.guildmembers[0]))
                    	bot.GuildRemove(kick);
                    break;
                }
                case 0x5D2B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    if(name.equals(bot.botname))
                    	bot.GuildRemove(bot.botname);
                    break;
                }
                case 0x672B:
                {
                	pack.getInt(2);
                	pack.getInt(1);
                    String name = pack.getString(0, 21, false);
                    if (name.equalsIgnoreCase(bot.account))
                    	bot.BuyStash();
                    break;
                }
                case 0x682B:
                {
                	pack.getInt(2);
                    pack.getInt(1);
                    String name = pack.getString(0, 21, false);
                    pack.getInt(1);
                    int slot = pack.getInt(1);
                    int stash = pack.getInt(1);
                    if(name.equalsIgnoreCase(bot.account))
                    	bot.MoveToStash(stash, slot, false);
                    break;
                }
                case 0x692B:
                {
                	pack.getInt(2);
                    pack.getInt(1);
                    String name = pack.getString(0, 21, false);
                    pack.getInt(1);
                    int slot = pack.getInt(1);
                    int stash = pack.getInt(1);
                    if(name.equalsIgnoreCase(bot.account))
                    	bot.MoveToStash(stash, slot, true);
                    break;
                }
                case 0x532B:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String player = pack.getString(0, 15, false);
                    if(name.equals(bot.botname))
                    	bot.RequestTrade(player);
                    break;
                }
                case 0x392F:
                {
                	int accept = pack.getInt(2);
                    pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    String player = pack.getString(0, 15, false);
                    if(name.equals(bot.botname))
                    	bot.TradeRequestAccept(player, accept);
                    break;
                }
                case 0x3727:
                {
                	String name = pack.getString(0, 15, false);
                    pack.getInt(6);
                    String message = pack.getString(0, 128, false);
                    if(name.equals(bot.botname))
                    	bot.TradeMessage(message, null);
                    break;
                }
                case 0x3427:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    if(name.equals(bot.botname))
                    	bot.ExitTrade();
                    break;
                }
                case 0x3127:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    pack.getInt(1);
                    int item = pack.getInt(4);
                    int item1 = pack.getInt(4);
                    int item2 = pack.getInt(4);
                    int gigas = pack.getInt(4);
                    int botstract = pack.getInt(4);
                    if(name.equals(bot.botname))
                    	bot.TradeItems(new int[]{item, item1, item2}, gigas, botstract);
                    break;
                }
                case 0x3227:
                {
                	pack.getInt(2);
                    String name = pack.getString(0, 15, false);
                    pack.getInt(1);
                    if(name.equals(bot.botname))
                    	bot.TradeAccept();
                    break;
                }
                case 0x6B2B:
                {
                	pack.getInt(2);
                    String block = pack.getString(0, 15, false);
                    bot.BlockUser(block);
                    break;
                }
                case 0x6C2B:
                {
                	pack.getInt(2);
                    String block = pack.getString(0, 15, false);
                    bot.UnBlockUser(block);
                    break;
                }
                case 0x0200:
                {
                	bot.lastreply = System.currentTimeMillis();
                	break;
                }
                case 0x3D2B:
                {
                	break;
                }
            }
        } catch (Exception e){
        }
    }
    
    public int getcmd(byte[] packet)
    {
    	int ret = 0;
    	ret+=(packet[1] & 0xFF);
    	ret+=(packet[0] & 0xFF) << (8);
    	return ret;
    }
    
    public int bytetoint(byte[] packet, int bytec)
    {
       	int ret = 0;
       	ret+=(packet[0+bytec] & 0xFF);
       	ret+=(packet[1+bytec] & 0xFF) << (8);
       	return ret;
    }
    
    protected byte[] read()
    {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        int codePoint;

        try
        {
            for (int i = 0; i < 4; i++)
            {
                codePoint = this.socketIn.read();
                buffer.put((byte)codePoint);//(byte)Main.decrypt[codePoint & 0xFF]);
            }
            int plen = bytetoint(buffer.array(), 2);
            if (bytetoint(buffer.array(), 0)==0xFFFF)
            	return null;
            byte[] quickstore = buffer.array();
            buffer = ByteBuffer.allocate(plen+5);
            buffer.put(quickstore);
            if (plen >= 1)
            {

                for (int i = 0; i < plen; i++)
                {
                    codePoint = this.socketIn.read();
                    buffer.put((byte)codePoint);//(byte)Main.decrypt[codePoint & 0xFF]);
                }
            }
        } catch (Exception e)
        {
            debug("Error (read): " + e);
            return null;
        }
		return buffer.array();
    }
    
    public void run()
    {
        try
        {
            this.socketIn = this.socket.getInputStream();
            this.socketOut = this.socket.getOutputStream();
            checkAccount();
            bot = new BotClass(this.account, this.ip, sql, socketOut, this, lobby);
            byte[] packet;
            while ((packet = read()) != null)
            {
                parsecmd(getcmd(packet), packet);
            }
        } catch (Exception e)
        {
            debug("Exception (run): " + e);
        }
        this.closecon();
    }
    
    protected void closecon()
    {
    	try{
    		if (bot.finalize)
    			return;
    		bot.finalize=true;
	    	if (bot.room!=null && bot.room.Exit(bot.roomnum, false))
	        	bot.RemoveRoom(bot.room);
	    	bot.room=null;
	    }catch (Exception e){debug("Error occured while removing user from room: "+e);}
    	try{
		    bot.closeThread();
		    bot=null;
		    server=null;
		    sql=null;
    	}catch (Exception e){debug("Error while freeing resources: "+e);}
	    try {
			socketIn.close();
        	socketOut.close();
        	socket.close();
		} catch (IOException e) {debug("Error while closing socket attributes: "+e);}
    }
}

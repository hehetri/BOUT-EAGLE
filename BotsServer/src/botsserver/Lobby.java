package botsserver;

import java.sql.ResultSet;
import java.util.Arrays;

public class Lobby{
	private String channelname;
	protected RoomServer roomserver;
	protected BotClass[] bots = new BotClass[600];
	protected Room[] rooms = new Room[1800];
	protected Standard standard;
	
    public Lobby(String channelnameb)
    {
        this.channelname=channelnameb;
        standard = new Standard();
    }
    
    public void SetRoomServer(RoomServer server)
    {
    	this.roomserver=server;
    }
    
    public String getChannelName()
    {
    	return channelname;
    }
    
    public void setRoom(int num, Room room)
    {
    	rooms[num]=room;
    }
    
    public int getEmptyRoom(int num)
    {
    	for (int i = num; i<(num+600); i++)
    		if (rooms[i]==null)
    			return i;
    	return -1;
    }
    
    public Room[] getRoomsbyPage(int mode, int page)
    {
    	Room[] room = new Room[6];
    	for (int i = 0; i<6; i++)
    		room[i]=rooms[mode*600+page*6+i];
    	return room;
    }
    
    public void sendAll(Packet packet, int num, boolean announce)
    {
    	int[] nums = GetNums(num);
    	for (int i = 0; i<nums.length; i++)
    	{
	    	if(nums[i]>=0 && bots[nums[i]]!=null && nums[i]!=num && (announce || bots[nums[i]].lobbyroomnum==-1))
	    	{
	    		try{
	    			bots[nums[i]].send(packet,true);
	    		} catch (Exception e){Main.debug("Error in lobby while sendAll: "+e);}
	    	}
    	}
    	//packet.clean();
    }
    
    public int[] GetNums(int num)
    {
    	int[] nums = new int[600];
    	int i=0;
    	ResultSet rs = bots[num].sql.psquery("SELECT * FROM `lobbylist` WHERE Channel=? ORDER BY num", new String[]{channelname});
        try{
        	while (rs.next()){
        		nums[i]=rs.getInt("num");
        		i++;
        	}
        	rs.close();
        }catch (Exception e){}
        return Arrays.copyOfRange(nums, 0, i);
    }
    
    public void sendAllroom(Packet packet, int num, int[] page)
    {
    	int[] nums = GetNums(num);
    	for (int i = 0; i<nums.length; i++)
    	{
	    	if(bots[nums[i]]!=null && nums[i]!=num && bots[nums[i]].lobbyroomnum==-1 && bots[nums[i]].page[0]==page[0] && bots[nums[i]].page[1]==page[1])
	    	{
	    		try{
	    			bots[nums[i]].send(packet,true);
	    		} catch (Exception e){}
	    	}
    	}
    	packet.clean();
    }
    
    public void adduser(BotClass bot, int num, Packet packet)
    {
    	bots[num]=bot;
    	roomserver.setbot(num, bot);
    	sendAll(packet, num, false);
    	packet.clean();
    }
    
    public void removeuser(int num)
    {
    	bots[num]=null;
    	roomserver.setbot(num, null);
    }
}

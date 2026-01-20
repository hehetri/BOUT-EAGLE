package botsserver;

import java.net.*;
import java.sql.*;

/**
 * Start the server.
 */
public class RelayStore {
    protected InetAddress[] address = new InetAddress[600];
    protected int[] remoteport = new int[600];
    public SQLDatabase sql;
    public RelayServer server;
    public String[] name = new String[600];
    public int[][] roomnums = new int[600][8];
    public int[] roomids = new int[600];
    public int[][] playernums = new int[600][8];
    public int[][] roomallnums = new int[1800][8];
    public String[] charname = new String[600];
    
    public RelayStore(RelayServer servaer, SQLDatabase sqi)
    {
    	this.sql = sqi;
    	this.server = servaer;
    	for (int i = 0; i<600; i++)
    		roomids[i]=-1;
    }
    
    public void debug(String msg)
    {
    	Main.debug("RelayStorage: "+msg);
    }
    
    public void removeNum(int number, int num)
    {
    	for (int i = 0; i<8; i++)
    		if (this.roomnums[number][i]==num) {
    			this.roomnums[number][i]=-1;
    			break;
    		}
    }
    
    public void removeByID(int number, int id)
    {
    	for (int i = 0; i<8; i++)
    		if (this.roomnums[number][i]==id)
    			this.roomnums[number][i]=-1;
    }
    
    public void RelaySetRoomNums(int number, int[] nums)
    {
    	for(int i = 0; i<8; i++)
    	{
    		this.roomnums[number][i]=nums[i];
    	}
    }
    
    public void AddUser(String username, int number, InetAddress address, int porta, String cname)
    {
    	this.address[number]=address;
    	this.name[number]=username;
    	this.remoteport[number]=porta;
    	this.charname[number]=cname;
    	this.roomids[number]=-1;
    }
    
    public void FinishAddUser(int number, InetAddress address, int porta)
    {
    	if(this.address[number].equals(address))
    		this.remoteport[number]=porta;
    	debug("Finished adding "+this.charname[number]+" at #"+number+" with address:port "+address+":"+porta);
    }
    
    public void removeuser(int number, String username)
    {
    	try {
	    	if(this.name[number]!=null && this.name[number].equalsIgnoreCase(username)){
	    		this.address[number]=null;
	    		this.name[number]=null;
	    		this.remoteport[number]=0;
	    		this.charname[number]=null;
	    		for(int i = 0; i<8; i++)
	        	{
	        		this.roomnums[number][i]=-1;
	        	}
	    	}
    	}catch (Exception e){debug("Error while removing user: "+e);}
    }
    
    public int getRNum(String username)
    {
    	int a = -1;
    	if(username==null || username.equals(""))
    		return a;
    	for(int i=0; i<600; i++)
    	{
    		if(this.charname[i]!=null && this.charname[i].equalsIgnoreCase(username))
    		{
    			a=i;
    			break;
    		}
    	}
    	return a;
    }
    
    public boolean checkUser(String username)
    {
    	for(int i=0; i<600; i++)
    	{
    		if(this.name[i]==username)
    		{
    			String[] arr = {username};
    			ResultSet lvl = sql.psquery("SELECT * FROM `lobbylist` WHERE name=?", arr);
    			try{
    	    	if (lvl.next()){
    	        	int clientnum=lvl.getInt("num");
    	        	if (clientnum==i)
    	        		return true;
    	    	}
    			}catch (Exception e){}
    		}
    	}
        return false;
    }
    
    public int getNum(String addresss, int porta)
    {
    	for(int i=1; i<600; i++)
    	{
    		if(address[i]!=null)
    		if(addresss.equals(this.address[i].getHostAddress()))
    		{
    			if (remoteport[i] == porta)
    			{
    				return i;
    			}
    		}
    	}
    	return -1;
    }
}

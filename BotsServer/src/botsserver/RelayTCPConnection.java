package botsserver;

import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;
import java.sql.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RelayTCPConnection extends Thread {
    protected Socket socket;
    protected InputStream socketIn;
    protected OutputStream socketOut;
    protected String account;
    protected String charname;
    public String user;
    public String pass;
    public SQLDatabase sql = Main.sql;
    public RelayStore relaystore;
    private int numb;
    protected ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	protected ScheduledFuture<?> autocheck = null;
	protected boolean finalize = false;
	
    public RelayTCPConnection(Socket socket, RelayTCP server, Lobby lobbi) {
        this.socket = socket;
    }
    
    public void debug(String msg)
    {
    	Main.debug("RelayIdentifier[11104]: "+msg);
    }
    
    private static Runnable autocheck(final RelayTCPConnection thread, final OutputStream socketOut) {
        return new Runnable() {
          @Override
          public void run() {
        	  try{
        		  socketOut.write(thread.encrypt(new byte[]{(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}));
        		  socketOut.flush();
        	  }catch (Exception e){
        		  thread.debug("remove user for error : "+e);
        		  thread.closecon();
        	  }
          }
        };
    }
    
    protected int isbanned(String account)
    {
        try
        {
        	String[] arr = {account};
            ResultSet rs = Main.sql.psquery("SELECT banned FROM bout_users WHERE username=? LIMIT 1", arr);
            if (rs.next())
            {
                return rs.getInt("banned");
            }
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
            ResultSet rs = Main.sql.psquery("SELECT username FROM bout_users WHERE current_ip=? LIMIT 1",arr);
            if (rs.next())
            {
                this.account = rs.getString("username");
                rs.close();
                arr[0]=this.account;
                rs = Main.sql.psquery("SELECT name FROM bout_characters WHERE username=? LIMIT 1", arr);
                if (rs.next())
                {
                	this.charname = rs.getString("name");
                }
            }
            rs.close();
            if (this.account != null && isbanned(this.account) == 0)
            {
                //Main.sql.doupdate("UPDATE bout_users SET current_ip='' WHERE username='"+this.account+"'");
            }
            else
            {
                account = "";
            }
        } catch (Exception e)
        {
            debug("Error :" + e);
        }
    }
    
    protected byte[] encrypt(byte[] a)
	{
		//byte[] b = new byte[a.length];
		//for (int i = 0; i<a.length; i++)
		//	b[i]=Main.encrypt[a[i] & 0xFF];
		return a;
	}
    
    protected void prasecmd1(int cmd, byte[] packet)
    {
        try
        {
            Packet pack = new Packet();
        	this.socketOut.flush();
            switch (cmd)
            {
            	case 0x32A0: 
            	{
            		relaystore = Main.getRelayStore();
            		int i = 0;
                    int num=1;
                    String[] arr = new String[0];
                    ResultSet rs = sql.psquery("SELECT * FROM `lobbylist` WHERE online = '1' ORDER BY Rnum",arr);
                    try{
                    	i=0;
                    	while (rs.next()){
                    		i++;
                    		num = rs.getInt("Rnum");
                    		if (i!=num && i<num){
                    			num=i;
                    			break;
                    		}
                    		else{
                    			num++;
                    		}
                    	}
                        rs.close();
                    }catch (Exception e){debug("problem creating ID"+e);}
                    i=num;
                    numb=num;
                    checkAccount();
                    InetAddress address=socket.getInetAddress();
                    arr = new String[]{account};
        			sql.psupdate("DELETE FROM `lobbylist` WHERE `username`=?", arr);
                    arr = new String[]{account, account+" ", "-1", ""+num,""};
                    sql.psupdate("INSERT INTO `lobbylist` (`username`, `name`, `date`, `status`, `num`, `Rnum`, `Channel`) VALUES (?, ?, now(), '1', ?, ?, ?)", arr);
                    relaystore.AddUser(account, num, address, -1, charname);
                    int b1 = i & 0xff;
                    int b2 = (i >> 8) & 0xff;
            		byte[] answerhead = {(byte)0x1A, (byte)0xA4, (byte)0x04, (byte)0x00};
            		byte[] answerpack = {(byte)0x01, (byte)0x00, (byte)b1, (byte)b2};
            		this.socketOut.write(encrypt(answerhead));
            		this.socketOut.flush();
            		this.socketOut.write(encrypt(answerpack));
            		this.socketOut.flush();
            		break;
            	}
            	
            	case 0x36A0:
            	{
            		pack.setPacket(packet);
            		pack.removeHeader();
            		int number = pack.getInt(2);
            		pack.getInt(2);
            		relaystore.roomids[number] = pack.getInt(2);// roomnum
            		pack.getInt(2);
            		int ab = 1;
            		int[] num = new int[8];
            		//roomplayer list starts...
            		for (int i = 0; i<8; i++){
            			if (ab == 8)
            				break;
            			ab=pack.getInt(1);
            			int relayneed=pack.getInt(1);
            			String roomplayer = pack.getString(0, 15, false);
            			if(relayneed!=1)
            				num[i]=relaystore.getRNum(roomplayer);
            			else
            				num[i]=-1;
            		}
            		relaystore.RelaySetRoomNums(number, num);
            		this.socketOut.flush();
            		break;
            	}
            	case 0x37A0:
            	{
            		pack.setPacket(packet);
            		pack.removeHeader();
            		int number=pack.getInt(2);
        			String roomplayer = pack.getString(0, 15, false);
        			int num=relaystore.getRNum(roomplayer);
        			try {
	        			if(num!=-1)
	        				relaystore.removeNum(number, num);
        			}catch (Exception e){debug(e.getMessage());}
        			this.socketOut.flush();
            	}
            	default:
            	{
            		this.socketOut.flush();
            		break;
            	}
            }
        } catch (Exception e)
        {
        	debug("Error during packet switch: " + e);
        	closecon();
        }
    }
    
    public int bytetoint(byte[] packet, int bytec)
    {
        try
        {
        	int ret = 0;
        	ret+=(packet[0+bytec] & 0xFF);
        	ret+=(packet[1+bytec] & 0xFF) << (8);
        	return ret;
        } catch (Exception e)
        {
        }
        return 0;
    }

    public int getcmd(byte[] packet)
    {
    	int ret = 0;
    	ret+=(packet[1] & 0xFF);
    	ret+=(packet[0] & 0xFF) << (8);
    	return ret;
    }
    
    public SocketAddress getRemoteAddress()
    {
        return this.socket.getRemoteSocketAddress();
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
            this.socketOut = (this.socket.getOutputStream());
            byte[] packet;
            autocheck = executor.scheduleAtFixedRate(autocheck(this, socketOut), 20, 10, TimeUnit.SECONDS);
            while ((packet = read()) != null)
            {
            	prasecmd1(getcmd(packet), packet);
            }
        } catch (Exception e)
        {
            debug("Exception (run): " + e.getMessage());
        }
        this.closecon();
    }
	
	protected void closecon(){
		if (finalize)
			return;
		try{
			autocheck.cancel(true);
		}catch (Exception e){debug("finalize(runnable) exception: " + e);}
		try {
			executor.shutdown();
		}catch (Exception e){debug("finalize(executor) exception: " + e);}
		try {
			relaystore.removeuser(numb, account);
		}catch (Exception e){debug("Error closing socket resources: " + e);}
		try {
			String[] arr = {account, ""+numb};
			sql.psupdate("DELETE FROM `lobbylist` WHERE `username`=? AND `Rnum`=?", arr);
		}catch (Exception e){debug("Error while removing ID: "+e);}
		try {
			sql=null;
			this.socketIn.close();
			this.socketOut.close();
			this.socket.close();
			finalize=true;
		}catch (Exception e){debug("Error closing socket resources: " + e);}
    }
}

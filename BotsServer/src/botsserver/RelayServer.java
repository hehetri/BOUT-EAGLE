package botsserver;

/**
 * @author BoutEagle
 */

import java.net.*;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RelayServer extends Thread {
    protected RelayServer server;
    public SQLDatabase sql;
	protected int port = 11013;
    protected DatagramSocket relaySocket;
    protected boolean listening;
    public RelayStore relaystore;
    public ExecutorService executor = Executors.newFixedThreadPool(100);
    
	/**
     * Creates a new instance of RelayServer.
     */
    public RelayServer(int sarverPort, Lobby lobbi) {
        this.port = sarverPort;
        this.listening = false;
    }
    
    /**
     * Gets the server's port.
     */
    public int getPort() {
        return this.port;
    }
    
    /**
     * Gets the server's listening status.
     */
    public boolean getListening() {
        return this.listening;
    }

    //public int getClientCount() {
    //    return this.clientConnections.size();
    //}

    
    /**
     * Roots a debug message to the main application.
     */
    protected void debug(String msg) {
        Main.debug("relayServer [" + this.port + "]: "+ msg);
    }
    
    public static boolean Ipbanned(InetAddress addressa){
    	String tempip = addressa.getHostAddress();
    	try{
    		String[] arr = {tempip};
    		ResultSet rs = Main.sql.psquery("SELECT `banned` FROM `ipbanned` WHERE `ip`=?", arr);
    		if (rs.next())
    		{
    			int banned = rs.getInt("banned");
    			if (banned == 1)
    				return true;
    		}
    	}catch(Exception e){}
        return false;
    }
    
    private static Runnable handlepack(final DatagramPacket packet, final RelayStore relaystore, final DatagramSocket relaySocket) {
        return new Runnable() {
          @Override
		    public void run() {
        	  try{
        		  InetAddress address = packet.getAddress();
        		  byte[] data = null;
        		  int portout = 0;
        		  if (!Ipbanned(address)){
        			  portout = packet.getPort();
        			  data = packet.getData();
        		  }
        		  int ret = 0;
        		  ret+=(data[1] & 0xFF);
        		  ret+=(data[0] & 0xFF) << (8);
        		  Packet pack = new Packet();
                  switch (ret)
                  {
                  	case 0x35A0: 
                  	{
                  		pack.setPacket(data);
                  		pack.removeHeader();
                        int Number = pack.getInt(2);
                        relaystore.FinishAddUser(Number, address, portout);
      		            byte[] answerpack = {(byte)0x1D, (byte)0xA4, (byte)0x02, (byte)0x00, (byte)0x01, (byte)0xCC};
      		            DatagramPacket dp = new DatagramPacket(answerpack, answerpack.length, address, portout);
      		            relaySocket.send(dp);
                  		break;
                  	}
                  	
                  	case 0x38A0: 
                  	{
                  		pack.setPacket(data);
                  		try{
                    		String addressos = address.getHostAddress();
                    		int length = pack.getPackSizeRelay();
                            pack.removeHeader();
                            int number = pack.getInt(2);
                            pack.getInt(2);
                            @SuppressWarnings("unused") int roomnum = pack.getInt(2);
                            pack.getInt(2);
                            int check = pack.getInt(2);
                            int check2 = pack.getInt(2);
                            Packet packt = new Packet();
                            packt.addInt(check, 2, false);
                            packt.addInt(check2, 2, false);
                            packt.addByteArray(pack.getPacket());
                            int read = pack.getreadStart();
                            byte[] answerpack = Arrays.copyOfRange(packt.getPacket(),read,read+length-8);
                            InetAddress[] raddress = relaystore.address;
                            int[] remoteport = relaystore.remoteport;
                            int[] nums = relaystore.roomnums[number].clone();
                            for (int i = 0; i<8; i++)
                            {
                            	if (nums[i]!=-1 && !addressos.equals(raddress[nums[i]].getHostAddress()))
                            	{
                            		try {
	                            		DatagramPacket dp = new DatagramPacket(answerpack, answerpack.length, raddress[nums[i]], remoteport[nums[i]]);
	                            		relaySocket.send(dp);
                            		}catch (Exception e){}
                            	}
                            	else if (nums[i]!=-1 && portout!=remoteport[nums[i]])
                            	{
                            		try {
	                            		DatagramPacket dp = new DatagramPacket(answerpack, answerpack.length, raddress[nums[i]], remoteport[nums[i]]);
	                            		relaySocket.send(dp);
                            		}catch (Exception e){}
                            	}
                            }
                  		}catch(Exception e){}
                  		break;
                  	}
                  	
                  	case 0x34A0:
                  	{
                  		pack.setPacket(data);
                  		pack.removeHeader();
                        int number = pack.getInt(2);
                  		int roomnum = relaystore.roomids[number];
                  		if (roomnum != -1) {
                  			int[] nums = relaystore.roomnums[number].clone();
	                  		for (int i = 0; i<8; i++)
	                        {
	                        	if (nums[i]!=-1 && relaystore.roomids[nums[i]] != roomnum)
	                        		relaystore.removeNum(number, nums[i]);
	                        }
                  		}
              			break;
                  	}
                  	
                  	default:
                  	{
                          //debug("Ignoring unknown packet");
                  		break;
                  	}
                  }
        	  } catch (Exception e) {}
          }
        };
    }
    
    public void run() {
        try {
            this.relaySocket = new DatagramSocket(this.port);
            this.listening = true;
            debug("listening");
            relaystore = new RelayStore(this, Main.sql);
            
			while (true) {
				DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
				relaySocket.receive(packet);
				executor.execute(handlepack(packet, relaystore, relaySocket));
			}
        }
        catch (Exception e) {
            debug("Exception (run): " + e.getMessage());
            this.finalize();
        }
    }
    
    /**
     * Closes the server's socket.
     */
    protected void finalize() {	 
        try {
            this.relaySocket.close();
            this.listening = false;
            debug("stopped");
        }
        catch (Exception e) {
            debug("Exception (finalize): " + e.getMessage());
        }
    }
}

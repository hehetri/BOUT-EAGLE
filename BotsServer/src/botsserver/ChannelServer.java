package botsserver;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;

public class ChannelServer extends Thread{
    private String channelname = Main.ChannelName;
    public Lobby lobby;
    protected int port;
    protected ServerSocket serverSocket;

    public ChannelServer(int serverPort) {
        this.port = serverPort;
    }
    
	protected void debug(String msg) {
        System.out.println("ChannelServer[" + this.port + "] "+ msg);
    }
    
    public boolean Ipbanned(Socket socket) throws Exception{
    	String[] tempip = {Main.getip(socket)};
    	ResultSet rs = Main.sql.psquery("SELECT `banned` FROM `ipbanned` WHERE `ip`=?",tempip);
        if (rs.next())
        {
        	int banned = rs.getInt("banned");
        	if (banned == 1)
        		return true;
        }
        String[] arr = new String[0];
        rs = Main.sql.psquery("SELECT * FROM `ipbanned`", arr);
        String banip="";
        while (rs.next()){
        	banip=rs.getString("ip");
        	if (rs.getInt("banned")==1){
        		if(banip.substring(banip.length() - 1).equals("*")){
        			banip=banip.substring(0, banip.length() - 1);
        			if(banip.equals(tempip[0].substring(0, banip.length()))){
        				return true;
        			}
        		}
        	}
        }
        return false;
    }
    
    public void run() {
        try {
            this.serverSocket = new ServerSocket(this.port);
            lobby = new Lobby(channelname);
            
            while (true) {
                Socket socket = this.serverSocket.accept();
                if(!Ipbanned(socket)){
                debug("client connection from " + socket.getRemoteSocketAddress());
                ChannelServerConnection socketConnection = new ChannelServerConnection(socket,this,lobby, Main.sql, this.channelname);
                socketConnection.start();
                }
                else{
                	debug("Ip banned client tried to connect with ip : " + socket.getRemoteSocketAddress());
                }
            }
        }
        catch (Exception e) {
            debug("Exception (run): " + e.getMessage());
        }
    }
    
    /**
     * Closes the server's socket.
     */
    protected void finalize() {	 
        try {
            this.serverSocket.close();
            debug("stopped");
        }
        catch (Exception e) {
            debug("Exception (finalize): " + e.getMessage());
        }
    }
}

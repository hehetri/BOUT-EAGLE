package botsserver;

import java.net.*;

public class RelayTCP extends Thread
{
    protected ServerSocket startrelayServer;
    protected int port = 11004;
    private boolean listening = false;
    private Lobby lobby;
    
    public RelayTCP(int serverPort, Lobby lobbi) {
    	this.lobby = lobbi;
        this.port = serverPort;
        this.listening = false;
    }
    
	public static void debug(String msg) {
    	Main.debug("RelayIdentifier: " + msg);
    }
	
	public void run()
	{
		try {
            debug("started and listening");
            this.startrelayServer = new ServerSocket(this.port);
            this.listening=true;

            while (this.listening) {
                Socket socket = this.startrelayServer.accept();
                if(!Main.getip(socket).equals("5.73.69.243")){
                	RelayTCPConnection startrelayConnection = new RelayTCPConnection(socket, this, lobby);
                	startrelayConnection.start();
                }
            }
        }catch(Exception e){}
	}
	
	protected void finalize() {	 
        try {
            this.startrelayServer.close();
            this.listening = false;
            debug("Stopped.");
        }
        catch (Exception e) {
            debug("Exception (finalize): " + e.getMessage());
        }
    }
}

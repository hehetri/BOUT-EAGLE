/*
 * LoginServer.java
 */

package LoginServer;

import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.sql.ResultSet;
import java.util.Vector;

/**
 * A LoginServer for communication between the connected clients.
 */
public class LoginServer extends Thread {

	public static final byte[] LOGIN_SUCCESSBYTE = { (byte) 0x01, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0xFF, // 00 00 00 01 FF 00 00 00 00 00 without ads
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, // 00 00 00 00 01 00 FF 00 00 00 with ads
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00 };
	public static final byte[] LOGIN_INCUSERBYTE = { (byte) 0x01, (byte) 0x00,
			(byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00 };
	public static final byte[] LOGIN_INCPASSBYTE = { (byte) 0x01, (byte) 0x00,
			(byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00 };
	public static final byte[] LOGIN_BANUSERBYTE = { (byte) 0x01, (byte) 0x00,
			(byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00 };
	public static final byte[] LOGIN_ALREADYLOGGEDIN = { (byte) 0x01,
			(byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00 };
	public static final byte[] LOGINHEADER = { (byte) 0xEC, (byte) 0x2C,
			(byte) 0x4A, (byte) 0x00 };
	protected ServerSocket socketServer;
	protected int port;
	protected boolean listening;
	protected Vector<LoginServerConnection> clientConnections;

	/**
	 * Creates a new instance of LoginServer.
	 */
	public LoginServer(int serverPort) {
		this.port = serverPort;
		this.clientConnections = new Vector<LoginServerConnection>();
		this.listening = false;
	}

	public boolean Ipbanned(Socket socket) throws Exception{
    	String tempip = Main.getip(socket);
    	ResultSet rs = Main.sql.doquery("SELECT `banned` FROM `ipbanned` WHERE `ip`='" + tempip + "'");
        if (rs.next())
        {
        	int banned = rs.getInt("banned");
        	if (banned == 1)
        		return true;
        }
        rs = Main.sql.doquery("SELECT * FROM `ipbanned`");
        String banip="";
        while (rs.next()){
        	banip=rs.getString("ip");
        	if (rs.getInt("banned")==1){
        		if(banip.substring(banip.length() - 1).equals("*")){
        			banip=banip.substring(0, banip.length() - 1);
        			if(banip.equals(tempip.substring(0, banip.length()))){
        				return true;
        			}
        		}
        	}
        }
        return false;
    }
	
	/**
	 * Gets the server's port.
	 */
	public int getPort() {
		return this.port;
	}

	/**
	 * Gets the number of clients.
	 */
	public int getClientCount() {
		return this.clientConnections.size();
	}

	/**
	 * Roots a debug message to the main application.
	 */
	protected void debug(String msg) {
		Main.debug("LoginServer (" + this.port + ")", msg);
	}

	/**
	 * Removes a client from the server (it's expected that the client closes
	 * its own connection).
	 */
	public boolean remove(SocketAddress remoteAddress) {
		try {
			for (int i = 0; i < this.clientConnections.size(); i++) {
				LoginServerConnection client = this.clientConnections.get(i);

				if (client.getRemoteAddress().equals(remoteAddress)) {
					this.clientConnections.remove(i);
					// debug("client " + remoteAddress + " removed");
					return true;
				}
			}
		} catch (Exception e) {
			debug("Exception (remove): " + e.getMessage());
		}

		return false;
	}

	/**
	 * Listens for client conections and handles them to ChatServerConnections.
	 */
	public void run() {
		try {
			Main.debug("[Server]", "LoginServer (" + this.port
					+ ") started and listening");
			this.socketServer = new ServerSocket(this.port);
			//this.socketServer.setSoTimeout(10000);
			this.listening = true;

			while (listening) {
				Socket socket = this.socketServer.accept();
				if (!Ipbanned(socket)) {
					debug("Client connection from " + Main.getip(socket));
					LoginServerConnection socketConnection = new LoginServerConnection(
							socket, this);
					socketConnection.start();
					this.clientConnections.add(socketConnection);
				}
			}
			;
		} catch (Exception e) {
			debug(e.getMessage());
		}
		// this.finalize();
	}

	/**
	 * Closes the server's socket.
	 */
	protected void finalize() {
		try {
			this.socketServer.close();
			this.listening = false;
			debug("Stopped.");
		} catch (Exception e) {
			debug("Exception (finalize): " + e.getMessage());
		}
	}
}

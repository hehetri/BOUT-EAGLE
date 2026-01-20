package botsserver;

import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class RoomServer extends Thread {
	protected BufferedReader socketIn;
	protected OutputStream socketOut;
	private Lobby lobby;
	private BotClass[] bot = new BotClass[600];
	private int port;

	public static void debug(String msg) {
		Main.debug("RoomServer[11011]: " + msg);
	}
	
	public RoomServer(int port, Lobby lobbi)
	{
		this.port=port;
		this.lobby=lobbi;
	}
	
	public void setbot(int num, BotClass botc)
	{
		bot[num]=botc;
	}

	public void run() {
		DatagramSocket socket=null;
		try {
			socket = new DatagramSocket(port);
			debug("started and listening");
			lobby.SetRoomServer(this);
			while (true) {
				try{
					DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
					socket.receive(packet);
					InetAddress address = packet.getAddress();
					int port = packet.getPort();
					byte[] data = packet.getData();

					if (data[0]==(byte)0xC9 && data[1]==(byte)0x00) {
						int numb = (data[4] & 0xFF) | (data[5] & 0xFF) << 8;
						bot[numb].setroomport(address.toString(), port);
					} else {
						byte[] packandheadb = { (byte) 0xC8, (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x00,(byte) 0x00, data[6] };
						DatagramPacket dp = new DatagramPacket(packandheadb, packandheadb.length, address, port);
						socket.send(dp);
					}
				}catch(Exception e){debug("Exception in roomserver: "+e);}
			}
		} catch (Exception e) {
			debug("Fatal Exception in roomserver: "+e);
			socket.close();
		}
	}
}

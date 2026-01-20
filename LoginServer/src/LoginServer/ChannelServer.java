package LoginServer;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.sql.ResultSet;

class ChannelServer {
	
	public static final byte[] CHANNEL_HEADERBYTE = { (byte) 0xEE, (byte) 0x2C,
			(byte) 0x50, (byte) 0x01 };
	public static final byte[] CHANNEL_FOOTER = { (byte) 0xFF, (byte) 0xFF,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	public static final byte[] CHANNEL_EMPTY = { (byte) 0xFF, (byte) 0xFF,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
			(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00 };
	public static final byte[] NULLBYTE = { (byte) 0x00 };

	public static String[] channel_detail = new String[12];
	public static int[] channel_id = new int[12];
	public static String[] channel_name = new String[12];
	public static int[] channel_namelength = new int[12];
	public static int[] channel_min = new int[12];
	public static int[] channel_max = new int[12];
	public static int[] channel_players = new int[12];
	public static int channel_i = 0;
	public static String[] channel_ip=new String[12];
	public static DatagramSocket serverSocket;
	protected static final String GET_CHANNEL_QUERY = "SELECT * FROM bout_channels WHERE status=1 LIMIT 12";

	protected static void debug(String msg) {
		Main.debug("ChannelServer (" + 11010 + ")", msg);
	}

	protected static void getChannels(int channelnum) {

		try {
			ResultSet rs = Main.sql.doquery("SELECT * FROM bout_channels WHERE status=1 and server='"+channelnum+"' LIMIT 12");
			if (channel_i != 1) {
				channel_i = 0;
			}
			String nullbyte = new String(NULLBYTE, "ISO8859-1");

			while (rs.next()) {
				channel_id[channel_i] = rs.getInt("id");
				channel_name[channel_i] = rs.getString("name");
				channel_ip[channel_i] = rs.getString("channelip");
				channel_namelength[channel_i] = channel_name[channel_i].length();

				channel_min[channel_i] = rs.getInt("minlevel");
				byte[] MINBYTE = { (byte) channel_min[channel_i] };

				channel_max[channel_i] = rs.getInt("maxlevel");
				byte[] MAXBYTE = { (byte) channel_max[channel_i] };

				channel_players[channel_i] = rs.getInt("players");
				int b1 = channel_players[channel_i] & 0xff;
				int b2 = (channel_players[channel_i] >> 8) & 0xff;
				byte[] PLAYERSBYTE = { (byte) b1, (byte) b2 };
				channel_detail[channel_i] = new String(PLAYERSBYTE, "ISO8859-1")
						+ new String(MINBYTE, "ISO8859-1")
						+ new String(MAXBYTE, "ISO8859-1")
						+ channel_name[channel_i];

				for (int i = 0; i < 22 - channel_namelength[channel_i]; i++)
					channel_detail[channel_i] += nullbyte;

				channel_i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getMessage());
		}
	}

	public static void main(){
		try {
		serverSocket = new DatagramSocket(11010);
		byte[] receiveData = new byte[12];
		byte[] sendData = new byte[340];
		DatagramPacket sendPacket;
		int port;
		InetAddress IPAddress;
		Main.debug("[Server]", "ChannelServer (" + 11010 + ") Started");
		while (true) {
			DatagramPacket receivePacket = new DatagramPacket(receiveData,
					receiveData.length);
			serverSocket.receive(receivePacket);
			byte[] packy = receivePacket.getData();
			//System.out.println((packy[0] & 0xFF) | (packy[1] & 0xFF) << 8);
			if (packy[0]==(byte)0xFA && packy[1]==(byte)0x2A){

				getChannels((int)(packy[4]));
				
				String channel_packet = new String(CHANNEL_HEADERBYTE,
						"ISO8859-1");
				;
				for (int i = 0; i < channel_i; i++)
					channel_packet += channel_detail[i];

				for (int i = 0; i < 12 - channel_i; i++)
					channel_packet += new String(CHANNEL_EMPTY, "ISO8859-1");
				
				for (int i = 0; i < 4; i++)
					if(channel_ip[i]!=null){
						String[] aip=channel_ip[i].split("\\.");
						byte[] bitarr={(byte) 0x00, (byte) 0x00,(byte) Integer.parseInt(aip[0]), (byte) Integer.parseInt(aip[1]), (byte) Integer.parseInt(aip[2]), (byte) Integer.parseInt(aip[3])};
						channel_packet += new String(bitarr, "ISO8859-1");
					}
					else
						channel_packet += new String(CHANNEL_FOOTER, "ISO8859-1");

				sendData = channel_packet.getBytes("ISO8859-1");

				IPAddress = receivePacket.getAddress();

				port = receivePacket.getPort();

				sendPacket = new DatagramPacket(sendData, sendData.length,
						IPAddress, port);

				serverSocket.send(sendPacket);
			}
		}
		} catch (Exception e){System.out.println("Exception: " + e.getMessage());serverSocket.close();}
	}

}
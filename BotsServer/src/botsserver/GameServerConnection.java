package botsserver;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.ResultSet;

public class GameServerConnection extends Thread {
	protected GameServer server;
	protected Lobby lobby;
	protected Socket socket;
	protected SQLDatabase sql;
	protected InputStream socketIn;
	protected OutputStream socketOut;
	protected String ip;
	protected String account;
	protected BotClass bot;
	protected Packet pack = new Packet();

	public GameServerConnection(Socket socket, GameServer server, Lobby lobby, SQLDatabase sql) {
		this.socket = socket;
		this.server = server;
		this.lobby = lobby;
		this.sql = sql;
		this.ip = Main.getip(socket);
	}

	private void debug(String msg) {
		Main.debug("GameServerConnection: " + msg);
	}

	protected int isbanned(String account) {
		try {
			String[] arr = {account};
			ResultSet rs = Main.sql.psquery("SELECT banned FROM bout_users WHERE username=? LIMIT 1", arr);
			if (rs.next()) {
				return rs.getInt("banned");
			}
		} catch (Exception e) {
		}
		return 0;
	}

	protected void checkAccount() {
		try {
			String[] arr = {Main.getip(socket)};
			ResultSet rs = Main.sql.psquery("SELECT username FROM bout_users WHERE current_ip=? LIMIT 1", arr);
			if (rs.next()) {
				this.account = rs.getString("username");
			}
			rs.close();
			if (this.account != null && isbanned(this.account) == 0) {
				return;
			}
			account = "";
		} catch (Exception e) {
			debug("Error :" + e);
		}
	}

	protected void resolveBot() {
		if (account == null || account.isEmpty())
			return;
		try {
			String[] arr = {account, lobby.getChannelName()};
			ResultSet rs = sql.psquery("SELECT num FROM lobbylist WHERE username=? AND Channel=? LIMIT 1", arr);
			if (rs.next()) {
				int num = rs.getInt("num");
				if (num >= 0 && num < lobby.bots.length) {
					bot = lobby.bots[num];
				}
			}
			rs.close();
		} catch (Exception e) {
			debug("Error resolving bot: " + e);
		}
	}

	protected void parsecmd(int cmd, byte[] packet) {
		try {
			pack.setPacket(packet);
			pack.removeHeader();
			switch (cmd) {
				case 0x0200: {
					if (bot != null)
						bot.lastreply = System.currentTimeMillis();
					break;
				}
				case 0xA627: {
					if (bot != null)
						Game.chatCommand(bot, pack);
					break;
				}
				default: {
					break;
				}
			}
		} catch (Exception e) {
		}
	}

	public int getcmd(byte[] packet) {
		int ret = 0;
		ret += (packet[1] & 0xFF);
		ret += (packet[0] & 0xFF) << (8);
		return ret;
	}

	public int bytetoint(byte[] packet, int bytec) {
		int ret = 0;
		ret += (packet[0 + bytec] & 0xFF);
		ret += (packet[1 + bytec] & 0xFF) << (8);
		return ret;
	}

	protected byte[] read() {
		ByteBuffer buffer = ByteBuffer.allocate(4);
		int codePoint;

		try {
			for (int i = 0; i < 4; i++) {
				codePoint = this.socketIn.read();
				buffer.put((byte) codePoint);
			}
			int plen = bytetoint(buffer.array(), 2);
			if (bytetoint(buffer.array(), 0) == 0xFFFF)
				return null;
			byte[] quickstore = buffer.array();
			buffer = ByteBuffer.allocate(plen + 5);
			buffer.put(quickstore);
			if (plen >= 1) {
				for (int i = 0; i < plen; i++) {
					codePoint = this.socketIn.read();
					buffer.put((byte) codePoint);
				}
			}
		} catch (Exception e) {
			debug("Error (read): " + e);
			return null;
		}
		return buffer.array();
	}

	public void run() {
		try {
			this.socketIn = this.socket.getInputStream();
			this.socketOut = this.socket.getOutputStream();
			checkAccount();
			resolveBot();
			byte[] packet;
			while ((packet = read()) != null) {
				parsecmd(getcmd(packet), packet);
			}
		} catch (Exception e) {
			debug("Exception (run): " + e);
		}
		this.closecon();
	}

	protected void closecon() {
		try {
			socketIn.close();
			socketOut.close();
			socket.close();
		} catch (Exception e) {
			debug("Error while closing socket attributes: " + e);
		}
	}
}

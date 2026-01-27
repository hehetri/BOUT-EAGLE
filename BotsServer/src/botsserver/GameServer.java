package botsserver;

import java.net.ServerSocket;
import java.net.Socket;

public class GameServer extends Thread {
	protected int port;
	protected ServerSocket serverSocket;
	protected Lobby lobby;
	private boolean listening = false;

	public GameServer(int serverPort, Lobby lobby) {
		this.port = serverPort;
		this.lobby = lobby;
	}

	protected void debug(String msg) {
		System.out.println("GameServer[" + this.port + "] " + msg);
	}

	public void run() {
		try {
			this.serverSocket = new ServerSocket(this.port);
			this.listening = true;

			while (this.listening) {
				Socket socket = this.serverSocket.accept();
				debug("client connection from " + socket.getRemoteSocketAddress());
				GameServerConnection socketConnection = new GameServerConnection(socket, this, lobby, Main.sql);
				socketConnection.start();
			}
		} catch (Exception e) {
			debug("Exception (run): " + e.getMessage());
		}
	}

	protected void finalize() {
		try {
			this.listening = false;
			this.serverSocket.close();
			debug("stopped");
		} catch (Exception e) {
			debug("Exception (finalize): " + e.getMessage());
		}
	}
}

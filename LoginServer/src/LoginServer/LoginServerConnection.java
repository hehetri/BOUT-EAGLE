/*
 * LoginServerConnection.java
 * This file handles the connections.
 */

package LoginServer;

//import java.math.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The LoginServerConnection handles individual client connections to the chat
 * server.
 */
public class LoginServerConnection extends Thread {
	protected Socket socket;
	protected InputStream socketIn;
	protected OutputStream socketOut;
	protected PrintWriter socketOut2;
	protected LoginServer server;
	public String user;
	public String pass;
	public int LOGIN_ID;
	public String LOGIN_USERNAME;
	public String LOGIN_PASSWORD;
	public int LOGIN_BANNED;
	public int LOGIN_ALLOG;
	public int LOGIN_RESULT;
	public String LOGIN_RESULTSTR;
	private URL URLObj;
	private URLConnection connect;
	private String mail;


	/*
	 * Creates a new instance of LoginServerConnection.
	 */
	public LoginServerConnection(Socket socket, LoginServer server) {
		this.socket = socket;
		this.server = server;
	}

	/**
	 * Gets the remote address of the client.
	 */
	public SocketAddress getRemoteAddress() {
		return this.socket.getRemoteSocketAddress();
	}

	/**
	 * Roots a debug message to the main application.
	 */
	protected void debug(String msg) {
		Main.debug(
				"LoginServerConnection ("
						+ this.socket.getRemoteSocketAddress() + ")", msg);
	}

	public void CheckUser(String user, String pass) {

		try {
//			boolean properlogin=phplogin();
			int bantime = -1;
			String banstime = "0";
			String[] user2 = new String[1];
			user2[0] = user;
			// ResultSet rs =
			// Main.sql.doquery("SELECT * FROM bout_users WHERE username='"+
			// user2 +"' LIMIT 1");
			Main.sql.doupdate("UPDATE bout_users SET current_ip='' WHERE last_ip='"+Main.getip(socket)+"'");
			ResultSet rs = Main.sql.psquery("SELECT * FROM bout_users WHERE username=? LIMIT 1", user2);
			if (rs.next()) {
				this.LOGIN_ID = rs.getInt("id");
				this.LOGIN_USERNAME = rs.getString("username");
				this.LOGIN_PASSWORD = (rs.getString("password"));
				this.LOGIN_BANNED = rs.getInt("banned");
				bantime = rs.getInt("bantime");
				banstime = rs.getString("banStime");
				this.LOGIN_ALLOG = rs.getInt("online");
				this.LOGIN_RESULT = 0;
			}
			boolean properlogin = true;//this.LOGIN_PASSWORD.equals(this.pass);
//			else if(properlogin){
//				user2 = new String[11];
//				user2[0] = this.user;
//				user2[1] = "";//hashing_method(this.pass);
//				user2[2] = "0";
//				user2[3] = "0"; //starting coins
//				user2[4] = this.mail;
//				user2[5] = "0";
//				user2[6] = "0.0.0.0";
//				user2[7] = "0";
//				user2[8] = "0.0.0.0";
//				user2[9] = "0";
//				user2[10] = this.user;
//				Main.sql.psupdate("INSERT INTO bout_users(`username`,`password`,`banned`,`coins`, `email`, `online`, `current_ip`, `logincount`, `last_ip`, `position`, `forumaccount`) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", user2);
//				this.LOGIN_ID=1;
//			}
			rs.close();
			if (bantime!=-1){
				user2= new String[1];
				user2[0]= banstime;
				try{
				rs = Main.sql.psquery("SELECT TIMESTAMPDIFF(SECOND, ?, now()) AS seconds", user2);
				if (rs.next()){
					int calctime = rs.getInt("seconds");
					if(calctime>bantime)
					{
						user2 = new String[2];
						user2[0] = "0";
						user2[1] = user;
						Main.sql.psupdate("UPDATE `bout_users` SET `banned`=? WHERE `username`=?", user2);
						Main.sql.psupdate("UPDATE `bout_users` SET `bantime`=? WHERE `username`=?", user2);
						user2 = new String[1];
						user2[0] = user;
						Main.sql.psupdate("UPDATE `bout_users` SET `banStime`=NULL WHERE `username`=?", user2);
						//openpage("http://bouteagle.ace-games.com/serverassg.php?assignment=3&username="+user+"&banned=0&bantime=0&banStime=NULL");
						this.LOGIN_BANNED=0;
					}
				}
				}catch(Exception e){System.out.println("time calc error: " +e);}
			}
			rs.close();
			if (this.LOGIN_ID == 0) {
				this.LOGIN_RESULT = 1;
			} else if (!properlogin) {
				this.LOGIN_RESULT = 2;
			} else if (this.LOGIN_BANNED == 1) {
				//openpage("http://bouteagle.ace-games.com/serverassg.php?assignment=3&username="+user+"&banned=1&bantime="+bantime+"&banStime="+banstime);
				this.LOGIN_RESULT = 3;
			} else if (this.LOGIN_ALLOG == 1) {
				this.LOGIN_RESULT = 4;
			}
			/*
			 * else if (this.LOGIN_USERNAME==user) { this.LOGIN_RESULT = 2; }
			 */
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception: " + e.getMessage());
		}
	}

	protected void prasecmd(int cmd, byte[] packet)
    {
        try
        {
        	debug(Integer.toString(cmd));
        	this.socketOut.flush();
            switch (cmd)
            {
            	case 0xF82A:
            	{
            		String thestring = new String(Arrays.copyOfRange(packet,4,27));
                    packet = Arrays.copyOfRange(packet,27,packet.length);
                    thestring = removenullbyte(thestring);
            		if (thestring.startsWith("H"))
            			thestring = thestring.replace("H", "");
            		this.user = thestring;
            		thestring = new String(packet);
                    thestring = removenullbyte(thestring.substring(0,32));
            		this.pass = thestring;
            		doLogin();
            	}
            	default:
            		this.socketOut.flush();
            }
        }catch (Exception e){}
    }
	
	protected byte[] encrypt(byte[] a)
	{
		/*byte[] b = new byte[a.length];
		for (int i = 0; i<a.length; i++)
			b[i]=Main.encrypt[a[i] & 0xFF];*/
		return a;
	}
	
	/**
	 * Writes the login packet.
	 */
	protected void doLogin() {
		try {
			CheckUser(user, pass);
			switch (this.LOGIN_RESULT) {
			case 0:
				updateaccount(user);
				this.socketOut.write(encrypt(LoginServer.LOGINHEADER));
				this.socketOut.flush();
				this.socketOut.write(encrypt(LoginServer.LOGIN_SUCCESSBYTE));// ,"ISO8859-1"));
				this.socketOut.flush();
				this.LOGIN_RESULTSTR = "Success";
				this.socket.close();
				break;
			case 1:
			default:
				this.socketOut.write(encrypt(LoginServer.LOGINHEADER));
				this.socketOut.flush();
				this.socketOut.write(encrypt(LoginServer.LOGIN_INCUSERBYTE));// ,"ISO8859-1"));
				this.socketOut.flush();
				this.LOGIN_RESULTSTR = "Incorrect Username";
				break;
			case 2:
				this.socketOut.write(encrypt(LoginServer.LOGINHEADER));
				this.socketOut.flush();
				this.socketOut.write(encrypt(LoginServer.LOGIN_INCPASSBYTE));// ,"ISO8859-1"));
				this.socketOut.flush();
				this.LOGIN_RESULTSTR = "Incorrect Password";
				break;
			case 3:
				this.socketOut.write(encrypt(LoginServer.LOGINHEADER));// ,
																// "ISO8859-1"));
				this.socketOut.flush();
				this.socketOut.write(encrypt(LoginServer.LOGIN_BANUSERBYTE));// ,"ISO8859-1"));
				this.socketOut.flush();
				this.LOGIN_RESULTSTR = "Banned Username";
				break;
			case 4:
				this.socketOut.write(encrypt(LoginServer.LOGINHEADER));// ,
																// "ISO8859-1"));
				this.socketOut.flush();
				this.socketOut.write(encrypt(LoginServer.LOGIN_ALREADYLOGGEDIN));// ,"ISO8859-1"));
				this.socketOut.flush();
				this.LOGIN_RESULTSTR = "User is already Logged in";
				break;
			}
			debug("[SERVER] Login Sent (" + this.LOGIN_RESULTSTR + ")");
		} catch (Exception e) {
			debug("Error (write): " + e.getMessage());
		}
	}

	/**
	 * update account information
	 */
	private void updateaccount(String user) {
		try {
			int logincount = 0;
			String[] user2 = new String[1];
			user2[0] = user;
			ResultSet rs = Main.sql.psquery("SELECT * FROM bout_users WHERE username=? LIMIT 1", user2);
			while (rs.next()) {
				logincount = rs.getInt("logincount");
			}
			logincount++;

			// get date
			java.util.Date dt = new java.util.Date();
			// set date format
			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			String ip = Main.getip(socket);

			// add later
			// online=1
			user2 = new String[5];
			user2[0] = ip;
			user2[1] = Integer.toString(logincount);
			user2[2] = ip;
			user2[3] = df.format(dt);
			user2[4] = user;
			Main.sql.psupdate("UPDATE bout_users SET current_ip=?, logincount=?, last_ip=?, lastlogin=? WHERE username=?",user2);
			//openpage("http://bouteagle.ace-games.com/serverassg.php?assignment=4&username="+user+"&mail="+this.mail+"&curIp="+Main.getip(socket)+"&create=0&pass="+this.pass+"&logcount="+logincount);
			// Main.sql.doupdate("UPDATE bout_users SET current_ip='"+ip+"', logincount="+logincount+", last_ip='"+ip+"', lastlogin='"+df.format(dt)+"' WHERE username='"+user+"'");
		} catch (Exception e) {
			debug("Error (updateAccount) : " + e.getMessage());
		}
	}
	
	protected String[] openpage(String page)
	{
		List<String> linelist = new ArrayList<String>();
		String[] lines=null;
		try {
            // Establish a URL and open a connection to it. Set it to output mode.
            URLObj = new URL(page);
            connect = URLObj.openConnection();
            connect.setDoOutput(true);	
        }
		catch (Exception ex) {
            System.out.println("An exception occurred. " + ex.getMessage());
		}
		
		try {
			
            BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            String lineRead = "";
			
            while ((lineRead = reader.readLine()) != null) {
            	linelist.add(lineRead);
            }
		}
		catch(Exception e){
			System.out.println("An exception occurred. " + e.getMessage());
		}
		lines=new String[linelist.size()];
		lines=linelist.toArray(lines);
		return lines;
	}
	
	public boolean phplogin()
	{
		boolean value=false;
		java.lang.System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
		try {
            // Establish a URL and open a connection to it. Set it to output mode.
            URLObj = new URL("https://bouteagle.ace-games.com/serverassg.php?assignment=1&username="+this.user+"&pass="+this.pass);
            connect = URLObj.openConnection();
            connect.setDoOutput(true);	
        }
        catch (Exception ex) {
            System.out.println("An exception occurred. " + ex.getMessage());
            System.exit(1);
        }
		
		
        try {
			
            // Now establish a buffered reader to read the URLConnection's input stream.
            BufferedReader reader = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            String lineRead = "";
			
            // Read all available lines of data from the URL and print them to screen.
            while ((lineRead = reader.readLine()) != null) {
                System.out.println(lineRead);
                String[] liner=lineRead.split(" ");
    			if(liner[0].equals("login_succes")){
    				value=true;
    				this.mail=liner[1];
    			}
    			else if(liner[0].equals("login_failed"))
    				value=false;
            }
            reader.close();
        }
        catch (Exception ex) {
            System.out.println("There was an error reading or writing to the URL: " + ex.getMessage());
        }
        return value;
	}

	/**
	 * Reads the buffer.
	 * 
	 */
    protected byte[] read()
    {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        int codePoint;
        
        try
        {
            for (int i = 0; i < 4; i++)
            {
                codePoint = this.socketIn.read();
                buffer.put((byte)(codePoint));
                //buffer.put((byte)(Main.decrypt[codePoint & 0xFF]));
            }
            int plen = bytetoint(buffer.array(), 2);
            if (bytetoint(buffer.array(), 0)==0xFFFF)
            	return null;
            byte[] quickstore = buffer.array();
            debug(Main.bytesToHex(quickstore));
            buffer = ByteBuffer.allocate(plen+5);
            buffer.put(quickstore);
            if (plen >= 1)
            {
                for (int i = 0; i < plen; i++)
                {
                    codePoint = this.socketIn.read();
                    buffer.put((byte)(codePoint));
                    //buffer.put((byte)Main.decrypt[codePoint & 0xFF]);
                }
            }
        } catch (Exception e)
        {
            debug("Error (read): " + e.getMessage());
            this.server.remove(this.getRemoteAddress());
            return null;
        }
		return buffer.array();
    }
    
    public int bytetoint(byte[] packet, int bytec)
    {
        try
        {
        	int ret = 0;
        	ret+=packet[bytec] & 0xFF;
        	ret+=(packet[bytec+1] & 0xFF) << (8);
        	return ret;
        } catch (Exception e)
        {
        }
        return 0;
    }
    
    public int getcmd(byte[] packet)
    {
    	return new BigInteger(1, Arrays.copyOfRange(packet,0,2)).intValue();
    }

    protected String removenullbyte(String thestring)
    {
        try
        {
        byte[] stringbyte = thestring.getBytes("ISO8859-1");
        int a = 0;
        while(stringbyte.length>a &&stringbyte[a] != 0x00)
            a++;
        
        return thestring.substring(0, a);
        
        } catch (Exception e){
        	debug("nullbyte remove exception "+e);
        }
        return null;
    }
    
	/**
	 * md5hash fuction for checkuser (unused)
	 */
	/*private String md5hash(String text) {
		try {
			MessageDigest md = null;
			byte[] encryptMsg = null;
			try {
				md = MessageDigest.getInstance("MD5");
				encryptMsg = md.digest(text.getBytes("ISO8859-1"));
			} catch (NoSuchAlgorithmException e) {
			}
			String swap = "";
			String byteStr = "";
			StringBuffer strBuf = new StringBuffer();
			for (int i = 0; i <= encryptMsg.length - 1; i++) {
				byteStr = Integer.toHexString(encryptMsg[i]);
				switch (byteStr.length()) {
				case 1:
					swap = "0" + Integer.toHexString(encryptMsg[i]);
					break;
				case 2:
					swap = Integer.toHexString(encryptMsg[i]);
					break;
				case 8:
					swap = (Integer.toHexString(encryptMsg[i])).substring(6, 8);
					break;
				}
				strBuf.append(swap);
			}
			String hash = strBuf.toString();
			return hash;
		} catch (Exception e) {
			debug("md5hash exception "+e);
		}
		return null;
	}*/

	/**
	 * Waits for messages from the client...
	 */
	public void run() {
		try {
			this.socketIn = this.socket.getInputStream();
			this.socketOut = (this.socket.getOutputStream());// , true);

			byte[] packet;
            while ((packet = read()) != null)
            {
            	//packet = Main.xor(packet, (byte)0x3D);
                prasecmd(getcmd(packet), packet);
            }
			
		} catch (Exception e) {
			debug("Error (run): " + e.getMessage());
		}
		this.finalize();

	}

	/**
	 * Closes the reader, the writer and the socket.
	 */
	protected void finalize() {
		try {
			this.server.remove(this.getRemoteAddress());
			this.socketIn.close();
			this.socketOut.close();
			this.socket.close();
			debug("Thread " + Thread.currentThread() + " removed");
			Thread.currentThread().interrupt();
			return;
		} catch (Exception e) {
			debug("Error (finalize): " + e.getMessage());
		}
	}

}